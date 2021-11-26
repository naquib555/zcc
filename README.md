# Zendesk Coding Challange

## Requirements

For building the application you need:
- JDK 1.8 +
- Maven 3.x

For running the application you need:
- Java 8 or greater

## How to Run the application

This application is packaged as a jar and it contains embedded tomcat. So no additional server is required to be installed.

Steps:
* Clone this repository
* Fullfill the requirements as per you need
* Go to the root directory of the application
* Option#1 (running the application)<br />
To the run the project immediately without building, a jar has been added in ```/target``` directory. So by using the following commands you can run the application.
```
        java -jar target/zcc.jar
```
* Option#2 (building the application and running: for this option you will require Maven 3.x)
If you want to build the project and run the application with ```java -jar``` command, then execute the following commands:
```
        mvn clean package
        java -jar target/zcc.jar
        or
        mvn spring-boot:run
```
Once the application is started, in the console it will show:
```
2021-11-26 18:05:23.286  INFO 4953 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8910 (http) with context path '/zcc'
2021-11-26 18:05:23.301  INFO 4953 --- [  restartedMain] com.zendesk.zcc.ZccApplication           : Started ZccApplication in 2.369 seconds (JVM running for 2.816)
```
