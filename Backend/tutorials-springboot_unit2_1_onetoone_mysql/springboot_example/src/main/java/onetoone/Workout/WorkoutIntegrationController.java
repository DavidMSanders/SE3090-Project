package onetoone.Workout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import onetoone.Notifications.NotificationService;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import onetoone.Trainer.TrainerClientRepository;

/**
 * @author Matthew Smith
 */
@RestController
@RequestMapping("/workouts/actions")
public class WorkoutIntegrationController {

    @Autowired
    private AssignedWorkoutRepository assignedWorkoutRepo;

    @Autowired
    private CompletedWorkoutRepository completedWorkoutRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerClientRepository trainerClientRepository;

    @PostMapping("/assign")
    public ResponseEntity<?> assignWorkout(
            @RequestParam int trainerId,
            @RequestParam int clientId,
            @RequestParam int workoutId) {

        if (!trainerClientRepository.existsByTrainerIdAndClientId(trainerId, clientId)) {
            return ResponseEntity.badRequest().body("No trainer-client relationship exists.");
        }

        Workout w = workoutRepository.findById(workoutId).orElse(null);
        User t = userRepository.findById(trainerId).orElse(null);
        User c = userRepository.findById(clientId).orElse(null);

        if (w == null || t == null || c == null) {
            return ResponseEntity.badRequest().body("Invalid parameters");
        }

        AssignedWorkout aw = new AssignedWorkout(w, c, t);
        assignedWorkoutRepo.save(aw);

        try {
            // Generate notification for client
            notificationService.sendNotification(trainerId, clientId, "Assignments",
                    "You have been assigned a new workout: " + w.getWorkoutName(), Math.toIntExact(aw.getId()));
        } catch (Exception e) {
        }

        return ResponseEntity.ok(aw);
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeWorkout(@RequestParam Long assignedWorkoutId) {
        AssignedWorkout aw = assignedWorkoutRepo.findById(assignedWorkoutId).orElse(null);
        if (aw == null)
            return ResponseEntity.badRequest().body("Not found");

        CompletedWorkout cw = new CompletedWorkout(aw);
        completedWorkoutRepo.save(cw);

        try {
            // Generate notification for trainer
            notificationService.sendNotification(Math.toIntExact(aw.getClient().getUserId()),
                    Math.toIntExact(aw.getTrainer().getUserId()), "Feedback",
                    "Client " + aw.getClient().getAccountName() + " completed workout: "
                            + aw.getWorkout().getWorkoutName(),
                    Math.toIntExact(cw.getId()));
        } catch (Exception e) {
        }

        return ResponseEntity.ok(cw);
    }
}
