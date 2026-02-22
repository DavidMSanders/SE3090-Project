package coms309;

//Added GetMap
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * Rest Controller for Fitness Tracking API
 * 4 endpoints
 * @author Chris Lopez
 */
@RestController
@RequestMapping("/fitness")
public class WelcomeController {


    private String userName = "Juan Martinez";
    private double userHeight = 68.5;
    private double userWeight = 172.0;
    private int stepGoal = 10000;
    private int todaySteps = 6264;

    /**
     * Endpoint 1 : Get user fitness profile
     * Returns user's basic fitness information that includes BMI
     * @return Map containing user profile data JSON
     */
    @GetMapping("/profile")
    public Map<String, Object> getUserProfile() {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", userName);
        profileData.put("height", userHeight);
        profileData.put("weight", userWeight);
        profileData.put("bmi", calculateBMI(userWeight, userHeight));
        profileData.put("bmiCategory", getBMICategory(calculateBMI(userWeight, userHeight)));
        return profileData;
    }

    /**
     * Endpoint 2 Get Daily step progress
     * Shows the current goal, progress percentage, and steps
     *
     * @return Map with step tracking information
     */

    @GetMapping("/steps")
    public Map<String, Object> getStepProgress(){
        Map<String, Object> stepInfo = new HashMap<>();
        stepInfo.put("dailyGoal", stepGoal);
        stepInfo.put("currentSteps", todaySteps);
        stepInfo.put("goalAchieved", todaySteps >= stepGoal);
        stepInfo.put("stepsRemaining", Math.max(0, stepGoal - todaySteps));
        stepInfo.put("percentComplete", (todaySteps * 100.0) / stepGoal);
        return stepInfo;
    }

    /**
     * Endpoint 3: Add Steps to daily count (Post request)
     * @param stepsToAdd AddNumber to steps to add
     *
     * @return Confirmation message w updated total
     */
    @PostMapping("/steps/add")
    public String addSteps(@RequestParam int stepsToAdd) {
        if(stepsToAdd < 0){
            return "Error: Steps cannot be negative!";
        }
        todaySteps += stepsToAdd;

        boolean isGoalReached = todaySteps >= stepGoal;
        return  String.format("Steps added: %d%n Total steps today: %d%nDaily goal: %d%nGoal reached: %s", stepsToAdd, todaySteps, stepGoal, isGoalReached ? "Yes!" : "Not yet, keep going");
    }
    /**
     * Endpoint 4: Update user weight (POST request)
     * Updates weight and automatically recalculate BMI
     *
     */

    @PostMapping("/weight")
    public Map<String, Object> updateWeight(@RequestParam double newWeight) {
        if (newWeight <= 0 || newWeight > 500) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid weight. Must be between 1 and 500 pounds. ");
            return errorResponse;

        }

        double previousWeight = this.userWeight;
        this.userWeight = newWeight;

        double updatedBMI = calculateBMI(userWeight, userHeight);
        double weightDifference = newWeight - previousWeight;

        Map<String, Object> updateResult = new HashMap<>();
        updateResult.put("previousWeight", previousWeight);
        updateResult.put("currentWeight", newWeight);
        updateResult.put("weightChange", weightDifference);
        updateResult.put("newBMI", updatedBMI);
        updateResult.put("bmiCategory", getBMICategory(updatedBMI));
        return updateResult;
    }

    /**
     * Helper Method: Calculate BMI (Body Mass Index)
     * Formula: (weight in pounds / (height in inches) ^ 2) * 703
     * @param weightInPounds
     * @param heightInches
     * @return
     */

    private double calculateBMI(double weightInPounds, double heightInches){
        double bmiValue = (weightInPounds / (heightInches * heightInches)) * 703;
        return Math.round(bmiValue * 100.0) / 100.0;
    }

    /**
     */

    private String getBMICategory(double bmiValue){
        if (bmiValue < 18.5)
            return "Underweight";
        else if (bmiValue < 25)
            return "Normal weight";
        else if (bmiValue < 30)
            return "Overweight";
        else return
            "Obese";
    }
}
