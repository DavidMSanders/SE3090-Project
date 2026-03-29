package onetoone.Groups;
import jakarta.persistence.*;


/**
 * Group Entity
 * @author Chris Lopez
 */

@Entity
@Table(name = "groups_table")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int groupId;

    @Column(name = "group_type", nullable = false)
    private String groupType;

    @Column(name = "group_title", nullable = false)
    private String groupTitle;

    @Column(name = "group_details")
    private String groupDetails;

    public Group() {
    }

    public Group(String groupTitle, String groupType, String groupDetails) {
        this.groupDetails = groupDetails;
        this.groupTitle = groupTitle;
        this.groupType = groupType;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }


    public String getGroupType() {
        return groupType;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDetails() {
        return getGroupDetails();
    }

    public void setGroupDetails(String groupDetails) {
        this.groupDetails = groupDetails;
    }

    @Override
    public String toString() {
        return "Group{" + "groupId=" + groupId + ", groupType='" + groupType + '\'' + groupId + ", groupType='" + '\'' +
                ", groupTitle='" + groupTitle + '\'' + ", groupDetails='" + groupDetails + '\'' + '}';


    }
}
