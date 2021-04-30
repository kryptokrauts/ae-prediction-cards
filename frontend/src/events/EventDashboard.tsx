import React from "react";
import { useSelector } from "react-redux";
import { useHistory } from "react-router";
import { AppState } from "../state";
import { EventList } from "./components/EventList";
import { PredictionEvent } from "./types";

export const EventDashboard = () => {
  const { account } = useSelector<AppState, any>(state => state.wallet);
  const history = useHistory();

  const events: Array<PredictionEvent> = [{
    id: '1',
    currency: 'AE',
    targetValue: 5,
    targetDate: '2020-07-01'
  },
  {
    id: '2',
    currency: 'BTC',
    targetValue: 70000,
    targetDate: '2020-08-01'
  }]

  return (
    <EventList onEventClick={(event) => history.push(`/${event.id}`)} events={events} />
  )
}