package com.kryptokrauts.oracle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.aeternity.sdk.service.oracle.domain.OracleQueryResult;
import com.kryptokrauts.client.CoingeckoClient;
import com.kryptokrauts.client.CoingeckoTicker;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PredictionOracle {

  private static final String OUTCOME_HIGHER = "higher";

  private static final String OUTCOME_LOWER_OR_EQUAL = "lower_or_equal";

  @Inject
  private ChainInteraction chainInteraction;

  @Inject
  @RestClient
  CoingeckoClient coingeckoClient;

  private ObjectMapper mapper;

  @PostConstruct
  public void init() {
    chainInteraction.extendOrRegisterOracle();
    mapper = new ObjectMapper();
  }

  @Scheduled(every = "{scheduler.oracle_query_interval}")
  public void checkForQueryAndRespond() {
    log.info("Check for new oracle queries triggered");
    OracleQueryResult oracleQueryResult = chainInteraction.checkForOracleQueries();
    if (oracleQueryResult != null) {
      log.info("OracleQueryResult: {}", oracleQueryResult);
      String query = oracleQueryResult.getQuery();
      if (query.contains(";") && query.split(";").length == 3) {
        String ticker = mapToCoingeckoTicker(query.split(";")[0]);
        String predictedPrice = query.split(";")[1];
        String date = parseToDate(query.split(";")[2]);
        Long predictionEndDatePrice = processCoingeckoRequest(ticker, date);
        String outcome = determinePredictionOutcome(predictedPrice, predictionEndDatePrice);
        if (outcome != null) {
          chainInteraction.respondToQuery(oracleQueryResult, outcome);
        }
      }
    }
  }

  @Scheduled(every = "{scheduler.oracle_expired_interval}")
  public void checkOracleExpired() {
    chainInteraction.extendOrRegisterOracle();
  }

  private Long processCoingeckoRequest(String ticker, String date) {
    try {
      String answer = coingeckoClient.getCoinValue(ticker, date, "false");
      log.info("Coingecko answer: {}", answer);
      if (answer != null) {
        JsonObject jsonResult = JsonObject.mapFrom(mapper.readValue(answer, Map.class));
        if (jsonResult.containsKey("market_data")
            && jsonResult.getJsonObject("market_data").containsKey("current_price") && jsonResult
                .getJsonObject("market_data").getJsonObject("current_price").containsKey("usd")) {
          Double usd = jsonResult.getJsonObject("market_data").getJsonObject("current_price")
              .getDouble("usd");
          usd = usd * 100;
          return usd.longValue();
        }
      }
    } catch (Exception e) {
      log.error("Cannot process coingecko request", e);
    }
    // TODO just for testing
    return 1000l;
  }

  private String mapToCoingeckoTicker(String coinTicker) {
    try {
      return CoingeckoTicker.valueOf(coinTicker.toUpperCase()).getCoingeckoTicker();
    } catch (Exception e) {
      log.warn("Unkown coin ticker {}", coinTicker);
      return coinTicker;
    }
  }

  private String determinePredictionOutcome(String predictedPrice, Long predictionEndDatePrice) {
    try {
      long predictedPriceUsd = Long.parseLong(predictedPrice);
      if (predictedPriceUsd > predictionEndDatePrice) {
        return OUTCOME_HIGHER;
      }
      return OUTCOME_LOWER_OR_EQUAL;
    } catch (Exception e) {
      log.error("Cannot determine prediction outcome", e);
    }
    return null;
  }

  private String parseToDate(String dateInMs) {
    try {
      Date predictionEndDate = new Date(Long.parseLong(dateInMs));
      return new SimpleDateFormat("dd-MM-yyyy").format(predictionEndDate);
    } catch (Exception e) {
      log.error("Cannot parse given timestamp {} to date", dateInMs);
    }
    return null;
  }

}
