import styled, { css } from "styled-components";
import { textColorMixing } from "./Text";

const headingStyleMixin = css`
  margin: 0;
  padding: 0;
  font-weight: normal;
`;

export const LargeHeading = styled.h1<{ light?: boolean }>`
  ${headingStyleMixin}
  ${textColorMixing}
`;

export const MidHeading = styled.h2<{ light?: boolean }>`
  ${headingStyleMixin}
  ${textColorMixing}
`;

export const SmallHeading = styled.h3<{ light?: boolean }>`
  ${headingStyleMixin}
  ${textColorMixing}
`;