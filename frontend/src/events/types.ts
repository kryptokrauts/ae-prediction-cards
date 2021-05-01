export interface PredictionEvent {
  id?: string;
  asset: string;
  target_price: number;
  start_timestamp: number;
  end_timestamp: number;
}