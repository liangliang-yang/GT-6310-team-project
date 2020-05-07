Project 7 Web Application
====== 

This project uses the Spring Boot framework to run a web application.

## Getting Started
To run this application locally, you must install the following:
1. [Maven](https://maven.apache.org/)
2. [Tomcat 8](http://tomcat.apache.org/tomcat-8.0-doc/setup.html)

## Running the application

You can run the application from the Maven command line or by packaging it as a WAR and deploying it to Tomcat.

### From Maven

Go to the project directory and execute the following command.
```
mvn spring-boot:run
```

### Standalone JAR
1. Package the file as a JAR. From the project home directory:
    ```
    mvn clean package
    ```
2. This will create a jar file in $PROJECT_HOME/target/project7-1.0-SNAPSHOT.jar. 
3. Move to the target folder
    ```
    cd $PROJECT_HOME/target/
    ```
4. Execute the jar
    ```
    java -jar project7-1.0-SNAPSHOT.jar
    ```
5. Open a browser and go to the webpage: http://localhost:8080