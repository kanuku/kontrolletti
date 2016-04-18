#### Kontrolletti       [![swagger-editor](https://img.shields.io/badge/swagger-editor-brightgreen.svg)](http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/zalando/kontrolletti/develop/kontrolletti.yaml#/)
[![Build Status](https://travis-ci.org/zalando/kontrolletti.svg?branch=develop)](https://travis-ci.org/zalando/kontrolletti) [![Coverage Status](https://coveralls.io/repos/zalando/kontrolletti/badge.svg?branch=develop)](https://coveralls.io/r/zalando/kontrolletti?branch=develop) [![codecov.io](http://codecov.io/github/zalando/kontrolletti/coverage.svg?branch=develop)](http://codecov.io/github/zalando/kontrolletti?branch=develop) [![Codacy Badge](https://www.codacy.com/project/badge/c56048c9306d4fda9881577ae38b3beb)](https://www.codacy.com/app/benibadboy/kontrolletti)

===
**Kontroletti** is a service that aggregates and stores information from multiple source control management systems for historical and auditing purposes. It then serves this information through a REST API in a unified model to make auditing easier. Used with [Kio](https://github.com/zalando-stups/kio), Zalando's application registry, it is out-of-the-box. 

###Why Kontroletti
Kontrolletti aims to solve a significant auditing compliance problem: validating each commit in every deployed application owned by an organization. Validation of commits can often produce false-negative results — an annoying problem to developers and compliance teams alike. Kontroletti strives for accuracy — taking advantage of Scala's functional programming aspects (enabling composibility and eliminating a whole category of bugs) to do so.

How Kontroletti works:
- fetches application information, especially repository information from Kio
- scans all repositories obtained from fetching the application information, and reading all commits
- validates commits with predefined rules and persisting them into the database
- exposes the information via an [HTTP API](http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/zalando/kontrolletti/develop/kontrolletti.yaml)

Kontroletti supports Git repositories hosted on Github.com, GitHub Enterprise (self-hosted) and self-hosted Stash.

If you are also interested in pure functional programming in Scala, please look at our [TODO](#todo) and [Contributing](#contributing) sections.

![codecov.io](http://codecov.io/github/zalando/kontrolletti/branch.svg?branch=develop)

### Development
To build or develop with Kontroletti, first install:
- Scala(2.11) 
- sbt(0.13.7) 
- Kio (see below)


```sh
## Export the access-token of stash and github REST API's:
export AUTH_TOKEN_GITHUB="19j1923u4Jh866ahsWLS==aw$"
export AUTH_TOKEN_STASH="5HHAS87JS12KL@/899??=112SALNCM"

## Configure url for stash server in `conf/application.conf`:
client.stash.hosts=["stash-scm.myserver.com"]

## Start the service:
sbt run

## Navigate to  http://localhost:9000/swagger
```

###Installing Kio
Kio releases are pushed as Docker images in the [public registry](https://hub.docker.com/r/stups/kio/). You can run Kio by starting it with Docker:

    $ docker run -it stups/kio


#### Running Unit Tests
```sh
## To run unit tests
sbt clean test

## To generate coverage reports
sbt clean coverage test && sbt coverageReport
```
===
#### Creating a Docker Image
```sh
sbt clean docker:publishLocal
```
===
#### Running the Docker Image
```sh
docker run -d -p 8080:9000 --name kontrolletti \
    -e AUTH_TOKEN_GITHUB="f12gals1/22am87h9j32" \
    -e AUTH_TOKEN_STASH="f12gals1/22am87h9j32" \
    pierone.stups.zalan.do/cd/kontrolletti:1.0-SNAPSHOT
```

### Contributor Guidelines
- We take code quality seriously, and don't wish to deviate from our quality or functional programming goals. Please do not use exceptions as control flow. 
- Please make side-effects explicit using proper types.
- Test your code thoroughly  with _meaningful tests_. We prefer property-based testing.
- Please include a relevant GitHub issue with every commit message.

##### Thanks to Our Current/Past Contributors
[Fernando Benjamin](https://github.com/kanuku), [João Santos](https://github.com/jmcs), [Lothar Schulz](https://github.com/lotharschulz), [Tao Yang](https://github.com/taojang)


### TODO List
- Replace manually-written test samples with property-based tests
- Be explicit about side effects and remove exception throwing
- Allow user-defined rules for validating commits
- Replace usage of ```scala.concurrent.Future```, which means
    - moving away from Play! framework
    - moving away from Slick
    - moving away from Akka


#### License

Copyright © 2015-2016 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
