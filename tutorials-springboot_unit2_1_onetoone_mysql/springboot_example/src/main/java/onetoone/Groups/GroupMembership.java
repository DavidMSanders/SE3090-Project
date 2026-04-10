package onetoone.Groups;

import jakarta.persistence.*;
import onetoone.Users.User;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "group_memberships")
public class GroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String membershipRole;

    private boolean canSendMessages;

    private boolean canManageMembers;

    private boolean canPostAnnouncements;

    private boolean canAssignWorkouts;

    public GroupMembership() {
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMembershipRole() {
        return membershipRole;
    }

    public void setMembershipRole(String membershipRole) {
        this.membershipRole = membershipRole;
    }

    public boolean isCanSendMessages() {
        return canSendMessages;
    }

    public void setCanSendMessages(boolean canSendMessages) {
        this.canSendMessages = canSendMessages;
    }

    public boolean isCanManageMembers() {
        return canManageMembers;
    }

    public void setCanManageMembers(boolean canManageMembers) {
        this.canManageMembers = canManageMembers;
    }

    public boolean isCanPostAnnouncements() {
        return canPostAnnouncements;
    }

    public void setCanPostAnnouncements(boolean canPostAnnouncements) {
        this.canPostAnnouncements = canPostAnnouncements;
    }

    public boolean isCanAssignWorkouts() {
        return canAssignWorkouts;
    }

    public void setCanAssignWorkouts(boolean canAssignWorkouts) {
        this.canAssignWorkouts = canAssignWorkouts;
    }
}
