import { Reducer } from "redux";
import { GenericAction } from "../../state";

export enum WalletStateActions {
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  UPDATE = 'UPDATE',
  DISCONNECT = 'DISCONNECT'
}

export const walletConnecting = () => ({
  type: WalletStateActions.CONNECTING
});

export const walletConnected = (payload: any) => ({
  type: WalletStateActions.CONNECTED,
  payload
});

export const walletUpdate = (payload: any) => ({
  type: WalletStateActions.UPDATE,
  payload
});

export const walletDisconnected = () => ({
  type: WalletStateActions.DISCONNECT
});


export const walletReducer: Reducer<any, GenericAction> = (state = {}, action: GenericAction) => {
  switch (action.type) {
    case WalletStateActions.CONNECTING:
      return {
        ...state,
        connecting: true
      }
    case WalletStateActions.CONNECTED:
      return {
        ...state,
        connecting: false,
        ...action.payload
      }
    case WalletStateActions.UPDATE:
      return {
        ...state,
        ...action.payload
      }
    case WalletStateActions.DISCONNECT: {
      return {}
    }
    default:
      return state;
  }
}
