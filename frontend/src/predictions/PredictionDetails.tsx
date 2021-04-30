import React from "react"
import { Box } from "../common/components/box/Box"
import { Button } from "../common/components/Button/Button"
import { Card, CardFooter } from "../common/components/cards/Card"
import { Input } from "../common/components/input/Input"
import { SmallHeading } from "../common/components/text/Heading"
import { Caption, SmallText } from "../common/components/text/Text"
import { EventInfo } from "../events/components/EventInfo"

interface Props {

}

export const PredictionDetails: React.FC<Props> = () => (
  <Box>
    <Box center>
      <EventInfo margin={[0, 0, "xlarge", 0]} />
    </Box>
    <Box row center>
      <Card vertical>
        <Box center>
          <SmallHeading>Lower</SmallHeading>
        </Box>
        <CardFooter>
          <Box row={false}>
            <Caption>owner</Caption>
            <SmallText>ak_asdfasdf</SmallText>
          </Box>
          <Box row={false} align="flex-end">
            <Caption>time remaining</Caption>
            <SmallText>17days</SmallText>
          </Box>
        </CardFooter>
      </Card>
      <Box margin={[0, 0, 0, 'xlarge']}>
        <Input light label="enter your daily rent price" number autoFocus centerLabel />
        <Button margin={['large', 0, 0, 0]} primary>rent the card</Button>
      </Box>
    </Box>
  </Box>
)