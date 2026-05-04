package onetoone.Users;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import onetoone.Groups.GroupMembership;
import onetoone.Groups.GroupMembershipRepository;
import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;

//import onetoone.Laptops.Laptop;
//import onetoone.Laptops.LaptopRepository;

/**
 * 
 * @author Chris Lopez
 * 
 */

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupMembershipRepository membershipRepository;


    @Autowired
    GroupRepository groupRepository;

    // 17. GET /users/{trainerId}/assignable-groups
    @GetMapping("/{trainerId}/assignable-groups")
    public ResponseEntity<List<Map<String, Object>>> getAssignableGroups(@PathVariable int trainerId) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (GroupMembership gm : membershipRepository.findAll()) {
            if (gm.getUserId() == trainerId && gm.isWorkoutEnabled()) {
                Map<String, Object> map = new HashMap<>();
                map.put("groupId", gm.getGroupId());
                Group g = groupRepository.findById(gm.getGroupId()).orElse(null);
                if (g != null) {
                    map.put("groupTitle", g.getGroupTitle());
                }
                res.add(map);
            }
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Deletes a user by username and date of birth
     * <p>
     * Body example
     * {
     * "username": "testuser1234",
     * "dateofBrth": 01/23/2004"
     * }
     */

    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestBody DeleteRequest requestData) {
        Map<String, String> responseMap = new HashMap<>();

        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate parsedBirthDate = LocalDate.parse(requestData.getDateOfBirth(), dateTimeFormatter);

            User accountToDelete = userRepository.findByAccountNameAndBirthDate(
                    requestData.getUsername(),
                    parsedBirthDate
            );


            if (accountToDelete != null) {
                userRepository.delete(accountToDelete);
                responseMap.put("message", "User deleted successfully");
                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
            }

        } catch (Exception error) {
            responseMap.put("message", "Error deleting user" + error.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    /**
     * DELETE by ID
     *
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUserByID(@PathVariable int id) {
        Map<String, String> responseMap = new HashMap<>();

        User foundAccount = userRepository.findById(id).orElse(null);

        if (foundAccount != null) {
            userRepository.delete(foundAccount);
            responseMap.put("message", "User deleted sucessfully");

            return ResponseEntity.ok(responseMap);
        } else {
            responseMap.put("message", "User not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
        }
    }

    /**
     * Sets a user's role to Trainer
     */

    @PutMapping("/{userId}/set-trainer")
    public ResponseEntity<Map<String, Object>> setTrainerRole(@PathVariable int userId){
        Map<String, Object> result = new HashMap<>();
        User found = userRepository.findById(userId).orElse(null);
        if (found == null){
            result.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

        found.setRole("Trainer");
        userRepository.save(found);

        List<GroupMembership> memberships = membershipRepository.findByUserId(userId);
        for (GroupMembership membership : memberships) {
            if (GroupMembership.ROLE_MEMBER.equals(membership.getAccessLevel())) {
                membership.setAccessLevel(GroupMembership.ROLE_TRAINER);
                membership.setPermissionByRole(GroupMembership.ROLE_TRAINER);
                membershipRepository.save(membership);
            }
        }

        result.put("message", "User role updated successfully");
        result.put("userId", found.getUserId());
        result.put("role", found.getRole());
        result.put("membershipsUpdated", memberships.size());
        return ResponseEntity.ok(result);

}

    @PutMapping("/{userId}/set-admin")
    public ResponseEntity<Map<String, Object>> assignAdminRole(@PathVariable int userId) {
        Map<String, Object> responseData = new HashMap<>();

        User targetAccount = userRepository.findById(userId).orElse(null);
        if (targetAccount == null) {
            responseData.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }

        targetAccount.setRole("Admin");
        userRepository.save(targetAccount);

        responseData.put("message", "User role updated successfully");
        responseData.put("userId", targetAccount.getUserId());
        responseData.put("role", targetAccount.getRole());
        return ResponseEntity.ok(responseData);
    }


    @PutMapping("/{userId}/set-client")
    public ResponseEntity<Map<String, Object>> assignClientRole(@PathVariable int userId) {
        Map<String, Object> responseData = new HashMap<>();

        User targetAccount = userRepository.findById(userId).orElse(null);
        if (targetAccount == null) {
            responseData.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        }

        targetAccount.setRole("Client");
        userRepository.save(targetAccount);

        responseData.put("message", "User role updated successfully");
        responseData.put("userId", targetAccount.getUserId());
        responseData.put("role", targetAccount.getRole());
        return ResponseEntity.ok(responseData);
    }

    /**
     * Get
     * Endpoint to get user by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        User foundAccount = userRepository.findById(id).orElse(null);

        if (foundAccount != null) {
            return ResponseEntity.ok(foundAccount);
        } else {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
        }
    }

    /**
     * Get
     * Endpoint to list all users
     * Use for testing
     */

    @PutMapping("/{userId}/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@PathVariable int userId, @RequestBody ProfileUpdateRequest request) {
        Map<String, Object> result = new HashMap<>();
        User foundUser = userRepository.findById(userId).orElse(null);
        if (foundUser == null) {
            result.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if (request.getAccountName() != null && !request.getAccountName().trim().isEmpty()) {
            foundUser.setAccountName(request.getAccountName());
        }
        if (request.getUserPassword() != null && !request.getUserPassword().trim().isEmpty()) {
            foundUser.setUserPassword(request.getUserPassword());
        }
        if (request.getBirthDate() != null && !request.getBirthDate().trim().isEmpty()) {
            try {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate parsedDate = LocalDate.parse(request.getBirthDate(), dateTimeFormatter);
                foundUser.setBirthDate(parsedDate);
            } catch (Exception e) {
                result.put("message", "Invalid date format. Please use MM/dd/yyyy");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        }

        userRepository.save(foundUser);
        result.put("message", "Profile updated successfully");
        result.put("user", foundUser);
        return ResponseEntity.ok(result);
    }

    /**
     * Get
     * Endpoint to list all users
     * Use for testing
     */

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * @author Matthew Smith
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {

        User foundUser = userRepository.findByAccountNameAndUserPassword(
                request.getAccountName(),
                request.getUserPassword());

        if (foundUser != null && foundUser.isEnabled()) {
            return ResponseEntity.ok(foundUser);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

/**
 * Request body class for delete endpoint
 */

class DeleteRequest {
    private String username;
    private String dateOfBirth;

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getDateOfBirth() {

        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {

        this.dateOfBirth = dateOfBirth;
    }

}

class ProfileUpdateRequest {
    private String accountName;
    private String userPassword;
    private String birthDate; // format: MM/dd/yyyy

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
