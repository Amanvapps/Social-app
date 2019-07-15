package e.aman.socialapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendRequestButton,DeclineFriendRequestButton;

    private DatabaseReference FriendRequestReference,UsersRef,FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId,ReceiverUserId , CURRENT_STATE, saveCurrentDate;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth=FirebaseAuth.getInstance();

       senderUserId = mAuth.getCurrentUser().getUid();
        ReceiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        initializeFields();

        UsersRef.child(ReceiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage=dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername=dataSnapshot.child("username").getValue().toString();
                    String myUserProfileName=dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus=dataSnapshot.child("status").getValue().toString();
                    String myCountry=dataSnapshot.child("country").getValue().toString();
                    String myGender=dataSnapshot.child("gender").getValue().toString();
                    String myRelationshipstatus=dataSnapshot.child("relationshipstatus").getValue().toString();
                    String myDOB=dataSnapshot.child("dob").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUsername);
                    userProfName.setText(myUserProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country:" + myCountry);
                    userGender.setText("Gender:" + myGender);
                    userRelation.setText("Relationship Status:" + myRelationshipstatus);
                    userDOB.setText("DOB:" + myDOB);

                    maintainanceOfButtons();

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(!senderUserId.equals(ReceiverUserId))
        {
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendFriendRequestButton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")) {
                        sendFriendRequestToAPerson();
                    }
                    if (CURRENT_STATE.equals("request_send")) {
                        cancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")){
                        acceptFriendRequest();
                    }
                   if(CURRENT_STATE.equals("friends"))
                   {
                       unfriendExistingFriend();
                   }


                }
            });
        }
        else
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void unfriendExistingFriend()
    {
        FriendsRef.child(senderUserId).child(ReceiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendsRef.child(ReceiverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendRequestButton.setEnabled(true);
                                CURRENT_STATE = "not_friends";
                                SendFriendRequestButton.setText("Send Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }


    private void acceptFriendRequest()
    {
        Calendar CallForDate = Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(CallForDate.getTime());

        FriendsRef.child(senderUserId).child(ReceiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
               if(task.isSuccessful())
               {
                   FriendsRef.child(ReceiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task)
                       {
                           if(task.isSuccessful())
                           {
                               FriendRequestReference.child(senderUserId).child(ReceiverUserId)
                                       .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task)
                                   {
                                       if(task.isSuccessful())
                                       {
                                           FriendRequestReference.child(ReceiverUserId).child(senderUserId)
                                                   .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task)
                                               {
                                                   if(task.isSuccessful())
                                                   {
                                                       SendFriendRequestButton.setEnabled(true);
                                                       CURRENT_STATE = "friends";
                                                       SendFriendRequestButton.setText("Unfriend this person");

                                                       DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                       DeclineFriendRequestButton.setEnabled(false);

                                                   }
                                               }
                                           });
                                       }
                                   }
                               });
                           }
                       }
                   });

               }
            }
        });



    }

    private void cancelFriendRequest()
    {
        FriendRequestReference.child(senderUserId).child(ReceiverUserId)
               .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestReference.child(ReceiverUserId).child(senderUserId)
                           .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendRequestButton.setEnabled(true);
                                CURRENT_STATE = "not_friends";
                                SendFriendRequestButton.setText("Send Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void maintainanceOfButtons()
    {
        FriendRequestReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(ReceiverUserId))
                {
                    String Request_Type=dataSnapshot.child(ReceiverUserId).child("request_type").getValue().toString();
                    if(Request_Type.equals("send"))
                    {
                        CURRENT_STATE = "request_send";
                        SendFriendRequestButton.setText("Cancel Friend Request");
                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                        DeclineFriendRequestButton.setEnabled(false);
                    }
                    else if(Request_Type.equals("received"))
                    {
                        CURRENT_STATE = "request_received";
                        SendFriendRequestButton.setText("Accept Friend Request");
                        DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                        DeclineFriendRequestButton.setEnabled(true);

                        DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                cancelFriendRequest();
                            }
                        });

                    }



                }
                else
                {
                    FriendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.hasChild(ReceiverUserId))
                            {
                                CURRENT_STATE = "friends";
                                SendFriendRequestButton.setText("Unfriend this person");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendFriendRequestToAPerson()
    {
        FriendRequestReference.child(senderUserId).child(ReceiverUserId).child("request_type")
                .setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
               if(task.isSuccessful())
               {
                   FriendRequestReference.child(ReceiverUserId).child(senderUserId)
                           .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task)
                       {
                           if(task.isSuccessful())
                           {
                               SendFriendRequestButton.setEnabled(true);
                               CURRENT_STATE = "request_send";
                               SendFriendRequestButton.setText("Cancel Friend Request");

                               DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                               DeclineFriendRequestButton.setEnabled(false);

                           }
                       }
                   });
               }
            }
        });
    }

    private void initializeFields()
    {
        userName=(TextView) findViewById(R.id.person_username);
        userProfName=(TextView)findViewById(R.id.person_full_name);
        userStatus=(TextView)findViewById(R.id.person_profile_status);
        userCountry=(TextView)findViewById(R.id.person_country);
        userGender=(TextView)findViewById(R.id.person_gender);
        userRelation=(TextView)findViewById(R.id.person_relationship_status);
        userDOB=(TextView)findViewById(R.id.person_dob);
        userProfileImage=(CircleImageView)findViewById(R.id.person_profile_pic);
        SendFriendRequestButton=(Button)findViewById(R.id.person_send_friend_request_button);
        DeclineFriendRequestButton=(Button)findViewById(R.id.person_decline_friend_request_button);

        CURRENT_STATE = "not_friends";


    }


}
