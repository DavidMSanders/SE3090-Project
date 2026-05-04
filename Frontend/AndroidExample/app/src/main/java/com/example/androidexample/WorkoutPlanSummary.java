package com.example.androidexample;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanSummary {

    private int planId;
    private String planName;

    private int acceptedCount;
    private int rejectedCount;
    private int completedCount;
    private int pendingCount;

    private List<String> acceptedUsers = new ArrayList<>();
    private List<String> rejectedUsers = new ArrayList<>();
    private List<String> completedUsers = new ArrayList<>();
    private List<String> pendingUsers = new ArrayList<>();

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public List<String> getAcceptedUsers() {
        return acceptedUsers;
    }

    public void setAcceptedUsers(List<String> acceptedUsers) {
        this.acceptedUsers = acceptedUsers;
    }

    public List<String> getRejectedUsers() {
        return rejectedUsers;
    }

    public void setRejectedUsers(List<String> rejectedUsers) {
        this.rejectedUsers = rejectedUsers;
    }

    public List<String> getCompletedUsers() {
        return completedUsers;
    }

    public void setCompletedUsers(List<String> completedUsers) {
        this.completedUsers = completedUsers;
    }

    public List<String> getPendingUsers() {
        return pendingUsers;
    }

    public void setPendingUsers(List<String> pendingUsers) {
        this.pendingUsers = pendingUsers;
    }
}
