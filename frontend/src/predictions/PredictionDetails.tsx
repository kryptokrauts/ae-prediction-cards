import { toAettos } from '@aeternity/aepp-sdk/es/utils/amount-formatter'
import BigNumber from 'bignumber.js'
import React, { useEffect, useState } from "react"
import { useSelector } from "react-redux"
import { useParams } from "react-router"
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider"
import { Box } from "../common/components/box/Box"
import { Button } from "../common/components/Button/Button"
import { Card, CardFooter } from "../common/components/cards/Card"
import { Input } from "../common/components/input/Input"
import { Spinner } from "../common/components/spinner/Spinner"
import { SmallHeading } from "../common/components/text/Heading"
import { BasicText, Caption, SmallText } from "../common/components/text/Text"
import { EventInfo } from "../events/components/EventInfo"
import { PredictionEvent } from "../events/types"
import { AppState } from "../state"
import { Prediction } from "./types"

BigNumber.config({ DECIMAL_PLACES: 0 });

interface Props {

}

const getPredictionLabel = (predictionEvent, id) => predictionEvent.nft_higher_id === id ? 'HIGHER' : 'LOWER';

const daysToMilliseconds = (days: number): number => days * 24 * 60 * 60 * 1000

export const PredictionDetails: React.FC<Props> = () => {
  const { eventId, predictionId } = useParams<{ eventId: string, predictionId: string }>();
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [event, setEvent] = useState<PredictionEvent>();
  const [prediction, setPrediction] = useState<Prediction>();
  const [rent, setRent] = useState(0);
  const [deposit, setDeposit] = useState(0);
  const predictionApi = usePredictionCardsApi();
  const { account } = useSelector<AppState, any>(state => state.wallet);

  useEffect(() => {
    (async () => {
      const [status, predictionEvent] = await predictionApi.getPrediction(eventId);
      setEvent(predictionEvent);
      const renter = await predictionApi.getNFTRenter(parseInt(predictionId));
      setPrediction({
        id: parseInt(predictionId),
        name: getPredictionLabel(predictionEvent, predictionId),
        owner: renter,
      });
      setIsLoading(false);
    })();
  }, [predictionApi]);

  const triggerRent = async () => {
    setIsProcessing(true);
    if (deposit > 0) {
      const depositResponse = await predictionApi.deposit(predictionId, toAettos(deposit));
      console.log('Deposit', depositResponse);
    }
    const aettos = new BigNumber(toAettos(rent));
    const milliseconds = new BigNumber(daysToMilliseconds(1));
    console.log(aettos.dividedBy(milliseconds).toPrecision(1, 1));
    const rentResponse = await predictionApi.rentNFT(predictionId, aettos.dividedBy(milliseconds).toString());
    console.log('Rent', rentResponse);
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
            <Card vertical>
              <Box center>
                <SmallHeading>{prediction?.name}</SmallHeading>
              </Box>
              <CardFooter>
                <Box row={false}>
                  <Caption>renter</Caption>
                  <SmallText>{prediction?.owner || 'None'}</SmallText>
                </Box>
              </CardFooter>
            </Card>
            <Box margin={[0, 0, 0, 'xlarge']} width="250px">
              <Input light label="rent price (AE/day)" type="number" autoFocus centerLabel value={rent.toString()} onChange={evt => setRent(parseFloat(evt.target.value))} />
              <Input light label="deposit (AE)" type="number" autoFocus centerLabel value={deposit.toString()} onChange={evt => setDeposit(parseFloat(evt.target.value))} />
              <Caption marginTop="large" light>With the current price and deposit you can rent the NFT for <strong>{Math.floor(deposit / rent) || 0} days</strong></Caption>
              {account ? (isProcessing ?
                <Box center margin={['large', 0, 0, 0]}>
                  <Spinner size="small" />
                </Box> :
                <Button margin={['large', 0, 0, 0]} primary onClick={() => triggerRent()}>rent the card</Button>
              ) : (
                <BasicText light marginTop="large">connect your wallet to rent this card</BasicText>
              )}
            </Box>
          </Box>
        </>
      )}
    </Box>
  );
}