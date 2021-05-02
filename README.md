<p align="center">
    <img alt="predictioncards" src="./images/logo.svg" />
</p>
<h1 align="center">
  Prediction Cards
</h1>

# About

Prediction Cards is a hybrid of NFT art and prediction markets.

Each outcome of a certain prediction is a unique Non Fungible Token (NFT). Instead of betting on an outcome, someone owns it (or to be more accurate, someone rents it). Concepts such as shares, bids, asks do not exist- even 'odds' are abstracted away, replaced by a 'rental price' for a specific interval (default: 1 day). At the end of the event, the longest owner of each outcome keeps it as owner (regardless of whether or not the outcome was correct). It thus becomes a collectable and can be traded or transferred to other addresses. Currently this is a quite simple solutions where we only allow predictions for future asset prices that can have two outcomes and thus two different NFTs (lowerOrEqual / higher). For each outcome a specific GIF image can be selected by the creator of the prediction.

_The original project where the idea comes from is https://realitycards.io/_

# How we built it

## Smart Contract

The Smart Contract was written in virtual pair programming by Michel (@mitch-lbw) and Marco (@marc0olo). We used our [aepp-sdk-java](https://github.com/kryptokrauts/aepp-sdk-java) in combination with our [contraect-maven-plugin](https://github.com/kryptokrauts/contraect-maven-plugin) that allows developers to generate Java classes for Smart Contracts written in Sophia. With these generated classes we were able to easily interact with the Smart Contract (deployment as well as _type-safe_ calls of functions). The cool thing about this is that we were able to write our contract tests in pure Java.

## Oracle-service & process-prediction-service

Michel and Marco also wrote the oracle-service and the process-prediction-service in Java. The oracle-service registers an oracle and makes sure that it never expires. It also polls in a certain interval whether a query has been requested via an _OracleQueryTx_. If an oracle query is present the oracle processes it by checking the price and responds with the respective result. The process-prediction-service periodically checks if a certain prediction has ended and performs the _OracleQueryTx_ to ask the oracle for the correct outcome. If the oracle responds in time this service will also perform a contract call to process the prediction outcome so that the Smart Contract can determine the winner NFT and the owners of the outcome NFT based on their renting time.

## AENS name with contract pointer

We claimed the name [predictioncards.chain](https://testnet.aenalytics.org/names/predictioncards.chain) and are using it to point to the address of our deployed contract.

## Frontend with connection to Superhero Wallet

The frontend was written in React by Jan-Patrick (@the-icarus) and makes use of the [aepp-sdk-js](https://github.com/aeternity/aepp-sdk-js). We allow the users to connect to the application using the Superhero Wallet extension so that they can easily create new predictions or rent a specific outcome NFT. We are using the contract pointer of predictioncards.chain to resolve the contract address for calling the contracts functions.

## NFT images

The GIF images that can be selected for the ourcome NFTs were created by Andreas and have been stored on IPFS using [Pinata](https://pinata.cloud/).

# Challenges we ran into

- Bugs in our contraect-maven-plugin that have been resolved during development
  - https://github.com/kryptokrauts/contraect-maven-plugin/pull/50
- Sophia development / functional programming
  - The REPL (https://repl.aeternity.io/) was extremly helpful to test certain code snippets on demand
- We wanted the oracle to respond with string value which is an `Int` and convert it into `Int`
  - Unfortunately in Lima this isn't possible
  - As "workaround" the oracle is now expected to respond with the values "higher" or "lower_or_equal" which are used to determine the winning outcome NFT
- Read-only dry-run calls using Superhero Wallet on testnet weren't be possible
  - We generally needed a default address that is being used to call the read-only entrypoints of a contract
- Testing time-based contract logic is currently not really possible
  - We added params to decrease time intervals for testing the logic

# Accomplishments that we're proud of

- We worked asynchronous on our tasks in a decentralized manner with almost no communication overhead
- We developed a fully working product that can be used on testnet
- We used our own tools to develop and test the Smart Contract

# What we learned

- We improved our functional programming skills

# What's next for Prediction Cards

- More decentralized oracles, e. g. a human crowd-oracle solution that allows to resolve generic outcomes
- Generic outcomes where the oracle resolves the winning outcome NFT id
  - Would allow to bet on any kind of outcome we can imagine
- Involvement of artists to provide unique and creative arts for the NFTs
