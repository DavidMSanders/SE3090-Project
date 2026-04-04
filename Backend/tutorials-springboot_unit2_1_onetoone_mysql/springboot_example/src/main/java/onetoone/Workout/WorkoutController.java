package onetoone.Workout;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.MarshalledObject;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workout Controller
 * 
 * @author Chris Lopez
 */

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutRepository repo;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addWorkout(@RequestBody WorkoutData incoming) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (isEmpty(incoming.getName()) || isEmpty(incoming.getDate())) {
                result.put("message", "name and date are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate parsedDate = LocalDate.parse(incoming.getDate(), pattern);

            Workout newWorkout = new Workout();
            newWorkout.setWorkoutName(incoming.getName());
            newWorkout.setScheduledDate(parsedDate);
            newWorkout.setDurationMinutes(incoming.getDurationMinutes());
            newWorkout.setWorkoutDescription(incoming.getDescription());
            newWorkout.setUserId(incoming.getUserId());
            newWorkout.setGroupId(incoming.getGroupId());
            newWorkout.setStatus("planned");
            newWorkout.setSourceType("self_created");

            Workout saved = repo.save(newWorkout);

            result.put("id", saved.getWorkoutId());
            result.put("name", saved.getWorkoutName());
            result.put("date", saved.getScheduledDate() != null ? saved.getScheduledDate().format(pattern) : null);
            result.put("durationMinutes", saved.getDurationMinutes());
            result.put("description", saved.getWorkoutDescription());

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (DateTimeParseException error) {
            result.put("message", "date must be in MM/dd/yyyy format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception error) {
            result.put("message", "Error creating workout: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/{workoutId}")
    public ResponseEntity<Map<String, Object>> fetchWorkoutById(@PathVariable Long workoutId) {
        Map<String, Object> result = new HashMap<>();
        Workout found = repo.findById(Math.toIntExact(workoutId)).orElse(null);

        if (found != null) {
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            result.put("id", found.getWorkoutId());
            result.put("name", found.getWorkoutName());
            result.put("date", found.getScheduledDate() != null ? found.getScheduledDate().format(pattern) : null);
            result.put("durationMinutes", found.getDurationMinutes());
            result.put("description", found.getWorkoutDescription());
            result.put("status", found.getStatus());
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Workout not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> fetchAllWorkouts() {
        List<Workout> allWorkouts = repo.findAll();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        List<Map<String, Object>> result = allWorkouts.stream().map(item -> {
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("id", item.getWorkoutId());
            workoutData.put("name", item.getWorkoutName());
            workoutData.put("date", item.getScheduledDate() != null ? item.getScheduledDate().format(pattern) : null);
            workoutData.put("durationMinutes", item.getDurationMinutes());
            workoutData.put("description", item.getWorkoutDescription());
            workoutData.put("status", item.getStatus());
            return workoutData;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modifyWorkout(@PathVariable int id, @RequestBody WorkoutData incoming) {
        Map<String, Object> result = new HashMap<>();
        try {
            Workout current = repo.findById(id).orElse(null);
            if (current == null) {
                result.put("message", "Workout not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            if (!isEmpty(incoming.getName()))
                current.setWorkoutName(incoming.getName());
            if (!isEmpty(incoming.getDate())) {
                DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                current.setScheduledDate(LocalDate.parse(incoming.getDate(), pattern));
            }
            if (incoming.getDurationMinutes() > 0)
                current.setDurationMinutes(incoming.getDurationMinutes());
            if (incoming.getDescription() != null)
                current.setWorkoutDescription(incoming.getDescription());
            if (incoming.getStatus() != null)
                current.setStatus(incoming.getStatus());

            Workout updated = repo.save(current);
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            result.put("id", updated.getWorkoutId());
            result.put("name", updated.getWorkoutName());
            result.put("date", updated.getScheduledDate() != null ? updated.getScheduledDate().format(pattern) : null);
            result.put("durationMinutes", updated.getDurationMinutes());
            result.put("description", updated.getWorkoutDescription());
            result.put("status", updated.getStatus());
            return ResponseEntity.ok(result);
        } catch (Exception error) {
            result.put("message", "Error updating workout");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removeWorkout(@PathVariable int id) {
        Map<String, String> result = new HashMap<>();
        Workout found = repo.findById(id).orElse(null);
        if (found != null) {
            repo.delete(found);
            result.put("message", "Workout deleted sucessfully");
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Workout not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}

class WorkoutData {
    private Long userId;
    private Long groupId;
    private String name;
    private String date;
    private int durationMinutes;
    private String description;
    private String status;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
