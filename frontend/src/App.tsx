import React from "react";
import { Route, Switch } from "react-router";
import styled from "styled-components";
import { WalletProvider } from "./common/aeternity/WalletProvider";
import { EventDashboard } from "./events/EventDashboard";
import { EventDetails } from "./events/EventDetails";
import { Navigation } from "./navigation/Navigation";

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  margin: ${props => props.theme.margin.xlarge};
`;

export const App = () => {
  return (
    <WalletProvider>
      <Navigation />
      <ContentWrapper>
        <Switch>
          <Route path="/" exact component={EventDashboard} />
          <Route path="/:eventId" exact component={EventDetails} />
        </Switch>
      </ContentWrapper>
    </WalletProvider>
  )
}
