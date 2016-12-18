package ro.softspot.copycat.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import ro.softspot.copycat.MainActivity;
import ro.softspot.copycat.R;
import ro.softspot.copycat.service.sync.SynchronizationService;

import static com.facebook.AccessToken.getCurrentAccessToken;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);

        AccessToken currentAccessToken = getCurrentAccessToken();

        if (currentAccessToken != null && !currentAccessToken.isExpired()) {
            onFacebookLoginSucces();
            return;
        }

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookLoginCallback());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCurrentUser();
    }

    private void saveCurrentUser() {
        SharedPreferences.Editor editor = getSharedPreferences("UserDetails", MODE_PRIVATE).edit();
        editor.putString("user", getCurrentAccessToken().getUserId());
        editor.apply();
    }

    public void onClick(View v) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void onFacebookLoginSucces() {
        saveCurrentUser();
        String userId = AccessToken.getCurrentAccessToken().getUserId();
        SynchronizationService.getInstance(this).newClient(this, userId);
        goToMainActivity();
    }


    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private class FacebookLoginCallback implements FacebookCallback<LoginResult> {

        @Override
        public void onSuccess(LoginResult loginResult) {
           onFacebookLoginSucces();
        }

        @Override
        public void onCancel() {
        }


        @Override
        public void onError(FacebookException exception) {
        }
    }


}
