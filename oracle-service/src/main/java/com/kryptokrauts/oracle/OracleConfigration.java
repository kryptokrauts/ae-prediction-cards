package com.kryptokrauts.oracle;

import java.math.BigInteger;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.kryptokrauts.aeternity.sdk.constants.Network;
import com.kryptokrauts.aeternity.sdk.constants.VirtualMachine;
import lombok.Getter;

@ApplicationScoped
@Getter
public class OracleConfigration {

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

  @ConfigProperty(name = "oracle_private_key")
  protected String oraclePrivateKey;

  @ConfigProperty(name = "num_trials_default", defaultValue = "60")
  protected int numTrialsDefault;

  @ConfigProperty(name = "beneficiary_private_key")
  protected String beneficiaryPrivateKey;

  @ConfigProperty(name = "local_node")
  protected boolean localNode;

  @ConfigProperty(name = "oracle.min_blocks_extension_trigger")
  protected long oracleMinBlocksExtensionTrigger;

  @ConfigProperty(name = "oracle.query_fee")
  protected BigInteger oracleQueryFee;

  @ConfigProperty(name = "oracle.initial_ttl")
  protected BigInteger oracleInitialTTL;

  @ConfigProperty(name = "oracle.extension_ttl")
  protected BigInteger oracleExtensionTTL;
}
