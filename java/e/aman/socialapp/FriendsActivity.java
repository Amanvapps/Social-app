package e.aman.socialapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView MyFriendList;

    private DatabaseReference FriendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String OnlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        OnlineUserId = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(OnlineUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        MyFriendList = (RecyclerView) findViewById(R.id.friend_list);
        MyFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        MyFriendList.setLayoutManager(linearLayoutManager);


        DisplayAllFriends();

    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        FriendsRef
                ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                final String usersIDs = getRef(position).getKey();
                viewHolder.setDate(model.getDate());

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("fullname").getValue().toString();
                        final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                        final String type;

                                if(dataSnapshot.hasChild("userState"))
                                {
                                    type = dataSnapshot.child("userState").child("type").getValue().toString();

                                    if(type.equals("online"))
                                    {
                                        viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                    }

                                }


                        viewHolder.setProfileimage(getApplicationContext(),profileImage);
                        viewHolder.setFullname(username);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]
                                        {
                                          username + "'s profile " ,
                                          "Send Message"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle("Select Options");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if(i == 0)
                                        {
                                            Intent profileIntent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                            profileIntent.putExtra("visit_user_id",usersIDs);
                                            startActivity(profileIntent);
                                        }
                                        if(i == 1)
                                        {
                                            Intent ChatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                            ChatIntent.putExtra("visit_user_id",usersIDs);
                                            ChatIntent.putExtra("username",username);
                                            startActivity(ChatIntent);
                                        }

                                    }
                                });

                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        MyFriendList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView onlineStatusView;



        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname) {
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_name);
            myName.setText(fullname);
        }

        public void setDate(String date){
            TextView FriendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            FriendsDate.setText("Friends Since: " + date);
        }


    }


    public void updateUserStatus(String state)
    {
        String saveCurrentDate , saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        usersRef.child(OnlineUserId).child("userState").updateChildren(currentStateMap);


    }

    @Override
    protected void onStart() {
        super.onStart();
    updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

}