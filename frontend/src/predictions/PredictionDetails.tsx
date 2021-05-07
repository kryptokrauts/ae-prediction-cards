import { toAe, toAettos } from '@aeternity/aepp-sdk/es/utils/amount-formatter';
import BigNumber from 'bignumber.js';
import React, { useEffect, useState } from "react";
import { useSelector } from 'react-redux';
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
import { AppState } from '../state';
import { PredictionCard } from './PredictionCards';
import { Prediction } from "./types";


const getPredictionLabel = (predictionEvent, id) => predictionEvent.nft_higher_id === id ? 'HIGHER' : 'LOWER';

export const PredictionDetails: React.FC = () => {
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
  const [currentDeposit, setCurrentDeposit] = useState(0);
  const [errorMsg, setErrorMsg] = useState<string>();

  useEffect(() => {
    (async () => {
      const [status, predictionEvent] = await predictionApi.getPrediction(eventId);
      const imageHash = await predictionApi.getNFTImage(predictionId);
      if (account) {
        const currDeposit = await predictionApi.getDeposit(predictionId, account);
        setCurrentDeposit(currDeposit);
      }
      let owner;
      if (status === 'ORACLE_PROCESSED') {
        owner = await predictionApi.getNFTOwner(predictionId);
      } else {
        owner = await predictionApi.getNFTRenter(predictionId);
      }
      setStatus(status);
      setEvent(predictionEvent);
      setPrediction({
        id: predictionId,
        name: getPredictionLabel(predictionEvent, predictionId),
        owner: owner,
        rent: getRentById(predictionEvent, predictionId),
        imageHash
      });
      setIsLoading(false);
      if (status === 'ORACLE_PROCESSED') {
        setIsWinnerNFT(predictionEvent.winning_nft_id === predictionId);
      }
    })();
  }, [predictionApi, eventId, predictionId, account]);

  const triggerRent = async () => {
    setIsProcessing(true);
    if (deposit > 0) {
      await predictionApi.deposit(predictionId, toAettos(deposit));
    }

    await predictionApi.rentNFT(predictionId, toAettosPerMillisecond(rent));
    const [, predictionEvent] = await predictionApi.getPrediction(eventId);
    const renter = await predictionApi.getNFTRenter(predictionId);
    const newDeposit = await predictionApi.getDeposit(predictionId, account);
    setDeposit(newDeposit);
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
    try {
      if (isWinnerNFT) {
        await predictionApi.claim(eventId);
      } else {
        await predictionApi.withdraw(predictionId);
      }
      const newDeposit = await predictionApi.getDeposit(predictionId, account);
      setDeposit(newDeposit);
    } catch (err) {
      setErrorMsg('Failed. Something went wrong.');
    }
    setIsProcessing(false);
  }

  const availableBalance = new BigNumber(toAe(currentDeposit)).toFixed(2);

  const hodlTimes = event?.nft_hodl_time.find(([nftId]) => nftId === predictionId);
  const hodlers = hodlTimes && hodlTimes[1]?.map(([acc]) => acc);
  const hasRented = account && hodlers?.includes(account);

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
            <PredictionCard prediction={prediction} isCompleted={status === 'ORACLE_PROCESSED'} />
            <Box margin={[0, 0, 0, 'xlarge']} width="250px">
              {status === 'ACTIVE' &&
                <>
                  <StyledLabel>
                    <BasicText center light>rent price (AE/day)</BasicText>
                    <StyledInput type="number" autoFocus value={rent.toString()} onChange={evt => setRent(parseFloat(evt.target.value))} />
                  </StyledLabel>
                  <StyledLabel>
                    <BasicText center light>deposit (AE) (optional)</BasicText>
                    <StyledInput type="number" autoFocus value={deposit.toString()} onChange={evt => setDeposit(parseFloat(evt.target.value))} />
                  </StyledLabel>
                  {currentDeposit > 0 && <Caption marginTop="large" light>Available balance for Prediction NFT <strong>{new BigNumber(toAe(currentDeposit)).toFixed(2)} AE</strong></Caption>}
                  {rent > 0 && <Caption marginTop="large" light>With the current price and deposit you can rent the NFT for <strong>{Math.floor((deposit + parseFloat(availableBalance)) / rent) || 0} days</strong></Caption>}
                  {!isProcessing && (account ? (
                    <Button margin={['large', 0, 0, 0]} primary onClick={() => triggerRent()} disabled={!rent}>rent the card</Button>
                  ) : (
                    <BasicText light marginTop="large" center>connect your wallet to rent this card</BasicText>
                  ))}
                </>
              }
              {isProcessing &&
                <Box center margin={['large', 0, 0, 0]}>
                  <Spinner size="small" />
                </Box>
              }
              {!isProcessing && status !== 'CREATED' && prediction?.owner === account && currentDeposit > 0 && !hasRented && <Button margin={['large', 0, 0, 0]} primary onClick={() => claimOrWithdraw()}>withdraw</Button>}
              {!isProcessing && status === 'ORACLE_PROCESSED' &&
                <>
                  {account ? (
                    (isWinnerNFT && hasRented && <Button margin={['large', 0, 0, 0]} primary onClick={() => claimOrWithdraw()}>claim</Button>)
                  ) : (
                    <BasicText light marginTop="large" center>connect your wallet to withdraw/claim</BasicText>
                  )}
                </>
              }
              {errorMsg && <BasicText marginTop="large" light center>{errorMsg}</BasicText>}
            </Box>
          </Box>
        </>
      )}
    </Box>
  );
}