import { toAePerDay } from "../common/utils/transformer";

export const getRentById = (predictionEvent, nftId): string | undefined => {
  const [, rent] = predictionEvent?.nft_last_rent_aettos_per_millisecond?.find(([nft]) => nft === nftId) || [];
  return toAePerDay(rent)?.toString()
}