import React from "react";
import { useHistory } from "react-router";
import { Box } from "../common/components/box/Box";
import { PredictionList } from "../predictions/PredictionList";
import { Prediction } from "../predictions/types";
import { EventInfo } from "./components/EventInfo";

export const EventDetails = () => {
  const history = useHistory();
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
      <PredictionList predictions={predictions} onPredictionClick={(prediction) => history.push(`${1}/${prediction.id}`)} />
    </Box>
  );
}