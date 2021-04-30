import { WalletClient } from "../aeternity/WalletClient";

export class PredictionCardsApi {
  private walletClient;
  private code;

  constructor(walletClient: WalletClient) {
    this.walletClient = walletClient;
  }

  async init() {
    if (!this.code) {
      this.code = await fetch('/PredictionCards.aes');
    }
    console.log(this.code);
  }



};