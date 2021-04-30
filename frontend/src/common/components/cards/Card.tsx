import styled from "styled-components";
import { glassHoverMixin, glassMixin } from "../../mixins/glass";

interface Props {
  onClick?: () => void;
  vertical?: boolean;
}

const StyledCard = styled.div<Props>`
  display: flex;
  flex-direction: column;

  ${glassMixin}
  padding: ${props => props.theme.padding.large};
  border-radius: ${props => props.theme.borderRadius};
  min-width: ${props => props.vertical ? 200 : 380}px;
  min-height:  ${props => props.vertical ? 300 : 200}px;

  ${glassHoverMixin}
`;

const StyledCardFooter = styled.div`
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  width: 100%;
`;

export const Card: React.FC<Props> = ({ children, onClick, vertical }) => (
  <StyledCard onClick={onClick} vertical={vertical}>
    {children}
  </StyledCard>
);

export const CardFooter: React.FC = ({ children }) => (
  <StyledCardFooter>
    {children}
  </StyledCardFooter>
);