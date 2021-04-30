import { css } from "styled-components";

export const glassMixinNoShadow = () => {
  return css`
    background: rgba( 255, 255, 255, 0.65 );
    backdrop-filter: blur( 4px );
    -webkit-backdrop-filter: blur( 4px );
    border: 1px solid rgba( 255, 255, 255, 0.18 );
  `;
}

export const glassMixin = () => {
  return css`
    ${glassMixinNoShadow}
    box-shadow: 0 8px 32px 0 rgba( 31, 38, 135, 0.37 );
  `;
}

export const glassHoverMixin = () => {
  return css`
    &:hover {
    }
  `;
};