export const DateFormatter = new Intl.DateTimeFormat('en-US', { dateStyle: 'long' });
export const CurrencyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
export const formatAddress = (address?: string) => address && `${address.substr(0, 3)} ${address.substr(3, 2)} ... ${address.substr(-3)}`;