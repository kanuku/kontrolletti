### Kontrolletti Job

#### Todo's
- [ ] Swagger Integration
- [x] Job Scheduler Integration
- [ ] Unit test examples
- [x] Webservice that returns simple json object
  
[Swagger integration](https://github.com/swagger-api/swagger-core/tree/develop_scala-2.11/modules/swagger-play2)  



#### Working on this project?
Then you need to install:  
1. scala 2.11
2. sbt >= 0.13
1. activator  
1. IDE [scala-ide](http://scala-ide.org/) [IntelliJ](https://www.jetbrains.com/idea/features/scala.html)  

####### How to import this project with scala-ide (eclipse) ?
```sh
# Will generate eclipse project files for this project
sbt eclipse
```


#### Running/Compiling this project
```sh
# Will compile, test and run the application
activator run
```
[Navigate to](http://localhost:9000/v1/repositories)

```sh
# Will run the application a and automatically 
activator ~run
```

```sh
activator docker:publishLocal

# After running this command, check for the image with
docker images
```

```sh
# The image needs to be published in your local/remote registry.
docker run -d -p 8008:9000
```  
[Navigate to](http://localhost:9000/v1/repositories)







