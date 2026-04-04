package onetoone.Workout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * @author Matthew Smith
 */
@RestController
@RequestMapping("/api/v1/workout-plans")
public class WorkoutPlanController {

    @Autowired
    private WorkoutPlanRepository planRepo;
    @Autowired
    private WorkoutRepository workoutRepo;

    @PostMapping
    public WorkoutPlan createPlan(@RequestBody WorkoutPlan plan) {
        return planRepo.save(plan);
    }

    @GetMapping("/trainer/{trainerId}")
    public List<WorkoutPlan> getPlansByTrainer(@PathVariable Long trainerId) {
        return planRepo.findByTrainerId(trainerId);
    }

    @PostMapping("/{planId}/assign/user/{userId}")
    public ResponseEntity<?> assignPlanToUser(@PathVariable Long planId, @PathVariable Long userId,
            @RequestParam String date) {
        WorkoutPlan plan = planRepo.findById(planId).orElse(null);
        if (plan == null)
            return ResponseEntity.badRequest().body("Plan not found");

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate parsedDate = LocalDate.parse(date, pattern);

        Workout workout = new Workout();
        workout.setUserId(userId);
        workout.setWorkoutPlanId(planId);
        workout.setCreatedByUserId(plan.getTrainerId());
        workout.setWorkoutName(plan.getPlanName());
        workout.setWorkoutDescription(plan.getPlanDescription());
        workout.setScheduledDate(parsedDate);
        workout.setStatus("assigned");
        workout.setSourceType("assigned_from_plan");

        return ResponseEntity.ok(workoutRepo.save(workout));
    }
}
