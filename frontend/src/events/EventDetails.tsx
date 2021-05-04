import React, { useEffect, useState } from "react";
import { useHistory, useParams } from "react-router";
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider";
import { Box } from "../common/components/box/Box";
import { Spinner } from "../common/components/spinner/Spinner";
import { toAePerDay } from "../common/utils/transformer";
import { PredictionList } from "../predictions/PredictionList";
import { Prediction } from "../predictions/types";
import { EventInfo } from "./components/EventInfo";
import { PredictionEvent } from "./types";

export const EventDetails = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const history = useHistory();
  const predictionApi = usePredictionCardsApi();
  const [event, setEvent] = useState<PredictionEvent>();
  const [isLoading, setIsLoading] = useState(true);
  const [predictions, setPredictions] = useState<Array<Prediction>>([{
    name: 'HIGHER'
  }, {
    name: 'LOWER'
  }]);

  const getRentById = (predictionEvent, nftId) => {
    const [nft, rent] = predictionEvent?.nft_last_rent_aettos_per_millisecond?.find(([nft]) => nft === nftId) || [];
    return rent;
  }

  useEffect(() => {
    (async () => {
      const [status, predictionEvent] = await predictionApi.getPrediction(eventId);
      setEvent(predictionEvent);
      predictions[0].id = predictionEvent.nft_higher_id;
      predictions[1].id = predictionEvent.nft_lower_equal_id;
      const [higherRenter, lowerRenter] = await Promise.all([
        predictionApi.getNFTRenter(predictionEvent.nft_higher_id),
        predictionApi.getNFTRenter(predictionEvent.nft_lower_equal_id),
      ]);
      const [higherImage, lowerImage] = await Promise.all([
        predictionApi.getNFTImage(predictionEvent.nft_higher_id),
        predictionApi.getNFTImage(predictionEvent.nft_lower_equal_id),
      ])
      setPredictions(([higher, lower]) => [{
        ...higher,
        rent: toAePerDay(getRentById(predictionEvent, predictionEvent.nft_higher_id))?.toString(),
        imageLower: higherImage,
        owner: higherRenter
      }, {
        ...lower,
        rent: toAePerDay(getRentById(predictionEvent, predictionEvent.nft_lower_equal_id))?.toString()?.toString(),
        imageHash: lowerImage,
        owner: lowerRenter
      }])
      setIsLoading(false);
    })();
  }, [predictionApi]);

  return (
    <Box center>
      {isLoading ? (
        <Spinner />
      ) : (
        <>
          <EventInfo margin={[0, 0, "xlarge", 0]} event={event!} />
          <PredictionList predictions={predictions} onPredictionClick={(prediction) => history.push(`${eventId}/${prediction.id}`)} />
        </>
      )}
    </Box>
  );
}