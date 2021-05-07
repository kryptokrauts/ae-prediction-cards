# Process Prediction Service

A Spring Boot project that runs schedulers which periodically check
if there are predictions in the state `CLOSED` and trigger the oracle defined in the contract
to resolve the outcome of the prediction.
Additionally the service can also check and extend the name `predictioncards.chain` if required.

## Properties
For proper running an `application.properties` file is mandatory which contains the following properties:

```
base_url= // protocol-prefixed base url
compiler_url= // protocol-prefixed url to compiler
network= // network to use, LOCAL_LIMA_NETWORK / TESTNET
target_vm=FATE
vm_version=5
abi_version=3
scheduler.ask_oracle.delay= // scheduler delay to ask the oracle (ms)
scheduler.process_oracle_response.delay= // scheduler delay to process oracle responses (ms)
scheduler.extend_name.delay= // scheduler delay to extend the name (ms)
name.min_blocks_extension_trigger= // lowest difference between block height and expiration height of the name, if below it will be extended
name.extension_ttl= // relative ttl of the AENS name for registration & update
private_key= // private key for service
beneficiary_private_key= // private key for funding the addresses (local miner)
oracle_address= // address of the oracle to use
local_node= // use the service on local node (true/false)
local_user_addresses= // comma separated list of addresses to fund in local mode
```