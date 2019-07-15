package e.aman.socialapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
private EditText UserEmail,UserPassword,UserConfirmPassword;
private Button CreateAccountButton;
private FirebaseAuth mAuth;
private ProgressDialog loadingbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);
        UserEmail=(EditText)findViewById(R.id.register_email);
        UserPassword=(EditText)findViewById(R.id.register_password);
        UserConfirmPassword=(EditText)findViewById(R.id.register_confirm_password);
        CreateAccountButton=(Button)findViewById(R.id.register_create_account);
        loadingbar=new ProgressDialog(this);
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }



    private void CreateNewAccount()
    {
     String email=UserEmail.getText().toString();
     String password=UserPassword.getText().toString();
     String confirmPassword=UserConfirmPassword.getText().toString();

     if(TextUtils.isEmpty(email)){
         Toast.makeText(getApplicationContext(),"Please write your email",Toast.LENGTH_SHORT).show();
     }
        else if(TextUtils.isEmpty(password)) {
         Toast.makeText(getApplicationContext(), "Please write your password", Toast.LENGTH_SHORT).show();
     }
      else if(TextUtils.isEmpty(confirmPassword)){
         Toast.makeText(getApplicationContext(),"Please confirm your password",Toast.LENGTH_SHORT).show();
     }
     else if(!password.equals(confirmPassword)){
         Toast.makeText(getApplicationContext(),"Password donot match",Toast.LENGTH_SHORT).show();
     }
else{
         loadingbar.setTitle("Creating new Account");
         loadingbar.setMessage("Please wait while we are creating your Account....");
         loadingbar.show();
         loadingbar.setCanceledOnTouchOutside(true);
         mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){

                sendEmailVerificationMessage();
              loadingbar.dismiss();
            }
            else{
                String message=task.getException().getMessage();
                Toast.makeText(getApplication(), "Error occured:"+message, Toast.LENGTH_SHORT).show();
               loadingbar.dismiss();
            }

             }
         });


     }
    }

    private void sendUserToLoginActivity() {
        Intent LoginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current_user= mAuth.getCurrentUser();
        if(current_user!=null){
            sendUserToMainActivity();
        }

    }

    private void sendEmailVerificationMessage()
    {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user!= null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Please verify your account , we have sent an email verification message to your gmail account...",Toast.LENGTH_LONG).show();
                        sendUserToLoginActivity();
                        mAuth.signOut();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), "Error occurred : "+ message, Toast.LENGTH_SHORT).show();
                      mAuth.signOut();
                    }
                }
            });
        }

    }

    private void sendUserToMainActivity()
    {
        Intent MainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
