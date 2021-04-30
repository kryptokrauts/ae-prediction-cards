import { createContext, useContext } from "react";
import { useWallet } from "../aeternity/WalletProvider";
import { PredictionCardsApi } from "./PredictionCards.api";

const PredictionCardsContext = createContext<any>(undefined);

export const usePredictionCardsApi = () => {
  return useContext(PredictionCardsContext);
};

export const PredictionCardsProvider: React.FC = ({ children }) => {
  const wallet = useWallet();
  const api = new PredictionCardsApi(wallet);

  return (
    <PredictionCardsContext.Provider value={api} >
      { children}
    </PredictionCardsContext.Provider>
  )
};