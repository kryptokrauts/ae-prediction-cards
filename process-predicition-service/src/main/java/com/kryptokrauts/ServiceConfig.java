package com.kryptokrauts;

import com.kryptokrauts.aeternity.sdk.constants.Network;
import com.kryptokrauts.aeternity.sdk.constants.VirtualMachine;
import java.math.BigInteger;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Getter
@ApplicationScoped
public class ServiceConfig {

  @ConfigProperty(name = "base_url")
  protected String baseUrl;

  @ConfigProperty(name = "compiler_url")
  protected String compilerUrl;

  @ConfigProperty(name = "network")
  protected Network network;

  @ConfigProperty(name = "target_vm")
  protected VirtualMachine targetVM;

  @ConfigProperty(name = "abi_version")
  protected BigInteger abiVersion;

  @ConfigProperty(name = "vm_version")
  protected BigInteger vmVersion;

  @ConfigProperty(name = "num_trials_default", defaultValue = "60")
  protected int numTrialsDefault;

  @ConfigProperty(name = "name.extension_ttl")
  protected BigInteger nameExtensionTTL;

  @ConfigProperty(name = "name.min_blocks_extension_trigger")
  protected long nameMinBlocksExtensionTrigger;

  @ConfigProperty(name = "private_key")
  protected String privateKey;

  @ConfigProperty(name = "beneficiary_private_key")
  protected String beneficiaryPrivateKey;

  @ConfigProperty(name = "oracle_address")
  protected String oracleAddress;

  @ConfigProperty(name = "local_node")
  protected boolean localNode;

  @ConfigProperty(name = "local_user_addresses")
  protected List<String> localUserAddresses;
}
