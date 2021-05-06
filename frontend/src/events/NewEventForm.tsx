import React, { useReducer, useState } from "react"
import { usePredictionCardsApi } from "../common/api/PredictionCardsApiProvider"
import { Box } from "../common/components/box/Box"
import { Button } from "../common/components/Button/Button"
import { ModalFooter } from "../common/components/dialog/dialog"
import { StyledInput, StyledLabel } from "../common/components/input/input"
import { Spinner } from "../common/components/spinner/Spinner"
import { SmallHeading } from "../common/components/text/Heading"
import { BasicText } from "../common/components/text/Text"


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
    asset: "AE",
    img_higher: '',
    img_lower: '',
    targetPrice: 0,
    start_timestamp: format(today),
    end_timestamp: "",
    max_increase_rent_amount_aettos: 1,
  });

  const createPredictionEvent = async (state) => {
    setIsLoading(true);
    await predictionApi.createPrediction(state, state.img_higher, state.img_lower);
    setIsLoading(false);
  }

  const available_assets = ["AE", "BTC", "MTL", "XPR", "DOGE"];
  const images = [
    {
      "asset": "AE",
      "lower": ["QmQTzC8wUtjk85L5edXgaq3nmT1ML7bFD7srkemekeAK8Y", "QmTzAwEX8uEboAyq748vUsMPgPq1M2sTv1hPxzY7x1PssR", "QmSTYri916FcUb1xDFLhS92Kqknb5EuuNsEa1zg57Rx7gJ"],
      "higher": ["QmNvXoxUZjAtRYiiGjTxianAJEbo4rW5VapnL9oPxRWdMn", "QmPaheBCzp82QUKFmnY2pHRz9JF4ouH1brMxrvrbDvrGnD", "QmaLjXtsb7Z1cKmXrBGFYbS26dGtv5ivt9HjNy4JQGi6Jq"]
    },
    {
      "asset": "BTC",
      "lower": ["QmZSRHgMewf95QdCQptc4P5JivBQd4zXavLX5dfYGYaXq5", "QmZsvPHwVMrMWvcHyh63YVXqGGrdGvUjPMNWGob1m3WRvo", "QmTpQi2EZntsLxwLL1zW3yaPgzww2Navzpu9NPbQJZ5u2w"],
      "higher": ["QmZwYYE72r43RzpQLae27ssJkd6wzTBWqu5VQ9UTU7waya", "QmXnwGEEZY32bgSFdNsHe2XMnhtQYu7FYcUyXgEFRoH4qx", "QmRPkay6esiaBBJUqGMpf5iPmk82Ur8NtF2VJXmbShkWDD"]
    },
    {
      "asset": "MTL",
      "lower": ["QmSuNJr6KWpVP5KorRSSpCXJJJTYjYutxg9D6Xn5zwGE8o", "QmVUUuSF8LEbUzCw3gKUdbWcVwyazPSZmP75beUN51euxu", "QmSdPVaQ4AUZPgGv99FSjbSNQLGzaH5LZVCmQR3oSV3GBJ"],
      "higher": ["QmWRCmLh33oDgtBChNeT5fRTwVzPPuKftfMjgGt6hyiKVh", "QmSgKZ9Ck2VhD83pmyKjLmbFcVMRB3wPRWaXbGsMXHKL9p", "QmViip6Hg4yJ6K4obAdMn5ofvtaaw2sdv9qkK5CiVqKPwC"]
    },
    {
      "asset": "XPR",
      "lower": ["QmWtmxuSFBLjBm5Vv6W2xuXoDEp5Fn73N7sHy4EuGiTgJ1", "QmfFGPoozKQqwwzEFnbmRxXEHL9756hQjViyWfe4dVPK3v", "QmW4nkM6fjML4Npa8s4MJ7EAtwZCicv8q2S6eT5spjDEaF"],
      "higher": ["QmV2jTDUQoTVXb2Jwadu59ugnHALkB4Km4tku4ZUEWsskt", "QmY87ttqhVs49jye9c3LHELtz9YTHtL4r3DXxhxAssxPop", "Qmb3uj9qV9mixH3Z8sHquFK2jK7U9snK3DfZ9vNZYzUwVB"]
    },
    {
      "asset": "DOGE",
      "lower": ["QmSw9RZRxXxcMEWNUYWcDhHc1yduz2CQxf4NsSDgeKWFv1", "QmcZYLEeX8Q9W3hqEZPDZoPW9mxpmQH31C1zkAXFDJkhnw", "QmdrfSHXsmYM8mJAm7SHPDQjNQzTuTVes874K2XfrxgW3R"],
      "higher": ["Qmaj7dtAtkwWh1zDrRNSNse1443WMcyVkjutFep4g5Z1ni", "QmPcnBi8PE32hJTSNjiJhT89idzEvwzZMrXvyCW3NczKb5", "QmUNe9uRsGyMxTWn2U9YhuZtgMQzaAgnCfDuxcVyLW2WAn"]
    }
  ]

  return (
    <>
      <SmallHeading>Create a new Event</SmallHeading>
      <Box margin={["medium", 0, 0, 0]}>
        <StyledLabel>
          <BasicText>Asset</BasicText>
          <select name="asset" value={state.asset} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })}>
            {available_assets.map(asset => <option key={asset} value={asset} >{asset}</option>)}
          </select>
        </StyledLabel>
        <StyledLabel>
          <BasicText>Target Price (USD)</BasicText>
          <StyledInput type="number" step=".01" value={state.target_price} name="target_price" onChange={evt => dispatch({ target: evt.target.name, value: parseFloat(evt.target.value) })} />
        </StyledLabel>
        <Box row justify="space-between">
          <StyledLabel>
            <BasicText>Start Date</BasicText>
            <StyledInput type="datetime-local" name="start_timestamp" value={state.start_timestamp} min={format(today)} max={state.endDate && format(new Date(state.endDate))} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
          </StyledLabel>
          <StyledLabel>
            <BasicText>End Date</BasicText>
            <StyledInput type="datetime-local" name="end_timestamp" value={state.end_timestamp} min={format(new Date(state.startDate))} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
          </StyledLabel>
        </Box>
        <Box row margin={['medium', 0, 0, 0]} justify="space-between">
          <Box>
            <SmallHeading>Higher NFT</SmallHeading>
            {images.find(({ asset }) => asset === state.asset)?.higher.map(hash => (
              <Box key={hash} row center margin={['small', 0, 0, 'small']}>
                <input type="radio" name="img_higher" value={hash} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
                <img src={`https://ipfs.io/ipfs/${hash}`} width="100" height="100" />
              </Box>
            ))}
          </Box>
          <Box row={false}>
            <SmallHeading>Lower NFT</SmallHeading>
            {images.find(({ asset }) => asset === state.asset)?.lower.map(hash => (
              <Box key={hash} row center margin={['small', 0, 0, 'small']}>
                <input type="radio" name="img_lower" value={hash} onChange={evt => dispatch({ target: evt.target.name, value: evt.target.value })} />
                <img src={`https://ipfs.io/ipfs/${hash}`} width="100" height="100" />
              </Box>
            ))}
          </Box>
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