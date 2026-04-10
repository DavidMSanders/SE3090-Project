package onetoone.Workout;

import onetoone.Groups.GroupMembership;
import onetoone.Groups.GroupMembershipRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Workout plan controller
 */
@RestController
@RequestMapping("/api/v1/workout-plans")
public class WorkoutPlanController {

    @Autowired
    private WorkoutPlanRepository planRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GroupMembershipRepository membershipRepo;

    // 1. POST /workout-plans
    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody WorkoutPlan plan) {
        Map<String, Object> res = new HashMap<>();

        if (plan.getTrainerId() == null || plan.getGroupId() == null || plan.getPlanName() == null
                || plan.getPlanName().trim().isEmpty()) {
            res.put("message", "trainerId, groupId, and planName are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        User trainer = userRepo.findById(plan.getTrainerId().intValue()).orElse(null);
        if (trainer == null) {
            res.put("message", "Trainer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        Optional<GroupMembership> trainerMembership =
                membershipRepo.findByGroupIdAndUserId(plan.getGroupId().intValue(), trainer.getUserId());

        if (trainerMembership.isEmpty()) {
            res.put("message", "Trainer is not a member of this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        if (!trainerMembership.get().isWorkoutEnabled()) {
            res.put("message", "Trainer does not have permission to create workout plans in this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        WorkoutPlan saved = planRepo.save(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 2. GET /workout-plans/trainer/{trainerId}
    @GetMapping("/trainer/{trainerId}")
    public List<WorkoutPlan> getPlansByTrainer(@PathVariable Long trainerId) {
        return planRepo.findByTrainerId(trainerId);
    }

    // Optional but useful
    @GetMapping("/group/{groupId}")
    public List<WorkoutPlan> getPlansByGroup(@PathVariable Long groupId) {
        return planRepo.findByGroupId(groupId);
    }

    // 3. GET /workout-plans/{workoutPlanId}
    @GetMapping("/{workoutPlanId}")
    public ResponseEntity<WorkoutPlan> getPlan(@PathVariable Long workoutPlanId) {
        return planRepo.findById(workoutPlanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. PUT /workout-plans/{workoutPlanId}
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

        Long effectiveTrainerId = incoming.getTrainerId() != null ? incoming.getTrainerId() : plan.getTrainerId();
        Long effectiveGroupId = incoming.getGroupId() != null ? incoming.getGroupId() : plan.getGroupId();

        if (effectiveTrainerId == null || effectiveGroupId == null) {
            res.put("message", "Workout plan must have trainerId and groupId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        User trainer = userRepo.findById(effectiveTrainerId.intValue()).orElse(null);
        if (trainer == null) {
            res.put("message", "Trainer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        Optional<GroupMembership> trainerMembership =
                membershipRepo.findByGroupIdAndUserId(effectiveGroupId.intValue(), trainer.getUserId());

        if (trainerMembership.isEmpty()) {
            res.put("message", "Trainer is not a member of this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        if (!trainerMembership.get().isWorkoutEnabled()) {
            res.put("message", "Trainer does not have permission to update workout plans in this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        if (incoming.getTrainerId() != null) {
            plan.setTrainerId(incoming.getTrainerId());
        }
        if (incoming.getGroupId() != null) {
            plan.setGroupId(incoming.getGroupId());
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