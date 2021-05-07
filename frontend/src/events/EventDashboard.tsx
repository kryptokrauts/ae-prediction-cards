import React, { forwardRef, useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useHistory } from "react-router";
import styled from "styled-components";
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider";
import { Box } from "../common/components/box/Box";
import { Button } from "../common/components/Button/Button";
import { Modal } from "../common/components/dialog/dialog";
import { Spinner } from "../common/components/spinner/Spinner";
import { AppState } from "../state";
import { EventList } from "./components/EventList";
import { NewEventForm } from "./NewEventForm";
import { PredictionEvent } from "./types";

const Toolbar = styled.div`
  display: flex;
  width: 100%;
  justify-content: flex-end;
  margin: 0 0 ${props => props.theme.margin.large} 0;
`;

export const EventDashboard = forwardRef((_, ref) => {
  const history = useHistory();
  const predictionApi = usePredictionCardsApi();
  const { account } = useSelector<AppState, any>(state => state.wallet);
  const [isLoading, setIsLoading] = useState(true);
  const [events, setEvents] = useState<Array<[string, PredictionEvent]>>([]);
  const [showModal, setShowModal] = useState(false);

  const update = async () => {
    setIsLoading(true);
    const result = await predictionApi.getPredictions();
    const potSizes = await Promise.all(
      result.map(([, e]) => predictionApi.getPotSize(e.id!))
    );
    setEvents(result.map(([status, event], i) => ([
      status, {
        ...event,
        pot_size: potSizes[i]
      }
    ])));
    setIsLoading(false);
  }

  useEffect(() => {
    update();
  }, [predictionApi]);

  const handleEventCreated = () => {
    setShowModal(false);
    update();
  }

  return (
    <Box width="100%" align="center">
      <Toolbar>
        <Button margin={[0, 0, 0, "medium"]} onClick={() => update()} primary>Reload</Button>
        {account &&
          <Button margin={[0, 0, 0, "medium"]} onClick={() => setShowModal(true)} primary>New Event</Button>}
      </Toolbar>
      <Box>
        {isLoading ? <Spinner /> : (
          <EventList onEventClick={(event) => history.push(`/${event.id}`)} events={events} />
        )}
      </Box>
      {showModal && (
        <Modal onClose={() => setShowModal(false)}>
          <NewEventForm onClose={() => setShowModal(false)} onEventCreated={() => handleEventCreated()} />
        </Modal>
      )}
    </Box>
  )
})