package com.example.hoctest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;



public class MainActivity extends AppCompatActivity {




    Button btn_GLogin;
    TextView text;
    ImageView image;
    ProgressBar progressBar;
    // Firebase Authentication
    FirebaseAuth mAuth;
    //google
    GoogleSignInClient mGoogleSignInClient;
    static final int GOOGLE_SIGN_IN = 222;
    //Facebook
    private CallbackManager mCallbackManager;
    private LoginButton btn_FLogin;
    private static final String TAG = "FacebookLogin";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get views from activity_main
        btn_GLogin = findViewById(R.id.btn_GLogin);
        text = findViewById(R.id.text);
        image = findViewById(R.id.image);
        progressBar = findViewById(R.id.progress_circular);

        //Firebase authentication in the database
        mAuth = FirebaseAuth.getInstance();

        /*
            Google Sign in
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btn_GLogin.setOnClickListener(v -> SignInGoogle());

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI();
        }

        /*
            Facebook sign in
         */
        btn_FLogin = findViewById(R.id.btn_FLogin);
        mCallbackManager = CallbackManager.Factory.create();
        btn_FLogin.setPermissions(Arrays.asList("email","public_profile"));

        btn_FLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
    }


    public void SignInGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }
    /*
        This method checks the google account Token and uses it to sign into to firebase authentication
        then updates the UI via moving into loggedActivity

        if it fails the user gets a toast with authentication failed
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);

                        updateUI();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);

                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(mAuth.getCurrentUser() != null)
        updateUI();

    }
    /*
        This checks if the user used google or facebook to sign in,

        Google: Try to connect to the users google account, then uses the firebaseAuthWithGoogle method
        catch if the authentication fails.


    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
                Log.d("TAG google", String.valueOf(requestCode));
            } catch (ApiException e) {
                Toast.makeText(MainActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }

        }
        else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    /*
    Handler for checking if the user sign in were successful
    then redirects the user to logged activity via updateUI
    if not the user gets a toast with authentication failed
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }
    private void updateUI() {
        startActivity(new Intent(this, LoggedActivity.class));
        finish();
    }
}