#### Kontrolletti API 
[![Build Status](https://travis-ci.org/zalando/kontrolletti.svg?branch=develop)](https://travis-ci.org/zalando/kontrolletti) [![Coverage Status](https://coveralls.io/repos/zalando/kontrolletti/badge.svg?branch=develop)](https://coveralls.io/r/zalando/kontrolletti?branch=develop)

The goal of this project is to retrieve and unify information from different SCM tools (Source Control Management) and provide an interface where this information can be requested.

Kontrolletti provides an REST API where projects for different





#### Todo:

- [X] Swagger Integration
- [x] Job Scheduler Integration
- [ ] Unit test examples
  - [ ] Include code coversage [scoverage](https://github.com/scoverage)
- [x] Webservice that returns simple json object
- [ ] Client for stash
- [ ] Client for github
- [ ] Client for github
  
[Swagger integration](https://github.com/swagger-api/swagger-core/tree/develop_scala-2.11/modules/swagger-play2)  

---

#### Working on this project?  
#### # **Do not push to master, but use a pull request!**  
Then you need to install:  
1. scala 2.11  
2. sbt >= 0.13  
3. activator  
4. IDE [scala-ide](http://scala-ide.org/) [IntelliJ](https://www.jetbrains.com/idea/features/scala.html)  
> ```sh
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
 






