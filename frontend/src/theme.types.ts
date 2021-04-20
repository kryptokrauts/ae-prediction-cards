interface Margin {
  small: string;
  medium: string;
  large: string;
  xlarge: string;
}

export type MarginSize = keyof Margin;

interface Padding {
  small: string;
  medium: string;
  large: string;
}

export type PaddingSize = keyof Padding;

export interface AppTheme {
  colors: {
    brand: string;
    secondary: string;
    accent: string;
  },
  padding: Padding,
  margin: Margin,
  borderRadius: string;
}