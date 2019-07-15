package e.aman.socialapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;

public class PostActivity extends AppCompatActivity {
private Toolbar mToolbar;

private ImageButton SelectPostImage;
private Button UpdatePostButton;
private EditText PostDescription;
private Uri imageUri;
private FirebaseAuth mAuth;
private String Description,downloadUrl;
private DatabaseReference usersRef,postRef;
private StorageReference PostImagesReference;
String current_user_id;
private String saveCurrentDate,saveCurrentTime,postRandomName;
private static final int galleryPick=1;
private ProgressDialog loadingbar;
private long CountPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        loadingbar=new ProgressDialog(this);
       PostImagesReference=FirebaseStorage.getInstance().getReference();
       mAuth=FirebaseAuth.getInstance();
       current_user_id=mAuth.getCurrentUser().getUid();
       postRef=FirebaseDatabase.getInstance().getReference().child("posts");
       usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        SelectPostImage=(ImageButton)findViewById(R.id.select_post_image);
        PostDescription=(EditText)findViewById(R.id.post_description);
        UpdatePostButton=(Button)findViewById(R.id.update_post_button);
        SelectPostImage.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view)
            {
                openGallery();
            }
        });

         UpdatePostButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 validatePostInfo();
             }
         });


         mToolbar=(Toolbar)findViewById(R.id.update_post_page_toolbar);
         setSupportActionBar(mToolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setDisplayShowHomeEnabled(true);
         getSupportActionBar().setTitle("Update Post");
    }

    private void validatePostInfo()
    {
       Description=PostDescription.getText().toString();

    if(imageUri==null){
        Toast.makeText(getApplicationContext(),"Please select post image",Toast.LENGTH_SHORT).show();
    }
    else if(TextUtils.isEmpty(Description)){
        Toast.makeText(getApplicationContext(),"Write something about your post",Toast.LENGTH_SHORT).show();
    }
    else
    {
        loadingbar.setTitle("Add new post");
        loadingbar.setMessage("Please wait,while we are updating your new post...");
        loadingbar.show();
        loadingbar.setCanceledOnTouchOutside(true);
        StoringImageToFirebaseStorage();
    }

    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar callforDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(callforDate.getTime());

        Calendar callforTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(callforDate.getTime());

        postRandomName=saveCurrentDate+saveCurrentTime;


      StorageReference filePath=PostImagesReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
      filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
         if(task.isSuccessful()){
             downloadUrl=task.getResult().getDownloadUrl().toString();
             SavingPostInformationToDatabase();


             Toast.makeText(getApplicationContext(),"Image Uploaded successfully to storage...",Toast.LENGTH_SHORT).show();
         }
         else{
             String message=task.getException().getMessage();
             Toast.makeText(getApplicationContext(),"Error ccurred: "+message,Toast.LENGTH_SHORT).show();
         }
          }
      });


    }

    private void SavingPostInformationToDatabase()
    {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    CountPosts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    CountPosts = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
       if(dataSnapshot.exists())
       {
           String userfullname=dataSnapshot.child("fullname").getValue().toString();
           String userprofileimage=dataSnapshot.child("profileimage").getValue().toString();

           HashMap postmap=new HashMap();
           postmap.put("uid",current_user_id);
           postmap.put("date",saveCurrentDate);
           postmap.put("time",saveCurrentTime);
           postmap.put("description",Description);
           postmap.put("postimage",downloadUrl);
           postmap.put("profileimage",userprofileimage);
           postmap.put("fullname",userfullname);
           postmap.put("counter",CountPosts);
           postRef.child(current_user_id + postRandomName).updateChildren(postmap).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
              if(task.isSuccessful())
              {
                  sendUserToMainActivity();
                  Toast.makeText(getApplicationContext(),"New post is updated succesfully",Toast.LENGTH_SHORT).show();
                  loadingbar.dismiss();
              }
              else
              {
                  Toast.makeText(getApplicationContext(),"Error occured while updating post",Toast.LENGTH_SHORT).show();
                  loadingbar.dismiss();
              }
               }
           });
       }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});



    }

    private void openGallery()
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,galleryPick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);
       if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null)
       {
           imageUri=data.getData();
           SelectPostImage.setImageURI(imageUri);                                          //to display user's image on a image button
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id=item.getItemId();
        if(id == android.R.id.home);
        {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
