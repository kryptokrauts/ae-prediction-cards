import React from "react"
import { Box } from "../common/components/box/Box"
import { Button } from "../common/components/Button/Button"
import { Modal, ModalFooter } from "../common/components/dialog/dialog"
import { Input } from "../common/components/input/Input"
import { SmallHeading } from "../common/components/text/Heading"

interface Props {
  onClose: () => void;
}

export const NewEventModal: React.FC<Props> = ({ onClose }) => {
  return (
    <Modal onClose={onClose}>
      <SmallHeading>Create a new Event</SmallHeading>
      <Box margin={["medium", 0, 0, 0]}>
        <Input label="Asset" />
        <Input label="Target Price" number />
        <Box row>
          <Input label="Start Date" />
          <Input label="End Date" />
        </Box>
      </Box>
      <ModalFooter>
        <Button onClick={onClose}>Close</Button>
        <Button primary>Create</Button>
      </ModalFooter>
    </Modal >
  )
}