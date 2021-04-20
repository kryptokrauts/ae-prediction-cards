import styled from "styled-components";
import { glassMixin } from "../../mixins/glass";

const StyledCard = styled.div`
  display: flex;
  flex-direction: column;

  ${glassMixin}
  padding: ${props => props.theme.padding.large};
  border-radius: ${props => props.theme.borderRadius};
  min-width: 380px;
  min-height: 200px;
`;

const StyledCardFooter = styled.div`
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  width: 100%;
`;

export const Card: React.FC = ({ children }) => (
  <StyledCard>
    {children}
  </StyledCard>
);

export const CardFooter: React.FC = ({ children }) => (
  <StyledCardFooter>
    {children}
  </StyledCardFooter>
);