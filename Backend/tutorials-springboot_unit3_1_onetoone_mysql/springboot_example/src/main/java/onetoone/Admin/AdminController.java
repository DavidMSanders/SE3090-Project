package onetoone.Admin;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;


/**
 * Admin Controller - Role management
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private UserRepository accountRepository;

    /**
     *
     * Change user role (Admin only)
     *
     */

    @PutMapping("/users/{personId}/role")
    public ResponseEntity<Map<String, Object>> modifyAccountRole(
            @PathVariable Integer personId,

    @RequestParam
    Integer supervisorId,
    @RequestParam
    String updatedRole)

    {
        Map<String, Object> responseData = new HashMap<>();


        Optional<User> supervisorLookup = accountRepository.findById(supervisorId);
        if (supervisorLookup.isEmpty()) {
            responseData.put("error", "Admin user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseData);

        }

        User supervisor = supervisorLookup.get();
        if (!supervisor.getRole().equals("Admin")) {
            responseData.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseData);
        }

        if (!updatedRole.equals("Client") && !updatedRole.equals("Trainer") && !updatedRole.equals("Manager") &&
                !updatedRole.equals("Admin")){
            responseData.put("error", "Invalid role. Must be: Client, Trainer, Manager, or Admin");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
    }

        Optional<User> personLookup = accountRepository.findById(personId);
        if (personLookup.isEmpty()){
            responseData.put("error", "Target user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }

        User targetAccount = personLookup.get();
        String previousRole = targetAccount.getRole();
        targetAccount.setRole(updatedRole);
        accountRepository.save(targetAccount);

        responseData.put("message", "Role updated successfully");
        responseData.put("userId", personId);
        responseData.put("username", targetAccount.getAccountName());
        responseData.put("oldRole", previousRole);
        responseData.put("newRole", updatedRole);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam Integer adminId) {
        Optional<User> admin = accountRepository.findById(adminId);
        if (admin.isEmpty() || !"Admin".equals(admin.get().getRole())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @GetMapping("/users/{personId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Integer personId, @RequestParam Integer adminId) {
        Optional<User> admin = accountRepository.findById(adminId);
        if (admin.isEmpty() || !"Admin".equals(admin.get().getRole())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }
        Optional<User> target = accountRepository.findById(personId);
        if (target.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Target user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        return ResponseEntity.ok(target.get());
    }

    @PutMapping("/users/{personId}")
    public ResponseEntity<?> updateUserDetails(@PathVariable Integer personId, @RequestParam Integer adminId, @RequestBody AdminProfileUpdateRequest request) {
        Optional<User> admin = accountRepository.findById(adminId);
        if (admin.isEmpty() || !"Admin".equals(admin.get().getRole())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }
        Optional<User> target = accountRepository.findById(personId);
        if (target.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Target user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        User foundUser = target.get();
        if (request.getAccountName() != null && !request.getAccountName().trim().isEmpty()) {
            foundUser.setAccountName(request.getAccountName());
        }
        if (request.getUserPassword() != null && !request.getUserPassword().trim().isEmpty()) {
            foundUser.setUserPassword(request.getUserPassword());
        }
        if (request.getBirthDate() != null && !request.getBirthDate().trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate parsedDate = LocalDate.parse(request.getBirthDate(), formatter);
                foundUser.setBirthDate(parsedDate);
            } catch (Exception e) {
                Map<String, String> err = new HashMap<>();
                err.put("message", "Invalid date format. Please use MM/dd/yyyy");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
            }
        }
        accountRepository.save(foundUser);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "User updated successfully");
        result.put("user", foundUser);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{personId}/status")
    public ResponseEntity<?> setUserStatus(@PathVariable Integer personId, @RequestParam Integer adminId, @RequestParam boolean enabled) {
        Optional<User> admin = accountRepository.findById(adminId);
        if (admin.isEmpty() || !"Admin".equals(admin.get().getRole())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }
        Optional<User> target = accountRepository.findById(personId);
        if (target.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Target user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        User foundUser = target.get();
        foundUser.setEnabled(enabled);
        accountRepository.save(foundUser);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Status updated successfully");
        result.put("userId", personId);
        result.put("isEnabled", enabled);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/users/{personId}")
    public ResponseEntity<?> deleteUserSecure(@PathVariable Integer personId, @RequestParam Integer adminId) {
        Optional<User> admin = accountRepository.findById(adminId);
        if (admin.isEmpty() || !"Admin".equals(admin.get().getRole())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }
        Optional<User> target = accountRepository.findById(personId);
        if (target.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Target user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        accountRepository.delete(target.get());
        Map<String, String> result = new HashMap<>();
        result.put("message", "User deleted successfully");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(@RequestParam Integer adminId) {
        Optional<User> admin = accountRepository.findById(adminId);
        if (admin.isEmpty() || !"Admin".equals(admin.get().getRole())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", accountRepository.count());
        stats.put("totalAdmins", accountRepository.countByRole("Admin"));
        stats.put("totalTrainers", accountRepository.countByRole("Trainer"));
        stats.put("totalClients", accountRepository.countByRole("Client"));
        stats.put("activeUsers", accountRepository.countByIsEnabled(true));
        stats.put("suspendedUsers", accountRepository.countByIsEnabled(false));
        return ResponseEntity.ok(stats);
    }

}

class AdminProfileUpdateRequest {
    private String accountName;
    private String userPassword;
    private String birthDate;

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
}
