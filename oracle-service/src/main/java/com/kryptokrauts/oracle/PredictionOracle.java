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

  private static final long ONE_DAY_IN_MS = 24 * 60 * 60 * 1000;

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
        String date = query.split(";")[2];
        Long unixTimestamp = toUnixTimestamp(date);
        String predictionDate = parseToDate(unixTimestamp);
        String dayBeforePredictiondate = parseToDate(unixTimestamp - ONE_DAY_IN_MS);
        Long predictionEndDatePrice =
            processCoingeckoRequest(ticker, predictionDate, dayBeforePredictiondate);
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

  private Long processCoingeckoRequest(String ticker, String predictionDate,
      String dayBeforePredictiondate) {
    try {
      String answer = coingeckoClient.getCoinValue(ticker, predictionDate, "false");
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
        } else {
          if (dayBeforePredictiondate != null) {
            log.warn(
                "Coingecko has not answered with expected format, this means we have a new day and coingecko did not provide the current price - retrieving price of day before");
            processCoingeckoRequest(ticker, predictionDate, null);
          } else {
            log.error(
                "Coingecko also did not deliver a price for the day before {}, please validate configuration",
                predictionDate);
          }
        }
      } else {
        log.warn("Coingecko has not answered at all");
      }
    } catch (Exception e) {
      log.error("Error occured requesting the price from coingecko", e);
    }
    return null;
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

  private Long toUnixTimestamp(String dateInMs) {
    try {
      return Long.parseLong(dateInMs);
    } catch (Exception e) {
      log.error("Cannot parse given milliseconds {} to date", dateInMs);
    }
    return null;
  }

  private String parseToDate(Long dateInMs) {
    try {
      Date predictionEndDate = new Date(dateInMs);
      return new SimpleDateFormat("dd-MM-yyyy").format(predictionEndDate);
    } catch (Exception e) {
      log.error("Cannot parse given timestamp {} to date", dateInMs);
    }
    return null;
  }

}
