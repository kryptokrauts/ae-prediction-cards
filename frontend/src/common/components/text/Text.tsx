import styled, { css } from "styled-components";

const textMixin = css`
  padding: 0;
  margin: 0;
`;

export const textColorMixing = (props: { light?: boolean }) => `color: ${props.light ? 'white' : 'black'};`;


export const BasicText = styled.p<{ light?: boolean }>`
  ${textMixin}
  ${textColorMixing}
  font-size: 16px;
`;

export const Caption = styled(BasicText)`
  font-size: 12px;
`;

export const SmallText = styled(BasicText) <{ light?: boolean }>`
  font-size: 14px;
`;