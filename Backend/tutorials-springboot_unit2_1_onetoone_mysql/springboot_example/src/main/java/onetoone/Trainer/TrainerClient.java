package onetoone.Trainer;

import jakarta.persistence.*;
import onetoone.Users.User;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "trainer_clients")
public class TrainerClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    public TrainerClient() {
    }

    public TrainerClient(User trainer, User client) {
        this.trainer = trainer;
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getTrainer() {
        return trainer;
    }

    public void setTrainer(User trainer) {
        this.trainer = trainer;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }
}
