package edu.gatech.cs6310.project7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * <p>This class starts the application as a SpringBoot application. See
 * <a href="https://projects.spring.io/spring-boot/">SpringBoot Documentation</a> for more information. The main
 * functionality that SpringBoot provides is a fast and easy framework for dependency injection and HTTP request
 * routing.</p>
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // This is how you start a SpringBoot application. This method is called automatically by Tomcat.
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }
}
