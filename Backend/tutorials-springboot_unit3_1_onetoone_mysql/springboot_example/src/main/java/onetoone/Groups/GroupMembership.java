package onetoone.Groups;

import jakarta.persistence.*;

@Entity
    @Table(name = "group_membership")
    public class GroupMembership{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int membershipId;

        @Column(name = "group_id", nullable = false)
        private int groupId;

        @Column(name = "user_id", nullable = false)
        private int userId;

        @Column(name = "access_level", nullable = false)
        private String accessLevel = "MEMBER";

        @Column(name = "chat_enabled")
        private boolean chatEnabled = true;

        @Column(name = "manage_enabled")
        private boolean manageEnabled = false;

        @Column(name = "announce_enabled")
        private boolean announceEnabled = false;

        @Column(name = "workout_enabled")
        private boolean workoutEnabled = false;

        public static final String ROLE_GROUP_LEADER = "GROUP_LEADER";
        public static final String ROLE_ADMIN = "ADMIN";
        public static final String ROLE_TRAINER = "TRAINER";
        public static final String ROLE_MEMBER = "MEMBER";

        public GroupMembership(){}

        public GroupMembership(int groupId, int userId, String accessLevel){
            this.groupId = groupId;
            this.userId = userId;
            this.accessLevel = accessLevel;
            setPermissionByRole(accessLevel);
        }

        public void setPermissionByRole(String role){
            switch(role){
                case "GROUP_LEADER":
                case "ADMIN":
                    this.chatEnabled = true;
                    this.manageEnabled = true;
                    this.announceEnabled = true;
                    this.workoutEnabled = true;
                    break;
                case "TRAINER":
                    this.chatEnabled = true;
                    this.manageEnabled = false;
                    this.announceEnabled = false;
                    this.workoutEnabled = false;
                    break;
                case "MEMBER":
                default:
                    this.chatEnabled = true;
                    this.manageEnabled = false;
                    this.announceEnabled = false;
                    this.workoutEnabled = false;
                    break;
            }
        }

        public boolean isLeaderLike() {
        return ROLE_GROUP_LEADER.equals(accessLevel) ||
        ROLE_ADMIN.equals(accessLevel);
        }

        public static String normalizeRole(String role) {
        if (role == null) return ROLE_MEMBER;
        switch (role.toUpperCase()) {
        case "GROUP_LEADER": return ROLE_GROUP_LEADER;
        case "ADMIN": return ROLE_ADMIN;
        case "TRAINER": return ROLE_TRAINER;
        default: return ROLE_MEMBER;
            }
        }

        public int getMembershipId() {
            return membershipId;
        }

        public void setMembershipId(int membershipId){
            this.membershipId = membershipId;
        }

        public int getGroupId(){
            return  groupId;
        }

        public void setGroupId(int groupId){
            this.groupId = groupId;
        }

        public int getUserId(){
            return userId;
        }

        public void setUserId(int userId){
            this.userId = userId;
        }

        public String getAccessLevel() {
            return accessLevel;
        }

        public void setAccessLevel(String accessLevel) {
            this.accessLevel = accessLevel;
        }

        public boolean isChatEnabled() {
            return chatEnabled;
        }

        public void setChatEnabled(boolean chatEnabled){
            this.chatEnabled = chatEnabled;
        }

        public boolean isManageEnabled() {
        return manageEnabled;
        }

        public void setManageEnabled(boolean manageEnabled) {
        this.manageEnabled = manageEnabled;
        }

        public boolean isAnnounceEnabled(){
            return announceEnabled;
        }
        public void setAnnounceEnabled(boolean announceEnabled) {
            this.announceEnabled = announceEnabled;
        }

        public boolean isWorkoutEnabled() {
            return workoutEnabled;
        }

        public void setWorkoutEnabled(boolean workoutEnabled) {
            this.workoutEnabled = workoutEnabled;
        }

        @Override
        public String toString(){
            return "GroupMembership{" + "membershipId=" + membershipId + ",groupId" + groupId + ", userId" + userId +
                    ", accessLevel='" + accessLevel + '\'' + '}';
        }
    }


