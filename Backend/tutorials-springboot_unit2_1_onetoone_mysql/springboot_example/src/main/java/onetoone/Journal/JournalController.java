package __rk1.backend.controller;

import __rk1.backend.model.JournalEntry;
import __rk1.backend.service.JournalService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping
    public JournalEntry createEntry(
            @RequestParam Long userId,
            @RequestParam String date,
            @RequestParam String content
    ) {
        return journalService.createEntry(
                userId,
                LocalDate.parse(date),
                content
        );
    }

    @GetMapping("/date")
    public List<JournalEntry> getEntriesByDate(
            @RequestParam Long userId,
            @RequestParam String date
    ) {
        return journalService.getEntriesForDate(
                userId,
                LocalDate.parse(date)
        );
    }

    @GetMapping("/month")
    public List<JournalEntry> getEntriesByMonth(
            @RequestParam long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return journalService.getEntriesForMonth(userId, year, month);
    }
}
