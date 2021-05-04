import React from "react";
import { Box } from "../common/components/box/Box";
import { Card, CardFooter } from "../common/components/cards/Card";
import { SmallHeading } from "../common/components/text/Heading";
import { BasicText, Caption, SmallText } from "../common/components/text/Text";
import { formatAddress } from "../common/utils/formatter";
import { Prediction } from "./types";

interface Props {
  prediction?: Prediction;
  onClick?: () => void;
}

export const PredictionCard: React.FC<Props> = ({ prediction, onClick }) => (
  <Card vertical onClick={onClick}>
    <Box center>
      <SmallHeading>{prediction?.name}</SmallHeading>
    </Box>
    {prediction?.rent &&
      <Box margin={['small', 0, 'small', 0]}>
        <BasicText center>{prediction?.rent} AE/day</BasicText>
      </Box>
    }
    <CardFooter>
      <Box row={false}>
        <Caption>renter</Caption>
        <SmallText>{formatAddress(prediction?.owner) || 'None'}</SmallText>
      </Box>
    </CardFooter>
  </Card>
)