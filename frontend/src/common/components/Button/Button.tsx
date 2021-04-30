import styled from "styled-components";
import { MarginSize } from "../../../theme.types";
import { glassMixinNoShadow } from "../../mixins/glass";
import { marginMixin } from "../../mixins/spacing";

interface ButtonProps {
  margin?: Array<MarginSize | number>
}

export const Button = styled.button<ButtonProps>`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: ${props => `${props.theme.padding.medium} ${props.theme.padding.large}`};
  ${marginMixin}
  border-radius: ${props => props.theme.borderRadius};

  cursor: pointer;

  ${glassMixinNoShadow}
`;