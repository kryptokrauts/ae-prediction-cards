import React, { useEffect, useState } from "react";
import { useHistory, useParams } from "react-router";
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider";
import { Box } from "../common/components/box/Box";
import { Spinner } from "../common/components/spinner/Spinner";
import { PredictionList } from "../predictions/PredictionList";
import { Prediction } from "../predictions/types";
import { EventInfo } from "./components/EventInfo";
import { PredictionEvent } from "./types";
import { getRentById } from "./utils";

export const EventDetails = () => {
  const params = useParams<{ eventId: string }>();
  const eventId = parseInt(params.eventId);
  const history = useHistory();
  const predictionApi = usePredictionCardsApi();
  const [event, setEvent] = useState<PredictionEvent>();
  const [status, setStatus] = useState<string>();
  const [isLoading, setIsLoading] = useState(true);
  const [predictions, setPredictions] = useState<Array<Prediction>>([{
    name: 'HIGHER'
  }, {
    name: 'LOWER'
  }]);

  useEffect(() => {
    (async () => {
      const [currStatus, predictionEvent] = await predictionApi.getPrediction(eventId);
      setStatus(currStatus);
      setEvent(predictionEvent);
      let higherOwner;
      let lowerOwner;
      if (currStatus === 'ORACLE_PROCESSED') {
        const res = await Promise.all([
          predictionApi.getNFTOwner(predictionEvent.nft_higher_id),
          predictionApi.getNFTOwner(predictionEvent.nft_lower_equal_id),
        ]);
        higherOwner = res[0];
        lowerOwner = res[1];
      } else {
        const res = await Promise.all([
          predictionApi.getNFTRenter(predictionEvent.nft_higher_id),
          predictionApi.getNFTRenter(predictionEvent.nft_lower_equal_id),
        ]);
        higherOwner = res[0];
        lowerOwner = res[1];
      }
      const [higherImage, lowerImage] = await Promise.all([
        predictionApi.getNFTImage(predictionEvent.nft_higher_id),
        predictionApi.getNFTImage(predictionEvent.nft_lower_equal_id),
      ])
      setPredictions(([higher, lower]) => [{
        ...higher,
        id: predictionEvent.nft_higher_id,
        rent: getRentById(predictionEvent, predictionEvent.nft_higher_id),
        imageHash: higherImage,
        owner: higherOwner
      }, {
        ...lower,
        id: predictionEvent.nft_lower_equal_id,
        rent: getRentById(predictionEvent, predictionEvent.nft_lower_equal_id),
        imageHash: lowerImage,
        owner: lowerOwner
      }])
      setIsLoading(false);
    })();
  }, [predictionApi, eventId]);

  return (
    <Box center>
      {isLoading ? (
        <Spinner />
      ) : (
        <>
          <EventInfo margin={[0, 0, "xlarge", 0]} event={event!} />
          <PredictionList predictions={predictions} onPredictionClick={(prediction) => history.push(`${eventId}/${prediction.id}`)} isComplete={status === 'ORACLE_PROCESSED'} winnerNFT={event?.winning_nft_id} />
        </>
      )}
    </Box>
  );
}