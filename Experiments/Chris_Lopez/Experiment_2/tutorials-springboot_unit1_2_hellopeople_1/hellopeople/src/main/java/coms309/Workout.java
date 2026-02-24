package coms309;



/**
 * Stores the workout including time, name, category, and energy burned
 * @author Chris Lopez
 */

public class Workout {
    private String workoutName;
    private int energyBurned;
    private String category;
    private int timeSpentMinutes;

    public Workout(){

    }

    public Workout(String workoutName, String category, int timeSpentMinutes, int energyBurned) {
        this.workoutName = workoutName;
        this.category = category;
        this.timeSpentMinutes = timeSpentMinutes;
        this.energyBurned = energyBurned;
    }

    public String getWorkoutName(){

        return this.workoutName;
    }

    public void setWorkoutName(String workoutName){
        this.workoutName = workoutName;
    }

    public String getCategory(){

        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTimeSpentMinutes(){

        return timeSpentMinutes;
    }

    public void setTimeSpentMinutes(int timeSpentMinutes){

        this.timeSpentMinutes = timeSpentMinutes;
    }

    public int getEnergyBurned(){
        return energyBurned;
    }

    public void setEnergyBurned(int energyBurned){
        this.energyBurned = energyBurned;
    }

    @Override
    public String toString(){
        return workoutName + " [" + category + "] | " + timeSpentMinutes + "mins | " + energyBurned + " calories";
    }
}
