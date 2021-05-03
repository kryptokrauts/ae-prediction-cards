import { Universal as Ae } from '@aeternity/aepp-sdk/es';
import { toAettos } from '@aeternity/aepp-sdk/es/utils/amount-formatter';
import { PredictionEvent } from "../../events/types";
import { WalletClient } from "../aeternity/WalletClient";

export class PredictionCardsApi {
  private walletClient: WalletClient;
  private code;
  private aensName = 'predictioncards.chain';

  constructor(walletClient: WalletClient) {
    this.walletClient = walletClient;
  }

  async createPrediction(event: PredictionEvent): Promise<any> {
    const instance = await this.getInteractiveInstance();
    const callResult = await instance.methods.create_prediction(this.convertDate(event.start_timestamp), this.convertDate(event.end_timestamp), toAettos(1), event.asset, event.target_price, "QmQBd6aAWy7EFTpZ4T6vJoaskzKdiERQT4Xwu7wwzaa8YH", "QmQBd6aAWy7EFTpZ4T6vJoaskzKdiERQT4Xwu7wwzaa8YH");
    return callResult;
  };

  async getPredictions(): Promise<Array<[string, PredictionEvent]>> {
    await this.init();
    const contractObj = await this.getDryRunInstance();
    const callResult = await contractObj.methods.all_predictions();
    return callResult.decodedResult;
  }

  async getPrediction(id: string): Promise<[string, PredictionEvent]> {
    const contractObj = await this.getDryRunInstance();
    const callResult = await contractObj.methods.prediction(id);
    return callResult.decodedResult;
  }

  async deposit(id: string, amount: number): Promise<any> {
    const contractObj = await this.getInteractiveInstance();
    const callResult = await contractObj.methods.deposit_to_nft(id, { amount })
    return callResult;
  }

  async getNFTRenter(id: number): Promise<string | undefined> {
    const contractObj = await this.getDryRunInstance();
    try {
      const callResult = await contractObj.methods.current_renter(id);
      return callResult.decodedResult;
    } catch (err) {
      return undefined;
    }
  }

  async rentNFT(id: string, amount: string): Promise<any> {
    const instance = await this.getInteractiveInstance();
    const callResult = await instance.methods.rent_nft(id, amount);
    return callResult;
  }

  private async init() {
    if (!this.code) {
      const response = await fetch('/PredictionCards.aes');
      this.code = await response.text();
    }
  }

  private convertDate(dateString: any) {
    const asDate = new Date(dateString);
    return asDate.getTime();
  }

  private async getDryRunInstance() {
    this.init();
    const networkConf = await this.walletClient.getNetworkConf();
    const SDKInstance = await Ae({
      ...networkConf
    });
    return await SDKInstance.getContractInstance(this.code, { contractAddress: this.aensName })
  }

  private async getInteractiveInstance() {
    await this.init();
    return await this.walletClient.getClient().getContractInstance(this.code, { contractAddress: this.aensName });
  }

};