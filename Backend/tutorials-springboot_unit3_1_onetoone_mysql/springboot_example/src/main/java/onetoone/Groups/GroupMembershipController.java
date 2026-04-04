package onetoone.Groups;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/groupMemberships")
public class GroupMembershipController {
    @Autowired
    private GroupMembershipRepository membershipRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Get a specific membership
     */
    @GetMapping("/user/{userId}/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getMembership(@PathVariable int userId, @PathVariable int groupId) {
        Map<String, Object> result = new HashMap<>();


        Optional<GroupMembership> membershipQuery = membershipRepo.findByGroupIdAndUserId(groupId, userId);


        if (membershipQuery.isPresent()) {

            GroupMembership m = membershipQuery.get();
            result.put("membershipId", m.getMembershipId());
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

    /**
     * Update a memmbers permissions
     * Only GROUP_LEADER or Admin can do this
     *
     * @param membershipId
     * @param incoming
     * @return
     */
    @PutMapping("/{membershipId}/permissions")
    public ResponseEntity<Map<String, Object>> updatePermission(@PathVariable int membershipId, @RequestBody PermissionBody incoming) {
        Map<String, Object> result = new HashMap<>();

        Optional<GroupMembership> targetQuery = membershipRepo.findById(membershipId);
        if (targetQuery.isEmpty()) {
            result.put("message", "Membership not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        Optional<GroupMembership> supervisorQuery = membershipRepo.findByGroupIdAndUserId(
                targetQuery.get().getGroupId(),
                incoming.getSupervisorId());
        if (supervisorQuery.isEmpty()) {
            result.put("message", "Supervisor is not a member of this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        if (!supervisorQuery.get().isManageEnabled()) {
            result.put("message", "You do not have permission to update permissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);

        }
        GroupMembership target = targetQuery.get();
        String currentLevel = target.getAccessLevel();
        if (currentLevel.equals("GROUP_LEADER") || currentLevel.equals("ADMIN")) {
            result.put("message", "Cannot change GROUP_LEADER or ADMIN permissions");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);

        }
        target.setAccessLevel(incoming.getNewAccessLevel());
        target.setChatEnabled(incoming.isSendEnabled());
        target.setManageEnabled(incoming.isAdminEnabled());
        target.setAnnounceEnabled(incoming.isPostEnabled());
        target.setWorkoutEnabled(incoming.isAssignEnabled());
        membershipRepo.save(target);

        result.put("membershipId", target.getMembershipId());
        result.put("groupId", target.getGroupId());
        result.put("userId", target.getUserId());
        result.put("membershipRole", target.getAccessLevel());
        result.put("canSendMessages", target.isChatEnabled());
        result.put("canManageMembers", target.isManageEnabled());
        result.put("canPostAnnouncements", target.isAnnounceEnabled());
        result.put("canAssignWorkouts", target.isWorkoutEnabled());
        result.put("message", "Permissions updated successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * Kick a member from a group
     * Only Group_Leader or Admin can do this
     */
    @DeleteMapping("/{membershipId}")
    public ResponseEntity<Map<String, Object>> removeMember(@PathVariable int membershipId, @RequestBody KickBody incoming) {
        Map<String, Object> result = new HashMap<>();

        Optional<GroupMembership> targetQuery = membershipRepo.findById(membershipId);

        if (targetQuery.isEmpty()) {
            result.put("message", "Membership not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        Optional<GroupMembership> supervisorQuery = membershipRepo.findByGroupIdAndUserId(
                targetQuery.get().getGroupId(),
                incoming.getSupervisorId());

        if (supervisorQuery.isEmpty()) {
            result.put("message", "Supervisor is not a member of this group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        if (!supervisorQuery.get().isManageEnabled()) {
            result.put("message", "You do not have permission to remove members");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        int removedUserId = targetQuery.get().getUserId();
        int groupId = targetQuery.get().getGroupId();
        Optional<User> removedUser = userRepo.findById(removedUserId);

        membershipRepo.delete(targetQuery.get());

        result.put("message", "Member removed from group successfully");
        result.put("groupId", groupId);
        result.put("removedUserId", removedUserId);
        result.put("removedUsername",
                removedUser.map(User::getAccountName).orElse("Unknown"));
        return ResponseEntity.ok(result);

    }
}

        /**
         * Request body for updating permissions
         */

        class PermissionBody {
            private int supervisorId;
            private String newAccessLevel;
            private boolean sendEnabled;
            private boolean adminEnabled;
            private boolean postEnabled;
            private boolean assignEnabled;


            public int getSupervisorId() {

                return supervisorId;
            }

            public void setSupervisorId(int supervisorId) {

                this.supervisorId = supervisorId;
            }

            public String getNewAccessLevel() {

                return newAccessLevel;
            }

            public void setNewAccessLevel(String newAccessLevel) {
                this.newAccessLevel = newAccessLevel;
            }

            public boolean isSendEnabled() {

                return sendEnabled;
            }

            public void setSendEnabled(boolean sendEnabled) {
                this.sendEnabled = sendEnabled;
            }


            public boolean isAdminEnabled() {
                return adminEnabled;
            }


            public boolean isAssignEnabled() {
                return assignEnabled;
            }

            public void setAdminEnabled(boolean adminEnabled) {
                this.adminEnabled = adminEnabled;
            }

            public boolean isPostEnabled() {
                return postEnabled;
            }

            public void setPostEnabled(boolean postEnabled) {
                this.postEnabled = postEnabled;
            }

            public void setAssignEnabled(boolean assignEnabled) {
                this.assignEnabled = assignEnabled;

            }
        }

            /**
             * Request body for kicking a member
             */

            class KickBody {
                private int supervisorId;

                public int getSupervisorId() {
                    return supervisorId;
                }

                public void setSupervisorId(int supervisorId) {
                    this.supervisorId = supervisorId;
                }
            }




