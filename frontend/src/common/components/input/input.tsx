import styled from "styled-components";

export const StyledLabel = styled.label`
  display: flex;
  flex-direction: column;
  margin: ${props => props.theme.margin.medium} 0 ${props => props.theme.margin.medium} 0;
`;

export const StyledInput = styled.input`
  width: 100%;
  background-color: transparent;
  border: none;
  border-bottom: 2px solid rgba( 255, 255, 255, 0.9);
  outline: none;
  padding: ${props => `${props.theme.padding.small} ${props.theme.padding.large}`};
  margin: ${props => props.theme.margin.medium} 0 0 0;
`;
