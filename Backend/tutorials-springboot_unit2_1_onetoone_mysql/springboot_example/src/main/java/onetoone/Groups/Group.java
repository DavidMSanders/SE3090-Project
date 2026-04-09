package onetoone.Groups;

import jakarta.persistence.*;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "gym_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private String groupName;

    private String groupType;

    @Column(columnDefinition = "TEXT")
    private String groupDescription;

    public Group() {
    }

    public Group(String groupName, String groupType, String groupDescription) {
        this.groupName = groupName;
        this.groupType = groupType;
        this.groupDescription = groupDescription;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }
}
