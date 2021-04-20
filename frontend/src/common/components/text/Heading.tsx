import styled, { css } from "styled-components";

const headingStyleMixin = css`
  margin: 0;
  padding: 0;
`;

export const LargeHeading = styled.h1`
  ${headingStyleMixin}
`;

export const MidHeading = styled.h2`
  ${headingStyleMixin}
`;

export const SmallHeading = styled.h3`
  ${headingStyleMixin}
`;