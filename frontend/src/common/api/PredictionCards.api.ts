import { PredictionEvent } from "../../events/types";
import { WalletClient } from "../aeternity/WalletClient";

export class PredictionCardsApi {
  private walletClient;
  private code;
  private aensName = 'predictioncards.chain';
  private instance;

  constructor(walletClient: WalletClient) {
    this.walletClient = walletClient;
  }

  async init() {
    if (!this.code) {
      const response = await fetch('/PredictionCards.aes');
      this.code = await response.text();
    }
    this.instance = await this.walletClient.client.getContractInstance(this.code, { contractAddress: this.aensName });
  }

  async createPrediction(event: PredictionEvent) {
    const callResult = await this.instance.methods.create_prediction(this.convertDate(event.startDate), this.convertDate(event.endDate), 10, event.asset, event.targetPrice, "QmQBd6aAWy7EFTpZ4T6vJoaskzKdiERQT4Xwu7wwzaa8YH", "QmQBd6aAWy7EFTpZ4T6vJoaskzKdiERQT4Xwu7wwzaa8YH");
    console.log(callResult);
  };

  private convertDate(dateString: string) {
    const asDate = new Date(dateString);
    return asDate.getTime();
  }

};