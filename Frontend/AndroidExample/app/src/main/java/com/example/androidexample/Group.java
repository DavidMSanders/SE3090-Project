package com.example.androidexample;

public class Group {
    private int groupID;
    private String groupName;
    private String groupType;
    private String groupDescription;
    private Integer membershipID;

    private String membershipRole;


    public Group(int groupID, String groupName, String groupType, String groupDescription, Integer membershipID, String membershipRole){
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupType = groupType;
        this.groupDescription = groupDescription;
        this.membershipID = membershipID;
        this.membershipRole = membershipRole;
    }

    public int getGroupID(){
        return groupID;
    }

    public String getGroupName(){
        return groupName;
    }

    public String getGroupType(){
        return groupType;
    }

    public String getGroupDescription(){
        return groupDescription;
    }

    public Integer getMembershipID(){
        return membershipID;
    }


    public boolean isMember(){
        return membershipID != null;
    }

    public void setMembershipID(Integer membershipID){
        this.membershipID = membershipID;
    }

    public String getMembershipRole() {
        return membershipRole;
    }
    public void setMembershipRole(String membershipRole) {
        this.membershipRole = membershipRole;
    }







}
