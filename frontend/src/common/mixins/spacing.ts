export const marginMixin = props => props.margin && `margin: ${props.margin.map(m => props.theme.margin[m] || `${m}px`).join(' ')};`;
