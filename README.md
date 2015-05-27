
#### Kontrolletti API   [![Build Status](https://travis-ci.org/zalando/kontrolletti.svg?branch=develop)](https://travis-ci.org/zalando/kontrolletti) [![Coverage Status](https://coveralls.io/repos/zalando/kontrolletti/badge.svg?branch=develop)](https://coveralls.io/r/zalando/kontrolletti?branch=develop)
Aggregates and unifies information from different Source Control Management for historical and auditing purposes.  
Provides a REST API where this information can be accessed.

***
#### Development
* You need scala(2.11) and sbt(0.13.6) installed to build or develop.  
```sh
# Export the access-token of stash and github REST API's:  
export AUTH_TOKEN_GITHUB="19j1923u4Jh866ahsWLS==aw$"
export AUTH_TOKEN_STASH="5HHAS87JS12KL@"/899??=112SALNCM"


# Configure url for stash server in `conf/application.conf`:
client.stash.hosts=["stash-scm.myserver.com"]


# Start the service:  
sbt run 
```
The service can be found here: http://localhost:9000/swagger

---

#### Unit tests
```sh
sbt clean test
```

#### Creating a docker image
```sh
sbt clean docker:publishLocal
```

#### Running the docker-image
```sh
docker run -d -p 8008:9000
```  



---
 






