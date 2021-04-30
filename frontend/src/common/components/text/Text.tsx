import styled, { css } from "styled-components";

const textMixin = css`
  padding: 0;
  margin: 0;
`;

export const textColorMixing = (props: { light?: boolean }) => `color: ${props.light ? 'white' : 'black'};`;

export const Caption = styled.p`
  ${textMixin}
  ${textColorMixing}

  font-size: 12px;
`;

export const BasicText = styled.p<{ light?: boolean }>`
  ${textMixin}
  ${textColorMixing}
  font-size: 16px;
`;