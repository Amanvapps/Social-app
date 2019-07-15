package e.aman.socialapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
private TextView  userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
private CircleImageView userProfileImage;

private DatabaseReference ProfileUserRef , FriendsRef , PostsRef;
private FirebaseAuth mAuth;

private String CurrentUserId;
private int count_friends = 0 , count_posts = 0;

private Button MyPosts , MyFriends;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        ProfileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("posts");


        userName = (TextView) findViewById(R.id.my_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        userRelation = (TextView) findViewById(R.id.my_relationship_status);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        MyFriends = (Button) findViewById(R.id.my_friends_button);
        MyPosts = (Button) findViewById(R.id.my_post_button);


        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendUserToFriendsActivity();
            }
        });

   MyPosts.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view)
       {
           sendUserToMyPostsActivity();
       }
   });


   PostsRef.orderByChild("uid")
           .startAt(CurrentUserId).endAt(CurrentUserId + "\uf8ff").addValueEventListener(new ValueEventListener() {
       @Override
       public void onDataChange(DataSnapshot dataSnapshot)
       {
           if(dataSnapshot.exists())
           {
               count_posts = (int) dataSnapshot.getChildrenCount();
               MyPosts.setText(Integer.toString(count_posts) + "  posts");
           }
           else
           {
               MyPosts.setText("0  posts");
           }
       }

       @Override
       public void onCancelled(DatabaseError databaseError) {

       }
   });




   FriendsRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
       @Override
       public void onDataChange(DataSnapshot dataSnapshot)
       {
           if(dataSnapshot.exists())
           {
               count_friends  = (int) dataSnapshot.getChildrenCount();
               MyFriends.setText(Integer.toString(count_friends) + "  friends");

           }
           else
           {
               MyFriends.setText("0  friends") ;
           }
       }

       @Override
       public void onCancelled(DatabaseError databaseError) {

       }
   });


        ProfileUserRef.addValueEventListener(new ValueEventListener()
        {
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

               Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

               userName.setText("@" + myUsername);
               userProfName.setText(myUserProfileName);
               userStatus.setText(myProfileStatus);
               userCountry.setText("Country:" + myCountry);
               userGender.setText("Gender:" + myGender);
               userRelation.setText("Relationship Status:" + myRelationshipstatus);
               userDOB.setText("DOB:" + myDOB);

           }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToFriendsActivity()
    {
        Intent FriendsActivityIntent = new Intent(ProfileActivity.this , FriendsActivity.class);
        startActivity(FriendsActivityIntent);
    }

    private void sendUserToMyPostsActivity()
    {
        Intent MyPostsActivityIntent = new Intent(ProfileActivity.this , MyPostsActivity.class);
        startActivity(MyPostsActivityIntent);
    }

}
