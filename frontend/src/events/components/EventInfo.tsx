import { Box } from "../../common/components/box/Box";
import { MidHeading } from "../../common/components/text/Heading";
import { MarginSize } from "../../theme.types";

interface Props {
  margin: Array<MarginSize | number>
}

export const EventInfo: React.FC<Props> = ({ margin }) => (
  <Box margin={margin}>
    <MidHeading light>Price of <strong>AE</strong></MidHeading>
    <MidHeading light>higher or lower than <strong>5 €</strong></MidHeading>
    <MidHeading light>by <strong>May, 1st 2020</strong></MidHeading>
  </Box>
)