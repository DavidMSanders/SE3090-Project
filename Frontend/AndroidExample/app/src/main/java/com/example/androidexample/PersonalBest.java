package com.example.androidexample;



public class PersonalBest {
    public int id;
    public int userId;
    public String exerciseName;
    public double bestValue;
    public String unit;
    public String recordDate;

    public PersonalBest() {
    }
    public PersonalBest(int id, int userId, String exerciseName, double bestValue, String unit, String recordDate) {
        this.id = id;
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.bestValue = bestValue;
        this.unit = unit;
        this.recordDate = recordDate;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public double getBestValue() {
        return bestValue;
    }

    public void setBestValue(double bestValue) {
        this.bestValue = bestValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }
}