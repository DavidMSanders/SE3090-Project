package onetoone.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 
 * @author Chris Lopez
 * 
 */ 

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByAccountNameAndBirthDate(String accountName, LocalDate birthDate);

    Optional<User> findFirstByAccountName(String accountName);


}
