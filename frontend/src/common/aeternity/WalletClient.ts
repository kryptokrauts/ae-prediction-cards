import { Node, RpcAepp } from '@aeternity/aepp-sdk/es';
import BrowserWindowMessageConnection from '@aeternity/aepp-sdk/es/utils/aepp-wallet-communication/connection/browser-window-message';
import WalletDetector from '@aeternity/aepp-sdk/es/utils/aepp-wallet-communication/wallet-detector';

export class WalletClient {
  private client;
  private detector;
  private accounts;

  async connect() {
    await this.init();
    await this.scanForWallets() // Start looking for new wallets
    this.accounts = await this.client.subscribeAddress('subscribe', 'connected');
    console.log(this.accounts);
  }

  async getActiveAccount() {
    const account = await this.client.address();
    const balance = await this.client.balance(account);
    return {
      account,
      balance
    }
  }
  private async init() {

    // Open iframe with Wallet if run in top window
    //  window !== window.parent || await this.getReverseWindow()
    this.client = await RpcAepp({
      name: 'AEPP',
      nodes: [
        { name: 'test-net', instance: await Node({ url: process.env.REACT_APP_NODE_URL, internalUrl: process.env.REACT_APP_NODE_INTERNAL_URL }) }
      ],
      compilerUrl: process.env.REACT_APP_COMPILER_URL,
      onNetworkChange(params) {
        if (this.getNetworkId() !== params.networkId) alert(`Connected network ${this.getNetworkId()} is not supported with wallet network ${params.networkId}`)
      },
      onAddressChange: async (addresses) => {
        console.log(addresses);
      },
      onDisconnect(msg) {
        console.log(msg)
      }
    })
  }

  private async scanForWallets() {
    return new Promise<void>(async (resolve, reject) => {
      // call-back function for new wallet event
      const handleWallets = async ({ wallets, newWallet }) => {
        newWallet = newWallet || Object.values(wallets)[0]
        // ask if you want to connect
        if (window.confirm(`Do you want to connect to wallet ${newWallet.name}`)) {
          // Stop scanning wallets
          this.detector.stopScan()
          // Connect to wallet
          await this.client.connectToWallet(await newWallet.getConnection());
          resolve();
        }
      }
      // Create connection object for WalletDetector
      const scannerConnection = await BrowserWindowMessageConnection({
        connectionInfo: { id: 'spy' }
      })
      // Initialize WalletDetector 
      this.detector = await WalletDetector({ connection: scannerConnection })
      // Start scanning
      this.detector.scan(handleWallets)
    });
  }

}