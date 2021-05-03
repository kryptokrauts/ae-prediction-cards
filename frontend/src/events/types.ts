export interface PredictionEvent {
  id?: number;
  asset: string;
  target_price: number;
  start_timestamp: number;
  end_timestamp: number;
  nft_higher_id: number;
  nft_lower_equal_id: number;
  max_increase_rent_amount_aettos: number;
}