package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;
public class Workout {

        private int id;
        private String name;
        private String date;
        private int durationMinutes;

        private String description;


        public Workout(int id, String name, String date, int durationMinutes, String description){
            this.id = id;
            this.name = name;
            this.date = date;
            this.durationMinutes = durationMinutes;
            this.description = description;

        }

        //Convert Json to a Workout object
    public static Workout fromJson(JSONObject json) throws JSONException {
        return new Workout(
        json.getInt("id"),
        json.getString("name"),
        json.getString("date"),
        json.getInt("durationMinutes"),
        json.getString("description")
        );
    }

    @Override
    public String toString() {
        return "Workout ID: " + id + "\n"+
                "Name: " + name + "\n" +
                "Date: " + date + "\n" +
                "Duration: " + durationMinutes + " minutes\n" +
                "Description: " + description;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("date", date);
        json.put("durationMinutes", durationMinutes);
        json.put("description", description);
        return json;
    }
    public int getID() {return id;}
    public String getName() {return name;}
    public String getDate() {return date;}
    public int getDuration() {return durationMinutes;}
    public String getDescription() {return description;}

    //set functions
    public void setName(String name){this.name = name;}
    public void setDate(String date){this.date = date;}
    public void setDuration(int duration){this.durationMinutes = duration;}
    public void setDescription(String description){this.description = description;}


}
