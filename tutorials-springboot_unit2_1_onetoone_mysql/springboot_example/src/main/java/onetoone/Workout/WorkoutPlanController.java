package onetoone.Workout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matthew Smith
 */
@RestController
@RequestMapping("/api/v1/workout-plans")
public class WorkoutPlanController {

    @Autowired
    private WorkoutPlanRepository planRepo;

    // 1. POST /workout-plans
    @PostMapping
    public WorkoutPlan createPlan(@RequestBody WorkoutPlan plan) {
        return planRepo.save(plan);
    }

    // 2. GET /workout-plans/trainer/{trainerId}
    @GetMapping("/trainer/{trainerId}")
    public List<WorkoutPlan> getPlansByTrainer(@PathVariable Long trainerId) {
        return planRepo.findByTrainerId(trainerId);
    }

    // 3. GET /workout-plans/{workoutPlanId}
    @GetMapping("/{workoutPlanId}")
    public ResponseEntity<WorkoutPlan> getPlan(@PathVariable Long workoutPlanId) {
        return planRepo.findById(workoutPlanId).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. PUT /workout-plans/{workoutPlanId}
    @PutMapping("/{workoutPlanId}")
    public ResponseEntity<Map<String, Object>> updatePlan(@PathVariable Long workoutPlanId,
            @RequestBody WorkoutPlan incoming) {
        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);
        if (plan == null)
            return ResponseEntity.notFound().build();

        if (incoming.getTrainerId() != null)
            plan.setTrainerId(incoming.getTrainerId());
        if (incoming.getPlanName() != null)
            plan.setPlanName(incoming.getPlanName());
        if (incoming.getPlanDescription() != null)
            plan.setPlanDescription(incoming.getPlanDescription());

        planRepo.save(plan);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Workout plan updated");
        res.put("workoutPlanId", plan.getWorkoutPlanId());
        return ResponseEntity.ok(res);
    }

    // 5. DELETE /workout-plans/{workoutPlanId}
    @DeleteMapping("/{workoutPlanId}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long workoutPlanId) {
        if (planRepo.existsById(workoutPlanId)) {
            planRepo.deleteById(workoutPlanId);
            Map<String, String> res = new HashMap<>();
            res.put("message", "Workout plan deleted");
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.notFound().build();
    }
}
