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
  private ServiceConfig config;

  @Autowired
  private ChainInteraction chainInteraction;

  @Scheduled(fixedRate = 10000)
  public void askOracle() {
    log.info("Asking oracle to resolve predictions ...");
    chainInteraction.askOracle();
  }

  @Scheduled(fixedRate = 10000)
  public void processOracleResponse() {
    log.info("Processing oracle responses ...");
    chainInteraction.processOracleResponse();
  }

  @Scheduled(fixedRate = 10000)
  public void getState() {
    log.info("Get state ...");
    chainInteraction.getState();
  }

  @Scheduled(fixedRate = 10000)
  public void extendName() {
    log.info("Check and extend name if required ...");
    chainInteraction.checkAndExtendName();
  }
}
