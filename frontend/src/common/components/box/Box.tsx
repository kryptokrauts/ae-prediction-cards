import styled from "styled-components";
import { MarginSize, PaddingSize } from "../../../theme.types";
import { marginMixin } from "../../mixins/spacing";

interface BoxProps {
  gap?: number;
  margin?: Array<MarginSize | number>;
  padding?: Array<PaddingSize | number>;
  row?: boolean;
  height?: string;
  width?: string;
  center?: boolean;
  wrap?: boolean;
  align?: string;
  justify?: "space-between";
};

const BoxWrapper = styled.div<BoxProps>`
  display: flex;
  ${marginMixin}
  ${props => props.padding && `padding: ${props.padding.map(p => props.theme.padding[p] || `${p}px`).join(' ')}`};

  ${props => props.height && `height: ${props.height}`};
  ${props => props.width && `width: ${props.width}`};

  ${props => props.center && `align-items: center`};
  ${props => props.row ? `flex-direction: row` : `flex-direction: column`};
  ${props => props.wrap && `flex-wrap: wrap`};
  ${props => props.height && `height: ${props.height}`};
  ${props => props.width && `width: ${props.width}`};
  ${props => props.align && `align-items: ${props.align}`};
  ${props => props.justify && `justify-content: ${props.justify}`};
`;

export const Box: React.FC<BoxProps> = ({ children, ...props }) => (
  <BoxWrapper {...props}>
    {children}
  </BoxWrapper>
);