package e.aman.socialapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
private Toolbar mToolbar;

private DatabaseReference SettingsUsersRef;
private FirebaseAuth mAuth;
private String CurrentUserId;
private StorageReference userprofileImageRef;


    private ProgressDialog loadingbar;

    final static int galleryPick=1;


private EditText userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
private Button UpdateAccountSettingsButton;
private CircleImageView userProfImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadingbar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();
        userprofileImageRef= FirebaseStorage.getInstance().getReference().child("ProfileImages");
        SettingsUsersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);


        mToolbar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName=(EditText)findViewById(R.id.settings_username);
        userProfName=(EditText)findViewById(R.id.settings_profile_fullname);
        userStatus=(EditText)findViewById(R.id.settings_status);
        userCountry=(EditText)findViewById(R.id.settings_country);
        userGender=(EditText)findViewById(R.id.settings_gender);
        userRelation=(EditText)findViewById(R.id.settings_relationship_status);
        userDOB=(EditText)findViewById(R.id.settings_dob);
        userProfImage=(CircleImageView)findViewById(R.id.settings_profile_image);
        UpdateAccountSettingsButton=(Button)findViewById(R.id.update_account_settings_button);


        SettingsUsersRef.addValueEventListener(new ValueEventListener() {
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

                 Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                 userName.setText(myUsername);
                 userProfName.setText(myUserProfileName);
                 userStatus.setText(myProfileStatus);
                 userCountry.setText(myCountry);
                 userGender.setText(myGender);
                 userRelation.setText(myRelationshipstatus);
                 userDOB.setText(myDOB);

             }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               validateAccountInfo();
            }
        });

userProfImage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view)
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,galleryPick);

    }
});

    }




        private void validateAccountInfo()
    {
        String username=userName.getText().toString();
        String profilename=userProfName.getText().toString();
        String status=userStatus.getText().toString();
        String country=userCountry.getText().toString();
        String gender =userGender.getText().toString();
        String relation=userRelation.getText().toString();
        String dob=userDOB.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(getApplicationContext(),"Please set a username....",Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(profilename))
        {
            Toast.makeText(getApplicationContext(),"Please set a profile name....",Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(status))
        {
            Toast.makeText(getApplicationContext(),"Please write a status....",Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(country))
        {
            Toast.makeText(getApplicationContext(),"Select your country....",Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(getApplicationContext(),"Please select your gender....",Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(relation))
        {
            Toast.makeText(getApplicationContext(),"Set your relationship status",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(getApplicationContext(),"Enter your Date Of Birth....",Toast.LENGTH_SHORT).show();

        }
        else
        {
            loadingbar.setTitle("Profile Image");
            loadingbar.setMessage("Please wait while we are updating your profile image...");
            loadingbar.setCanceledOnTouchOutside(true);
            updateAccountInfo(username,profilename,status,country,gender,relation,dob);
        }

    }

    private void updateAccountInfo(String username, String profilename, String status, String country, String gender, String relation, String dob)
    {
        HashMap usermap= new HashMap();
        usermap.put("country",country);
        usermap.put("username",username);
        usermap.put("country",country);
        usermap.put("dob",dob);
        usermap.put("fullname",profilename);
        usermap.put("gender",gender);
        usermap.put("relationshipstatus",relation);
        usermap.put("status",status);
        SettingsUsersRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    sendUserToMainActivity();
                    Toast.makeText(getApplicationContext(), "Account Settings Updated Successfully", Toast.LENGTH_SHORT).show();
               loadingbar.dismiss();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Error Occured while updating account information",Toast.LENGTH_SHORT).show();
              loadingbar.dismiss();
                }
            }
        });






    }
    private void sendUserToMainActivity()
    {
        Intent MainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }


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

                StorageReference filePath=userprofileImageRef.child(CurrentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Profile image stored successfully to firebase storage",Toast.LENGTH_SHORT).show();
                            final String downloadUrl=task.getResult().getDownloadUrl().toString();
                            SettingsUsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent selfIntent=new Intent(SettingsActivity.this,SettingsActivity.class);
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


}
