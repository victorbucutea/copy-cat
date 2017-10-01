package ro.softspot.copycat.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

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

    private void saveCurrentUser(String email) {
        SharedPreferences.Editor editor = getSharedPreferences("UserDetails", MODE_PRIVATE).edit();
        editor.putString("user", email);
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

        GraphRequest request = GraphRequest.newMeRequest(
                getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());

                        if (object == null) {
                            showNoConnectionError();
                            return;
                        }
                        // Application code
                        try {
                            String email = object.getString("email");
                            email = email.replaceAll("@", "_");
                            saveCurrentUser(email);
                            SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
                            editor.putString("user",email);
                            editor.apply();
                            SynchronizationService.getInstance(LoginActivity.this).newClient(LoginActivity.this);
                            goToMainActivity();
                        } catch (JSONException e) {
                            Log.e("LOGIN", "Error while parsing fb graph response ", e);
                        }
                    }

                    private void showNoConnectionError() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder
                                .setTitle("Error")
                                .setMessage("Cannot contact server")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.cancel();
                                        finishAffinity();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
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
