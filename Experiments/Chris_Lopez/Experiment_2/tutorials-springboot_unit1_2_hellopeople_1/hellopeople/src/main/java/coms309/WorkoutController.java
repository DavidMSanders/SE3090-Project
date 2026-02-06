package coms309;



import org.springframework.web.bind.annotation.*;

import java.util.ArrayList ;
import java.util.HashMap;
import java.util.List;

/**
 * Rest Controller for workout sesion management
 * Handles all CRUDL Operations for fitness workouts
 * @author Chris Lopez
 */

@RestController
public class WorkoutController {
    private final HashMap<String, Workout> sessionLog = new HashMap<>();

    @PostMapping("/workouts")
    public String logNewWorkout(@RequestBody Workout session) {
        sessionLog.put(session.getWorkoutName(), session);

        String confirmMsg = "Workout" + session.getWorkoutName()
                + "' logged! You burned " + session.getEnergyBurned()
                + "calories in " + session.getTimeSpentMinutes() + "minutes.";
        return confirmMsg;
    }

    // Read Specific Workout
    @GetMapping ("/workouts/{workoutName}")
    public Workout getWorkoutSession(@PathVariable String workoutName) {
        Workout session = sessionLog.get(workoutName);
        return session;
    }

    // Filter By Category
    @GetMapping("/workouts/search")
    public List<Workout> findByCategory(@RequestParam("category") String categoryName) {
        List<Workout> results = new ArrayList<>();

        for (Workout session : sessionLog.values()) {
            if (session.getCategory().equalsIgnoreCase(categoryName)) {
                results.add(session);
            }
        }
        return results;
    }

    // Custom Total Energy Burned
    @GetMapping("/workouts/analytics/total-energy")
    public String calculateTotalEnergy() {
        int totalEnergy = 0;

        for (Workout session : sessionLog.values()) {
            totalEnergy += session.getEnergyBurned();
        }

        return "Total energy burned  across all workouts: " + totalEnergy + " calories";
    }

    // Average Workout Time
    @GetMapping("/workouts/analytics/avg-time")
    public String getAverageTime() {
        if (sessionLog.isEmpty()) {
            return "No workouts logged yet";
        }

        int totalTime = 0;
        for (Workout session : sessionLog.values()) {
            totalTime += session.getTimeSpentMinutes();
        }

        int avgTime = totalTime / sessionLog.size();
        return "Average workout duration: " + avgTime + " minutes";
    }

    // Update
    @PutMapping("/workouts/{workoutName}")
    public Workout modifyWorkout(@PathVariable String workoutName, @RequestBody Workout updatedSession) {
        sessionLog.put(workoutName, updatedSession);
        return updatedSession;
    }

    // Delete
    @DeleteMapping("/workouts/{workoutName}")
    public HashMap<String, Workout> removeWorkout(@PathVariable String workoutName){
        sessionLog.remove(workoutName);
        return sessionLog;

}

}
