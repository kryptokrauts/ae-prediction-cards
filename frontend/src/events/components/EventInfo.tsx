import { Box } from "../../common/components/box/Box";
import { MidHeading } from "../../common/components/text/Heading";
import { CurrencyFormatter, DateFormatter } from "../../common/utils/formatter";
import { MarginSize } from "../../theme.types";
import { PredictionEvent } from "../types";

interface Props {
  margin: Array<MarginSize | number>;
  event: PredictionEvent;
}

export const EventInfo: React.FC<Props> = ({ margin, event }) => (
  <Box margin={margin}>
    <MidHeading light>Price of <strong>{event.asset}</strong></MidHeading>
    <MidHeading light>higher or lower than <strong>{CurrencyFormatter.format(event.target_price)}</strong></MidHeading>
    <MidHeading light>by <strong>{DateFormatter.format(event.end_timestamp)}</strong></MidHeading>
  </Box>
)