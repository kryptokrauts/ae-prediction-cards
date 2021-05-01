import styled from "styled-components";
import { Box } from "../../common/components/box/Box";
import { Card, CardFooter } from "../../common/components/cards/Card";
import { BasicText, Caption } from "../../common/components/text/Text";
import { PredictionEvent } from "../types";

interface Props {
  onEventClick: (event: PredictionEvent) => void;
  events: Array<[string, PredictionEvent]>;
}

const StyledEventList = styled.div`
  display: flex;
  flex-wrap: wrap;
  justify-content: center;

  margin-top: -${props => props.theme.margin.large};
  margin-left: -${props => props.theme.margin.large};

  & > * {
    margin-top: ${props => props.theme.margin.large};
    margin-left: ${props => props.theme.margin.large};
  }
`;

const DateFormatter = new Intl.DateTimeFormat('en-US', { dateStyle: 'long' });
const CurrencyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

export const EventList = ({ onEventClick, events }: Props) => (
  <StyledEventList>
    {events.length === 0 && <BasicText>No event found. Create one now!</BasicText>}
    {events.map(([type, evt]) => (
      <Card key={evt.id} onClick={() => onEventClick(evt)} tag={type}>
        <BasicText>Price of <strong>{evt.asset}</strong></BasicText>
        <BasicText>higher or lower than <strong>{CurrencyFormatter.format(evt.target_price)}</strong></BasicText>
        <BasicText>by <strong>{DateFormatter.format(evt.end_timestamp)}</strong></BasicText>
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