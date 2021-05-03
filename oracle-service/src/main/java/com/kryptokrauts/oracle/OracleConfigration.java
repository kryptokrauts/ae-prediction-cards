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
  private String baseUrl;

  @ConfigProperty(name = "compiler_url")
  private String compilerUrl;

  @ConfigProperty(name = "network")
  private Network network;

  @ConfigProperty(name = "target_vm")
  private VirtualMachine targetVM;

  @ConfigProperty(name = "abi_version")
  private BigInteger abiVersion;

  @ConfigProperty(name = "vm_version")
  private BigInteger vmVersion;

  @ConfigProperty(name = "oracle_private_key")
  private String oraclePrivateKey;

  @ConfigProperty(name = "num_trials_default", defaultValue = "60")
  private int numTrialsDefault;

  @ConfigProperty(name = "beneficiary_private_key")
  private String beneficiaryPrivateKey;

  @ConfigProperty(name = "local_node")
  private boolean localNode;

  @ConfigProperty(name = "oracle.min_blocks_extension_trigger")
  private long oracleMinBlocksExtensionTrigger;

  @ConfigProperty(name = "oracle.query_fee")
  private BigInteger oracleQueryFee;

  @ConfigProperty(name = "oracle.initial_ttl")
  private BigInteger oracleInitialTTL;

  @ConfigProperty(name = "oracle.extension_ttl")
  private BigInteger oracleExtensionTTL;
}
