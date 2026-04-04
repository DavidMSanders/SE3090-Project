package onetoone.Chat_Messages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/*
@author Chris Lopez
 */
public interface MessageRepository extends JpaRepository<Message, Long>{
@Query("SELECT m FROM Message m WHERE " + "((m.originatorId = :firstPerson AND m.targetUserId = :secondPerson) OR " +
"(m.originatorId = :secondPerson AND m.targetUserId = :firstPerson)) " + "AND m.activeFlag = true " +
        "ORDER BY m.createdTimestamp ASC")

    List<Message> fetchConversation(@Param("firstPerson") Integer firstPerson, @Param("secondPerson") Integer secondPerson);
    List <Message> findByOriginatorIdAndActiveFlagTrueOrderByCreatedTimestampDesc(Integer originatorId);
    List <Message> findByTargetUserIdAndActiveFlagTrueOrderByCreatedTimestampDesc(Integer targetUserId);
    List<Message> findByGroupIdOrderByCreatedTimestampAsc(Integer groupId);
    List<Message> findByGroupId(Integer groupId);


    @Query("SELECT m FROM Message m WHERE " + "(m.originatorId = :personId OR m.targetUserId = :personId) " +
            "AND m.activeFlag = true " + "ORDER BY m.createdTimestamp DESC")


    List <Message> retrieveAllMessagesForUser(@Param("personId") Integer personId);


    @Query("SELECT m FROM Message m WHERE " + "m.targetUserId = :personId " + "AND m.readFlag = false " +
            "AND m.activeFlag = true " + "ORDER BY m.createdTimestamp DESC")
    List<Message> fetchUnreadMessages(@Param("personId") Integer personId);

    @Query("SELECT m FROM Message m WHERE " + "(m.originatorId = :personId OR m.targetUserId = :personId) " +
            "AND m.textBody LIKE %:queryText% " + "AND m.activeFlag = true " +
      "ORDER BY m.createdTimestamp DESC")
    List <Message> searchMessageText(@Param("personId") Integer personId, @Param("queryText") String queryText);


}
