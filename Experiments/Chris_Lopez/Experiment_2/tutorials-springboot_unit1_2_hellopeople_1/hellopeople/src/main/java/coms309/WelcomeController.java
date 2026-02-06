package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple Hello World Controller to display the string returned
 *
 * @author Chri Lopez
 */

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Welcome Tracker System!" + "This API helps log and analyze the fitness sessions. " +
                " Available features: workout logging, cateogry filtering, " + "calorie tracking, and workout anayltics" +
                "Start by visiting /workouts to see all logged session.";
    }
}
