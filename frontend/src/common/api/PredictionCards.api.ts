import { Universal as Ae } from '@aeternity/aepp-sdk/es';
import { PredictionEvent } from "../../events/types";
import { WalletClient } from "../aeternity/WalletClient";

export class PredictionCardsApi {
  private walletClient: WalletClient;
  private code;
  private aensName = 'predictioncards.chain';

  constructor(walletClient: WalletClient) {
    this.walletClient = walletClient;
  }

  async init() {
    if (!this.code) {
      const response = await fetch('/PredictionCards.aes');
      this.code = await response.text();
    }
  }

  async createPrediction(event: PredictionEvent) {
    const instance = await this.walletClient.getClient().getContractInstance(this.code, { contractAddress: this.aensName });
    const callResult = await instance.methods.create_prediction(this.convertDate(event.start_timestamp), this.convertDate(event.end_timestamp), 10, event.asset, event.target_price, "QmQBd6aAWy7EFTpZ4T6vJoaskzKdiERQT4Xwu7wwzaa8YH", "QmQBd6aAWy7EFTpZ4T6vJoaskzKdiERQT4Xwu7wwzaa8YH");
    return callResult;
  };

  async getPredictions(): Promise<Array<[string, PredictionEvent]>> {
    const contractObj = await this.getDryRunInstance();
    const callResult = await contractObj.methods.all_predictions();
    return callResult.decodedResult;
  }

  private convertDate(dateString: any) {
    const asDate = new Date(dateString);
    return asDate.getTime();
  }

  private async getDryRunInstance() {
    const networkConf = await this.walletClient.getNetworkConf();
    const SDKInstance = await Ae({
      ...networkConf
    });
    return await SDKInstance.getContractInstance(this.code, { contractAddress: this.aensName })
  }

};