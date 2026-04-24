package onetoone.PersonalBest;

import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all personal best tracking endpoints
 * Each personal best is private and specific to each user
 */
public class PersonalBestController {

    @Autowired
    private PersonalBestRepository pbRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Add new personal best entry
     */

    @PostMapping("/personalbests")
    public ResponseEntity<Map<String, Object>> createBest(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long ownerId = Long.valueOf(
                    body.get("userId").toString());
            if (!userRepo.existsById(ownerId.intValue())) {
                result.put("message", "User not found");
                return ResponseEntity.status(
                        HttpStatus.NOT_FOUND).body(result);
            }

            String activityLabel = body.get(
                    "exerciseName").toString();
            Double peakScore = Double.valueOf(
                    body.get("bestValue").toString());
            String measureUnit = body.containsKey("unit") ?
                    body.get("unit").toString() : null;
            LocalDate achievedDate = body.containsKey("recordDate")
                    && body.get("recordDate") != null ?
                    LocalDate.parse(
                            body.get("recordDate").toString()) :
                    LocalDate.now();

            PersonalBest freshEntry = new PersonalBest(
                    ownerId, activityLabel, peakScore,
                    measureUnit, achievedDate);
            freshEntry = pbRepo.save(freshEntry);

            result.put("message",
                    "Personal best recorded successfully");
            result.put("id", freshEntry.getEntryId());
            result.put("userId", freshEntry.getOwnerId());
            result.put("exerciseName",
                    freshEntry.getActivityLabel());
            result.put("bestValue", freshEntry.getPeakScore());
            result.put("unit", freshEntry.getMeasureUnit());
            result.put("recordDate",
                    freshEntry.getAchievedDate().toString());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("message",
                    "Error recording personal best: "
                            + e.getMessage());
            return ResponseEntity.status(
                    HttpStatus.BAD_REQUEST).body(result);
        }
    }
    /**
     * Get all personal bests for a user
    */

    @GetMapping("/users/{userId}/personalbests")
    public ResponseEntity<List<Map<String, Object>>> getAllBests(
        @PathVariable Long userId) {
    List<PersonalBest> entryList =
            pbRepo.findByOwnerId(userId);
    List<Map<String, Object>> result = new ArrayList<>();
    for (PersonalBest entry : entryList) {
        result.add(buildEntryMap(entry));
    }
    return ResponseEntity.ok(result);
    }

    /**
    * Get personal bests with optional filter by exercise name
    * exerciseName is optional - filters resylts if provided
    */

    @GetMapping("/users/{userId}/personalbests/{exerciseName}")
    public ResponseEntity<List<Map<String, Object>>> getBestByExercise(
        @PathVariable Long userId,
        @PathVariable String exerciseName) {
    List<PersonalBest> entryList = pbRepo
            .findByOwnerIdAndActivityLabelContainingIgnoreCase(
                    userId, exerciseName);
    List<Map<String, Object>> result = new ArrayList<>();
    for (PersonalBest entry : entryList) {
        result.add(buildEntryMap(entry));
    }
    return ResponseEntity.ok(result);
    }

    /**
    * Update an existing personal best
    */
    @PutMapping("/personalbests/{id}")
    public ResponseEntity<Map<String, Object>> updateBest(
        @PathVariable Long id,
        @RequestBody Map<String, Object> body) {
    Map<String, Object> result = new HashMap<>();
    PersonalBest targetEntry =
            pbRepo.findById(id).orElse(null);
    if (targetEntry == null) {
        result.put("message", "Personal best not found");
        return ResponseEntity.status(
                HttpStatus.NOT_FOUND).body(result);
    }

    if (body.containsKey("exerciseName")) {
        targetEntry.setActivityLabel(
                body.get("exerciseName").toString());
    }
    if (body.containsKey("bestValue")) {
        targetEntry.setPeakScore(Double.valueOf(
                body.get("bestValue").toString()));
    }
    if (body.containsKey("unit")) {
        targetEntry.setMeasureUnit(
                body.get("unit").toString());
    }
    if (body.containsKey("recordDate") &&
            body.get("recordDate") != null) {
        targetEntry.setAchievedDate(LocalDate.parse(
                body.get("recordDate").toString()));
    }

    targetEntry = pbRepo.save(targetEntry);
    result.put("message",
            "Personal best updated successfully");
    result.put("id", targetEntry.getEntryId());
    result.put("userId", targetEntry.getOwnerId());
    result.put("exerciseName",
            targetEntry.getActivityLabel());
    result.put("bestValue", targetEntry.getPeakScore());
    result.put("unit", targetEntry.getMeasureUnit());
    result.put("recordDate",
            targetEntry.getAchievedDate().toString());
    return ResponseEntity.ok(result);
    }

    /**
     * Delete a personal best entry
     */
    @DeleteMapping("/personalbests/{id}")
    public ResponseEntity<Map<String, Object>> deleteBest(
        @PathVariable Long id) {
    Map<String, Object> result = new HashMap<>();
    PersonalBest targetEntry =
            pbRepo.findById(id).orElse(null);
    if (targetEntry == null) {
        result.put("message", "Personal best not found");
        return ResponseEntity.status(
                HttpStatus.NOT_FOUND).body(result);
    }
    pbRepo.delete(targetEntry);
    result.put("message",
            "Personal best deleted successfully");
    result.put("id", id);
    return ResponseEntity.ok(result);
    }

    private Map<String, Object> buildEntryMap(PersonalBest entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entry.getEntryId());
        map.put("userId", entry.getOwnerId());
        map.put("exerciseName", entry.getActivityLabel());
        map.put("bestValue", entry.getPeakScore());
        map.put("unit", entry.getMeasureUnit());
        map.put("recordDate", entry.getAchievedDate() != null ?
                entry.getAchievedDate().toString() : null);
        return map;
    }
}
