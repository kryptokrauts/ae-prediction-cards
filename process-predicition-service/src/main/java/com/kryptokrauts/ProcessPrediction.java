package com.kryptokrauts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class ProcessPrediction {

  @Autowired
  private ChainInteraction chainInteraction;

  @Scheduled(fixedDelayString = "${scheduler.ask_oracle.delay}")
  public void askOracle() {
    log.info("Asking oracle to resolve predictions ...");
    chainInteraction.askOracle();
  }

  @Scheduled(fixedDelayString = "${scheduler.process_oracle_response.delay}")
  public void processOracleResponse() {
    log.info("Processing oracle responses ...");
    chainInteraction.processOracleResponse();
  }

//  @Scheduled(fixedDelayString = "${scheduler.extend_name.delay}")
//  public void extendName() {
//    log.info("Check and extend name if required ...");
//    chainInteraction.checkAndExtendName();
//  }
}
