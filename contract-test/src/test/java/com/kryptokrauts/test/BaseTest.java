package com.kryptokrauts.test;

import com.kryptokrauts.aeternity.sdk.constants.BaseConstants;
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
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueriesResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueryResult;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleTTLType;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.PostTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.AbstractTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleRegisterTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.OracleRespondTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.SpendTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.unit.UnitConversionService;
import com.kryptokrauts.aeternity.sdk.service.unit.impl.DefaultUnitConversionServiceImpl;
import com.kryptokrauts.aeternity.sdk.util.UnitConversionUtil;
import com.kryptokrauts.aeternity.sdk.util.UnitConversionUtil.Unit;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.math.BigInteger;
import java.util.Random;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

@Slf4j
public class BaseTest {

  protected static final BigInteger ONE = BigInteger.ONE;

  protected static final BigInteger ZERO = BigInteger.ZERO;

  private static final String PRIVATE_KEY =
      "79816BBF860B95600DDFABF9D81FEE81BDB30BE823B17D80B9E48BE0A7015ADF";

  protected static UnitConversionService unitConversionService18Decimals =
      new DefaultUnitConversionServiceImpl();

  protected static KeyPair beneficiaryKeyPair;

  protected static AeternityServiceConfiguration config;

  protected static AeternityService aeternityService;

  static int NUM_TRIALS_DEFAULT = 60;

  protected static KeyPairService keyPairService = new KeyPairServiceFactory().getService();

  protected static KeyPair oracleKeyPair;

  protected static String oracleAddress;

  private static final boolean deployOnTestnet = false;

  @BeforeAll
  public static void init() throws Throwable {
    KeyPairService keyPairService = new KeyPairServiceFactory().getService();
    beneficiaryKeyPair = keyPairService.recoverKeyPair(PRIVATE_KEY);
    log.info("Beneficiary Keypair: {}", beneficiaryKeyPair.getAddress());
    System.setProperty("testnet_deploy", new Boolean(deployOnTestnet).toString());
    if (deployOnTestnet) {
      initTestNet();
    } else {
      initLocal();
    }
  }

  public static void initLocal() throws Throwable {
    log.info("Initializing for local node");
    config =
        AeternityServiceConfiguration.configure()
            .compilerBaseUrl("http://compiler.aelocal:3080")
            .baseUrl("http://aelocal")
            .network(Network.DEVNET)
            .indaexBaseUrl("http://aelocal/v2")
            .keyPair(beneficiaryKeyPair)
            .targetVM(
                VirtualMachine.FATE
                    .withVmVersion(new BigInteger("5"))
                    .withAbiVersion(new BigInteger("3")))
            .millisBetweenTrailsToWaitForConfirmation(100l)
            .compile();
    aeternityService = new AeternityServiceFactory().getService(config);
    log.info("Registering new oracle");
    oracleKeyPair = registerOracle();
    oracleAddress = oracleKeyPair.getOracleAddress();
    log.info("Registered oracle has address {}", oracleAddress);
  }

  private static void initTestNet() {
    log.info("Initializing for testnet");
    oracleAddress = "ok_4wfqkCeV6D5bTqRKkDSvpoxMPofACvPPXevsMcpRygxKMYVST";
    config =
        AeternityServiceConfiguration.configure()
            .compilerBaseUrl(BaseConstants.DEFAULT_TESTNET_COMPILER_URL)
            .baseUrl(BaseConstants.DEFAULT_TESTNET_URL)
            .network(Network.TESTNET)
            .keyPair(beneficiaryKeyPair)
            .targetVM(
                VirtualMachine.FATE
                    .withVmVersion(new BigInteger("5"))
                    .withAbiVersion(new BigInteger("3")))
            .millisBetweenTrailsToWaitForConfirmation(100l)
            .compile();
    aeternityService = new AeternityServiceFactory().getService(config);
  }

  protected static BigInteger getNextKeypairNonce(String address) {
    return getAccount(address).getNonce().add(ONE);
  }

  protected static AccountResult getAccount(String address) {
    if (address == null) {
      return aeternityService.accounts.blockingGetAccount();
    }
    return aeternityService.accounts.blockingGetAccount(address);
  }

  protected static PostTransactionResult blockingPostTx(AbstractTransactionModel<?> tx)
      throws Throwable {
    return blockingPostTx(tx, null);
  }

  protected static PostTransactionResult blockingPostTx(
      AbstractTransactionModel<?> tx, String privateKey) throws Throwable {
    if (privateKey == null) {
      privateKey = beneficiaryKeyPair.getEncodedPrivateKey();
    }
    PostTransactionResult postTxResponse =
        aeternityService.transactions.blockingPostTransaction(tx, privateKey);
    log.debug("PostTx hash: " + postTxResponse.getTxHash());
    TransactionResult txValue = waitForTxMined(postTxResponse.getTxHash());
    log.debug(
        String.format(
            "Transaction of type %s is mined at block %s with height %s",
            txValue.getTxType(), txValue.getBlockHash(), txValue.getBlockHeight()));

    return postTxResponse;
  }

  protected static TransactionResult waitForTxMined(String txHash) throws Throwable {
    int blockHeight = -1;
    TransactionResult minedTx = null;
    int doneTrials = 1;

    while (blockHeight == -1 && doneTrials < NUM_TRIALS_DEFAULT) {
      minedTx =
          callMethodAndGetResult(
              () -> aeternityService.info.asyncGetTransactionByHash(txHash),
              TransactionResult.class);
      if (minedTx.getBlockHeight().intValue() > 1) {
        log.debug("Mined tx: " + minedTx);
        blockHeight = minedTx.getBlockHeight().intValue();
      } else {
        log.debug(
            String.format(
                "Transaction not mined yet, trying again in 1 second (%s of %s)...",
                doneTrials, NUM_TRIALS_DEFAULT));
        Thread.sleep(1000);
        doneTrials++;
      }
    }

    if (blockHeight == -1) {
      throw new InterruptedException(
          String.format(
              "Transaction %s was not mined after %s trials, aborting", txHash, doneTrials));
    }

    return minedTx;
  }

  protected static <T> T callMethodAndGetResult(Supplier<Single<T>> observerMethod, Class<T> type)
      throws Throwable {
    return callMethodAndGetResult(NUM_TRIALS_DEFAULT, observerMethod, type, false);
  }

  protected static <T> T callMethodAndGetResult(
      Integer numTrials, Supplier<Single<T>> observerMethod, Class<T> type, boolean awaitException)
      throws Throwable {

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
        log.debug(
            String.format(
                "Unable to receive object of type %s, trying again in 1 second (%s of %s)...",
                type.getSimpleName(), doneTrials, numTrials));
        Thread.sleep(1000);
        doneTrials++;
      } else {
        if (!awaitException) {
          result = singleTestObserver.values().get(0);
        } else {
          log.debug(
              String.format(
                  "Waiting for exception, trying again in 1 second (%s of %s)...",
                  doneTrials, numTrials));
          Thread.sleep(1000);
          doneTrials++;
        }
      }
    } while (result == null);

    return result;
  }

  protected static KeyPair registerOracle() throws Throwable {
    KeyPair oracleKeyPair = keyPairService.generateKeyPair();
    BigInteger amount = UnitConversionUtil.toAettos("10", Unit.AE).toBigInteger();
    SpendTransactionModel spendTx =
        SpendTransactionModel.builder()
            .amount(amount)
            .sender(beneficiaryKeyPair.getAddress())
            .recipient(oracleKeyPair.getAddress())
            .ttl(BigInteger.ZERO)
            .nonce(getNextKeypairNonce(beneficiaryKeyPair.getAddress()))
            .build();
    PostTransactionResult postResult = blockingPostTx(spendTx);
    log.debug(postResult.getTxHash());

    BigInteger currentHeight = aeternityService.info.blockingGetCurrentKeyBlock().getHeight();
    BigInteger initialOracleTtl = currentHeight.add(BigInteger.valueOf(5000));

    OracleRegisterTransactionModel oracleRegisterTx =
        OracleRegisterTransactionModel.builder()
            .accountId(oracleKeyPair.getAddress())
            .abiVersion(ZERO)
            .nonce(getAccount(oracleKeyPair.getAddress()).getNonce().add(ONE))
            .oracleTtl(initialOracleTtl)
            .oracleTtlType(OracleTTLType.BLOCK)
            .queryFee(BigInteger.valueOf(100))
            .queryFormat("string")
            .responseFormat("string")
            .ttl(ZERO)
            .build();

    postResult = blockingPostTx(oracleRegisterTx, oracleKeyPair.getEncodedPrivateKey());

    return oracleKeyPair;
  }

  protected static void fundAddress(String recipient, BigInteger amount) throws Throwable {
    log.debug("Spending amount of {} to recipient {}", amount, recipient);
    SpendTransactionModel spendTx =
        SpendTransactionModel.builder()
            .amount(amount)
            .sender(beneficiaryKeyPair.getAddress())
            .recipient(recipient)
            .ttl(BigInteger.ZERO)
            .nonce(getNextKeypairNonce(beneficiaryKeyPair.getAddress()))
            .build();
    PostTransactionResult postResult = blockingPostTx(spendTx);
    log.info("Spending amount of {} to recipient {} successful", amount, recipient);
  }

  protected static String getAddress(String otherPointer) {
    if (otherPointer.contains("_")) {
      return "ak_" + otherPointer.split("_")[1];
    }
    return otherPointer;
  }

  protected void simulateOracleResponse() throws Throwable {
    OracleQueriesResult oracleQueriesResult =
        aeternityService.oracles.blockingGetOracleQueries(oracleKeyPair.getOracleAddress());
    OracleQueryResult oracleQueryResult =
        oracleQueriesResult.getQueryResults().stream().findFirst().get();
    System.out.println("First oracle query result: " + oracleQueriesResult);

    BigInteger nonce = getAccount(oracleKeyPair.getAddress()).getNonce().add(ONE);
    OracleRespondTransactionModel oracleRespondTx =
        OracleRespondTransactionModel.builder()
            .oracleId(oracleKeyPair.getOracleAddress())
            .queryId(oracleQueryResult.getId())
            .nonce(nonce)
            .response(new Integer(new Random().nextInt(100000)).toString())
            .responseTtl(BigInteger.valueOf(1000))
            .ttl(ZERO)
            .build();

    PostTransactionResult postResult =
        this.blockingPostTx(oracleRespondTx, oracleKeyPair.getEncodedPrivateKey());
    System.out.println("Oracle posted response: " + postResult);
  }
}
