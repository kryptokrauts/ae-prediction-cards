import { AnyAction, applyMiddleware, combineReducers, createStore } from "redux";
import thunk from "redux-thunk";
import { walletReducer } from "./common/aeternity/walletState";

export interface GenericAction extends AnyAction {
  payload: any;
}

export interface AppState {
  wallet: {
    account: string;
    balance: string;
    connecting: boolean;
  }
}

export const initStore = () => {
  const rootReducer = combineReducers({
    wallet: walletReducer
  });
  return createStore(rootReducer, applyMiddleware(thunk));
};