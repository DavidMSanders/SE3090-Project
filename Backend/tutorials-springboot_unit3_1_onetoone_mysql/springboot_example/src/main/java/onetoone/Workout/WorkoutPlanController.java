package onetoone.Workout;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workout plan controller
 *
 * Spec endpoints:
 * 1. POST /workout-plans
 * 2. GET /workout-plans/trainer/{trainerId}
 * 3. GET /workout-plans/{workoutPlanId}
 * 4. PUT /workout-plans/{workoutPlanId}
 * 5. DELETE /workout-plans/{workoutPlanId}
 */
@RestController
@RequestMapping("/api/v1/workout-plans")
public class WorkoutPlanController {

    @Autowired
    private WorkoutPlanRepository planRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * 1. POST /workout-plans
     * Input:
     * { "trainerId": 3, "planName": "Push Day", "planDescription": "Chest and triceps focus" }
     */
    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody WorkoutPlan plan) {
        Map<String, Object> res = new HashMap<>();

        if (plan.getTrainerId() == null || plan.getPlanName() == null || plan.getPlanName().trim().isEmpty()) {
            res.put("message", "trainerId and planName are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        User trainer = userRepo.findById(plan.getTrainerId().intValue()).orElse(null);
        if (trainer == null) {
            res.put("message", "Trainer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        WorkoutPlan saved = planRepo.save(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 2. GET /workout-plans/trainer/{trainerId}
     */
    @GetMapping("/trainer/{trainerId}")
    public List<WorkoutPlan> getPlansByTrainer(@PathVariable Long trainerId) {
        return planRepo.findByTrainerId(trainerId);
    }

    /**
     * 3. GET /workout-plans/{workoutPlanId}
     */
    @GetMapping("/{workoutPlanId}")
    public ResponseEntity<WorkoutPlan> getPlan(@PathVariable Long workoutPlanId) {
        return planRepo.findById(workoutPlanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 4. PUT /workout-plans/{workoutPlanId}
     * Input:
     * { "trainerId": 3, "planName": "Push Day Updated", "planDescription": "Chest, shoulders, and triceps" }
     */
    @PutMapping("/{workoutPlanId}")
    public ResponseEntity<Map<String, Object>> updatePlan(
            @PathVariable Long workoutPlanId,
            @RequestBody WorkoutPlan incoming) {

        Map<String, Object> res = new HashMap<>();

        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);
        if (plan == null) {
            res.put("message", "Workout plan not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        if (incoming.getTrainerId() != null) {
            User trainer = userRepo.findById(incoming.getTrainerId().intValue()).orElse(null);
            if (trainer == null) {
                res.put("message", "Trainer not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }
            plan.setTrainerId(incoming.getTrainerId());
        }

        if (incoming.getPlanName() != null) {
            plan.setPlanName(incoming.getPlanName());
        }

        if (incoming.getPlanDescription() != null) {
            plan.setPlanDescription(incoming.getPlanDescription());
        }

        planRepo.save(plan);
        res.put("message", "Workout plan updated");
        res.put("workoutPlanId", plan.getWorkoutPlanId());
        return ResponseEntity.ok(res);
    }

    /**
     * 5. DELETE /workout-plans/{workoutPlanId}
     */
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