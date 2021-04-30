import styled from "styled-components";

type SpinnerSize = "small" | "medium" | "large";

const getSpinnerSize = (props: { size?: SpinnerSize }): number => {
  switch (props.size) {
    case 'small':
      return 24;
    case 'medium':
      return 46;
    case 'large':
    default:
      return 64;
  }
}

export const Spinner = styled.div<{ size?: SpinnerSize }>`
  display: inline-block;
  width: ${props => getSpinnerSize(props) + 6};
  height: ${props => getSpinnerSize(props) + 6};

  &:after {
    content: " ";
    display: block;
    width: ${getSpinnerSize}px;
    height: ${getSpinnerSize}px;
    margin: 8px;
    border-radius: 50%;
    border: 6px solid #fff;
    border-color: #fff transparent #fff transparent;
    animation: spinning 1.2s linear infinite;
  }
  @keyframes spinning {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
`