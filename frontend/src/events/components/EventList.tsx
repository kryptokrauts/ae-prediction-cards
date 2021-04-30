import styled from "styled-components";
import { Box } from "../../common/components/box/Box";
import { Card, CardFooter } from "../../common/components/cards/Card";
import { BasicText, Caption } from "../../common/components/text/Text";
import { PredictionEvent } from "../types";

interface Props {
  onEventClick: (event: PredictionEvent) => void;
  events: Array<PredictionEvent>;
}

const StyledEventList = styled.div`
  display: flex;

  margin-top: -${props => props.theme.margin.large};
  margin-left: -${props => props.theme.margin.large};

  & > * {
    margin-top: ${props => props.theme.margin.large};
    margin-left: ${props => props.theme.margin.large};
  }
`;

export const EventList = ({ onEventClick, events }: Props) => (
  <StyledEventList>
    {events.map((evt) => (
      <Card key={evt.id} onClick={() => onEventClick(evt)}>
        <BasicText>Price of <strong>{evt.asset}</strong></BasicText>
        <BasicText>higher or lower than <strong>{evt.targetPrice}</strong></BasicText>
        <BasicText>by <strong>{evt.endDate}</strong></BasicText>
        <CardFooter>
          <Box row={false}>
            <Caption>current pot size</Caption>
            <BasicText>4,198.00 AE</BasicText>
          </Box>
          <Box row={false} align="flex-end">
            <Caption>time remaining</Caption>
            <BasicText>17days</BasicText>
          </Box>
        </CardFooter>
      </Card>
    ))}
  </StyledEventList>
)