package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;
public class Workout {

        private int workoutId;
        private int userId;
        private Integer assignedWorkoutId; //nullable obviously
        private int createdByUserId;
        private String WorkoutName;
        private String description;
        private Integer durationMinutes; //nullable for assigned-workouts
        private String scheduledDate;
        private String status;
        private String sourceType;

        //extra field we should probably add later
        private String createdByUsername;





        public Workout(int workoutId, int userId,
                       Integer assignedWorkoutId,
                       int createdByUserId, String workoutName,
                       String description, Integer durationMinutes,
                       String scheduledDate, String status,
                       String sourceType){
            this.workoutId = workoutId;
            this.userId = userId;
            this.assignedWorkoutId = assignedWorkoutId;
            this.createdByUserId = createdByUserId;
            this.WorkoutName = workoutName;
            this.description = description;
            this.durationMinutes = durationMinutes;
            this.scheduledDate = scheduledDate;
            this.status = status;
            this.sourceType = sourceType;
        }



        //Convert Json to a Workout object
        public static Workout fromJson(JSONObject json) throws JSONException {
            return new Workout(
                    json.getInt("workoutId"),
                    json.getInt("userId"),
                    json.isNull("assignedWorkoutId") ? null : json.getInt("assignedWorkoutId"),
                    json.getInt("createdByUserId"),
                    json.getString("workoutName"),
                    json.optString("workoutDescription", ""),
                    json.isNull("durationMinutes") ? null : json.getInt("durationMinutes"),
                    json.getString("scheduledDate"),
                    json.getString("status"),
                    json.getString("sourceType")
            );
        }

//    @Override
//    public String toString() {
//        return "Workout ID: " + id + "\n"+
//                "Name: " + name + "\n" +
//                "Date: " + date + "\n" +
//                "Duration: " + durationMinutes + " minutes\n" +
//                "Description: " + description;
//    }


    // For updating a self-created workout only
    public JSONObject toUpdateJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("workoutName", WorkoutName);
        json.put("workoutDescription", description);
        json.put("durationMinutes", durationMinutes == null ? JSONObject.NULL : durationMinutes);
        json.put("scheduledDate", scheduledDate);
        return json;
    }
    public int getWorkoutId() {
            return workoutId;
        }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public Integer getAssignedWorkoutId() {
        return assignedWorkoutId;
    }
    public void setAssignedWorkoutId(Integer assignedWorkoutId) {
        this.assignedWorkoutId = assignedWorkoutId;
    }
    public int getCreatedByUserId() {
        return createdByUserId;
    }
    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    public String getWorkoutName() {
        return WorkoutName;
    }
    public void setWorkoutName(String workoutName) {
        WorkoutName = workoutName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    public String getScheduledDate() {
        return scheduledDate;
    }
    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public boolean isSelfCreated() {
        return "self_created".equalsIgnoreCase(sourceType);
    }

    public boolean isAssignedFromPlan() {
        return "assigned_from_plan".equalsIgnoreCase(sourceType);
    }





}
