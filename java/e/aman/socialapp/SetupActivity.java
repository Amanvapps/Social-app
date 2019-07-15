package e.aman.socialapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
private EditText UserName,FullName,CountryName;
private Button SaveInformationbutton;
private CircleImageView ProfileImage;
private FirebaseAuth mAuth;
private DatabaseReference usersref;
private ProgressDialog loadingbar;
private StorageReference userprofileImageRef;
final static int galleryPick=1;
String currentUserId;
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    loadingbar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        usersref= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserName=(EditText)findViewById(R.id.setup_username);
        FullName=(EditText)findViewById(R.id.setup_full_name);
        CountryName=(EditText)findViewById(R.id.setup_country_name);
        SaveInformationbutton=(Button)findViewById(R.id.setup_information_button);
      ProfileImage=(CircleImageView)findViewById(R.id.setup_profile_image);
        userprofileImageRef= FirebaseStorage.getInstance().getReference().child("ProfileImages");
        SaveInformationbutton.setOnClickListener(new View.OnClickListener(){
    @Override
    public void onClick(View v) {
        saveAccountSetupInformation();
    }
});


        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPick);

            }
        });
     usersref.addValueEventListener(new ValueEventListener() {
     @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists())
              {
                  if(dataSnapshot.hasChild("profileimage")){
                      String image=dataSnapshot.child("profileimage").getValue().toString();
                      Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);

                  }
                  else{
                      Toast.makeText(getApplicationContext(),"Please select a profile image",Toast.LENGTH_SHORT).show();
                  }
               }
          }

           @Override
           public void onCancelled(DatabaseError databaseError) {

            }
        });
 }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null){
           Uri imageUri=data.getData();
           CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                   .setAspectRatio(1,1).start(this);
       }
       if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
           CropImage.ActivityResult result=CropImage.getActivityResult(data);
           if(resultCode==RESULT_OK)
           {
               loadingbar.setTitle("Profile Image");
               loadingbar.setMessage("Please wait while we are updating your profile image...");
               loadingbar.setCanceledOnTouchOutside(true);

               loadingbar.show();

               Uri resultUri=result.getUri();

               StorageReference filePath=userprofileImageRef.child(currentUserId + ".jpg");
               filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if(task.isSuccessful()){
                           Toast.makeText(getApplicationContext(),"Profile image stored successfully to firebase storage",Toast.LENGTH_SHORT).show();
                           final String downloadUrl=task.getResult().getDownloadUrl().toString();
                           usersref.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Intent selfIntent=new Intent(SetupActivity.this,SetupActivity.class);
                                    startActivity(selfIntent);
                                    Toast.makeText(getApplicationContext(),"Profile image stored successfully in firebase database",Toast.LENGTH_SHORT).show();
                                      loadingbar.dismiss();
                                }
                                else
                                    {
                                    String message=task.getException().getMessage();
                                    Toast.makeText(getApplicationContext(),"Error Occured:"+message,Toast.LENGTH_SHORT).show();
                                      loadingbar.dismiss();
                                    }
                               }
                           });


                       }
                   }
               });
           }


           }
           else{
           Toast.makeText(getApplicationContext(),"Error occurred:Image cant be cropped",Toast.LENGTH_SHORT).show();
       }




}

    private void saveAccountSetupInformation() {
    String username=UserName.getText().toString();
    String fullname=FullName.getText().toString();
    String countryname=CountryName.getText().toString();
    if(TextUtils.isEmpty(username)){
        Toast.makeText(getApplicationContext(), "Fill username", Toast.LENGTH_SHORT).show();
    }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(getApplicationContext(), "Fill your Full Name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(countryname)){
            Toast.makeText(getApplicationContext(), "Fill your country", Toast.LENGTH_SHORT).show();
        }
       else{
            loadingbar.setTitle("Saving information...");
            loadingbar.setMessage("Please wait while we are updating your information");
            loadingbar.dismiss();
            loadingbar.setCanceledOnTouchOutside(true);
          HashMap userMap= new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",countryname);
            userMap.put("status","Hey There!I Am using ChatHub,developed by AmanTechnology");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");
            usersref.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                 if(task.isSuccessful()){
                     sendUserToMainActivity();



                     Toast.makeText(getApplicationContext(), "Account information Updated", Toast.LENGTH_LONG).show();
               loadingbar.dismiss();
                 }

                 else {
                     String message=task.getException().getMessage();
                     Toast.makeText(getApplicationContext(),"Error occurred:"+message,Toast.LENGTH_LONG).show();
                loadingbar.dismiss();
                 }
                }
            });

        }
}

    private void sendUserToMainActivity() {
        Intent MainIntent=new Intent(SetupActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }

}
