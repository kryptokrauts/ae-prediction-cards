package com.kryptokrauts;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ProcessPrediction {

  @Inject
  private ChainInteraction chainInteraction;

  @Scheduled(every = "{scheduler.process_prediction_interval}")
  public void processPredictions() {
    log.info("Process predictions ...");
    chainInteraction.processPredictions();
  }

  @Scheduled(every = "{scheduler.extend_name_interval}")
  public void extendName() {
    log.info("Check and extend name if required ...");
    chainInteraction.checkAndExtendName();
  }
}
