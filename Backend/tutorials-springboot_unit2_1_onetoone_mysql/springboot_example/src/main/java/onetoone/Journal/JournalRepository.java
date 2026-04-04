package onetoone.Journal;

import onetoone.Journal.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JournalRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    List<JournalEntry> findByUserIdAndEntryDateBetween(
            Long userId,
            LocalDate start,
            LocalDate end);
}