import styled, { css } from "styled-components";

const textMixin = css`
  padding: 0;
  margin: 0;
`;

export const Caption = styled.p`
  ${textMixin}

  font-size: 12px;
`;


export const Text = styled.p`
  ${textMixin}

  font-size: 16px;
`;