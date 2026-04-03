package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    private final Context context;
    private final List<Group> groupList;

    private static final String Base_URL =
            "https://18366156-06c4-4fa7-b2cb-53279f7fd5ad.mock.pstmn.io";

    public GroupAdapter(Context context, List<Group> groupList){
        this.context = context;
        this.groupList = groupList;

    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_box, parent, false);
        return new GroupViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);

        holder.tvGroupName.setText(group.getGroupName());
        holder.tvGroupType.setText(group.getGroupType());
        holder.tvGroupDescription.setText(group.getGroupDescription());

        updateButtonState(holder, group);

        //Button enabling/disabling


        holder.btnJoinGroup.setOnClickListener(v -> {
            if (group.isMember()) {
                return;
            }

            holder.btnJoinGroup.setEnabled(false);
            holder.btnJoinGroup.setText("Joining...");

            sendJoinGroupRequest(group, holder, position);
        });


        holder.btnGoToChat.setOnClickListener(v -> {
            if (!group.isMember()) {
                Toast.makeText(context, "Join the group first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, GroupChatActivity.class);
            intent.putExtra("groupId", group.getGroupID());
            intent.putExtra("groupName", group.getGroupName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);


        });





    }




    // Send POST /api/v1/groups/{groupID}/join to join a group
    private void sendJoinGroupRequest(Group group, GroupViewHolder holder, int position){
        String url = Base_URL + "/api/v1/groups/" + group.getGroupID() + "/join";

        //get userID from keepUser
        KeepUser keepUser = new KeepUser(context);
        int userId = keepUser.getUserId();

        //make json body for req
        JSONObject body = new JSONObject();
        try{
            body.put("userId", userId);
        } catch (JSONException e){
            Toast.makeText(context, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            holder.btnJoinGroup.setEnabled(true);
            holder.btnJoinGroup.setText("Join");
            return;
        }

        // If your backend does not require a body, null is fine.
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    // Try to read membershipId from backend response if present
                    int membershipId = response.optInt("membershipId", -1);
                    String role = response.optString("role", "MEMBER");


                    if (membershipId != -1) {
                        group.setMembershipID(membershipId);
                        group.setMembershipRole(role);
                    } else {
                        //fallback if mock server/backend doesn't return it yet
                        group.setMembershipID(1);
                    }


                    notifyItemChanged(position);

                    Toast.makeText(
                            context,
                            response.toString(),
                           // "Joined " + group.getGroupName(),
                            Toast.LENGTH_LONG
                    ).show();
                },
                error -> {
                    // restore button if request failed
                    holder.btnJoinGroup.setEnabled(true);
                    holder.btnJoinGroup.setText("Join");

                    Toast.makeText(
                            context,
                            "Join failed: " + readableVolleyError(error),
                            Toast.LENGTH_LONG
                    ).show();
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private String readableVolleyError(VolleyError error) {
        if(error==null) return "Unknown error";
        if(error.networkResponse!=null) {
            return "HTTP " + error.networkResponse.statusCode;
        }
        else {
            return error.toString();
        }
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvGroupType, tvGroupDescription;
        Button btnJoinGroup, btnGoToChat;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupType = itemView.findViewById(R.id.tvGroupType);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);

            btnJoinGroup = itemView.findViewById(R.id.btnJoinGroup);
            btnGoToChat = itemView.findViewById(R.id.btnGoToChat);
        }
    }

    private void updateButtonState(GroupViewHolder holder, Group group){
        if(group.isMember()){
            holder.btnJoinGroup.setText("Joined");
            holder.btnJoinGroup.setEnabled(false);
            holder.btnJoinGroup.setAlpha(0.5f);

            holder.btnGoToChat.setEnabled(true);
            holder.btnGoToChat.setAlpha(1.0f);

        }
        else{
            holder.btnJoinGroup.setText("Join");
            holder.btnJoinGroup.setEnabled(true);
            holder.btnJoinGroup.setAlpha(1.0f);

            holder.btnGoToChat.setEnabled(false);
            holder.btnGoToChat.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return groupList == null ? 0 : groupList.size();
    }




}
