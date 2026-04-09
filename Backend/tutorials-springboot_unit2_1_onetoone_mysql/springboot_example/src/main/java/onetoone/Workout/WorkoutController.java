package onetoone.Workout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutRepository repo;

    @Autowired
    private AssignedWorkoutRepository assignedRepo;

    // Helper: Map Workout to spec JSON
    private Map<String, Object> mapWorkoutToSpec(Workout w) {
        Map<String, Object> map = new HashMap<>();
        map.put("workoutId", w.getWorkoutId());
        map.put("userId", w.getUserId());
        map.put("createdByUserId", w.getCreatedByUserId());
        map.put("workoutName", w.getWorkoutName());
        map.put("workoutDescription", w.getWorkoutDescription());
        map.put("scheduledDate", w.getScheduledDate() != null ? w.getScheduledDate().toString() : null);
        map.put("status", w.getStatus());
        map.put("sourceType", w.getSourceType());

        // Find assignedWorkoutId (if linked)
        Long assignedId = null;
        for (AssignedWorkout aw : assignedRepo.findAll()) {
            if (aw.getWorkout() != null && aw.getWorkout().getWorkoutId().equals(w.getWorkoutId())) {
                assignedId = aw.getId();
                break;
            }
        }
        map.put("assignedWorkoutId", assignedId);
        return map;
    }

    // 11. POST /workouts
    @PostMapping
    public ResponseEntity<Map<String, Object>> addWorkout(@RequestBody Map<String, Object> incoming) {
        Workout newWorkout = new Workout();
        newWorkout.setWorkoutName((String) incoming.get("workoutName"));
        newWorkout.setWorkoutDescription((String) incoming.get("workoutDescription"));

        if (incoming.get("scheduledDate") != null) {
            newWorkout.setScheduledDate(LocalDate.parse(incoming.get("scheduledDate").toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        Long userId = incoming.get("userId") != null ? Long.valueOf(incoming.get("userId").toString()) : null;
        newWorkout.setUserId(userId);
        newWorkout.setCreatedByUserId(userId); // self created
        newWorkout.setStatus("planned");
        newWorkout.setSourceType("self_created");

        Workout saved = repo.save(newWorkout);

        Map<String, Object> result = new HashMap<>();
        result.put("workoutId", saved.getWorkoutId());
        result.put("message", "Workout created successfully");
        return ResponseEntity.ok(result);
    }

    // 12. GET /workouts/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getWorkoutsByUser(@PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String date) {
        List<Workout> all = repo.findAll().stream()
                .filter(w -> w.getUserId() != null && w.getUserId().equals(userId))
                .filter(w -> w.getStatus() == null || !w.getStatus().equalsIgnoreCase("rejected"))
                .collect(Collectors.toList());

        if (status != null) {
            all = all.stream().filter(w -> status.equalsIgnoreCase(w.getStatus())).collect(Collectors.toList());
        }
        if (groupId != null) {
            all = all.stream().filter(w -> groupId.equals(w.getGroupId())).collect(Collectors.toList());
        }
        if (date != null) {
            LocalDate target = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            all = all.stream().filter(w -> target.equals(w.getScheduledDate())).collect(Collectors.toList());
        }

        // Custom Sort: (scheduledDate >= CURRENT_DATE) DESC, scheduledDate ASC
        LocalDate today = LocalDate.now();
        all.sort((a, b) -> {
            LocalDate d1 = a.getScheduledDate() != null ? a.getScheduledDate() : LocalDate.MIN;
            LocalDate d2 = b.getScheduledDate() != null ? b.getScheduledDate() : LocalDate.MIN;

            boolean d1Future = !d1.isBefore(today);
            boolean d2Future = !d2.isBefore(today);

            if (d1Future && !d2Future)
                return -1;
            if (!d1Future && d2Future)
                return 1;

            return d1.compareTo(d2);
        });

        List<Map<String, Object>> result = all.stream().map(this::mapWorkoutToSpec).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // 13. GET /workouts/{workoutId}
    @GetMapping("/{workoutId}")
    public ResponseEntity<Map<String, Object>> fetchWorkoutById(@PathVariable Long workoutId) {
        Workout found = repo.findById(workoutId).orElse(null);
        if (found != null) {
            return ResponseEntity.ok(mapWorkoutToSpec(found));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 14. PUT /workouts/{workoutId}/status
    @PutMapping("/{workoutId}/status")
    public ResponseEntity<Map<String, Object>> updateWorkoutStatus(@PathVariable Long workoutId,
            @RequestBody Map<String, String> incoming) {
        Workout current = repo.findById(workoutId).orElse(null);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }

        current.setStatus(incoming.get("status"));
        repo.save(current);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Workout status updated");
        result.put("workoutId", current.getWorkoutId());
        result.put("status", current.getStatus());
        return ResponseEntity.ok(result);
    }

    // 15. PUT /workouts/{workoutId}
    @PutMapping("/{workoutId}")
    public ResponseEntity<Map<String, Object>> modifyWorkout(@PathVariable Long workoutId,
            @RequestBody Map<String, Object> incoming) {
        Workout current = repo.findById(workoutId).orElse(null);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }

        if (incoming.get("workoutName") != null)
            current.setWorkoutName((String) incoming.get("workoutName"));
        if (incoming.get("workoutDescription") != null)
            current.setWorkoutDescription((String) incoming.get("workoutDescription"));
        if (incoming.get("scheduledDate") != null) {
            current.setScheduledDate(LocalDate.parse(incoming.get("scheduledDate").toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        repo.save(current);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Workout updated");
        result.put("workoutId", current.getWorkoutId());
        return ResponseEntity.ok(result);
    }

    // 16. DELETE /workouts/{workoutId}
    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Map<String, String>> removeWorkout(@PathVariable Long workoutId) {
        Map<String, String> result = new HashMap<>();
        Workout found = repo.findById(workoutId).orElse(null);
        if (found != null) {
            repo.delete(found);
            result.put("message", "Workout deleted");
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Workout not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
