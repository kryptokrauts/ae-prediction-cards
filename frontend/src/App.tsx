import React from "react";
import { Route, Switch } from "react-router";
import styled from "styled-components";
import { WalletProvider } from "./common/aeternity/WalletProvider";
import { PredictionCardsProvider } from "./common/api/PredictionCardsApiProvider";
import { EventDashboard } from "./events/EventDashboard";
import { EventDetails } from "./events/EventDetails";
import { Navigation } from "./navigation/Navigation";
import { PredictionDetails } from "./predictions/PredictionDetails";

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  margin: 96px 32px 32px 32px;
`;

export const App = () => {
  return (
    <WalletProvider>
      <PredictionCardsProvider>
        <Navigation />
        <ContentWrapper>
          <Switch>
            <Route path="/" exact component={EventDashboard} />
            <Route path="/:eventId" exact component={EventDetails} />
            <Route path="/:eventId/:predictionId" exact component={PredictionDetails} />
          </Switch>
        </ContentWrapper>
      </PredictionCardsProvider>
    </WalletProvider>
  )
}
