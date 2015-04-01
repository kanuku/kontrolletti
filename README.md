### Kontrolletti Job

##### Todo's
- [ ] Swagger Integration
- [ ] Job Scheduler Integration
- [ ] Unit test examples
- [x] Webservice that returns simple json object
  
[Swagger integration](https://github.com/swagger-api/swagger-core/tree/develop_scala-2.11/modules/swagger-play2)  



##### Working on this project?
You will need the next tools to work on this project:  
1. Install scala 2.11  
1. Install activator  
1. Install an IDE [scala-ide](http://scala-ide.org/) [IntelliJ](https://www.jetbrains.com/idea/features/scala.html)  




##### Running the application
```sh
# Will compile, test and run the application
activator run
```
[Navigate to](http://localhost:9000/v1/repositories)

##### Running in development mode
```sh
# Will run the application a and automatically 
activator ~run
```

##### Create docker image in local registry

```sh
activator docker:publishLocal

# After running this command, check for the image with
docker images
```
 
##### Run the docker image  

```sh
# The image needs to be published in your local/remote registry.
docker run -d -p 8008:9000
```  
[Navigate to](http://localhost:9000/v1/repositories)







