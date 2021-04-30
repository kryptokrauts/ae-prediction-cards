import React from "react";
import styled from "styled-components";
import { SmallText, textColorMixing } from "../text/Text";

interface Props {
  label: string;
  light?: boolean;
  number?: boolean;
  autoFocus?: boolean;
  centerLabel?: boolean;
}

const StyledLabel = styled.label<Partial<Props>>`
  display: flex;
  flex-direction: column;
  text-align: ${props => props.centerLabel ? 'center' : 'initial'};
  margin: ${props => props.theme.margin.medium} 0 ${props => props.theme.margin.medium} 0;
`;

const StyledInput = styled.input<Partial<Props>>`
  width: 100%;
  background-color: transparent;
  border: none;
  border-bottom: 2px solid rgba( 255, 255, 255, 0.9);
  outline: none;
  padding: ${props => `${props.theme.padding.small} ${props.theme.padding.large}`};
  margin: ${props => props.theme.margin.medium} 0 0 0;
  ${textColorMixing}
`;

export const Input: React.FC<Props> = ({ label, light, number, autoFocus, centerLabel }) => (
  <StyledLabel centerLabel={centerLabel}>
    <SmallText light={light}>{label}</SmallText>
    <StyledInput light={light} type={number ? 'number' : 'text'} autoFocus={autoFocus} />
  </StyledLabel>
)