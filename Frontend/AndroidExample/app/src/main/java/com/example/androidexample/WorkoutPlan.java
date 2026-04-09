package com.example.androidexample;

public class WorkoutPlan {
    private int workoutPlanId;
    private int trainerId;

    private String pName;
    private String pDescription;

    private String createdAt;

    public WorkoutPlan() {}

    public WorkoutPlan(int workoutPlanId, int trainerId, String pName, String pDescription, String createdAt){
        this.workoutPlanId = workoutPlanId;
        this.trainerId = trainerId;
        this.pName = pName;
        this.pDescription = pDescription;
        this.createdAt = createdAt;
    }

    public int getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(int workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }
    public String getpDescription() {
        return pDescription;
    }

    public void setpDescription(String pDescription) {
        this.pDescription = pDescription;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
