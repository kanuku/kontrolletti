#### Kontrolletti       [![swagger-editor](https://img.shields.io/badge/swagger-editor-brightgreen.svg)](http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/zalando/kontrolletti/develop/kontrolletti.yaml#/)
[![Build Status](https://travis-ci.org/zalando/kontrolletti.svg?branch=develop)](https://travis-ci.org/zalando/kontrolletti) [![Coverage Status](https://coveralls.io/repos/zalando/kontrolletti/badge.svg?branch=develop)](https://coveralls.io/r/zalando/kontrolletti?branch=develop) [![codecov.io](http://codecov.io/github/zalando/kontrolletti/coverage.svg?branch=develop)](http://codecov.io/github/zalando/kontrolletti?branch=develop) [![Codacy Badge](https://www.codacy.com/project/badge/c56048c9306d4fda9881577ae38b3beb)](https://www.codacy.com/app/benibadboy/kontrolletti)
A service that aggregates and stores information from different Source Control Management for historical and auditing purposes and serves this information through a REST API in a unified model.

The goal of Kontrolletti is to solve a auditing compliance problem: validating each commit in every deployed application owns by an organization.
Kontrolletti archives the goal by
* fetching application information, especially repository information from [Kio](https://github.com/zalando-stups/kio)
* scanning all repositories obtained above and reading all commits
* validating commits with predefined rules, persisting into database
* expose the information via [HTTP API](http://editor.swagger.io/#/?import=https://raw.githubusercontent.com/zalando/kontrolletti/develop/kontrolletti.yaml)

Currently it supports git repositories hosted on github.com, self-hosted Github Enterprise and also self-hosted Stash.

Producing false negative results is very annoying for other developers within the organization. We are striving to apply best practices in purely functional programming to keep the correctness as high as possible. For this we definitely need your help. If you are also interested in purely functional programming in scala, please have a look at [TODO](#todo) and [Contributing](#contributing) sections.

![codecov.io](http://codecov.io/github/zalando/kontrolletti/branch.svg?branch=develop)


===
#### Development
You need scala(2.11) and sbt(0.13.7) installed to build or develop.
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

===
#### Running unit tests
```sh
## To run unit tests
sbt clean test

## To generate coverage reports
sbt clean coverage test && sbt coverageReport
```
===
#### Creating a docker image
```sh
sbt clean docker:publishLocal
```
===
#### Running the docker-image
```sh
docker run -d -p 8080:9000 --name kontrolletti \
    -e AUTH_TOKEN_GITHUB="f12gals1/22am87h9j32" \
    -e AUTH_TOKEN_STASH="f12gals1/22am87h9j32" \
    pierone.stups.zalan.do/cd/kontrolletti:1.0-SNAPSHOT
```


===
#### Contributing

##### Contributor Guidelines
* Purity is a goal. We need to be honest about what we code. Please do not use exceptions as control flow. Please make side-effects explicit using proper types.
* Code should be well tested with _meaningful tests_. We prefer property-based testing.
* Every commit message must contain relevant github issue.

##### Contributors
* [Fernando Benjamin](https://github.com/kanuku)
* [João Santos](https://github.com/jmcs)
* [Lothar Schulz](https://github.com/lotharschulz)
* [Tao Yang](https://github.com/taojang)


===
#### TODO
* Replace manually written test samples with property-based tests
* Be explicit about side-effects, remove exception throwing
* Allow user-defined rules for validating commits
* Replace usage of ```scala.concurrent.Future```, which means
  * moving away from Play! framework
  * moving away from Slick
  * moving away from Akka


===
#### License


Copyright © 2015-2016 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
