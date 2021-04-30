import styled from "styled-components";
import { Box } from "../common/components/box/Box";
import { Card, CardFooter } from "../common/components/cards/Card";
import { SmallHeading } from "../common/components/text/Heading";
import { Caption, SmallText } from "../common/components/text/Text";
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
      <Card vertical onClick={() => onPredictionClick(prediction)}>
        <Box center>
          <SmallHeading>{prediction.name}</SmallHeading>
        </Box>
        <CardFooter>
          <Box row={false}>
            <Caption>owner</Caption>
            <SmallText>{prediction.owner}</SmallText>
          </Box>
          <Box row={false} align="flex-end">
            <Caption>time remaining</Caption>
            <SmallText>17days</SmallText>
          </Box>
        </CardFooter>
      </Card>
    ))}
  </StyledPredictionList>
)