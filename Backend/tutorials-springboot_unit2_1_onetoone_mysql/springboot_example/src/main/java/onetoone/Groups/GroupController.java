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

    // 18. GET /groups/{groupId}/assignable-clients?trainerId=3
    @GetMapping("/{groupId}/assignable-clients")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAssignableClients(
            @PathVariable Long groupId, @RequestParam int trainerId) {
        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (GroupMembership gm : membershipRepo.findAll()) {
            if (gm.getGroup() != null && gm.getGroup().getGroupId().equals(groupId)) {
                User u = gm.getUser();
                if (u != null && u.getUserId() != trainerId && "Client".equalsIgnoreCase(u.getRole())) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("userId", u.getUserId());
                    map.put("username", u.getAccountName());
                    res.add(map);
                }
            }
        }
        return ResponseEntity.ok(res);
    }
}
