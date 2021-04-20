import styled from "styled-components";
import { glassMixinNoShadow } from "../../mixins/glass";


export const Button = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: ${props => `${props.theme.padding.medium} ${props.theme.padding.large}`};
  border-radius: ${props => props.theme.borderRadius};

  cursor: pointer;

  ${glassMixinNoShadow}
`;