package onetoone.auth;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Signup Controller
 * 
 * @author Chris Lopez
 *
 */

@RestController
@RequestMapping("/api/v1/auth")

public class SignupController {

    @Autowired
    private UserRepository userRepo;

    /**
     *
     * POST /api/v1/auth/signup
     *
     *
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> createAccount(@RequestBody RegistrationInfo info) {
        Map<String, String> response = new HashMap<>();

        try {
            User existing = userRepo.findByAccountName(info.getUsername());
            if (existing != null) {
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthdate = LocalDate.parse(info.getDateOfBirth(), pattern);

            User newUser = new User(
                    info.getUsername(),
                    info.getPassword(),
                    birthdate);

            // save database
            userRepo.save(newUser);

            response.put("message", "User created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception problem) {
            response.put("message", "Error creating user: " + problem.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        String accountName = request.getAccountName();
        String userPassword = request.getUserPassword();

        if (accountName == null || userPassword == null || accountName.trim().isEmpty() || userPassword.isEmpty()) {
            response.put("status", false);
            response.put("message", "Username and password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User foundUser = userRepo.findByAccountName(accountName.trim());

        if (foundUser == null || !userPassword.equals(foundUser.getUserPassword())) {
            response.put("status", false);
            response.put("message", "login failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        response.put("status", true);
        response.put("message", "Login successful");
        response.put("userId", foundUser.getUserId());
        response.put("username", foundUser.getAccountName());
        return ResponseEntity.ok(response);
    }
}

/**
 * Request body for SignUp
 */

class RegistrationInfo {
    private String username;
    private String password;
    private String dateOfBirth;

    public RegistrationInfo() {
    }

    public RegistrationInfo(String username, String password, String dateOfBirth) {

        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.password = password;

    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

}
