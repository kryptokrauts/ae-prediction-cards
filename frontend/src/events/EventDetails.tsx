import React from "react";
import { useHistory, useParams } from "react-router";
import { Box } from "../common/components/box/Box";
import { PredictionList } from "../predictions/PredictionList";
import { Prediction } from "../predictions/types";
import { EventInfo } from "./components/EventInfo";
import { PredictionEvent } from "./types";

export const EventDetails = () => {
  const history = useHistory();
  const { eventId } = useParams<{ eventId: string }>();
  const event: PredictionEvent = {
    asset: 'AE',
    targetPrice: 5,
    startDate: '2020-06-1',
    endDate: '2020-07-01'
  };
  const predictions: Array<Prediction> = [{
    id: '1',
    name: 'Lower',
    owner: 'ak_asdasdf',
    rent: 20,
    winndingOdds: 70
  }, {
    id: '2',
    name: 'Higher',
    owner: 'ak_asdasdf',
    rent: 10,
    winndingOdds: 30
  }]
  return (
    <Box center>
      <EventInfo margin={[0, 0, "xlarge", 0]} />
      <PredictionList predictions={predictions} onPredictionClick={(prediction) => history.push(`${eventId}/${prediction.id}`)} />
    </Box>
  );
}