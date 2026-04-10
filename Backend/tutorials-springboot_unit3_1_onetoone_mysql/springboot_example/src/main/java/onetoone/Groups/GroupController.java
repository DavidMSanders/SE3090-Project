package onetoone.Groups;

import onetoone.Chat_Messages.MessageRepository;
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

//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;


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

    @Autowired
    private MessageRepository messageRepo;


    /**
     * POST /api/v1/groups
     */
    //@Operation(summary = "Create a new group")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "201", description = "Group created successfully"),
            //@ApiResponse(responseCode = "400", description = "Invalid input"),
            //@ApiResponse(responseCode = "404", description = "Creator user not found")
    //})
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody GroupRequest incoming){
        Map<String, Object> result = new HashMap<>();

        try{
            if(incoming.getGroupTitle() == null || incoming.getGroupTitle().trim().isEmpty()){
                result.put("message", "groupTitle is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            if(!incoming.getGroupType().equals("Trainer Group") && !incoming.getGroupType().equals("Direct Message Group")){
                result.put("message", "groupType must be Trainer Group or Direct Message Group");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            Optional<User> creatorQuery = userRepo.findById(incoming.getCreatorId());
            if (creatorQuery.isEmpty()) {
                result.put("message", "Creator user not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            Group newGroup = new Group(

                    incoming.getGroupTitle(),
                    incoming.getGroupType(),
                    incoming.getGroupDetails());

            Group saved = groupRepo.save(newGroup);

            String creatorRole = creatorQuery.get().getRole();
            String accessLevel = creatorRole.equals("Trainer") ? "GROUP_LEADER" : "MEMBER";

            GroupMembership membership = new GroupMembership(
                    saved.getGroupId(),
                    incoming.getCreatorId(),
                    accessLevel);
            membershipRepo.save(membership);

            result.put("groupId", saved.getGroupId());
            result.put("groupName", saved.getGroupTitle());
            result.put("groupType", saved.getGroupType());
            result.put("groupDescription", saved.getGroupDetails());
            result.put("creatorId", incoming.getCreatorId());
            result.put("accessLevel", accessLevel);
            result.put("message", "Group created successfully");
                    return ResponseEntity.status(HttpStatus.CREATED).body(result);

        }
            catch (Exception e) {
                result.put("message", "Error creating group: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
    }


    //@Operation(summary = "Get all groups with membership status for a user")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    //})
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> fetchAllGroups(
            @RequestParam int userId,
            @RequestParam(required = false) String search){
        List<Group> allGroups = groupRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();


        for (Group g : allGroups){
            if(search != null && !g.getGroupTitle().toLowerCase().contains(search.toLowerCase())){
                continue;
            }

            Map<String, Object> groupData = new HashMap<>();
            groupData.put("groupId", g.getGroupId());
            groupData.put("groupName", g.getGroupTitle());
            groupData.put("groupType", g.getGroupType());
            groupData.put("groupDescription", g.getGroupDetails());

            Optional<GroupMembership> membershipQuery = membershipRepo.findByGroupIdAndUserId(g.getGroupId(), userId);
            if (membershipQuery.isPresent()){
                groupData.put("membershipId", membershipQuery.get().getMembershipId());
                groupData.put("membershipRole", membershipQuery.get().getAccessLevel());
            } else {
                groupData.put("membershipId", null);
                groupData.put("membershipRole", null);
            } result.add(groupData);
        }
        return ResponseEntity.ok(result);
    }

    //@Operation(summary = "Get a single group by ID")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Group found"),
            //@ApiResponse(responseCode = "404", description = "Group not found")
    //})
    @GetMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> fetchGroupById(@PathVariable int groupId){
        Map<String, Object> result = new HashMap<>();

        Group found = groupRepo.findById(groupId).orElse(null);
        if (found != null){
            result.put("groupId", found.getGroupId());
            result.put("groupName", found.getGroupTitle());
            result.put("groupType", found.getGroupType());
            result.put("groupDescription", found.getGroupDetails());
            return ResponseEntity.ok(result);

        } else {
            result.put("message", "Group not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    //@Operation(summary = "Delete a group and all its memberships")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            //@ApiResponse(responseCode = "404", description = "Group not found")
    //})
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, String>> removeGroup(@PathVariable int groupId){
        Map<String, String> result = new HashMap<>();


        Group found = groupRepo.findById(groupId).orElse(null);
        if(found != null){
            membershipRepo.deleteAll(membershipRepo.findByGroupId(groupId));
            groupRepo.delete(found);
            result.put("message", "Group deleted sucessfully");
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Group not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    //@Operation(summary = "Join a group")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "201", description = "Joined group successfully"),
            //@ApiResponse(responseCode = "404", description = "Group or user not found"),
            //@ApiResponse(responseCode = "409", description = "User already a member")
    //})
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Map<String, Object>> joinGroup(@PathVariable int groupId, @RequestBody JoinRequest incoming){
        Map<String, Object> result = new HashMap<>();


        Group group = groupRepo.findById(groupId).orElse(null);
        if (group == null){
            result.put("message", "Group not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        Optional<User> accountQuery = userRepo.findById(incoming.getUserId());
        if(accountQuery.isEmpty()){
            result.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if(membershipRepo.existsByGroupIdAndUserId(groupId, incoming.getUserId())){ result.put("message" ,
                "User is already a member of this group");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }
    String accountRole = accountQuery.get().getRole();
    String entryLevel = accountRole.equals("Trainer") ? "TRAINER" : "MEMBER";

    GroupMembership newMembership = new GroupMembership(groupId, incoming.getUserId(), entryLevel);

    if (accountRole.equals("Admin")) {
        newMembership.setChatEnabled(true);
        newMembership.setManageEnabled(true);
        newMembership.setAnnounceEnabled(true);
        newMembership.setWorkoutEnabled(true);
    }


    membershipRepo.save(newMembership);

    result.put("membershipId", newMembership.getMembershipId());
    result.put("groupId", groupId);
    result.put("userId", incoming.getUserId());
    result.put("membershipRole", newMembership.getAccessLevel());
    result.put("canSendMessages", newMembership.isChatEnabled());
    result.put("canManageMembers", newMembership.isManageEnabled());
    result.put("canPostAnnouncements", newMembership.isAnnounceEnabled());
    result.put("canAssignWorkouts", newMembership.isWorkoutEnabled());
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}

    //@Operation(summary = "Leave a group")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Left group successfully"),
            //@ApiResponse(responseCode = "404", description = "User not a member of this group")
    //})
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Map<String, String>> leaveGroup(@PathVariable int groupId, @RequestBody JoinRequest incoming){
    Map <String, String> result = new HashMap<>();

    Optional<GroupMembership> membershipQuery = membershipRepo.findByGroupIdAndUserId(groupId, incoming.getUserId());

    if(membershipQuery.isPresent()){
        membershipRepo.delete(membershipQuery.get());
        result.put("message", "Left group successully");
        return ResponseEntity.ok(result);
    } else {
        result.put("message", "User not a member of this group");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    }

    //@Operation(summary = "Get all members of a group with their permissions")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Members retrieved successfully")
    //})
    @GetMapping("/{groupId}/members")
public ResponseEntity<List<Map<String, Object>>> fetchGroupMembers(@PathVariable int groupId){
    List<GroupMembership> membershipList = membershipRepo.findByGroupId(groupId);
    List<Map<String, Object>> result = new ArrayList<>();

    for(GroupMembership m : membershipList){
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("membershipId", m.getMembershipId());
        memberData.put("userId", m.getUserId());
        memberData.put("membershipRole", m.getAccessLevel());
        memberData.put("canSendMessages", m.isChatEnabled());
        memberData.put("canManageMembers", m.isManageEnabled());
        memberData.put("canPostAnnouncements", m.isAnnounceEnabled());
        memberData.put("canAssignWorkouts", m.isWorkoutEnabled());




        Optional<User> accountQuery = userRepo.findById(m.getUserId());
        memberData.put("username", accountQuery.map(User::getAccountName).orElse("Unknown"));
        result.add(memberData);
    }
    return ResponseEntity.ok(result);
}

    //@Operation(summary = "Get a specific member in a group")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Member found"),
            //@ApiResponse(responseCode = "404", description = "Member not found in this group")
    //})
@GetMapping("/{groupId}/members/{userId}")
public ResponseEntity<Map<String, Object>> fetchMemberById(@PathVariable int groupId, @PathVariable int userId){
    Map<String, Object> result = new HashMap<>();

    Optional<GroupMembership> membershipQuery = membershipRepo.findByGroupIdAndUserId(groupId, userId);

    if(membershipQuery.isPresent()){
        GroupMembership m = membershipQuery.get();
        Optional<User> accountQuery = userRepo.findById(userId);


    result.put("membershipId",m.getMembershipId());
    result.put("groupId", m.getGroupId());
    result.put("userId", m.getUserId());
    result.put("username", accountQuery.map(User::getAccountName).orElse("Unknown"));
    result.put("membershipRole", m.getAccessLevel());
    result.put("canSendMessages", m.isChatEnabled());
    result.put("canManageMembers", m.isManageEnabled());
    result.put("canPostAnnouncements", m.isAnnounceEnabled());
    result.put("canAssignWorkouts", m.isWorkoutEnabled());
    return ResponseEntity.ok(result);
} else {
        result.put("message", "Member not found in this group");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    }

    //@Operation(summary = "Get chat history for a group ordered oldest to newest")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "Messages retrieved successfully")
    //})
    @GetMapping("/{groupId}/messages")
public ResponseEntity<List<Map<String, Object>>> fetchGroupMessages(@PathVariable int groupId){
        List<Map<String, Object>> result = new ArrayList<>();
        List<onetoone.Chat_Messages.Message> chatLog = messageRepo
                .findByGroupRefOrderByCreatedTimestampAsc(groupId);
        for (onetoone.Chat_Messages.Message entry : chatLog) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", entry.getId());
            messageData.put("originatorId", entry.getOriginatorId());
            messageData.put("groupId", entry.getGroupId());
            messageData.put("targetUserId", entry.getTargetUserId());
            messageData.put("messageType", entry.getMessageType());
            messageData.put("textBody", entry.getTextBody());
            messageData.put("createdTimestamp", entry.getCreatedTimestamp());
            messageData.put("editTimestamp", entry.getEditTimestamp());


            Optional<User> accountQuery = userRepo.findById(entry.getOriginatorId());
            messageData.put("username", accountQuery.map(User::getAccountName).orElse("Unknown"));
            result.add(messageData);

        }

        return ResponseEntity.ok(result);
}


    //@Operation(summary = "Get all groups a user belongs to")
    //@ApiResponses(value = {
            //@ApiResponse(responseCode = "200", description = "User groups retrieved successfully")
    //})
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> fetchUserGroups(@PathVariable int userId){
    List<GroupMembership> membershipList = membershipRepo.findByUserId(userId);
    List<Map<String, Object>> result = new ArrayList<>();



    for(GroupMembership m : membershipList){
        Group group = groupRepo.findById(m.getGroupId()).orElse(null);
        if(group != null){
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("groupId", group.getGroupId());
            groupData.put("groupName", group.getGroupTitle());
            groupData.put("groupType", group.getGroupType());
            groupData.put("membershipRole", m.getAccessLevel());
            result.add(groupData);
        }
    }
    return ResponseEntity.ok(result);
}

    @PutMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> updateGroup(@PathVariable int groupId, @RequestBody GroupUpdateRequest incoming) {
        Map<String, Object> result = new HashMap<>();

        Group found = groupRepo.findById(groupId).orElse(null);
        if (found == null) {
            result.put("message", "Group not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        Optional<GroupMembership> requesterLookup = membershipRepo.findByGroupIdAndUserId(groupId, incoming.getRequestUserId());
        if (requesterLookup.isEmpty() || !requesterLookup.get().getAccessLevel().equals("GROUP_LEADER")) {
            result.put("message", "Only GROUP_LEADER can update the group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        found.setGroupTitle(incoming.getGroupName());
        found.setGroupDetails(incoming.getGroupDescription());
        found.setGroupType(incoming.getGroupType());
        groupRepo.save(found);

        result.put("groupId", found.getGroupId());
        result.put("requestUserId", incoming.getRequestUserId());
        result.put("groupName", found.getGroupTitle());
        result.put("groupDescription", found.getGroupDetails());
        result.put("groupType", found.getGroupType());
        return ResponseEntity.ok(result);
    }

    // 18. GET /groups/{groupId}/assignable-clients?trainerId=3
    // @GetMapping("/{groupId}/assignable-clients")
    // public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAssignableClients(
    //         @PathVariable Long groupId, @RequestParam int trainerId) {
    //     java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
    //     for (GroupMembership gm : membershipRepo.findAll()) {
    //         if (gm.getGroup() != null && gm.getGroup().getGroupId().equals(groupId)) {
    //             User u = gm.getUser();
    //             if (u != null && u.getUserId() != trainerId && "Client".equalsIgnoreCase(u.getRole())) {
    //                 java.util.Map<String, Object> map = new java.util.HashMap<>();
    //                 map.put("userId", u.getUserId());
    //                 map.put("username", u.getAccountName());
    //                 res.add(map);
    //             }
    //         }
    //     }
    //     return ResponseEntity.ok(res);
    // }

    @GetMapping("/{groupId}/assignable-clients")
    public ResponseEntity<List<Map<String, Object>>> getAssignableClients(
        @PathVariable int groupId,
        @RequestParam int trainerId) {

    List<Map<String, Object>> res = new ArrayList<>();

    for (GroupMembership gm : membershipRepo.findByGroupId(groupId)) {
        if (gm.getUserId() != trainerId) {
            Optional<User> userQuery = userRepo.findById(gm.getUserId());

            if (userQuery.isPresent()) {
                User u = userQuery.get();

                if ("Client".equalsIgnoreCase(u.getRole())) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", u.getUserId());
                    map.put("username", u.getAccountName());
                    res.add(map);
                }
            }
        }
    }

    return ResponseEntity.ok(res);
}


}

/**
 * Request body to create a group
 */
    class GroupRequest {

    private String groupTitle;
    private String groupType;
    private String groupDetails;
    private int creatorId;


    public String getGroupTitle() {

        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {

        this.groupTitle = groupTitle;
    }

    public String getGroupType() {

        return groupType;
    }

    public void setGroupType(String groupType) {

        this.groupType = groupType;
    }

    public String getGroupDetails() {

        return groupDetails;
    }

    public void setGroupDetails(String groupDetails) {

        this.groupDetails = groupDetails;
    }

    public int getCreatorId() {

        return creatorId;
    }

    public void setCreatorId(int creatorId) {

        this.creatorId = creatorId;
    }
}


    /**
     * Request body for leaving or joining a group
     */

    class JoinRequest {
        private int userId;


        public int getUserId(){

            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId; }
    }

/**
 * Request body for updating a group
 */
    class GroupUpdateRequest {
    private int groupId;
    private int requestUserId;
    private String groupName;
    private String groupDescription;
    private String groupType;

    public int getGroupId() {
        return groupId; }
    public void setGroupId(int groupId) {
        this.groupId = groupId; }

    public int getRequestUserId() {
        return requestUserId; }
    public void setRequestUserId(int requestUserId) {
        this.requestUserId = requestUserId; }

    public String getGroupName() {
        return groupName; }
    public void setGroupName(String groupName) {
        this.groupName = groupName; }

    public String getGroupDescription() {
        return groupDescription; }
    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription; }

    public String getGroupType() {
        return groupType; }
    public void setGroupType(String groupType) {
        this.groupType = groupType; }
}

