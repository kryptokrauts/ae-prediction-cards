import React from "react";
import styled from "styled-components";
import { Box } from "../common/components/box/Box";
import { Card, CardFooter } from "../common/components/cards/Card";
import { SmallHeading } from "../common/components/text/Heading";
import { BasicText, Caption, SmallText } from "../common/components/text/Text";
import { formatAddress } from "../common/utils/formatter";
import { Prediction } from "./types";

const NFTImage = styled.img`
  width: 150px;
  height: auto;
  border-radius: ${props => props.theme.borderRadius};
`;

interface Props {
  prediction?: Prediction;
  onClick?: () => void;
}

export const PredictionCard: React.FC<Props> = ({ prediction, onClick }) => (
  <Card vertical onClick={onClick}>
    <Box center>
      <SmallHeading>{prediction?.name}</SmallHeading>
    </Box>
    <Box center margin={['medium', 0, 0, 0]}>
      <NFTImage src={`https://ipfs.io/ipfs/${prediction?.imageHash}`} alt="NFT image" />
    </Box>
    {prediction?.rent && prediction?.owner &&
      <Box margin={['medium', 0, 0, 0]}>
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