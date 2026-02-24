package coms309;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample Spring Boot Application.
 * 
 * @author Chris Lopez
 */

@SpringBootApplication
public class Application {
	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);


        System.out.println("========================================");
        System.out.println("    Workout Tracker API");
        System.out.println("========================================");
        System.out.println("Available Endpoints:");
        System.out.println("    GET /workouts");
        System.out.println("    POST /workouts");
        System.out.println("    GET /workouts{name}");
        System.out.println("    GET /workouts/search?category=Cardio");
        System.out.println("    GET /workouts/analytics/total-energy");
        System.out.println("    GET /workouts/analytics/avg-time");
        System.out.println("    PUT /workouts/{name}");
        System.out.println("    DELETE /workouts/{name}");
        System.out.println("=========================================");
    }

}
