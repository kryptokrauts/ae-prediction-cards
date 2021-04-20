import styled from 'styled-components';
import svgLogo from './Logo.svg';

const StyledImg = styled.img`
  max-width: 100%;
  max-height: 100%;
`;

export const Logo = () => (
  <StyledImg src={svgLogo} />
)