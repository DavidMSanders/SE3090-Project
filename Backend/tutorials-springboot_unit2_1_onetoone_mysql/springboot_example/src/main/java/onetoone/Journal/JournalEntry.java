package onetoone.Journal;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

//check during office hours, possible that CalenderEventController is using this? Did it a while ago might not have implemented it properly...
//anything calender should be using Calender, and everything Journal is for Journal.
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate entryDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    public JournalEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public JournalEntry(Long userId, LocalDate entryDate, String content) {
        this.userId = userId;
        this.entryDate = entryDate;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
