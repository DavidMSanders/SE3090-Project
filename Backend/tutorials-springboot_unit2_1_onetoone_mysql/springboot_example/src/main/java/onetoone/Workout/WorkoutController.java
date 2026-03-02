package onetoone.Workout;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     *
     * {
     *     "name": "Push Day",
     *     "date": "02/24/2026"
     *     "durationMinutes": 60,
     *     "description": "Chest + Triceps"
     * }
     */

    @RequestMapping
    public ResponseEntity<Map<String, Object>> addWorkout(@RequestBody WorkoutData incoming){
        Map<String, Object> result = new HashMap<>();
        try {
            if (isEmpty(incoming.getName() || isEmpty(incoming.getDate())) {
                result.put("message", "name and date are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate parsedDate = LocalDate.parse(incoming.getDate(), pattern));

            Workout newWorkout = new Workout(){
                incoming.getName();
                parsedDate,
                incoming.getDurationMinutes(),
                incoming.getDescription()
                        );
        Workout saved = repo.saved(newWorkout);

        result.put("id, ", saved.getWorkoutId());
        result.put("name" , saved.getWorkoutName());
        result.put("date", saved.getSessionDate().format(pattern));
        result.put("durationMinutes", saved.getMinutes());
        result.put("description", saved.getDetails());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } catch (DateTimeParseException error){
                result.put("message", "date must be in MM/DD/YYYY format");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST.body(result);
            } catch (Exception error){
                result.put("message", "Error created workout: " + error.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);


            }
        }

        /**
         * READ - GET
         *
         * {
         * "id": 1
         * "name": "Push Day",
         * "date": "02/24/2026",
         * "durationMinutes: 60,
         * "description": "Chest + triceps"
         * }
         */

        @GetMapping("/{workoutId}")
                public ResponseEntity<Map<String, Object>> fetchWorkoutById(@PathVariable int workoutId ){
                    Map<String, Object> result = new HashMap<>();

                    Workout found = repo.findById(workoutId).orElse(null);

                    if(found != null){
                        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");


                        result.put("id", found.getWorkoutId());
                        result.put("name", found.getWorkoutName());
                        result.put("date", found.getSessionDate().format((pattern)));
                        result.put("durationMinutes", found.getMinutes());
                        result.put("description", found.getDetails());
                        return ResponseEntity.ok(result);
                        else {
                            result.put("message", "Workout not found");
                            return ResponseEntity.status((HttpStatus.NOT_FOUND).body(result);
                        }

                    }
        }
    }

}
