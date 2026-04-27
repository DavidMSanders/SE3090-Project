package com.example.androidexample;



public class PersonalBest {
    public int id;
    public int userId;
    public String exerciseName;
    public double bestValue;
    public String unit;
    public String recordDate;

    public PersonalBest(int id, int userId, String exerciseName, double bestValue, String unit, String recordDate) {
        this.id = id;
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.bestValue = bestValue;
        this.unit = unit;
        this.recordDate = recordDate;
    }
}