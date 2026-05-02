package onetoone.Workout;

import onetoone.Groups.GroupMembership;
import onetoone.Groups.GroupMembershipRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Assigned workout controller
 *
 * Spec endpoints:
 * 6. POST /assigned-workouts
 * 7. GET /assigned-workouts/client/{clientId}
 * 8. GET /assigned-workouts/plan/{workoutPlanId}
 * 9. PUT /assigned-workouts/{assignedWorkoutId}/status
 * 10. DELETE /assigned-workouts/{assignedWorkoutId}
 */
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

    @Autowired
    private GroupMembershipRepository membershipRepo;

    /**
     * 6. POST /assigned-workouts
     * Input:
     * { "trainerId": 3, "workoutPlanId": 10, "clientId": 7, "groupId": 5, "scheduledDate": "2026-04-10" }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> assignWorkout(@RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();

        Long trainerId = req.get("trainerId") != null
                ? Long.valueOf(req.get("trainerId").toString()) : null;
        Long workoutPlanId = req.get("workoutPlanId") != null
                ? Long.valueOf(req.get("workoutPlanId").toString()) : null;
        Long clientId = req.get("clientId") != null
                ? Long.valueOf(req.get("clientId").toString()) : null;
        Long groupId = req.get("groupId") != null
                ? Long.valueOf(req.get("groupId").toString()) : null;
        String scheduledDate = req.get("scheduledDate") != null
                ? req.get("scheduledDate").toString() : null;

        if (trainerId == null || workoutPlanId == null || clientId == null || groupId == null || scheduledDate == null) {
            res.put("message", "trainerId, workoutPlanId, clientId, groupId, and scheduledDate are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);
        if (plan == null) {
            res.put("message", "Workout plan not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        User trainer = userRepo.findById(trainerId.intValue()).orElse(null);
        if (trainer == null) {
            res.put("message", "Trainer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        User client = userRepo.findById(clientId.intValue()).orElse(null);
        if (client == null) {
            res.put("message", "Client not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        Optional<GroupMembership> trainerMembershipQuery =
                membershipRepo.findByGroupIdAndUserId(groupId.intValue(), trainer.getUserId());

        if (trainerMembershipQuery.isEmpty()) {
            res.put("message", "Trainer is not a member of this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        if (!trainerMembershipQuery.get().isWorkoutEnabled()) {
            res.put("message", "Trainer does not have permission to assign workouts in this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        Optional<GroupMembership> clientMembershipQuery =
                membershipRepo.findByGroupIdAndUserId(groupId.intValue(), client.getUserId());

        if (clientMembershipQuery.isEmpty()) {
            res.put("message", "Client is not a member of this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(scheduledDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            res.put("message", "scheduledDate must be in yyyy-MM-dd format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

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

        res.put("assignedWorkoutId", savedAssigned.getId());
        res.put("workoutId", savedWorkout.getWorkoutId());
        res.put("message", "Workout assigned successfully");
        return ResponseEntity.ok(res);
    }

    /**
     * 7. GET /assigned-workouts/client/{clientId}
     * Optional query params: groupId, status
     */
    @GetMapping("/client/{clientId}")
    public List<Map<String, Object>> getByClient(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String status) {

        List<Map<String, Object>> results = new ArrayList<>();
        List<AssignedWorkout> all = assignedRepo.findAll();

        for (AssignedWorkout aw : all) {
            if (aw.getClient() == null || aw.getClient().getUserId() != clientId.intValue()) {
                continue;
            }

            Workout workout = aw.getWorkout();
            if (workout == null) {
                continue;
            }

            if (groupId != null && !groupId.equals(workout.getGroupId())) {
                continue;
            }

            if (status != null && (workout.getStatus() == null || !status.equalsIgnoreCase(workout.getStatus()))) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("assignedWorkoutId", aw.getId());
            map.put("workoutPlanId", workout.getWorkoutPlanId());
            map.put("clientId", aw.getClient().getUserId());
            map.put("groupId", workout.getGroupId());
            map.put("scheduledDate", workout.getScheduledDate() != null ? workout.getScheduledDate().toString() : null);
            map.put("status", workout.getStatus());
            map.put("createdAt", workout.getCreatedAt() != null ? workout.getCreatedAt().toString() : null);
            results.add(map);
        }

        return results;
    }

    /**
     * 8. GET /assigned-workouts/plan/{workoutPlanId}
     */
    @GetMapping("/plan/{workoutPlanId}")
    public List<Map<String, Object>> getByPlan(@PathVariable Long workoutPlanId) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<AssignedWorkout> all = assignedRepo.findAll();

        for (AssignedWorkout aw : all) {
            Workout workout = aw.getWorkout();
            if (workout == null || !workoutPlanId.equals(workout.getWorkoutPlanId())) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("assignedWorkoutId", aw.getId());
            map.put("clientId", aw.getClient() != null ? aw.getClient().getUserId() : null);
            map.put("groupId", workout.getGroupId());
            map.put("scheduledDate", workout.getScheduledDate() != null ? workout.getScheduledDate().toString() : null);
            map.put("status", workout.getStatus());
            results.add(map);
        }

        return results;
    }

    /**
     * 9. PUT /assigned-workouts/{assignedWorkoutId}/status
     */
    @PutMapping("/{assignedWorkoutId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long assignedWorkoutId,
            @RequestBody Map<String, String> req) {

        AssignedWorkout aw = assignedRepo.findById(assignedWorkoutId).orElse(null);
        if (aw == null) {
            return ResponseEntity.notFound().build();
        }

        String newStatus = req.get("status");
        aw.getWorkout().setStatus(newStatus);
        workoutRepo.save(aw.getWorkout());

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Assigned workout status updated");
        res.put("assignedWorkoutId", aw.getId());
        res.put("status", newStatus);
        return ResponseEntity.ok(res);
    }

    /**
     * 10. DELETE /assigned-workouts/{assignedWorkoutId}
     */
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

    /**
     * GET /assigned-workouts/plan/{workoutPlanId}/summary
     */
    @GetMapping("/plan/{workoutPlanId}/summary")
    public ResponseEntity<Map<String, Object>> getPlanSummary(@PathVariable Long workoutPlanId) {
        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<AssignedWorkout> all = assignedRepo.findAll();
        Map<String, Object> summary = buildPlanSummary(plan, all);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /assigned-workouts/plan/{workoutPlanId}/summary/counts
     */
    @GetMapping("/plan/{workoutPlanId}/summary/counts")
    public ResponseEntity<Map<String, Object>> getPlanSummaryCounts(@PathVariable Long workoutPlanId) {
        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<AssignedWorkout> all = assignedRepo.findAll();
        Map<String, Object> summary = buildPlanSummary(plan, all);
        summary.remove("users");
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /assigned-workouts/plan/{workoutPlanId}/summary/users
     */
    @GetMapping("/plan/{workoutPlanId}/summary/users")
    public ResponseEntity<Map<String, Object>> getPlanSummaryUsers(@PathVariable Long workoutPlanId) {
        WorkoutPlan plan = planRepo.findById(workoutPlanId).orElse(null);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<AssignedWorkout> all = assignedRepo.findAll();
        Map<String, Object> summary = buildPlanSummary(plan, all);
        
        Map<String, Object> userOnly = new HashMap<>();
        userOnly.put("planId", summary.get("planId"));
        userOnly.put("planName", summary.get("planName"));
        userOnly.put("users", summary.get("users"));
        return ResponseEntity.ok(userOnly);
    }

    /**
     * GET /assigned-workouts/trainer/{trainerId}/plan-summaries
     */
    @GetMapping("/trainer/{trainerId}/plan-summaries")
    public ResponseEntity<List<Map<String, Object>>> getTrainerPlanSummaries(@PathVariable Long trainerId) {
        List<WorkoutPlan> trainerPlans = planRepo.findByTrainerId(trainerId);
        if (trainerPlans == null || trainerPlans.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<AssignedWorkout> allAssigned = assignedRepo.findAll();
        List<Map<String, Object>> summaries = new ArrayList<>();

        for (WorkoutPlan plan : trainerPlans) {
            summaries.add(buildPlanSummary(plan, allAssigned));
        }

        return ResponseEntity.ok(summaries);
    }

    private Map<String, Object> buildPlanSummary(WorkoutPlan plan, List<AssignedWorkout> allAssigned) {
        List<String> acceptedUsers = new ArrayList<>();
        List<String> rejectedUsers = new ArrayList<>();
        List<String> completedUsers = new ArrayList<>();
        List<String> pendingUsers = new ArrayList<>();

        for (AssignedWorkout aw : allAssigned) {
            Workout workout = aw.getWorkout();
            if (workout == null || workout.getWorkoutPlanId() == null || !workout.getWorkoutPlanId().equals(plan.getWorkoutPlanId())) {
                continue;
            }

            String status = workout.getStatus() != null ? workout.getStatus().toLowerCase() : "unknown";
            String username = aw.getClient() != null ? aw.getClient().getAccountName() : "Unknown User";

            if (status.equals("accepted")) {
                acceptedUsers.add(username);
            } else if (status.equals("rejected")) {
                rejectedUsers.add(username);
            } else if (status.equals("completed")) {
                completedUsers.add(username);
            } else {
                // "assigned", "pending", or unhandled map to pending
                pendingUsers.add(username);
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("planId", plan.getWorkoutPlanId());
        res.put("planName", plan.getPlanName());
        res.put("acceptedCount", acceptedUsers.size());
        res.put("rejectedCount", rejectedUsers.size());
        res.put("completedCount", completedUsers.size());
        res.put("pendingCount", pendingUsers.size());

        Map<String, List<String>> userGroups = new HashMap<>();
        userGroups.put("accepted", acceptedUsers);
        userGroups.put("rejected", rejectedUsers);
        userGroups.put("completed", completedUsers);
        userGroups.put("pending", pendingUsers);

        res.put("users", userGroups);
        return res;
    }
}