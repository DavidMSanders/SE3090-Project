package onetoone.Workout;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 *Workout Entity
 *@author Chris Lopez
 */
@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int workoutId;

    @Column(name = "name")
    private String workoutName;

    @Column(name = "date")
    private String sessionDate;

    @Column( name = "duration_minutes")
    private int minutes;

    @Column(name = "description")
    private String details;

    public Workout(){
    }

    public Workout(String workoutName, LocalDate sessionDate, int minutes, String details){
        this.workoutName = workoutName;
        this.sessionDate = sessionDate;
        this.minutes = minutes;
        this.details = details;
    }


    public int getWorkoutId(){
        return workoutId;
    }

    public void setWorkoutId(int workoutId){
        this.workoutId = workoutId;
    }


    public void setWorkoutName(String workoutName){
        this.workoutName = workoutName;
    }

    public String getWorkoutName(){
        return workoutName;
    }

    public void setSessionDate(LocalDate sessionDate){
        this.sessionDate = sessionDate;
    }

    public LocalDate getSessionDate(){
        return sessionDate;
    }


    public String getDetails(){
        return details;
    }

    public int getMinutes(){
        return minutes;
    }



    public void setDetails(String details){
        this.details = details;
    }

    public void setMinutes(int minutes){
        this.minutes = minutes;
    }

    @Override
    public String toString(){
        return "Workout{" +
                "workoutId=" + workoutId +
                ", workoutName=" + workoutName + '\'' +
                ", sessionDate=" + sessionDate +
                ", minutes=" + minutes +
                ", details=" + details + '\'' +
                '}';
    }

}
