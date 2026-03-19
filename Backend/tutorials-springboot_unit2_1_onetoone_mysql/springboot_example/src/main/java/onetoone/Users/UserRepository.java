package onetoone.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 
 * @author Chris Lopez
 * 
 */ 

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByAccountNameAndBirthDate(String accountName, LocalDate birthDate);

    User findByAccountName(String accountName);


}
