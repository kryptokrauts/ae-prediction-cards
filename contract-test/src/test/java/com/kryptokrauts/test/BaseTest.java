package com.kryptokrauts.test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
import com.kryptokrauts.aeternity.sdk.constants.AENS;
import com.kryptokrauts.aeternity.sdk.constants.Network;
import com.kryptokrauts.aeternity.sdk.constants.VirtualMachine;
import com.kryptokrauts.aeternity.sdk.domain.secret.KeyPair;
import com.kryptokrauts.aeternity.sdk.service.account.domain.AccountResult;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionResult;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairService;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.name.domain.NameEntryResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueriesResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueryResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleTTLType;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.PostTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.AbstractTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.NameClaimTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.NamePreclaimTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.NameUpdateTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleQueryTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleRegisterTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleRespondTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.SpendTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.unit.UnitConversionService;
import com.kryptokrauts.aeternity.sdk.service.unit.impl.DefaultUnitConversionServiceImpl;
import com.kryptokrauts.aeternity.sdk.util.CryptoUtils;
import com.kryptokrauts.aeternity.sdk.util.EncodingUtils;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseTest {


  protected static UnitConversionService unitConversionService18Decimals;

  protected static KeyPair beneficiaryKeyPair;

  protected static KeyPair oracleKeyPair;

  protected static AeternityServiceConfiguration config;

  protected static AeternityService aeternityService;

  protected static KeyPairService keyPairService;


  @BeforeAll
  public static void init() {
    log.info("Initializing for local node");
    keyPairService = new KeyPairServiceFactory().getService();
    unitConversionService18Decimals = new DefaultUnitConversionServiceImpl();
    beneficiaryKeyPair = keyPairService.recoverKeyPair(BENEFICIARY_PRIVATE_KEY);
    log.info("Recovered Beneficiary Keypair: {}", beneficiaryKeyPair.getAddress());
    oracleKeyPair = keyPairService.recoverKeyPair(ORACLE_PRIVATE_KEY);
    log.info("Recovered Oracle Keypair: {}", oracleKeyPair.getAddress());

    config = getConfigForKeypair(beneficiaryKeyPair);
    aeternityService = new AeternityServiceFactory().getService(config);
  }

  protected static AeternityServiceConfiguration getConfigForKeypair(KeyPair kp) {
    return AeternityServiceConfiguration.configure().compilerBaseUrl("http://localhost:3080")
        .baseUrl("http://localhost").network(Network.LOCAL_LIMA_NETWORK)
        .indaexBaseUrl("http://localhost:4000").keyPair(kp)
        .targetVM(VirtualMachine.FATE.withVmVersion(new BigInteger("5"))
            .withAbiVersion(new BigInteger("3")))
        .millisBetweenTrailsToWaitForConfirmation(100l).compile();
  }

  protected BigInteger getNextKeypairNonce() {
    return getAccount(beneficiaryKeyPair.getAddress()).getNonce().add(ONE);
  }

  protected BigInteger getNextKeypairNonce(String address) {
    return getAccount(address).getNonce().add(ONE);
  }

  protected AccountResult getAccount(String address) {
    if (address == null) {
      return aeternityService.accounts.blockingGetAccount();
    }
    return aeternityService.accounts.blockingGetAccount(address);
  }

  protected PostTransactionResult blockingPostTx(AbstractTransactionModel<?> tx) {
    return blockingPostTx(tx, null);
  }

  protected PostTransactionResult blockingPostTx(AbstractTransactionModel<?> tx,
      String privateKey) {
    if (privateKey == null) {
      privateKey = beneficiaryKeyPair.getEncodedPrivateKey();
    }
    PostTransactionResult postTxResponse =
        aeternityService.transactions.blockingPostTransaction(tx, privateKey);
    log.debug("PostTx hash: " + postTxResponse.getTxHash());
    TransactionResult txValue = waitForTxMined(postTxResponse.getTxHash());
    log.debug(String.format("Transaction of type %s is mined at block %s with height %s",
        txValue.getTxType(), txValue.getBlockHash(), txValue.getBlockHeight()));

    return postTxResponse;
  }

  public static void main(String[] args) {
    String s =
        "{oracle=oracle=ok_23rbLat3Yo8thVji6hosFqK2n2ddVCoTfCCLp6jrDCXtE92oyj,relative_query_ttl=1000,rent_interval=86400000,predictions={1=id=1,start_timestamp=1620249983118,end_timestamp=1620250103118,max_increase_rent_amount_aettos=11574074074,asset=BTC,target_price=75000,nft_lower_equal_id=1,nft_higher_id=2,last_rent_timestamp={2=1620250103118},nft_last_rent_aettos_per_millisecond={2=11574074074},nft_hodl_time={2={address=ak_twR4h7dEcUtc2iSEDv8kB7UFJJDGiEDQCXr85C3fYF8FdVdyo=48975}},collected_nft_balance={2=566840277774150},oracle_query=Optional[oracle_query=oq_ozWKqethTcjww1juyLU3PoDnPPMEWdMqcypJERgXr8i36ouF4],winning_nft_id=Optional[2],renter_claimed={}, 2=id=2,start_timestamp=1620250448005,end_timestamp=1620250568005,max_increase_rent_amount_aettos=11574074074,asset=BTC,target_price=75000,nft_lower_equal_id=3,nft_higher_id=4,last_rent_timestamp={4=1620250568005},nft_last_rent_aettos_per_millisecond={4=11574074074},nft_hodl_time={4={address=ak_twR4h7dEcUtc2iSEDv8kB7UFJJDGiEDQCXr85C3fYF8FdVdyo=48794}},collected_nft_balance={4=564745370366756},oracle_query=Optional[oracle_query=oq_2rZXSZNjbBUqgHf9Joj8bo2cctDTTGpLg1fCukzWudYXFNkVf9],winning_nft_id=Optional[4],renter_claimed={}, 3=id=3,start_timestamp=1620250755895,end_timestamp=1620250875895,max_increase_rent_amount_aettos=11574074074,asset=BTC,target_price=75000,nft_lower_equal_id=5,nft_higher_id=6,last_rent_timestamp={6=1620250827191},nft_last_rent_aettos_per_millisecond={6=11574074074},nft_hodl_time={},collected_nft_balance={},oracle_query=Optional.empty,winning_nft_id=Optional.empty,renter_claimed={}},nft_meta={1=0x00, 2=0x01, 3=0x00, 4=0x01, 5=0x00, 6=0x01},nft_prediction={1=1, 2=1, 3=2, 4=2, 5=3, 6=3},nft_renter={2=address=ak_twR4h7dEcUtc2iSEDv8kB7UFJJDGiEDQCXr85C3fYF8FdVdyo, 4=address=ak_twR4h7dEcUtc2iSEDv8kB7UFJJDGiEDQCXr85C3fYF8FdVdyo, 6=address=ak_twR4h7dEcUtc2iSEDv8kB7UFJJDGiEDQCXr85C3fYF8FdVdyo},renter_nft_balance={address=ak_twR4h7dEcUtc2iSEDv8kB7UFJJDGiEDQCXr85C3fYF8FdVdyo=nft_balance={6=3000000000000000000}},next_prediction_id=4,next_nft_id=7}";
    System.out.println(Json.encode(s));
  }

  protected TransactionResult waitForTxMined(String txHash) {
    try {
      int blockHeight = -1;
      TransactionResult minedTx = null;
      int doneTrials = 1;

      while (blockHeight == -1 && doneTrials < NUM_TRIALS_DEFAULT) {
        minedTx = callMethodAndGetResult(
            () -> aeternityService.info.asyncGetTransactionByHash(txHash), TransactionResult.class);
        if (minedTx.getBlockHeight().intValue() > 1) {
          log.debug("Mined tx: " + minedTx);
          blockHeight = minedTx.getBlockHeight().intValue();
        } else {
          log.debug(
              String.format("Transaction not mined yet, trying again in 1 second (%s of %s)...",
                  doneTrials, NUM_TRIALS_DEFAULT));
          Thread.sleep(1000);
          doneTrials++;
        }
      }

      if (blockHeight == -1) {
        throw new InterruptedException(String
            .format("Transaction %s was not mined after %s trials, aborting", txHash, doneTrials));
      }
      return minedTx;
    } catch (Throwable e) {
      throw new RuntimeException("Test failed due to", e);
    }
  }

  protected <T> T callMethodAndGetResult(Supplier<Single<T>> observerMethod, Class<T> type)
      throws Throwable {
    return callMethodAndGetResult(NUM_TRIALS_DEFAULT, observerMethod, type, false);
  }

  protected <T> T callMethodAndGetResult(Integer numTrials, Supplier<Single<T>> observerMethod,
      Class<T> type, boolean awaitException) throws Throwable {

    if (numTrials == null) {
      numTrials = NUM_TRIALS_DEFAULT;
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

  protected KeyPair registerOracle() {
    log.info("Registering oracle {}", oracleKeyPair.getOracleAddress());
    fundAddress(oracleKeyPair.getAddress(), unitConversionService18Decimals.toSmallestUnit("1"));

    BigInteger currentHeight = aeternityService.info.blockingGetCurrentKeyBlock().getHeight();
    BigInteger initialOracleTtl = currentHeight.add(BigInteger.valueOf(5000));

    OracleRegisterTransactionModel oracleRegisterTx =
        OracleRegisterTransactionModel.builder().accountId(oracleKeyPair.getAddress())
            .abiVersion(ZERO).nonce(getAccount(oracleKeyPair.getAddress()).getNonce().add(ONE))
            .oracleTtl(initialOracleTtl).oracleTtlType(OracleTTLType.BLOCK)
            .queryFee(BigInteger.valueOf(100)).queryFormat("string").responseFormat("string")
            .ttl(ZERO).build();
    blockingPostTx(oracleRegisterTx, oracleKeyPair.getEncodedPrivateKey());
    log.info("New oracle {} registered", oracleKeyPair.getOracleAddress());
    return oracleKeyPair;
  }

  protected void fundAddress(String recipient, BigInteger amount) {
    log.debug("Spending amount of {} to recipient {}", amount, recipient);
    SpendTransactionModel spendTx = SpendTransactionModel.builder().amount(amount)
        .sender(beneficiaryKeyPair.getAddress()).recipient(recipient).ttl(BigInteger.ZERO)
        .nonce(getNextKeypairNonce(beneficiaryKeyPair.getAddress())).build();
    blockingPostTx(spendTx);
    log.info("Spending amount of {} to recipient {} successful", amount, recipient);
  }

  protected String getAddress(String otherPointer) {
    if (otherPointer.contains("_")) {
      return "ak_" + otherPointer.split("_")[1];
    }
    return otherPointer;
  }

  protected void simulateOracleResponse(String outcome) {
    OracleQueriesResult oracleQueriesResult =
        aeternityService.oracles.blockingGetOracleQueries(oracleKeyPair.getOracleAddress());
    OracleQueryResult oracleQueryResult =
        oracleQueriesResult.getQueryResults().stream().findFirst().get();
    log.info("First oracle query result: {}", oracleQueriesResult);

    BigInteger nonce = getAccount(oracleKeyPair.getAddress()).getNonce().add(ONE);
    OracleRespondTransactionModel oracleRespondTx =
        OracleRespondTransactionModel.builder().oracleId(oracleKeyPair.getOracleAddress())
            .queryId(oracleQueryResult.getId()).nonce(nonce).response(outcome)
            .responseTtl(oracleQueryResult.getResponseTtl().getValue()).ttl(ZERO).build();

    PostTransactionResult postResult =
        blockingPostTx(oracleRespondTx, oracleKeyPair.getEncodedPrivateKey());
    log.info("Oracle posted response: {}", postResult);
  }

  protected void claimName() {
    BigInteger salt = CryptoUtils.generateNamespaceSalt();
    NamePreclaimTransactionModel namePreclaimTx =
        NamePreclaimTransactionModel.builder().accountId(beneficiaryKeyPair.getAddress())
            .name(predictioncards).salt(salt).nonce(getNextKeypairNonce()).ttl(ZERO).build();
    blockingPostTx(namePreclaimTx);
    NameClaimTransactionModel nameClaimTx =
        NameClaimTransactionModel.builder().accountId(beneficiaryKeyPair.getAddress())
            .name(predictioncards).nameSalt(salt).nonce(getNextKeypairNonce()).ttl(ZERO).build();
    blockingPostTx(nameClaimTx);
    log.info("Name {} claimed for contract", predictioncards);
  }

  protected void updateContractPointer(String contractId) {

    BigInteger nameTtl = BigInteger.valueOf(50000l);
    BigInteger clientTtl = BigInteger.valueOf(50l);

    NameEntryResult nameEntryResult = aeternityService.names.blockingGetNameId(predictioncards);

    NameUpdateTransactionModel nameUpdateTx = NameUpdateTransactionModel.builder()
        .accountId(beneficiaryKeyPair.getAddress()).nameId(nameEntryResult.getId())
        .nonce(getNextKeypairNonce(beneficiaryKeyPair.getAddress())).ttl(ZERO).clientTtl(clientTtl)
        .nameTtl(nameTtl).pointers(new HashMap<String, String>() {
          {
            put(AENS.POINTER_KEY_CONTRACT, contractId);
          }
        }).build();
    blockingPostTx(nameUpdateTx);
    log.info("Contract now points to {}", predictioncards);
  }

  protected void createOracleQuery(String coin, String price) throws Throwable {
    log.info("Creating oracle query for coin {} and price {}", coin, price);
    String oracleId = oracleKeyPair.getOracleAddress();
    BigInteger nonce = getNextKeypairNonce();
    OracleQueryTransactionModel oracleQueryTx = OracleQueryTransactionModel.builder()
        .senderId(beneficiaryKeyPair.getAddress()).oracleId(oracleId).nonce(nonce)
        .query(coin + ";" + price + ";" + System.currentTimeMillis())
        .queryFee(BigInteger.valueOf(100)).queryTtl(BigInteger.valueOf(100)).ttl(ZERO)
        .queryTtlType(OracleTTLType.DELTA).responseTtl(BigInteger.valueOf(100)).build();

    PostTransactionResult postResult =
        blockingPostTx(oracleQueryTx, beneficiaryKeyPair.getEncodedPrivateKey());
    log.info(postResult.getTxHash());
    String queryId = EncodingUtils.queryId(beneficiaryKeyPair.getAddress(), nonce, oracleId);
    OracleQueryResult oracleQuery =
        aeternityService.oracles.blockingGetOracleQuery(oracleId, queryId);
    log.info(oracleQuery.toString());
  }

  protected String getPredictionCardsContractId() {
    return aeternityService.names.blockingGetNameId(predictioncards).getContractPointer().get();
  }

  protected static final BigInteger ONE = BigInteger.ONE;

  protected static final BigInteger ZERO = BigInteger.ZERO;

  private static final int NUM_TRIALS_DEFAULT = 60;

  private static final String BENEFICIARY_PRIVATE_KEY =
      "79816BBF860B95600DDFABF9D81FEE81BDB30BE823B17D80B9E48BE0A7015ADF";

  private static final String ORACLE_PRIVATE_KEY =
      "1deef3b577299999062bf163fcd0ffd211f2baef1fc352f1bd762d4187e1fb968a2dc5ef45f7a3734baa332ce87423380e44fe6302760cb6bff9a99d697e47fc";

  private static final String predictioncards = "predictioncards.chain";
}
