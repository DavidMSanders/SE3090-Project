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
 * @author Chris Lopez
 */


@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutRepository repo;

    /**
     * Create - POST
     * <p>
     * {
     * "name": "Push Day",
     * "date": "02/24/2026"
     * "durationMinutes": 60,
     * "description": "Chest + Triceps"
     * }
     */

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

            Workout newWorkout = new Workout(
                    incoming.getName(), parsedDate,
                    incoming.getDurationMinutes(),
                    incoming.getDescription());

            Workout saved = repo.save(newWorkout);

            result.put("id", saved.getWorkoutId());
            result.put("name", saved.getWorkoutName());
            result.put("date", saved.getSessionDate().format(pattern));
            result.put("durationMinutes", saved.getMinutes());
            result.put("description", saved.getDetails());

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (DateTimeParseException error) {
            result.put("message", "date must be in MM/DD/YYYY format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception error) {
            result.put("message", "Error creating workout: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);


        }
    }


    /**
     * READ - GET
     * <p>
     * {
     * "id": 1
     * "name": "Push Day",
     * "date": "02/24/2026",
     * "durationMinutes: 60,
     * "description": "Chest + triceps"
     * }
     */

    @GetMapping("/{workoutId}")
    public ResponseEntity<Map<String, Object>> fetchWorkoutById(@PathVariable int workoutId) {
        Map<String, Object> result = new HashMap<>();

        Workout found = repo.findById(workoutId).orElse(null);

        if (found != null) {
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");


            result.put("id", found.getWorkoutId());
            result.put("name", found.getWorkoutName());
            result.put("date", found.getSessionDate().format(pattern));
            result.put("durationMinutes", found.getMinutes());
            result.put("description", found.getDetails());
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Workout not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


    /**
     * READ ALL - GET /api/v1/workouts
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> fetchAllWorkouts() {
        List<Workout> allWorkouts = repo.findAll();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        List<Map<String, Object>> result = allWorkouts.stream().map(item -> {
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("id", item.getWorkoutId());
            workoutData.put("name", item.getWorkoutName());
            workoutData.put("date", item.getSessionDate().format(pattern));
            workoutData.put("durationMinutes", item.getMinutes());
            workoutData.put("description", item.getDetails());
            return workoutData;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * UPDATE PUT /api/v1/workouts/{id}
     * <p>
     * {
     * "id": 1,
     * "name":
     * "date"
     * "durationMinutes"
     * "description":
     * }
     */

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modifyWorkout(@PathVariable int id, @RequestBody
    WorkoutData incoming) {
        Map<String, Object> result = new HashMap<>();

        try {
            Workout current = repo.findById(id).orElse(null);

            if (current == null) {
                result.put("message", "Workout not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            if (!isEmpty(incoming.getName())) {
                current.setWorkoutName(incoming.getName());
            }

            if (!isEmpty(incoming.getDate())) {
                DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate parsedDate = LocalDate.parse(incoming.getDate(), pattern);
                current.setSessionDate(parsedDate);
            }

            if (incoming.getDurationMinutes() > 0) {
                current.setMinutes(incoming.getDurationMinutes());
            }

            if (incoming.getDescription() != null) {
                current.setDetails(incoming.getDescription());
            }

            Workout updated = repo.save(current);

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            result.put("id", updated.getWorkoutId());
            result.put("name", updated.getWorkoutName());
            result.put("date", updated.getSessionDate().format(pattern));
            result.put("durationMinutes", updated.getMinutes());
            result.put("description", updated.getDetails());

            return ResponseEntity.ok(result);


        } catch (DateTimeParseException error) {
            result.put("message", "date must be in MM/DD/yyyy format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception error) {
            result.put("message", "Error updating workout: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Delete - DELETE /api/v1/workouts/{id}
     */

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


        private boolean isEmpty (String text){
            return text == null || text.trim().isEmpty();
        }
    }


    /**
     * Request body class for workout endpoints
     */

    class WorkoutData {
        private String name;
        private String date;
        private int durationMinutes;
        private String description;

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
    }









