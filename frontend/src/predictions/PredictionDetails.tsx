import { toAettos } from '@aeternity/aepp-sdk/es/utils/amount-formatter';
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useParams } from "react-router";
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider";
import { Box } from "../common/components/box/Box";
import { Button } from "../common/components/Button/Button";
import { StyledInput, StyledLabel } from '../common/components/input/input';
import { Spinner } from "../common/components/spinner/Spinner";
import { BasicText, Caption } from "../common/components/text/Text";
import { toAettosPerMillisecond } from '../common/utils/transformer';
import { EventInfo } from "../events/components/EventInfo";
import { PredictionEvent } from "../events/types";
import { AppState } from "../state";
import { PredictionCard } from './PredictionCards';
import { Prediction } from "./types";


interface Props {

}

const getPredictionLabel = (predictionEvent, id) => predictionEvent.nft_higher_id === id ? 'HIGHER' : 'LOWER';

export const PredictionDetails: React.FC<Props> = () => {
  const { eventId, predictionId } = useParams<{ eventId: string, predictionId: string }>();
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [event, setEvent] = useState<PredictionEvent>();
  const [prediction, setPrediction] = useState<Prediction>();
  const [rent, setRent] = useState<number>(0);
  const [deposit, setDeposit] = useState(0);
  const predictionApi = usePredictionCardsApi();
  const { account } = useSelector<AppState, any>(state => state.wallet);

  useEffect(() => {
    (async () => {
      const [, predictionEvent] = await predictionApi.getPrediction(eventId);
      setEvent(predictionEvent);
      const renter = await predictionApi.getNFTRenter(parseInt(predictionId));
      setPrediction({
        id: parseInt(predictionId),
        name: getPredictionLabel(predictionEvent, predictionId),
        owner: renter,
      });
      setIsLoading(false);
    })();
  }, [predictionApi, eventId, predictionId]);

  const triggerRent = async () => {
    setIsProcessing(true);
    if (deposit > 0) {
      const depositResponse = await predictionApi.deposit(predictionId, toAettos(deposit));
    }

    await predictionApi.rentNFT(predictionId, toAettosPerMillisecond(rent));
    setIsProcessing(false);
  }

  return (
    <Box>
      {isLoading ? (
        <Spinner />
      ) : (
        <>
          <Box center>
            <EventInfo margin={[0, 0, "xlarge", 0]} event={event!} />
          </Box>
          <Box row center>
            <PredictionCard prediction={prediction} />
            <Box margin={[0, 0, 0, 'xlarge']} width="250px">
              <StyledLabel>
                <BasicText center>rent price (AE/day)</BasicText>
                <StyledInput type="number" autoFocus value={rent.toString()} onChange={evt => setRent(parseFloat(evt.target.value))} />
              </StyledLabel>
              <StyledLabel>
                <BasicText center>deposit (AE)</BasicText>
                <StyledInput type="number" autoFocus value={deposit.toString()} onChange={evt => setDeposit(parseFloat(evt.target.value))} />
              </StyledLabel>
              <Caption marginTop="large" light>With the current price and deposit you can rent the NFT for <strong>{Math.floor(deposit / rent) || 0} days</strong></Caption>
              {account ? (isProcessing ?
                <Box center margin={['large', 0, 0, 0]}>
                  <Spinner size="small" />
                </Box> :
                <Button margin={['large', 0, 0, 0]} primary onClick={() => triggerRent()}>rent the card</Button>
              ) : (
                <BasicText light marginTop="large" center>connect your wallet to rent this card</BasicText>
              )}
            </Box>
          </Box>
        </>
      )}
    </Box>
  );
}