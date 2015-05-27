
[![Build Status](https://travis-ci.org/zalando/kontrolletti.svg?branch=develop)](https://travis-ci.org/zalando/kontrolletti) [![Coverage Status](https://coveralls.io/repos/zalando/kontrolletti/badge.svg?branch=develop)](https://coveralls.io/r/zalando/kontrolletti?branch=develop)
***
#### Kontrolletti API  
Aggregates and unifies information from different Source Control Management for historical and auditing purposes.  
Provides a REST API where this information can be accessed.

***
#### Development
1. You need scala(2.11) and sbt(0.13.6) installed to build or develop.  
1. Generate access tokens for stash and github REST API's and export them as environment variables:  
```sh
export AUTH_TOKEN_GITHUB="19j1923u4Jh866ahsWLS==aw$"
export AUTH_TOKEN_STASH="5HHAS87JS12KL@"/899??=112SALNCM"
```

To start the service:  
```sh 
sbt run 
```
The service can be found here: http://localhost:9000/swagger

---

#### Testing
```sh
sbt clean coverage test
```


```sh
# Will run the application a and automatically 
activator ~run
```

```sh
activator docker:publishLocal

After running this command, check for the image with
docker images
```
  
```sh
# The image needs to be published in your local/remote registry.
docker run -d -p 8008:9000
```  
 [Navigate to](http://localhost:9000/repositories)


---
 






