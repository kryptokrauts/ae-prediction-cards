import styled from "styled-components";
import { Box } from "../../common/components/box/Box";
import { Card, CardFooter } from "../../common/components/cards/Card";
import { BasicText, Caption } from "../../common/components/text/Text";
import { CurrencyFormatter, DateFormatter } from "../../common/utils/formatter";
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
            <BasicText>it's a secret - stay tuned</BasicText>
          </Box>
        </CardFooter>
      </Card>
    ))}
  </StyledEventList>
)