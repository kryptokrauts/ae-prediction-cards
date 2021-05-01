import styled from "styled-components";
import { glassHoverMixin, glassMixin, glassMixinNoShadow } from "../../mixins/glass";

interface Props {
  onClick?: () => void;
  vertical?: boolean;
  tag?: string;
}

const StyledCard = styled.div<Props>`
  display: flex;
  flex-direction: column;
  position: relative;

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

const StyledCardTag = styled.div`
  position: absolute;
  border-radius: ${props => props.theme.borderRadius};
  padding: ${props => props.theme.padding.small};
  top: 6px;
  right: 6px;
  ${glassMixinNoShadow};
`;

export const Card: React.FC<Props> = ({ children, onClick, vertical, tag }) => (
  <StyledCard onClick={onClick} vertical={vertical}>
    {tag && <StyledCardTag>{tag}</StyledCardTag>}
    {children}
  </StyledCard>
);

export const CardFooter: React.FC = ({ children }) => (
  <StyledCardFooter>
    {children}
  </StyledCardFooter>
);