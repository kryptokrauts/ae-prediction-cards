import React, { useReducer, useState } from "react"
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider"
import { Box } from "../common/components/box/Box"
import { Button } from "../common/components/Button/Button"
import { ModalFooter } from "../common/components/dialog/dialog"
import { Input } from "../common/components/input/Input"
import { Spinner } from "../common/components/spinner/Spinner"
import { SmallHeading } from "../common/components/text/Heading"

interface Props {
  onClose: () => void;
}

const format = (value: Date) => {
  return `${value.getFullYear()}-${(value.getMonth() + 1).toString().padStart(2, '0')}-${value.getDate().toString().padStart(2, '0')}`
}

const newEventFormReducer = (state, action) => {
  return {
    ...state,
    [action.target]: action.value
  }
}

export const NewEventForm: React.FC<Props> = ({ onClose }) => {
  const [isLoading, setIsLoading] = useState(false);
  const today = new Date();
  const predictionApi = usePredictionCardsApi();
  const [state, dispatch] = useReducer(newEventFormReducer, {
    asset: "",
    targetPrice: 0,
    start_timestamp: format(today),
    end_timestamp: "",
    max_increase_rent_amount_aettos: 1,
  });

  const createPredictionEvent = async (state) => {
    setIsLoading(true);
    await predictionApi.createPrediction(state);
    setIsLoading(false);
  }

  return (
    <>
      <SmallHeading>Create a new Event</SmallHeading>
      <Box margin={["medium", 0, 0, 0]}>
        <Input label="Asset" name="asset" value={state.asset} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
        <Input label="Target Price" type="number" value={state.target_price} name="target_price" onChange={evt => dispatch({ target: evt.target.name, value: parseInt(evt.target.value) })} />
        <Box row justify="space-between">
          <Input label="Start Date" type="date" name="start_timestamp" value={state.start_timestamp} min={format(today)} max={state.endDate && format(new Date(state.endDate))} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
          <Input label="End Date" type="date" name="end_timestamp" value={state.end_timestamp} min={format(new Date(state.startDate))} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
        </Box>
      </Box>
      <ModalFooter>
        {isLoading ? <Spinner size="small" /> : (
          <>
            <Button onClick={onClose}>Close</Button>
            <Button primary onClick={() => createPredictionEvent(state)}>Create</Button>
          </>
        )}
      </ModalFooter>
    </>
  )
}