# Process Prediction Service

This project uses Quarkus to create a scheduler which periodically checks
if there are predictions in the state `CLOSED` and triggers the oracle defined in the contract
to resolve the outcome of the prediction.
Additionally the service checks and extends the name `predictioncards.chain` if required.
For proper running an `.env` file is mandatory which contains the following properties

```
base_url= // protocol-prefixed base url
compiler_url= // protocol-prefixed url to compiler
network= // network to use, LOCAL_LIMA_NETWORK / TESTNET
target_vm=FATE
vm_version=5
abi_version=3
scheduler.extend_name_interval= // scheduler interval to extend the name, e.g. 60s
scheduler.process_prediction_interval= // scheduler interval to process predictions
name.min_blocks_extension_trigger= // lowest difference between block height and expiration height of the name, if below it will be extended
name.extension_ttl= // relative ttl of the AENS name for registration & update
private_key= // private key for service
beneficiary_private_key= // private key for funding the addresses (local miner)
oracle_address= // address of the oracle to use
local_node= // use the service on local node (true/false)
local_user_addresses= // comma separated list of addresses to fund in local mode
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`
