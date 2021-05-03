import styled, { css } from "styled-components";
import { MarginSize } from "../../../theme.types";

const textMixin = css`
  padding: 0;
  margin: 0;
`;

export const textColorMixing = (props: { light?: boolean }) => `color: ${props.light ? 'white' : 'black'};`;


export const BasicText = styled.p<{ light?: boolean, marginTop?: MarginSize }>`
  ${textMixin}
  ${textColorMixing}
  font-size: 16px;
  ${props => props.marginTop && `margin-top: ${props.theme.margin[props.marginTop]};`}
`;

export const Caption = styled(BasicText)`
  font-size: 12px;
`;

export const SmallText = styled(BasicText) <{ light?: boolean }>`
  font-size: 14px;
`;