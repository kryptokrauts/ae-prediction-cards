import { useState } from "react";
import styled from "styled-components";
import { useWallet } from "../common/aeternity/WalletProvider";
import { Button } from "../common/components/Button/Button";
import { Logo } from "../common/components/logo/Logo";
import { MidHeading } from "../common/components/text/Heading";
import { Text } from "../common/components/text/Text";
import { glassMixin } from "../common/mixins/glass";

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
`;

export const Navigation = () => {
  const wallet = useWallet();
  const [activeAccount, setActiveAccount] = useState<{ account?: string, balance?: string }>({});

  const handleConnect = async () => {
    await wallet.connect();
    const acc = await wallet.getActiveAccount();
    console.log(acc);
    setActiveAccount(acc);
  };

  return (
    <StyledWrapper>
      <Logo />
      <MidHeading>PredictionCards</MidHeading>
      <NavActions>
        {activeAccount?.account ? (
          <Text>{activeAccount.account}</Text>
        ) : (
          <Button onClick={handleConnect}>Connect Wallet</Button>
        )}
      </NavActions>
    </StyledWrapper>
  )
}