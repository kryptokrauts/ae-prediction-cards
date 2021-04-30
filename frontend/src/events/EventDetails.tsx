import { useParams } from "react-router";
import { Box } from "../common/components/box/Box";
import { MidHeading } from "../common/components/text/Heading";

export const EventDetails = () => {
  const { eventId } = useParams<{ eventId: string }>();
  return (
    <Box>
      <MidHeading light>Price of <strong>AE</strong></MidHeading>
      <MidHeading light>higher or lower than <strong>5 €</strong></MidHeading>
      <MidHeading light>by <strong>May, 1st 2020</strong></MidHeading>
    </Box>
  );
}