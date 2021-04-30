import { useSelector } from "react-redux";
import { useHistory } from "react-router";
import styled from "styled-components";
import { useWallet } from "../common/aeternity/WalletProvider";
import { Button } from "../common/components/Button/Button";
import { Logo } from "../common/components/logo/Logo";
import { Spinner } from "../common/components/spinner/Spinner";
import { MidHeading } from "../common/components/text/Heading";
import { BasicText } from "../common/components/text/Text";
import { glassMixin } from "../common/mixins/glass";
import { AppState } from "../state";

const StyledWrapper = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 64px;
  padding: ${props => `${props.theme.padding.small} ${props.theme.padding.large}`};
  ${glassMixin}
`;

const NavActions = styled.div`
  display: flex;
  margin-left: auto;
  align-items: center;
`;

export const Navigation = () => {
  const history = useHistory();
  const wallet = useWallet();
  const { account, balance, connecting } = useSelector<AppState, any>(state => state.wallet);

  const handleConnect = async () => {
    await wallet.connect();
  };

  return (
    <StyledWrapper>
      <Logo />
      <MidHeading onClick={() => history.push('/')}>PredictionCards</MidHeading>
      <NavActions>
        {account ? (
          <>
            <BasicText>{account}</BasicText>
            <Button margin={[0, 0, 0, "small"]}>New Event</Button>
          </>
        ) : (connecting ? (
          <Spinner size="small" />
        ) : (
          <Button onClick={handleConnect}>Connect Wallet</Button>
        )
        )}
      </NavActions>
    </StyledWrapper >
  )
}