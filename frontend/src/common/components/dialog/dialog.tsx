import React, { useEffect } from 'react';
import ReactDOM from 'react-dom';
import styled from "styled-components";
import { glassMixin } from '../../mixins/glass';
import { Box } from '../box/Box';

interface Props {
  onClose?: () => void;
}

const StyledModal = styled(Box).attrs({
  padding: ["large", "large", "large", "large"]
})`
  position: relative;
  display: flex;
  min-width: 400px;
  max-width: calc(100% - 100px);
  min-height: 300px;
  max-height: calc(100% - 100px);
  border-radius: ${props => props.theme.borderRadius};
  ${glassMixin};
`;

const Close = styled.div`
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  top: 5px;
  right: 5px;
  background-color: black;
  color: white;
  border-radius: 100%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  line-height: 20px;
  cursor: pointer;
`;

export const Modal: React.FC<Props> = ({ children, onClose }) => {
  const wrapper = document.createElement('div');
  wrapper.classList.add('dialog');

  useEffect(() => {
    document.body.append(wrapper);
    return (() => {
      document.body.removeChild(wrapper);
    });
  })
  return ReactDOM.createPortal(
    <StyledModal>
      {onClose && <Close onClick={onClose}>&#10006;</Close>}
      {children}
    </StyledModal>,
    wrapper
  );
}