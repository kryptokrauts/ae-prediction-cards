package com.kryptokrauts;

import com.kryptokrauts.aeternity.sdk.constants.AENS;
import com.kryptokrauts.aeternity.sdk.domain.secret.KeyPair;
import com.kryptokrauts.aeternity.sdk.service.account.domain.AccountResult;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.aeternity.sdk.service.info.domain.KeyBlockResult;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionResult;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairService;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.name.domain.NameEntryResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.RegisteredOracleResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.PostTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.AbstractTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.NameClaimTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.NamePreclaimTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.NameUpdateTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.SpendTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.unit.UnitConversionService;
import com.kryptokrauts.aeternity.sdk.service.unit.impl.DefaultUnitConversionServiceImpl;
import com.kryptokrauts.aeternity.sdk.util.CryptoUtils;
import com.kryptokrauts.contraect.generated.PredictionCards;
import com.kryptokrauts.contraect.generated.PredictionCards.Oracle;
import com.kryptokrauts.contraect.generated.PredictionCards.Prediction;
import com.kryptokrauts.contraect.generated.PredictionCards.Prediction_state;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChainInteraction {

  private static final String AENS_NAME = "predictioncards.chain";

  private AeternityServiceConfiguration aeternityServiceConfig;

  private AeternityService aeternityService;

  private KeyPair serviceKeyPair;

  private KeyPairService keyPairService;

  private UnitConversionService unitConversionService18Decimals;

  private PredictionCards contract;

  private String contractAddress;

  private HashMap<BigInteger, String> predictionIdQueryMap = new HashMap<>();

  @Autowired
  private ServiceConfig serviceConfig;

  @PostConstruct
  private void init() throws Throwable {
    log.info("Initializing chain connection");
    keyPairService = new KeyPairServiceFactory().getService();
    serviceKeyPair = keyPairService.recoverKeyPair(serviceConfig.getPrivateKey());
    log.info("Service address: {}", serviceKeyPair.getAddress());
    aeternityServiceConfig = AeternityServiceConfiguration.configure()
        .compilerBaseUrl(serviceConfig.getCompilerUrl())
        .baseUrl(serviceConfig.getBaseUrl())
        .network(serviceConfig.getNetwork())
        .keyPair(serviceKeyPair)
        .targetVM(serviceConfig.getTargetVM().withVmVersion(serviceConfig.getVmVersion())
            .withAbiVersion(serviceConfig.getAbiVersion()))
        .millisBetweenTrailsToWaitForConfirmation(100l)
        .compile();
    unitConversionService18Decimals = new DefaultUnitConversionServiceImpl();
    aeternityService = new AeternityServiceFactory().getService(aeternityServiceConfig);
    NameEntryResult nameEntryResult = aeternityService.names.blockingGetNameId(AENS_NAME);
    if (nameEntryResult.getRootErrorMessage() != null) {
      if (serviceConfig.isLocalNode()) {
        log.info("Initializing local setup due to missing name.");
        initLocalAccounts();
        contract = new PredictionCards(aeternityServiceConfig, null);
        Pair<String, String> deployedContract = contract
            .deploy(new Oracle(serviceConfig.getOracleAddress()), Optional.empty(),
                Optional.of(new BigInteger("200")));
        contractAddress = deployedContract.getValue1();
        log.info("Contract address: {}", contractAddress);
        claimNameAndSetPointer();
      } else {
        throw new RuntimeException("Name does not exist.");
      }
    } else if (nameEntryResult.getContractPointer().isEmpty()) {
      throw new RuntimeException("Contract pointer not set.");
    } else {
      contractAddress = nameEntryResult.getContractPointer().get();
      log.info("Contract pointer exists.");
      log.info("Using contract at address: {}", contractAddress);
      contract = new PredictionCards(aeternityServiceConfig, contractAddress);
    }
    log.info("Chain connection initialization successful");
  }

  public void askOracle() {
    // contract pointer might have changed
    String contractPointer = getContractPointer();
    if (!contractAddress.equals(contractPointer)) {
      log.info("Contract pointer changed - old address {} - new address {}", contractAddress,
          contractPointer);
      // cc https://github.com/kryptokrauts/contraect-maven-plugin/issues/52
      contract = new PredictionCards(aeternityServiceConfig, contractPointer);
      contractAddress = contractPointer;
      // clear prediction ids as we are using a new contract
      predictionIdQueryMap.clear();
    }
    List<Prediction> closedPredictions = contract
        .predictions(new Prediction_state("CLOSED"));
    if (closedPredictions.isEmpty()) {
      log.info("No predictions to resolve ...");
    } else {
      for (Prediction prediction : closedPredictions) {
        // only if oracle query doesn't exist
        if (prediction.getOracle_query().isEmpty()) {
          log.info(
              "No oracle query present. Asking oracle to resolve the winning outcome for prediction with id: {}",
              prediction.getId());
          String txHash = contract.ask_for_winning_option(prediction.getId(), getOracleFee());
          log.info("tx-hash (ask_for_winning_option): {}", txHash);
          try {
            waitForTxMined(txHash);
          } catch (Throwable e) {
            log.error("Unexpected error while waiting for tx to be mined.", e);
          }
          try {
            Prediction predictionWithQuery = contract.prediction(prediction.getId()).getValue1();
            predictionIdQueryMap.put(prediction.getId(),
                predictionWithQuery.getOracle_query().get().getOracle_query());
          } catch (Exception e) {
            log.error("Oracle query not present yet. Should be there on the next run =)", e);
          }
        } else {
          predictionIdQueryMap
              .put(prediction.getId(), prediction.getOracle_query().get().getOracle_query());
        }
      }
    }
  }

  public void processOracleResponse() {
    log.info("before: {}", predictionIdQueryMap);
    for (BigInteger pId : predictionIdQueryMap.keySet()) {
      Boolean hasResponded = false;
      try {
        hasResponded = contract.check_oracle_has_responded(pId);
      } catch (Throwable e) {
        log.info("Oracle query expired: {}", predictionIdQueryMap.get(pId));
        predictionIdQueryMap.remove(pId);
        String txHash = contract.ask_for_winning_option(pId, getOracleFee());
        log.info("tx-hash (ask_for_winning_option): {}", txHash);
        try {
          waitForTxMined(txHash);
        } catch (Throwable e2) {
          log.error("Unexpected error while waiting for tx to be mined.", e2);
        }
      }
      if (hasResponded) {
        log.info("Oracle has responded. Processing response for prediction with id '{}'", pId);
        String txHash = contract.process_oracle_response(pId);
        log.info("tx-hash (process_oracle_response): {}", txHash);
        try {
          waitForTxMined(txHash);
          predictionIdQueryMap.remove(pId);
          log.info("Oracle response successfully processed.");
        } catch (Throwable e) {
          log.error("Unexpected error while waiting for tx to be mined.", e);
        }
      }
    }
    log.info("after remove: {}", predictionIdQueryMap);
  }

  public void checkAndExtendName() {
    KeyBlockResult keyBlockResult = aeternityService.info.blockingGetCurrentKeyBlock();
    NameEntryResult nameEntryResult = aeternityService.names.blockingGetNameId(AENS_NAME);
    log.info("Name is about to expire at block {} - current at block height is {}",
        nameEntryResult.getTtl().longValue(), keyBlockResult.getHeight().longValue());
    long nameTtlLeft =
        nameEntryResult.getTtl().longValue() - keyBlockResult.getHeight().longValue();
    if (nameTtlLeft < serviceConfig.getNameMinBlocksExtensionTrigger()) {
      log.info("Extending name before expiration ...");
      try {
        updateName();
      } catch (Throwable e) {
        log.error("Error extending name", e);
      }
    }
  }

  /**
   * ---------------------------------------------------------------------------------
   * <p>
   * Helper methods
   * <p>
   * ---------------------------------------------------------------------------------
   */

  private String getContractPointer() {
    // no checks as we assume the name is present and the pointer is set
    NameEntryResult nameEntryResult =
        aeternityService.names.blockingGetNameId(AENS_NAME);
    return nameEntryResult.getContractPointer().get();
  }

  private BigInteger getOracleFee() {
    // no checks as we assume the name is present and the pointer is set
    NameEntryResult nameEntryResult =
        aeternityService.names.blockingGetNameId(AENS_NAME);
    RegisteredOracleResult registeredOracleResult = aeternityService.oracles
        .blockingGetRegisteredOracle(nameEntryResult.getOraclePointer().get());
    return registeredOracleResult.getQueryFee();
  }

  private void claimNameAndSetPointer() throws Throwable {
    BigInteger salt = CryptoUtils.generateNamespaceSalt();
    NamePreclaimTransactionModel namePreclaimTx =
        NamePreclaimTransactionModel.builder()
            .accountId(serviceKeyPair.getAddress())
            .name(AENS_NAME)
            .salt(salt)
            .nonce(getNextKeypairNonce(serviceKeyPair.getAddress()))
            .ttl(BigInteger.ZERO)
            .build();

    PostTransactionResult result = blockingPostTx(namePreclaimTx,
        serviceKeyPair.getEncodedPrivateKey());
    log.info("NamePreclaimTx hash: " + result.getTxHash());

    NameClaimTransactionModel nameClaimTx =
        NameClaimTransactionModel.builder()
            .accountId(serviceKeyPair.getAddress())
            .name(AENS_NAME)
            .nameSalt(salt)
            .nonce(getNextKeypairNonce(serviceKeyPair.getAddress()))
            .ttl(BigInteger.ZERO)
            .build();

    result = this.blockingPostTx(nameClaimTx, serviceKeyPair.getEncodedPrivateKey());
    log.info("NameClaimTx hash: " + result.getTxHash());
    waitForTxMined(result.getTxHash());

    updateName();
  }

  private void updateName() throws Throwable {
    NameEntryResult nameEntryResult =
        aeternityService.names.blockingGetNameId(AENS_NAME);
    NameUpdateTransactionModel nameUpdateTx =
        NameUpdateTransactionModel.builder()
            .accountId(serviceKeyPair.getAddress())
            .nameId(nameEntryResult.getId())
            .nonce(getNextKeypairNonce(serviceKeyPair.getAddress()))
            .ttl(BigInteger.ZERO)
            .clientTtl(BigInteger.valueOf(50l))
            .nameTtl(serviceConfig.getNameExtensionTTL())
            .pointers(
                new HashMap<>() {
                  {
                    put(AENS.POINTER_KEY_CONTRACT, contractAddress);
                    put(AENS.POINTER_KEY_ORACLE, serviceConfig.getOracleAddress());
                  }
                })
            .build();
    log.info(nameUpdateTx.toString());
    PostTransactionResult result = this.blockingPostTx(nameUpdateTx);
    waitForTxMined(result.getTxHash());
  }

  private AccountResult getAccount(String address) {
    if (address == null) {
      return aeternityService.accounts.blockingGetAccount();
    }
    return aeternityService.accounts.blockingGetAccount(address);
  }

  private PostTransactionResult blockingPostTx(AbstractTransactionModel<?> tx) throws Throwable {
    return blockingPostTx(tx, null);
  }

  private PostTransactionResult blockingPostTx(AbstractTransactionModel<?> tx, String privateKey)
      throws Throwable {
    if (privateKey == null) {
      privateKey = serviceConfig.getPrivateKey();
    }
    PostTransactionResult postTxResponse =
        aeternityService.transactions.blockingPostTransaction(tx, privateKey);
    log.debug("PostTx hash: " + postTxResponse.getTxHash());
    TransactionResult txValue = waitForTxMined(postTxResponse.getTxHash());
    log.debug(String.format("Transaction of type %s is mined at block %s with height %s",
        txValue.getTxType(), txValue.getBlockHash(), txValue.getBlockHeight()));

    return postTxResponse;
  }

  private TransactionResult waitForTxMined(String txHash) throws Throwable {
    int blockHeight = -1;
    TransactionResult minedTx = null;
    int doneTrials = 1;

    while (blockHeight == -1 && doneTrials < serviceConfig.getNumTrialsDefault()) {
      minedTx = callMethodAndGetResult(
          () -> aeternityService.info.asyncGetTransactionByHash(txHash), TransactionResult.class);
      if (minedTx.getBlockHeight().intValue() > 1) {
        log.debug("Mined tx: " + minedTx);
        blockHeight = minedTx.getBlockHeight().intValue();
      } else {
        log.debug(String.format("Transaction not mined yet, trying again in 1 second (%s of %s)...",
            doneTrials, serviceConfig.getNumTrialsDefault()));
        Thread.sleep(1000);
        doneTrials++;
      }
    }

    if (blockHeight == -1) {
      throw new InterruptedException(String
          .format("Transaction %s was not mined after %s trials, aborting", txHash, doneTrials));
    }

    return minedTx;
  }

  private <T> T callMethodAndGetResult(Supplier<Single<T>> observerMethod, Class<T> type)
      throws Throwable {
    return callMethodAndGetResult(serviceConfig.getNumTrialsDefault(), observerMethod, type, false);
  }

  private <T> T callMethodAndGetResult(Integer numTrials, Supplier<Single<T>> observerMethod,
      Class<T> type, boolean awaitException) throws Throwable {

    if (numTrials == null) {
      numTrials = serviceConfig.getNumTrialsDefault();
    }

    int doneTrials = 1;
    T result = null;

    do {
      Single<T> resultSingle = observerMethod.get();
      TestObserver<T> singleTestObserver = resultSingle.test();
      singleTestObserver.awaitTerminalEvent();
      if (singleTestObserver.errorCount() > 0) {
        if (awaitException) {
          throw singleTestObserver.errors().get(0);
        }
        if (doneTrials == numTrials) {
          log.error("Following error(s) occured while waiting for result of call, aborting");
          for (Throwable error : singleTestObserver.errors()) {
            log.error(error.toString());
          }
          throw new InterruptedException("Max number of function call trials exceeded, aborting");
        }
        log.debug(String.format(
            "Unable to receive object of type %s, trying again in 1 second (%s of %s)...",
            type.getSimpleName(), doneTrials, numTrials));
        Thread.sleep(1000);
        doneTrials++;
      } else {
        if (!awaitException) {
          result = singleTestObserver.values().get(0);
        } else {
          log.debug(String.format("Waiting for exception, trying again in 1 second (%s of %s)...",
              doneTrials, numTrials));
          Thread.sleep(1000);
          doneTrials++;
        }
      }
    } while (result == null);
    return result;
  }

  private BigInteger getNextKeypairNonce(String address) {
    AccountResult accountResult = getAccount(address);
    if (accountResult.getRootErrorMessage() != null) {
      log.info("Account not found.");
      return BigInteger.ONE;
    }
    return accountResult.getNonce().add(BigInteger.ONE);
  }

  /**
   * ---------------------------------------------------------------------------------
   * <p>
   * Local node methods
   * <p>
   * ---------------------------------------------------------------------------------
   */

  private KeyPair beneficiaryKeyPair;

  private void initLocalAccounts() {
    try {
      log.info("Initializing local accounts");
      beneficiaryKeyPair = keyPairService.recoverKeyPair(serviceConfig.getBeneficiaryPrivateKey());
      fundAddress(serviceKeyPair.getAddress(),
          unitConversionService18Decimals.toSmallestUnit("10"));
      log.info("Funded local service account '{}' with 10 AE", serviceKeyPair.getAddress());
      for (String address : serviceConfig.getLocalUserAddresses()) {
        fundAddress(address, unitConversionService18Decimals.toSmallestUnit("100"));
        log.info("Funded local user account '{}' with 100 AE", address);
      }
    } catch (Throwable e) {
      log.error("Error funding service account", e);
    }
  }

  private void fundAddress(String recipient, BigInteger amount) throws Throwable {
    log.debug("Spending amount of {} to recipient {}", amount, recipient);
    SpendTransactionModel spendTx = SpendTransactionModel.builder().amount(amount)
        .sender(beneficiaryKeyPair.getAddress()).recipient(recipient).ttl(BigInteger.ZERO)
        .nonce(getNextKeypairNonce(beneficiaryKeyPair.getAddress())).build();
    PostTransactionResult postResult =
        blockingPostTx(spendTx, serviceConfig.getBeneficiaryPrivateKey());
    log.debug("Spending amount of {} to recipient {} successful: {}", amount, recipient,
        postResult);
  }
}
