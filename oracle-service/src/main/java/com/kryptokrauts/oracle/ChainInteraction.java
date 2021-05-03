package com.kryptokrauts.oracle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.kryptokrauts.aeternity.sdk.domain.secret.KeyPair;
import com.kryptokrauts.aeternity.sdk.service.account.domain.AccountResult;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.aeternity.sdk.service.info.domain.KeyBlockResult;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionResult;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairService;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueriesResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueryResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleTTLType;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.RegisteredOracleResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.PostTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.AbstractTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleExtendTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleRegisterTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleRespondTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.SpendTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.unit.UnitConversionService;
import com.kryptokrauts.aeternity.sdk.service.unit.impl.DefaultUnitConversionServiceImpl;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ChainInteraction {

  private AeternityServiceConfiguration config;

  private AeternityService aeternityService;

  private KeyPair oracleKeyPair;

  private KeyPairService keyPairService;

  private UnitConversionService unitConversionService18Decimals;

  @Inject
  private OracleConfigration configuration;

  @PostConstruct
  private void init() throws Throwable {
    log.info("Initializing chain connection");
    keyPairService = new KeyPairServiceFactory().getService();
    oracleKeyPair = keyPairService.recoverKeyPair(configuration.getOraclePrivateKey());
    config = AeternityServiceConfiguration.configure()
        .compilerBaseUrl(configuration.getCompilerUrl()).baseUrl(configuration.getBaseUrl())
        .network(configuration.getNetwork()).keyPair(oracleKeyPair)
        .targetVM(configuration.getTargetVM().withVmVersion(configuration.getVmVersion())
            .withAbiVersion(configuration.getAbiVersion()))
        .millisBetweenTrailsToWaitForConfirmation(100l).compile();
    aeternityService = new AeternityServiceFactory().getService(config);
    unitConversionService18Decimals = new DefaultUnitConversionServiceImpl();
    log.info("Chain connection initialization successful");
    if (configuration.isLocalNode()) {
      initLocalNodeOracle();
    }
  }

  /**
   * check if queries for this oracle exist
   * 
   * @return
   */
  public OracleQueryResult checkForOracleQueries() {
    try {
      OracleQueriesResult oracleQueriesResult =
          aeternityService.oracles.blockingGetOracleQueries(oracleKeyPair.getOracleAddress());
      if (oracleQueriesResult != null && oracleQueriesResult.getQueryResults().size() > 0) {
        log.info("Got {} oracle queries processing first one",
            oracleQueriesResult.getQueryResults().size());
        return oracleQueriesResult.getQueryResults().stream().findFirst().get();
      }
    } catch (Throwable e) {
      log.error("Error checking for oracle queries", e);
    }
    return null;
  }

  /**
   * respond to an oracle query
   * 
   * @param oracleQueryResult
   */
  public void respondToQuery(OracleQueryResult oracleQueryResult, String outcome) {
    try {
      BigInteger nonce = getNextKeypairNonce(oracleKeyPair.getAddress());
      OracleRespondTransactionModel oracleRespondTx = OracleRespondTransactionModel.builder()
          .oracleId(oracleKeyPair.getOracleAddress()).queryId(oracleQueryResult.getId())
          .nonce(nonce).response(outcome).responseTtl(oracleQueryResult.getResponseTtl().getValue())
          .ttl(BigInteger.ZERO).build();
      this.blockingPostTx(oracleRespondTx, oracleKeyPair.getEncodedPrivateKey());
      log.info("Oracle responded with {} to query {}", outcome, oracleQueryResult.getQuery());
    } catch (Throwable e) {
      log.error("Error creating response for oracle query", e);
    }
  }

  /**
   * check if the oracle is registered
   * 
   * if no -> register
   * 
   * if yes -> check if the oracles ttl is still sufficient
   * 
   * if no -> extend
   */
  public void extendOrRegisterOracle() {
    KeyBlockResult keyBlockResult = aeternityService.info.blockingGetCurrentKeyBlock();
    RegisteredOracleResult registeredOracleResult =
        aeternityService.oracles.blockingGetRegisteredOracle(oracleKeyPair.getOracleAddress());
    if (registeredOracleResult == null || registeredOracleResult.getTtl() == null) {
      registerOracle();
    } else if (keyBlockResult != null && registeredOracleResult != null
        && registeredOracleResult.getTtl() != null) {
      log.info("Oracle is about to expire at block {} - current at block height is {}",
          registeredOracleResult.getTtl().longValue(), keyBlockResult.getHeight().longValue());
      long oracleTtlLeft =
          registeredOracleResult.getTtl().longValue() - keyBlockResult.getHeight().longValue();
      if (oracleTtlLeft <= configuration.getOracleMinBlocksExtensionTrigger()) {
        extendOracle();
      }
    }
  }

  /**
   * ---------------------------------------------------------------------------------
   * 
   * Helper methods
   * 
   * ---------------------------------------------------------------------------------
   */

  private void registerOracle() {
    try {
      log.info("Oracle is not registered, trying to register it");
      BigInteger nonce = getAccount(oracleKeyPair.getAddress()).getNonce().add(BigInteger.ONE);
      OracleRegisterTransactionModel oracleRegisterTx = OracleRegisterTransactionModel.builder()
          .accountId(oracleKeyPair.getAddress()).abiVersion(BigInteger.ZERO).nonce(nonce)
          .oracleTtl(configuration.getOracleInitialTTL()).oracleTtlType(OracleTTLType.DELTA)
          .queryFee(configuration.getOracleQueryFee()).queryFormat("string")
          .responseFormat("string").ttl(BigInteger.ZERO).build();
      PostTransactionResult postResult = this.blockingPostTx(oracleRegisterTx);
      log.info("Oracle {} successfully registered within transaction: {}",
          oracleKeyPair.getAddress(), postResult.getTxHash());
    } catch (Throwable e) {
      log.error("Error checking if registering oracle", e);
    }
  }

  private void extendOracle() {
    try {
      BigInteger nonce = getNextKeypairNonce(oracleKeyPair.getAddress());
      BigInteger extensionOracleTtl =
          getCurrentBlockHeight().add(configuration.getOracleExtensionTTL());
      log.info("Oracle is about to expire - extending for another {} blocks until block {}",
          configuration.getOracleExtensionTTL(), extensionOracleTtl);
      OracleExtendTransactionModel oracleExtendTx = OracleExtendTransactionModel.builder()
          .nonce(nonce).oracleId(oracleKeyPair.getOracleAddress())
          .oracleRelativeTtl(configuration.getOracleExtensionTTL()).ttl(BigInteger.ZERO).build();
      this.blockingPostTx(oracleExtendTx, oracleKeyPair.getEncodedPrivateKey());
    } catch (Throwable e) {
      log.error("Error extending oracle", e);
    }
  }

  private BigInteger getCurrentBlockHeight() {
    return aeternityService.info.blockingGetCurrentKeyBlock().getHeight();
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
      privateKey = configuration.getOraclePrivateKey();
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

    while (blockHeight == -1 && doneTrials < configuration.getNumTrialsDefault()) {
      minedTx = callMethodAndGetResult(
          () -> aeternityService.info.asyncGetTransactionByHash(txHash), TransactionResult.class);
      if (minedTx.getBlockHeight().intValue() > 1) {
        log.debug("Mined tx: " + minedTx);
        blockHeight = minedTx.getBlockHeight().intValue();
      } else {
        log.debug(String.format("Transaction not mined yet, trying again in 1 second (%s of %s)...",
            doneTrials, configuration.getNumTrialsDefault()));
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
    return callMethodAndGetResult(configuration.getNumTrialsDefault(), observerMethod, type, false);
  }

  private <T> T callMethodAndGetResult(Integer numTrials, Supplier<Single<T>> observerMethod,
      Class<T> type, boolean awaitException) throws Throwable {

    if (numTrials == null) {
      numTrials = configuration.getNumTrialsDefault();
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
    return getAccount(address).getNonce().add(BigInteger.ONE);
  }

  /**
   * ---------------------------------------------------------------------------------
   * 
   * Local node methods
   * 
   * ---------------------------------------------------------------------------------
   */

  private KeyPair beneficiaryKeyPair;

  private void initLocalNodeOracle() {
    try {
      log.info("Initializing local node oracle");
      beneficiaryKeyPair = keyPairService.recoverKeyPair(configuration.getBeneficiaryPrivateKey());
      fundAddress(oracleKeyPair.getAddress(),
          unitConversionService18Decimals.toSmallestUnit(BigDecimal.ONE));
      log.info("Funded local oracle address with 1 AE");
    } catch (Throwable e) {
      log.error("Error funding oracle", e);
    }
  }

  private void fundAddress(String recipient, BigInteger amount) throws Throwable {
    log.debug("Spending amount of {} to recipient {}", amount, recipient);
    SpendTransactionModel spendTx = SpendTransactionModel.builder().amount(amount)
        .sender(beneficiaryKeyPair.getAddress()).recipient(recipient).ttl(BigInteger.ZERO)
        .nonce(getNextKeypairNonce(beneficiaryKeyPair.getAddress())).build();
    PostTransactionResult postResult =
        blockingPostTx(spendTx, configuration.getBeneficiaryPrivateKey());
    log.debug("Spending amount of {} to recipient {} successful: {}", amount, recipient,
        postResult);
  }
}
