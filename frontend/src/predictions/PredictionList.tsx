import React from "react";
import styled from "styled-components";
import { PredictionCard } from "./PredictionCards";
import { Prediction } from "./types";

interface Props {
  predictions: Array<Prediction>;
  onPredictionClick: (prediction: Prediction) => void;
}

const StyledPredictionList = styled.div`
  display: flex;

  margin-top: -${props => props.theme.margin.large};
  margin-left: -${props => props.theme.margin.large};

  & > * {
    margin-top: ${props => props.theme.margin.large};
    margin-left: ${props => props.theme.margin.large};
  }
`;

export const PredictionList: React.FC<Props> = ({ predictions, onPredictionClick }) => (
  <StyledPredictionList>
    {predictions.map(prediction => (
      <PredictionCard key={prediction.id} prediction={prediction} onClick={() => onPredictionClick(prediction)} />
    ))}
  </StyledPredictionList>
)