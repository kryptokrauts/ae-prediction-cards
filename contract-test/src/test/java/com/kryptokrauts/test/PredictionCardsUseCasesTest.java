package com.kryptokrauts.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import org.javatuples.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import com.kryptokrauts.aeternity.sdk.domain.secret.KeyPair;
import com.kryptokrauts.aeternity.sdk.exception.AException;
import com.kryptokrauts.contraect.generated.PredictionCards;
import com.kryptokrauts.contraect.generated.PredictionCards.Address;
import com.kryptokrauts.contraect.generated.PredictionCards.Oracle;
import com.kryptokrauts.contraect.generated.PredictionCards.Prediction;
import com.kryptokrauts.contraect.generated.PredictionCards.Prediction_state;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PredictionCardsUseCasesTest extends BaseTest {

  private static String contractId;

  private static KeyPair renter1;

  private static KeyPair renter2;

  private static KeyPair renter3;

  private static BigInteger oneAEPerDayinAettosPerMilliSecond =
      unitConversionService18Decimals.toSmallestUnit("1").divide(BigInteger.valueOf(86400000));

  @Test
  @Order(1)
  public void prepareLocalNode() {
    oracleKeyPair = registerOracle();
    deployContract();
    claimName();
    updateContractPointer(contractId);

    renter1 = keyPairService.generateKeyPair();
    renter2 = keyPairService.generateKeyPair();
    renter3 = keyPairService.generateKeyPair();

    BigInteger oneAE = unitConversionService18Decimals.toSmallestUnit("1");
    fundAddress(renter1.getAddress(), oneAE);
    fundAddress(renter2.getAddress(), oneAE);
    fundAddress(renter3.getAddress(), oneAE);

    log.info("Following renters funded with one AE: {} {} {}", renter1.getAddress(),
        renter2.getAddress(), renter3.getAddress());
  }

  @Test
  @Order(2)
  public void testMultipleRenters() {
    renter1 = keyPairService.generateKeyPair();
    renter2 = keyPairService.generateKeyPair();
    BigInteger oneAE = unitConversionService18Decimals.toSmallestUnit("1");
    fundAddress(renter1.getAddress(), oneAE.multiply(BigInteger.valueOf(150)));
    fundAddress(renter2.getAddress(), oneAE.multiply(BigInteger.valueOf(150)));
    PredictionCards contract = new PredictionCards(config, getPredictionCardsContractId());
    Long OneMins = 60l * 3l * 1000l;
    long startTimestamp = System.currentTimeMillis();

    Prediction prediction = contract.create_prediction(BigInteger.valueOf(startTimestamp),
        BigInteger.valueOf(startTimestamp + OneMins), oneAEPerDayinAettosPerMilliSecond, "BTC",
        BigInteger.valueOf(75000), "0x00", "0x01").getValue1();
    log.info("Prediction created {}", prediction);

    PredictionCards renter1Contract =
        new PredictionCards(getConfigForKeypair(renter1), getPredictionCardsContractId());
    PredictionCards renter2Contract =
        new PredictionCards(getConfigForKeypair(renter2), getPredictionCardsContractId());

    // fund renters
    renter1Contract.deposit_to_nft(prediction.getNft_higher_id(),
        unitConversionService18Decimals.toSmallestUnit(BigDecimal.valueOf(5)));
    log.info("Renter 1 balance: {}",
        aeternityService.accounts.blockingGetAccount(renter1.getAddress()).getBalance());
    renter2Contract.deposit_to_nft(prediction.getNft_higher_id(),
        unitConversionService18Decimals.toSmallestUnit(BigDecimal.valueOf(3)));
    log.info("Renter 2 balance: {}",
        aeternityService.accounts.blockingGetAccount(renter2.getAddress()).getBalance());

    // renter higher: r1, r2 ,r1
    log.info("Renting higher");
    renter1Contract.rent_nft(prediction.getNft_higher_id(), oneAEPerDayinAettosPerMilliSecond);
    renter2Contract.rent_nft(prediction.getNft_higher_id(),
        oneAEPerDayinAettosPerMilliSecond.add(BigInteger.valueOf(10000)));
    renter1Contract.rent_nft(prediction.getNft_higher_id(),
        oneAEPerDayinAettosPerMilliSecond.add(BigInteger.valueOf(100000)));
    log.info("state {}", contract.get_state());

    // rent lower: r1, r2
    log.info("Renting lower");
    renter1Contract.deposit_to_nft(prediction.getNft_lower_equal_id(),
        unitConversionService18Decimals.toSmallestUnit(BigDecimal.valueOf(30)));
    log.info("Renter 1 balance: {}",
        aeternityService.accounts.blockingGetAccount(renter1.getAddress()).getBalance());
    renter2Contract.deposit_to_nft(prediction.getNft_lower_equal_id(),
        unitConversionService18Decimals.toSmallestUnit(BigDecimal.valueOf(30)));
    log.info("Renter 2 balance: {}",
        aeternityService.accounts.blockingGetAccount(renter2.getAddress()).getBalance());
    renter1Contract.rent_nft(prediction.getNft_lower_equal_id(), oneAEPerDayinAettosPerMilliSecond);
    renter2Contract.rent_nft(prediction.getNft_lower_equal_id(),
        oneAEPerDayinAettosPerMilliSecond.add(BigInteger.valueOf(1)));
    log.info("state after renting all{}", contract.get_state());

    // wait for prediction end
    Prediction_state state = contract.get_prediction_state(prediction.getId());

    while (state.getPrediction_state().toString().equals("ACTIVE")) {
      try {
        log.info("State is {} waiting", state);
        state = contract.get_prediction_state(prediction.getId());
        BigInteger pot = contract.get_pot_size(prediction.getId());
        log.info("Pot size is {}, {}", pot, unitConversionService18Decimals.toBiggestUnit(pot));
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // resolve prediction
    log.info("Prediction ended with state: {}", contract.get_prediction_state(prediction.getId()));
    log.info("Calling oracle {}", contract.ask_for_winning_option(prediction.getId(), oneAE));
    log.info("Oracle answered? {}", contract.check_oracle_has_responded(prediction.getId()));
    log.info("Simulating answer");
    this.simulateOracleResponse("higher");
    log.info("Oracle answered? {}", contract.check_oracle_has_responded(prediction.getId()));
    log.info("State before processing response: {}", contract.get_state());
    log.info("Processing oracle response {}", contract.process_oracle_response(prediction.getId()));
    log.info("State after processing response: {}", contract.get_state());

    // claim
    log.info("Renter 1 balance before claim higher: {}",
        aeternityService.accounts.blockingGetAccount(renter1.getAddress()).getBalance());
    log.info(renter1Contract.claim(prediction.getId()));
    log.info("Renter 1 {} balance after claim higher: {}", renter1.getAddress(),
        aeternityService.accounts.blockingGetAccount(renter1.getAddress()).getBalance());
    log.info("State after renter 1 claimed win {}", contract.get_state());

    log.info("Renter 2 balance before claim higher: {}",
        aeternityService.accounts.blockingGetAccount(renter2.getAddress()).getBalance());
    log.info(renter2Contract.claim(prediction.getId()));
    log.info("Renter 2 {} balance after claim higher: {}", renter2.getAddress(),
        aeternityService.accounts.blockingGetAccount(renter2.getAddress()).getBalance());

    // r2 claim again
    log.info("State after renter 2 claimed win {}", contract.get_state());
    log.info("Claiming again");
    try {
      log.info(renter2Contract.claim(prediction.getId()));
      log.info("State after renter 2 claimed 2 times {}", contract.get_state());
    } catch (AException e) {
      log.info("Expected - can't claim more than once");
    }
  }

  @Test
  @Disabled
  public void createPrediction() {
    renter1 = keyPairService.generateKeyPair();
    BigInteger oneAE = unitConversionService18Decimals.toSmallestUnit("1");
    fundAddress(renter1.getAddress(), oneAE.multiply(BigInteger.valueOf(10)));

    PredictionCards contract = new PredictionCards(config, getPredictionCardsContractId());
    Long OneMins = 60l * 2l * 1000l;
    long startTimestamp = System.currentTimeMillis();

    Prediction prediction = contract.create_prediction(BigInteger.valueOf(startTimestamp),
        BigInteger.valueOf(startTimestamp + OneMins), oneAEPerDayinAettosPerMilliSecond, "BTC",
        BigInteger.valueOf(75000), "0x00", "0x01").getValue1();
    log.info("Prediction created {}", prediction);
    log.info("Pot size is {}, {}", contract.get_pot_size(prediction.getId()),
        unitConversionService18Decimals.toBiggestUnit(contract.get_pot_size(prediction.getId())));

    // check if renter 1 can rent
    try {
      log.info("Renter 1 can rent: {}", contract.can_rent(new Address(renter1.getAddress()),
          prediction.getNft_higher_id(), oneAEPerDayinAettosPerMilliSecond));
    } catch (AException e) {
      log.info("Expected - can't rent");
    }

    // deposit and rent
    log.info("Renter 1 deposit 3 AE to prediction {}", prediction.getId());
    contract.deposit_to_nft(prediction.getNft_higher_id(),
        unitConversionService18Decimals.toSmallestUnit(BigDecimal.valueOf(3)));
    log.info("Renter 1 can rent prediction with id {}: {}", prediction.getId(),
        contract.can_rent(new Address(renter1.getAddress()), prediction.getNft_higher_id(),
            oneAEPerDayinAettosPerMilliSecond));
    contract.rent_nft(prediction.getNft_higher_id(), oneAEPerDayinAettosPerMilliSecond);
    log.info("Renter 1 rented, state is {}", contract.get_state());
    log.info("Pot size is {}, {}", contract.get_pot_size(prediction.getId()),
        unitConversionService18Decimals.toBiggestUnit(contract.get_pot_size(prediction.getId())));

    Prediction_state state = contract.get_prediction_state(prediction.getId());

    while (state.getPrediction_state().toString().equals("ACTIVE")) {
      try {
        log.info("State is {} waiting", state);
        state = contract.get_prediction_state(prediction.getId());
        BigInteger pot = contract.get_pot_size(prediction.getId());
        log.info("Pot size is {}, {}", pot, unitConversionService18Decimals.toBiggestUnit(pot));
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    log.info("Prediction ended with state: {}", contract.get_prediction_state(prediction.getId()));
    log.info("Calling oracle {}", contract.ask_for_winning_option(prediction.getId(), oneAE));
    log.info("Oracle answered? {}", contract.check_oracle_has_responded(prediction.getId()));
    log.info("Simulating answer");
    this.simulateOracleResponse("higher");
    log.info("Oracle answered? {}", contract.check_oracle_has_responded(prediction.getId()));

    log.info("Processing oracle response {}", contract.process_oracle_response(prediction.getId()));
    log.info("State: {}", contract.get_state());
    log.info(contract.claim(prediction.getId()));
    log.info("State: {}", contract.get_state());
  }

  private void deployContract() {
    PredictionCards predictionCards = new PredictionCards(config, null);
    Pair<String, String> deployedContract =
        predictionCards.deploy(new Oracle(oracleKeyPair.getOracleAddress()), Optional.empty(),
            Optional.of(BigInteger.valueOf(1000)));
    contractId = deployedContract.getValue1();
    log.info("Deploying contract successful - contract has address {}", contractId);
  }
}
