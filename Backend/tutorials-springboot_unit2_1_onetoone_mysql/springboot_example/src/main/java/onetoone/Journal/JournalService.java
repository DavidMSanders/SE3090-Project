package __rk1.backend.service;

import __rk1.backend.model.JournalEntry;
import __rk1.backend.repository.JournalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class JournalService {

    private final JournalRepository journalRepository;

    public JournalService(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    public JournalEntry createEntry(Long userId, LocalDate date, String content) {
        JournalEntry entry = new JournalEntry(userId, date, content);
        return journalRepository.save(entry);
    }

    public List<JournalEntry> getEntriesForDate(Long userId, LocalDate date) {
        return journalRepository.findByUserIdAndEntryDate(userId, date);
    }

    public List<JournalEntry> getEntriesForMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return journalRepository.findByUserIdAndEntryDateBetween(userId, start, end);
    }
}
