import React from "react";
import styled from "styled-components";
import { WalletProvider } from "./common/aeternity/WalletProvider";
import { Box } from "./common/components/box/Box";
import { Card, CardFooter } from "./common/components/cards/Card";
import { Caption, Text } from "./common/components/text/Text";
import { Navigation } from "./navigation/Navigation";

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  margin: ${props => props.theme.margin.xlarge};
`;

export const App = () => (
  <WalletProvider>
    <Navigation />
    <ContentWrapper>
      <Card>
        <Text>Price of <strong>AE</strong></Text>
        <Text>higher or lower than <strong>AE</strong></Text>
        <Text>by <strong>May, 1st 2020</strong></Text>
        <CardFooter>
          <Box row={false}>
            <Caption>current pot size</Caption>
            <Text>4,198.00 AE</Text>
          </Box>
          <Box row={false} align="flex-end">
            <Caption>time remaining</Caption>
            <Text>17days</Text>
          </Box>
        </CardFooter>
      </Card>
    </ContentWrapper>
  </WalletProvider>
)
