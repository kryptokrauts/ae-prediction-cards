package com.kryptokrauts;

import com.kryptokrauts.aeternity.sdk.constants.Network;
import com.kryptokrauts.aeternity.sdk.constants.VirtualMachine;
import java.math.BigInteger;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServiceConfig {

  @Value("${base_url}")
  protected String baseUrl;

  @Value("${compiler_url}")
  protected String compilerUrl;

  @Value("${network}")
  protected Network network;

  @Value("${target_vm}")
  protected VirtualMachine targetVM;

  @Value("${abi_version}")
  protected BigInteger abiVersion;

  @Value("${vm_version}")
  protected BigInteger vmVersion;

  @Value("${num_trials_default:60}")
  protected int numTrialsDefault;

  @Value("${name.extension_ttl}")
  protected BigInteger nameExtensionTTL;

  @Value("${name.min_blocks_extension_trigger}")
  protected long nameMinBlocksExtensionTrigger;

  @Value("${private_key}")
  protected String privateKey;

  @Value("${beneficiary_private_key}")
  protected String beneficiaryPrivateKey;

  @Value("${oracle_address}")
  protected String oracleAddress;

  @Value("${local_node}")
  protected boolean localNode;

  @Value("${local_user_addresses}")
  protected List<String> localUserAddresses;
}
