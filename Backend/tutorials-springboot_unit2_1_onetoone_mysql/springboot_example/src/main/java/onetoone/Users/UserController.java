package onetoone.Users;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
    public ResponseEntity<Map<String, String>> deleteUserByID(@PathVariable int id){
        Map<String, String> responseMap = new HashMap<>();

        User foundAccount = userRepository.findById(id).orElse(null);


        if (foundAccount != null){
            userRepository.delete(foundAccount);
            responseMap.put("message", "User deleted sucessfully");

            return ResponseEntity.ok(responseMap);
        } else {
            responseMap.put("message", "User not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
        }
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

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
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



/*
 * @Autowired
    LaptopRepository laptopRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/users")
    List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping(path = "/users/{id}")
    User getUserById( @PathVariable int id){
        return userRepository.findById(id);
    }

    @PostMapping(path = "/users")
    String createUser(@RequestBody User user){
        if (user == null)
            return failure;
        userRepository.save(user);
        return success;
    }

    @PutMapping("/users/{id}")
    User updateUser(@PathVariable int id, @RequestBody User request){
        User user = userRepository.findById(id);
        if(user == null)
            return null;
        userRepository.save(request);
        return userRepository.findById(id);
    }   
    
    @PutMapping("/users/{userId}/laptops/{laptopId}")
    String assignLaptopToUser(@PathVariable int userId,@PathVariable int laptopId){
        User user = userRepository.findById(userId);
        Laptop laptop = laptopRepository.findById(laptopId);
        if(user == null || laptop == null)
            return failure;
        laptop.setUser(user);
        user.setLaptop(laptop);
        userRepository.save(user);
        return success;
    }

    @DeleteMapping(path = "/users/{id}")
    String deleteUser(@PathVariable int id){
        userRepository.deleteById(id);
        return success;
    }
}
 **/
