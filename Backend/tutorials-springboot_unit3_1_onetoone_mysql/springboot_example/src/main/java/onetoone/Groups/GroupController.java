package onetoone.Groups;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Group Controller
 * @author Chris Lopez
 */
@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMembershipRepository membershipRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * POST /api/v1/groups
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody GroupRequest incoming){
        Map<String, Object> result = new HashMap<>();

        try{
            if(incoming.getGroupTitle() == null || incoming.getGroupTitle().trim().isEmpty()){
                result.put("message", "groupTitle is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            if(!incoming.getGroupType().equals("Trainer Group") && !incoming.getGroupType().equals("Direct Message Group
        }
    }
}
