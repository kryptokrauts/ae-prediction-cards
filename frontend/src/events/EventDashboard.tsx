import React, { useEffect, useState } from "react";
import { useHistory } from "react-router";
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider";
import { Spinner } from "../common/components/spinner/Spinner";
import { EventList } from "./components/EventList";
import { PredictionEvent } from "./types";

export const EventDashboard = () => {
  const history = useHistory();
  const predictionApi = usePredictionCardsApi();
  const [isLoading, setIsLoading] = useState(true);
  const [events, setEvents] = useState<Array<[string, PredictionEvent]>>([]);

  useEffect(() => {
    (async () => {
      const result = await predictionApi.getPredictions();
      setEvents(result);
      setIsLoading(false);
    })();
  }, [predictionApi]);

  return (
    <>
      {isLoading ? <Spinner /> : (
        <EventList onEventClick={(event) => history.push(`/${event.id}`)} events={events} />
      )}
    </>
  )
}