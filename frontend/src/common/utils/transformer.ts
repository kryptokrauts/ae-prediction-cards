import { toAe, toAettos } from '@aeternity/aepp-sdk/es/utils/amount-formatter';
import BigNumber from 'bignumber.js';


BigNumber.config({ DECIMAL_PLACES: 0 });

const daysToMilliseconds = (days: number): number => days * 24 * 60 * 60 * 1000

const milliseconds = new BigNumber(daysToMilliseconds(1));

export const toAettosPerMillisecond = (aePerDay?: string | number) => aePerDay && new BigNumber(toAettos(aePerDay)).dividedBy(milliseconds).toString();
export const toAePerDay = (aettosPerMillisecond?: string | number) => aettosPerMillisecond && new BigNumber(toAe(aettosPerMillisecond)).multipliedBy(milliseconds).toFixed(2).toString();