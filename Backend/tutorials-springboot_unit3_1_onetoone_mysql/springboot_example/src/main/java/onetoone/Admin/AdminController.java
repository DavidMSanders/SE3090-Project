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

}
