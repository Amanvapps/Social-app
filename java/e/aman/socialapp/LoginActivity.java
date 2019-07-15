package e.aman.socialapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
private Button LoginButton;
private EditText UserEmail,UserPassword;
private TextView NeedNewAccountLink , PrivacyLink;
private  FirebaseAuth mAuth;
private ImageView googleSignInButton , facebookSignInButton , twitterSignInButton ;
private ProgressDialog loadingbar;
private static final int  RC_SIGN_IN=1;
private static final String TAG="LoginActivity";
private GoogleApiClient mGoogleSignInClient;
private Boolean EmailAddressChecker;

    private TextView ForgetPasswordLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PrivacyLink  = (TextView)findViewById(R.id.privacy_policy_link);
        LoginButton=(Button)findViewById(R.id.login_account);
        ForgetPasswordLink=(TextView)findViewById(R.id.forget_password_link);
        googleSignInButton=(ImageView)findViewById(R.id.google_signin_button);
        facebookSignInButton = (ImageView)findViewById(R.id.facebook_signin_button);
        twitterSignInButton = (ImageView)findViewById(R.id.twitter_signin_button);
        UserEmail=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        mAuth=FirebaseAuth.getInstance();
        loadingbar=new ProgressDialog(this);

        NeedNewAccountLink=(TextView)findViewById(R.id.register_account_link);

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();


            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

        facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToStillUpdateActivity();
            }
        });


        twitterSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToStillUpdateActivity();
            }
        });


LoginButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
          AllowingUserToLoginIn();
    }
});
// Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

      mGoogleSignInClient=new GoogleApiClient.Builder(this)
      .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
          {
               Toast.makeText(getApplicationContext(),"Connection to google sign in failed",Toast.LENGTH_SHORT).show();
          }
      })
              .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
              .build();

      googleSignInButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              signIn();
          }
      });

        PrivacyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPrivacyPolicy();
            }
        });


    }

    private void sendUserToStillUpdateActivity()
    {
        Intent stillUpdateIntent = new Intent(LoginActivity.this , StillUpdate.class );
        startActivity(stillUpdateIntent);
    }

    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            loadingbar.setTitle("Google sigin...");
            loadingbar.setMessage("Please wait while we are logging using your google account....");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess())
            {
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(getApplicationContext(),"Please wait,while we are processing your request",Toast.LENGTH_SHORT).show();

            }

            else
                {
                    Toast.makeText(getApplicationContext(),"Can't get auth result",Toast.LENGTH_SHORT).show();

                }
                      }

    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserToMainActivity();
                            loadingbar.dismiss();
                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message=task.getException().toString();
                            sendUserToLoginActivity();
                            Toast.makeText(getApplicationContext(),"Not authenticated Try Again!!" + message,Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }

                    }
                });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(LoginActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToPrivacyPolicy()
    {
        Intent privacyPolicyIntent = new Intent(LoginActivity.this , PrivacyPolicy.class);
        startActivity(privacyPolicyIntent);


    }


    private void AllowingUserToLoginIn() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Email field can't be empty",Toast.LENGTH_SHORT).show();
        }
       if(TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Password can't be empty",Toast.LENGTH_SHORT).show();
       }
       else{
           loadingbar.setTitle("Logging in...");
           loadingbar.setMessage("Please wait while we are logging into your Account....");
           loadingbar.show();
           loadingbar.setCanceledOnTouchOutside(true);


           mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
              if(task.isSuccessful()){

                  verifyEmailAddress();
             loadingbar.dismiss();
              }     else{
                  String message=task.getException().getMessage();
                  Toast.makeText(getApplication(),"Error occured:"+message,Toast.LENGTH_SHORT).show();
             loadingbar.dismiss();
              }
               }
           });



       }
    }

    private void verifyEmailAddress()
    {
        FirebaseUser users = mAuth.getCurrentUser();
        EmailAddressChecker = users.isEmailVerified();
        if(EmailAddressChecker)
        {
            sendUserToMainActivity();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please Verify Your Account First....", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current_user= mAuth.getCurrentUser();
        if(current_user!=null){
            sendUserToMainActivity();
        }

    }


    private void sendUserToMainActivity() {
        Intent MainIntent=new Intent(LoginActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);

    }
}
