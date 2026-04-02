package onetoone.Groups;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class GroupMembershipController {
    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepo;

    /**
     * Get a specific membership
     */
    @GetMapping("/user/{userId}/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getMembership(@PathVariable int userId, @PathVariable int groupId) {
        Map<String, Object> result = new HashMap<>();


        Optional<GroupMembership> membershipQuery = membershipRepository.findByGroupIdAndUserId(groupId, userId);


        if(membershipQuery.isPresent()){
            Map<String, Object> result = new HashMap<>();
            result.put("membershipId", m.membershipQuery.get());
            result.put("groupId", m.getGroupId());
            result.put("userId", m.getUserId());
            result.put("membershipRole", m.getAccessLevel());
            result.put("canSendMessages", m.isChatEnabled());
            result.put("canManageMembers", m.isManageEnabled());
            result.put("canPostAnnouncements", m.isAnnounceEnabled());
            result.put("canAssignWorkouts", m.isWorkoutEnabled());
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Membership not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    @PutMapping("/{membershipId}/permissions")
    public ResponseEntity<Map<String, Object>> updatePermission(@PathVariable int membershipId, @RequestBody PermissionBody incoming){
        Map<String, Object> result = new HashMap<>();


    }
                                                                )
    }

}
