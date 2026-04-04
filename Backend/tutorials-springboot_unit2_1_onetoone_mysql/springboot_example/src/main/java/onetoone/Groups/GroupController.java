package onetoone.Groups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import onetoone.Users.UserRepository;
import onetoone.Users.User;

import java.util.List;

/**
 * @author Matthew Smith
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

    @PostMapping
    public Group createGroup(@RequestBody Group group) {
        return groupRepo.save(group);
    }

    @GetMapping
    public List<Group> getAllGroups() {
        return groupRepo.findAll();
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @PathVariable int userId,
            @RequestParam String role) {
        Group group = groupRepo.findById(groupId).orElse(null);
        User user = userRepo.findById(userId).orElse(null);

        if (group == null || user == null) {
            return ResponseEntity.badRequest().body("Group or User not found");
        }

        if (membershipRepo.existsByGroup_GroupIdAndUser_Id(groupId, userId)) {
            return ResponseEntity.badRequest().body("User already in group");
        }

        GroupMembership mem = new GroupMembership();
        mem.setGroup(group);
        mem.setUser(user);
        mem.setMembershipRole(role);
        mem.setCanSendMessages(true);
        mem.setCanManageMembers(role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Trainer"));
        mem.setCanAssignWorkouts(role.equalsIgnoreCase("Trainer"));

        return ResponseEntity.ok(membershipRepo.save(mem));
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMembership> getMembers(@PathVariable Long groupId) {
        return membershipRepo.findByGroup_GroupId(groupId);
    }
}
