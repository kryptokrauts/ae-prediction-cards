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
import { getRentById } from '../events/utils';
import { AppState } from "../state";
import { PredictionCard } from './PredictionCards';
import { Prediction } from "./types";


interface Props {

}

const getPredictionLabel = (predictionEvent, id) => predictionEvent.nft_higher_id === id ? 'HIGHER' : 'LOWER';

export const PredictionDetails: React.FC<Props> = () => {
  const params = useParams<{ eventId: string, predictionId: string }>();
  const eventId = parseInt(params.eventId);
  const predictionId = parseInt(params.predictionId);

  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [event, setEvent] = useState<PredictionEvent>();
  const [prediction, setPrediction] = useState<Prediction>();
  const [rent, setRent] = useState<number>(0);
  const [deposit, setDeposit] = useState(0);
  const predictionApi = usePredictionCardsApi();
  const { account } = useSelector<AppState, any>(state => state.wallet);
  const [isWinnerNFT, setIsWinnerNFT] = useState(false);

  useEffect(() => {
    (async () => {
      const [status, predictionEvent] = await predictionApi.getPrediction(eventId);
      const renter = await predictionApi.getNFTRenter(predictionId);
      const imageHash = await predictionApi.getNFTImage(predictionId);
      setStatus(status);
      setEvent(predictionEvent);
      setPrediction({
        id: predictionId,
        name: getPredictionLabel(predictionEvent, predictionId),
        owner: renter,
        rent: getRentById(predictionEvent, predictionId),
        imageHash
      });
      setIsLoading(false);
      if (status === 'ORACLE_PROCESSED') {
        setIsWinnerNFT(predictionEvent.winning_nft_id === predictionId);
      }
    })();
  }, [predictionApi, eventId, predictionId]);

  const triggerRent = async () => {
    setIsProcessing(true);
    if (deposit > 0) {
      await predictionApi.deposit(predictionId, toAettos(deposit));
    }

    await predictionApi.rentNFT(predictionId, toAettosPerMillisecond(rent));
    const [, predictionEvent] = await predictionApi.getPrediction(eventId);
    const renter = await predictionApi.getNFTRenter(predictionId);
    setEvent(predictionEvent);
    setPrediction(curr => ({
      ...curr,
      owner: renter,
      rent: getRentById(predictionEvent, predictionId)
    }))
    setIsProcessing(false);
  }

  const claimOrWithdraw = async () => {
    setIsProcessing(true);
    if (isWinnerNFT) {
      await predictionApi.claim(eventId);
    } else {
      await predictionApi.withdraw(predictionId);
    }
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

              {status === 'ACTIVE' &&
                <>
                  <StyledLabel>
                    <BasicText center light>rent price (AE/day)</BasicText>
                    <StyledInput type="number" autoFocus value={rent.toString()} onChange={evt => setRent(parseFloat(evt.target.value))} />
                  </StyledLabel>
                  <StyledLabel>
                    <BasicText center light>deposit (AE)</BasicText>
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
                </>
              }
              {status !== 'CREATED' && prediction?.owner !== account && <Button margin={['large', 0, 0, 0]} primary onClick={() => claimOrWithdraw()}>withdraw</Button>}
              {status === 'ORACLE_PROCESSED' &&
                <>
                  {account ? (isProcessing ?
                    <Box center margin={['large', 0, 0, 0]}>
                      <Spinner size="small" />
                    </Box> :
                    (isWinnerNFT && <Button margin={['large', 0, 0, 0]} primary onClick={() => claimOrWithdraw()}>claim</Button>)
                  ) : (
                    <BasicText light marginTop="large" center>connect your wallet to withdraw/claim</BasicText>
                  )}
                </>
              }
            </Box>
          </Box>
        </>
      )}
    </Box>
  );
}