
[![Build Status](https://travis-ci.org/zalando/kontrolletti.svg?branch=develop)](https://travis-ci.org/zalando/kontrolletti) [![Coverage Status](https://coveralls.io/repos/zalando/kontrolletti/badge.svg?branch=develop)](https://coveralls.io/r/zalando/kontrolletti?branch=develop)
***
#### Kontrolletti API  
Aggregates and unifies information from different Source Control Management for history and auditing purposes.  
Provides a REST API where this information can be accessed.

***
#### Development
You need to install:  
1. scala 2.11  
2. sbt >= 0.13  
3. activator  
4. IDE [scala-ide](http://scala-ide.org/) [IntelliJ](https://www.jetbrains.com/idea/features/scala.html)  
```sh
# Will generate eclipse project files for this project
sbt eclipse
```

---

#### Running/Compiling this project
```sh
# Will compile, test and run the application
activator run
```
 [Navigate to](http://localhost:9000/repositories)

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
 






