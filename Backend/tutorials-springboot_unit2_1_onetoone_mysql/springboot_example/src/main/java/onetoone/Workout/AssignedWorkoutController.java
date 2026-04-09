package onetoone.Workout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import onetoone.Users.User;
import onetoone.Users.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assigned-workouts")
public class AssignedWorkoutController {

    @Autowired
    private AssignedWorkoutRepository assignedRepo;
    @Autowired
    private WorkoutRepository workoutRepo;
    @Autowired
    private WorkoutPlanRepository planRepo;
    @Autowired
    private UserRepository userRepo;

    // 6. POST /assigned-workouts
    @PostMapping
    public ResponseEntity<Map<String, Object>> assignWorkout(@RequestBody Map<String, Object> req) {
        Long trainerId = Long.valueOf(req.get("trainerId").toString());
        Long workoutPlanId = Long.valueOf(req.get("workoutPlanId").toString());
        Long clientId = Long.valueOf(req.get("clientId").toString());
        Long groupId = req.get("groupId") != null ? Long.valueOf(req.get("groupId").toString()) : null;
        String scheduledDate = req.get("scheduledDate").toString();

        User trainer = userRepo.findById(trainerId.intValue()).orElse(null);
        User client = userRepo.findById(clientId.intValue()).orElse(null);
        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);

        if (trainer == null || client == null || plan == null) {
            return ResponseEntity.badRequest().build();
        }

        LocalDate date = LocalDate.parse(scheduledDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Workout workout = new Workout();
        workout.setUserId(clientId);
        workout.setGroupId(groupId);
        workout.setWorkoutPlanId(workoutPlanId);
        workout.setCreatedByUserId(trainerId);
        workout.setWorkoutName(plan.getPlanName());
        workout.setWorkoutDescription(plan.getPlanDescription());
        workout.setScheduledDate(date);
        workout.setStatus("assigned");
        workout.setSourceType("assigned_from_plan");

        Workout savedWorkout = workoutRepo.save(workout);

        AssignedWorkout assigned = new AssignedWorkout(savedWorkout, client, trainer);
        AssignedWorkout savedAssigned = assignedRepo.save(assigned);

        Map<String, Object> res = new HashMap<>();
        res.put("assignedWorkoutId", savedAssigned.getId());
        res.put("workoutId", savedWorkout.getWorkoutId());
        res.put("message", "Workout assigned successfully");
        return ResponseEntity.ok(res);
    }

    // 7. GET /assigned-workouts/client/{clientId}
    @GetMapping("/client/{clientId}")
    public List<Map<String, Object>> getByClient(@PathVariable Long clientId) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<AssignedWorkout> all = assignedRepo.findAll();
        for (AssignedWorkout aw : all) {
            if (aw.getClient().getUserId() == clientId.intValue()) {
                Map<String, Object> map = new HashMap<>();
                map.put("assignedWorkoutId", aw.getId());
                map.put("workoutPlanId", aw.getWorkout().getWorkoutPlanId());
                map.put("clientId", aw.getClient().getUserId());
                map.put("groupId", aw.getWorkout().getGroupId());
                map.put("scheduledDate",
                        aw.getWorkout().getScheduledDate() != null ? aw.getWorkout().getScheduledDate().toString()
                                : null);
                map.put("status", aw.getWorkout().getStatus());
                map.put("createdAt", aw.getWorkout().getCreatedAt().toString());
                results.add(map);
            }
        }
        return results;
    }

    // 8. GET /assigned-workouts/plan/{workoutPlanId}
    @GetMapping("/plan/{workoutPlanId}")
    public List<Map<String, Object>> getByPlan(@PathVariable Long workoutPlanId) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<AssignedWorkout> all = assignedRepo.findAll();
        for (AssignedWorkout aw : all) {
            if (workoutPlanId.equals(aw.getWorkout().getWorkoutPlanId())) {
                Map<String, Object> map = new HashMap<>();
                map.put("assignedWorkoutId", aw.getId());
                map.put("clientId", aw.getClient().getUserId());
                map.put("groupId", aw.getWorkout().getGroupId());
                map.put("scheduledDate",
                        aw.getWorkout().getScheduledDate() != null ? aw.getWorkout().getScheduledDate().toString()
                                : null);
                map.put("status", aw.getWorkout().getStatus());
                results.add(map);
            }
        }
        return results;
    }

    // 9. PUT /assigned-workouts/{assignedWorkoutId}/status
    @PutMapping("/{assignedWorkoutId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long assignedWorkoutId,
            @RequestBody Map<String, String> req) {
        AssignedWorkout aw = assignedRepo.findById(assignedWorkoutId).orElse(null);
        if (aw == null)
            return ResponseEntity.notFound().build();

        String newStatus = req.get("status");
        aw.getWorkout().setStatus(newStatus);
        workoutRepo.save(aw.getWorkout());

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Assigned workout status updated");
        res.put("assignedWorkoutId", aw.getId());
        res.put("status", newStatus);
        return ResponseEntity.ok(res);
    }

    // 10. DELETE /assigned-workouts/{assignedWorkoutId}
    @DeleteMapping("/{assignedWorkoutId}")
    public ResponseEntity<Map<String, String>> deleteAssigned(@PathVariable Long assignedWorkoutId) {
        AssignedWorkout aw = assignedRepo.findById(assignedWorkoutId).orElse(null);
        if (aw != null) {
            Long workoutId = aw.getWorkout().getWorkoutId();
            assignedRepo.delete(aw);
            workoutRepo.deleteById(workoutId);
            Map<String, String> res = new HashMap<>();
            res.put("message", "Assigned workout deleted");
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.notFound().build();
    }
}
