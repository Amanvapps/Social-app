package e.aman.socialapp;


import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.cert.CertPathBuilderSpi;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private ImageButton AddNewPostButton;
    private Toolbar mtoolbar;
   private  FirebaseAuth mAuth;
   private DatabaseReference user_ref,postsRef, LikesRef;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CircleImageView navProfileImage;
    private TextView navUsername ;
String currentuserId;

Boolean LikeChecker=false;

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        postsRef=FirebaseDatabase.getInstance().getReference().child("posts");
        mAuth=FirebaseAuth.getInstance();
        LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
       user_ref= FirebaseDatabase.getInstance().getReference().child("Users");
       currentuserId=mAuth.getCurrentUser().getUid();
       AddNewPostButton=(ImageButton)findViewById(R.id.add_new_post_button);


        mtoolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView=(NavigationView)findViewById(R.id.navigation_view);


      postList=(RecyclerView)findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);
 navProfileImage=(CircleImageView) navView.findViewById(R.id.nav_profile_image);
    navUsername=(TextView) navView.findViewById(R.id.nav_user_full_name);


    user_ref.child(currentuserId).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                if(dataSnapshot.hasChild("fullname"))
                {
                    String fullname=dataSnapshot.child("fullname").getValue().toString();
                    navUsername.setText(fullname);

                }
                if(dataSnapshot.hasChild("profileimage")){
                    String image=dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);

                }
                else{
                    Toast.makeText(getApplicationContext(),"Profile name donot exist",Toast.LENGTH_SHORT).show();
                }
           }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });


    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                user_menu_selector(item);
                return false;
            }
        });


AddNewPostButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
sendUserToPostActivity();
    }
});
  DisplayAllUsersPost();





}




    private void DisplayAllUsersPost()
    {
        Query SortPostsInDescendingOrder = postsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (
            Posts.class,
            R.layout.all_posts_layout,
        PostsViewHolder.class,
            SortPostsInDescendingOrder
            )
        {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

                final String PostKey=getRef(position).getKey();



              viewHolder.setFullname(model.getFullname());
              viewHolder.setTime(model.getTime());
              viewHolder.setDate(model.getDate());
              viewHolder.setDescription(model.getDescription());
              viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
              viewHolder.setPostimage(getApplicationContext(),model.getPostimage());

                viewHolder.setLikeButtonStatus(PostKey);


              viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view)
                  {
                  Intent ClickPostIntent=new Intent(MainActivity.this,ClickPostActivity.class);
                  ClickPostIntent.putExtra("PostKey",PostKey);
                  startActivity(ClickPostIntent);
                  }
              });

              viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view)
                  {
                      Intent CommentsIntent=new Intent(MainActivity.this,CommentsActivity.class);
                      CommentsIntent.putExtra("PostKey",PostKey);
                      startActivity(CommentsIntent);
                  }
              });


              viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view)
                  {
                     LikeChecker = true;

                     LikesRef.addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot)
                         {
                            if(LikeChecker.equals(true))
                            {
                                if(dataSnapshot.child(PostKey).hasChild(currentuserId))
                                {
                                    LikesRef.child(PostKey).child(currentuserId).removeValue();
                                    LikeChecker = false;
                                }
                                else
                                {
                                    LikesRef.child(PostKey).child(currentuserId).setValue(true);
                                    LikeChecker = false;
                                }
                            }
                         }

                         @Override
                         public void onCancelled(DatabaseError databaseError) {

                         }
                     });

                  }
              });

             //   updateUserStatus("online");

            }
        };
   postList.setAdapter(firebaseRecyclerAdapter);


    }

 public static class PostsViewHolder extends RecyclerView.ViewHolder
 {

    View mView;

    ImageButton LikePostButton,CommentPostButton;
    TextView DisplayNoOfLikes;
    int count_likes;
    String currentUserId;
    DatabaseReference LikesRef;


     public PostsViewHolder(View itemView) {
         super(itemView);
        mView=itemView;

        LikePostButton=(ImageButton) mView.findViewById(R.id.like_button);
        DisplayNoOfLikes=(TextView) mView.findViewById(R.id.display_no_of_likes);
        CommentPostButton=(ImageButton) mView.findViewById(R.id.comment_button);

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

     }

     public void setLikeButtonStatus(final String Postkey)
     {
         LikesRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot)
             {
                 if(dataSnapshot.child(Postkey).hasChild(currentUserId))
                 {
                     count_likes= (int) dataSnapshot.child(Postkey).getChildrenCount();
                     LikePostButton.setImageResource(R.drawable.like);
                     DisplayNoOfLikes.setText(Integer.toString(count_likes) + (" Likes"));
                 }
                 else
                 {
                     count_likes= (int) dataSnapshot.child(Postkey).getChildrenCount();
                     LikePostButton.setImageResource(R.drawable.dislike);
                     DisplayNoOfLikes.setText(Integer.toString(count_likes) + (" Likes"));
                 }

             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });
     }


     public void setFullname(String fullname) {
         TextView username=(TextView) mView.findViewById(R.id.post_username);
         username.setText(fullname);
     }
     public void setProfileimage(Context ctx,String profileimage) {
         CircleImageView image=(CircleImageView) mView.findViewById(R.id.post_profile_image);
         Picasso.with(ctx).load(profileimage).into(image);
     }
     public void setTime(String time) {
         TextView PostTime=(TextView) mView.findViewById(R.id.post_time);
         PostTime.setText("   " + time);

     }
     public void setDate(String date) {
         TextView PostDate=(TextView) mView.findViewById(R.id.post_date);
         PostDate.setText("   " + date);
     }
     public void setDescription(String description)
     {
         TextView PostDescription=(TextView) mView.findViewById(R.id.post_description);
         PostDescription.setText(description);
     }
     public void setPostimage(Context ctx1,String postimage) {
      ImageView PostImage=(ImageView) mView.findViewById(R.id.post_image);
      Picasso.with(ctx1).load(postimage).into(PostImage);
     }
 }

    private void sendUserToPostActivity() {
    Intent addNewPostIntent=new Intent(MainActivity.this,PostActivity.class);
    startActivity(addNewPostIntent);
    }

    private void user_menu_selector(MenuItem item)
    {
    switch(item.getItemId()){
        case R.id.nav_home: {
            Toast.makeText(getApplicationContext(), "Home button selected", Toast.LENGTH_SHORT).show();
        break;}
        case R.id.nav_find_friends:
        {
          sendUserToFindFriendsActivity();
         break;
        }
        case R.id.nav_friends:{
          sendUSerToFriendsActivity();
            break;
        }
        case R.id.nav_Logout:{
          //  updateUserStatus("offline");
           mAuth.signOut();
           sendUserToLoginActivity();
            break;
        }

        case R.id.nav_messages:{
            sendUSerToFriendsActivity();
            break;
        }
        case R.id.nav_post:{
          sendUserToPostActivity();
            break;
        }
        case R.id.nav_profile:
        {
            sendUserToProfileActivity();
        break;
        }
        case R.id.nav_settings:
        {

            sendUserToSettingsActivity();

        break;}
        }


    }
/*
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

        user_ref.child(currentuserId).child("userState").updateChildren(currentStateMap);


    }
*/
    private void sendUSerToFriendsActivity()
    {
        Intent FriendsIntent=new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(FriendsIntent);
    }

    private void sendUserToFindFriendsActivity()
    {
        Intent SettingsIntent=new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(SettingsIntent);

    }

    private void sendUserToProfileActivity()
    {
        Intent SettingsIntent=new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(SettingsIntent);
    }

    private void sendUserToSettingsActivity() {

    Intent SettingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
    startActivity(SettingsIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser=mAuth.getCurrentUser();
        if(currentuser==null){
            sendUserToLoginActivity();
        }
        else if(currentuser!=null){
            CheckUserExistence();

        }
    }


    private void CheckUserExistence() {
    final String current_user_id=mAuth.getCurrentUser().getUid();
    user_ref=FirebaseDatabase.getInstance().getReference().child("Users");
    user_ref.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
         if(!dataSnapshot.hasChild(current_user_id)){
             sendUserToSetupActivity();
         }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
    }

    private void sendUserToSetupActivity() {
    Intent setupintent=new Intent(MainActivity.this,SetupActivity.class);
        setupintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupintent);
        finish();

    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
        return super.onOptionsItemSelected(item);
    }

}
