# Oracle Service

This project uses Quarkus to create a scheduler which periodically checks for oracle requests and answers them. Additional the oracle periodically checks if an extension is necessary. For proper running an .env file is mandatory which contains the following properties

```
oracle_private_key= // private key for oracle
beneficiary_private_key= // private key for funding the oracle (local)
base_url= // protocol-prefixed base url
compiler_url= // protocol-prefixed url to compiler
network= // network to use, DEVNET (local) / TESTNET
target_vm=FATE
vm_version=5
abi_version=3
scheduler.oracle_query_interval= // scheduler interval within the oracle service checks for new queries, i.e. 30s
scheduler.oracle_expired_interval= // scheduler interval within the oracle service checks if the oracle expires and needs to be extended, i.e. 30s>
local_node=<use the service on local node true/false
oracle.min_blocks_extension_trigger= // lowest difference between block height and expiration height of the oracle, if below, oracle will be extended i.e. 10
oracle.query_fee= // oracle query fee i.e 100
oracle.initial_ttl= // oracle initial ttl (relative), i.e 5000
oracle.extension_ttl= // oracle extension ttl (relative) i.e, 1000 >

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
