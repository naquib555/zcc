# Zendesk Coding Challange

This is a spring boot application, used to complete the requirements of Zendesk Internship Coding Challenge. The functional requirements that are covered for this challange are:
- Connect Zendesk API
- Request all the tickets for your account
- Display them in a list
- Display individual ticket details
- Page through tickets when more than 25 tickets are returned

Non functional requirements covered:
- A README with installation and usage instruction ( You are reading this file now :smiley: )
- A very basic UI, browser based has been implemented using thymeleaf
- For single ticket view, only ticket subject, description, and status has been used
- Handled API unavailability (Showing human readable message for unavailability or any other errors)
- Basic Unit Test cases written in Junit


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
* Fullfill the requirements as per your need
* Go to the root directory of the application
* Option#1 (running the application)<br />
To the run the project immediately without building, a jar has been added in ```/target``` directory. So by using the following commands you can run the application.
```
        java -jar target/zcc.jar
```
* Option#2 (building the application and running: for this option you will require Maven 3.x)<br />
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

## About this service
This application will show all the tickets of a user account. Individual tickets can also be searched and viewed.

Once the application is running, to view the outcome of the application, follow the below steps:
- Open a browser and use this url ```http://localhost:8910/zcc/ticket``` or ```http://127.0.0.1:8910/zcc/ticket```
- It will show two option ```Show all tickets``` or ```Search a ticket```
- To view all the tickets click ```Show all tickets``` or use any of these links ```http://localhost:8910/zcc/ticket/showAll``` or ```http://127.0.0.1:8910/zcc/ticket/showAll```
- You can search for a ticket or click ```view``` in ```Show all tickets``` page for a ticket's detail description.

Thank you!
