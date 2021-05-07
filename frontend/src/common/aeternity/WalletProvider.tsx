import { createContext, useContext } from "react";
import { useDispatch } from "react-redux";
import { WalletClient } from "./WalletClient";

const WalletContext = createContext<WalletClient | undefined>(undefined);

export const useWallet = () => {
  const context = useContext(WalletContext);
  if (!context) {
    throw Error("Wallet client not available");
  }
  return context;
};

export const WalletProvider: React.FC = ({ children }) => {
  const dispatcher = useDispatch();
  const wallet = new WalletClient(dispatcher);
  if (localStorage.getItem('autoconnectwallet') === 'true') {
    wallet.connect();
  }
  return (
    <WalletContext.Provider value={wallet}>
      {children}
    </WalletContext.Provider>
  )
};