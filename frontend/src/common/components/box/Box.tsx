import styled from "styled-components";
import { MarginSize, PaddingSize } from "../../../theme.types";

interface BoxProps {
  gap?: number;
  margin?: Array<MarginSize>;
  padding?: Array<PaddingSize>;
  row?: boolean;
  height?: string;
  width?: string;
  center?: boolean;
  wrap?: boolean;
  align?: string;
};

const applyGap = ({ gap }: Pick<BoxProps, 'gap'>): string | undefined => {
  if (gap) {
    return `
      margin-top: -${gap}px;
      margin-left: -${gap}px;

      & > * {
        margin-top: ${gap}px;
        margin-left: ${gap}px;
      }
    `;
  }
};

const BoxWrapper = styled.div<BoxProps>`
  display: flex;
  ${props => props.margin && `margin: ${props.margin.map(m => props.theme.margin[m] || `${m}px`).join(' ')}`};
  ${props => props.padding && `padding: ${props.padding.map(p => props.theme.padding[p] || `${p}px`).join(' ')}`};

  ${props => props.height && `height: ${props.height}`};
  ${props => props.width && `width: ${props.width}`};
`;

const InnerBox = styled.div<BoxProps>`
  display: flex;

  ${props => props.center && `align-items: center`};
  ${props => props.row ? `flex-direction: row` : `flex-direction: column`};
  ${props => props.wrap && `flex-wrap: wrap`};
  ${props => props.height && `height: ${props.height}`};
  ${props => props.width && `width: ${props.width}`};
  ${props => props.align && `align-items: ${props.align}`};

  ${applyGap}
`;

export const Box: React.FC<BoxProps> = ({ children, row = true, margin, ...props }) => (
  <BoxWrapper margin={margin}>
    <InnerBox gap={props.gap} row={row} {...props}>
      {children}
    </InnerBox>
  </BoxWrapper>
);