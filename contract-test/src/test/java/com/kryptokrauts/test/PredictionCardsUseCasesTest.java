package com.kryptokrauts.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kryptokrauts.aeternity.sdk.domain.secret.KeyPair;
import com.kryptokrauts.aeternity.sdk.exception.AException;
import com.kryptokrauts.contraect.generated.PredictionCards;
import com.kryptokrauts.contraect.generated.PredictionCards.Address;
import com.kryptokrauts.contraect.generated.PredictionCards.Oracle;
import com.kryptokrauts.contraect.generated.PredictionCards.Prediction;
import java.math.BigInteger;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PredictionCardsUseCasesTest extends BaseTest {

  private static PredictionCards predictionCards;

  private static String contractId;

  private static KeyPair renter1;

  private static KeyPair renter2;

  private static KeyPair renter3;

  private static Prediction prediction;

  private static BigInteger oneAEPerDayinAettosPerMilliSecond;

  @BeforeAll
  public static void initDeployContract() throws Throwable {

    predictionCards = new PredictionCards(config, null);
    Pair<String, String> deployedContract =
        predictionCards.deploy(new Oracle(oracleAddress), Optional.empty());
    contractId = deployedContract.getValue1();
    log.info("Deploying contract successful - contract has address {}", contractId);

    oneAEPerDayinAettosPerMilliSecond =
        unitConversionService18Decimals.toSmallestUnit("1").divide(BigInteger.valueOf(86400000));

    if (Boolean.getBoolean(System.getProperty("testnet_deploy")) == false) {
      /**
       * Prediction with one AE as max diff between 2 rents It starts immediately and ends after 10
       * mins
       */
      Long TenMinsInMS = 60l * 10l * 1000l;
      long startTimestamp = System.currentTimeMillis();
      prediction =
          predictionCards
              .create_prediction(
                  BigInteger.valueOf(startTimestamp),
                  BigInteger.valueOf(startTimestamp + TenMinsInMS),
                  oneAEPerDayinAettosPerMilliSecond,
                  "Bitcoin",
                  BigInteger.valueOf(75000),
                  "0x00",
                  "0x01")
              .getValue1();
      log.info("Prediction created {}", prediction);

      renter1 = keyPairService.generateKeyPair();
      renter2 = keyPairService.generateKeyPair();
      renter3 = keyPairService.generateKeyPair();

      BigInteger oneAE = unitConversionService18Decimals.toSmallestUnit("1");
      fundAddress(renter1.getAddress(), oneAE);
      fundAddress(renter2.getAddress(), oneAE);
      fundAddress(renter3.getAddress(), oneAE);

      log.info(
          "Following renters funded with one AE: {} {} {}",
          renter1.getAddress(),
          renter2.getAddress(),
          renter3.getAddress());
    }
  }

  @Test
  @DisabledIfSystemProperty(named = "testnet_deploy", matches = "true")
  public void testRenter1CanRentFail() {
    Exception exception =
        assertThrows(
            AException.class,
            () ->
                predictionCards.can_rent(
                    new Address(renter1.getAddress()),
                    prediction.getNft_higher_id(),
                    oneAEPerDayinAettosPerMilliSecond));
    String expectedMessage = "ERROR_NO_BALANCE";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
}
