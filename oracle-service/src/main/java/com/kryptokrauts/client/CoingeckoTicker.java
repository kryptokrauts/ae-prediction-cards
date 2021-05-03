package com.kryptokrauts.client;

import lombok.Getter;

@Getter
public enum CoingeckoTicker {

  BTC("bitcoin"), MTL("metal"), AE("aeternity"), XPR("proton"), DOGE("dogecoin");

  private String coingeckoTicker;

  private CoingeckoTicker(String coingeckoTicker) {
    this.coingeckoTicker = coingeckoTicker;
  }

}
