import React from "react";
import { useParams } from "react-router";
import { Box } from "../common/components/box/Box";
import { MidHeading } from "../common/components/text/Heading";
import { PredictionList } from "../predictions/PredictionList";
import { Prediction } from "../predictions/types";
import { PredictionEvent } from "./types";

export const EventDetails = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const event: PredictionEvent = {
    currency: 'AE',
    targetValue: 5,
    targetDate: '2020-07-01'
  };
  const predictions: Array<Prediction> = [{
    name: 'Lower',
    owner: 'ak_asdasdf',
    rent: 20,
    winndingOdds: 70
  }, {
    name: 'Higher',
    owner: 'ak_asdasdf',
    rent: 10,
    winndingOdds: 30
  }]
  return (
    <Box center>
      <Box margin={[0, 0, "xlarge", 0]}>
        <MidHeading light>Price of <strong>AE</strong></MidHeading>
        <MidHeading light>higher or lower than <strong>5 €</strong></MidHeading>
        <MidHeading light>by <strong>May, 1st 2020</strong></MidHeading>
      </Box>
      <PredictionList predictions={predictions} />
    </Box>
  );
}