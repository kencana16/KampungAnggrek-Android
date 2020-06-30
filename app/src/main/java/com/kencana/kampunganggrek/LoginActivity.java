package com.kencana.kampunganggrek;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kencana.kampunganggrek.Util.AppController;
import com.kencana.kampunganggrek.Util.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout til_user, til_pass;
    TextInputEditText ti_user, ti_pass;
    Button signin;
    ImageButton googleSignin;
    LoginButton fbSignin;
    TextView signup;

    ProgressDialog pDialog;

    int success;
    ConnectivityManager conMgr;

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    public final static String TAG_USERNAME = "username";
    String tag_json_obj = "json_obj_req";

    SharedPreferences sharedpreferences;
    Boolean session = false;
    String username;
    public static final String my_shared_preferences = "my_shared_preferences";
    public static final String session_status = "session_status";
    private GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            if (conMgr.getActiveNetworkInfo() != null
                    &&
                    conMgr.getActiveNetworkInfo().isAvailable()
                    &&
                    conMgr.getActiveNetworkInfo().isConnected()) {
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }

        ti_user = findViewById(R.id.ti_user_signin);
        ti_pass  = findViewById(R.id.ti_pass_signin);
        til_user = findViewById(R.id.til_user_signin);
        til_pass = findViewById(R.id.til_pass_signin);
        signin = findViewById(R.id.button_signinSignin);
        googleSignin = findViewById(R.id.button_signinGoogle);
        fbSignin = findViewById(R.id.button_signinFacebook);
        signup = findViewById(R.id.button_signupSignin);

        // Cek session login jika TRUE maka langsung buka MainActivity
        sharedpreferences = getSharedPreferences(my_shared_preferences,Context.MODE_PRIVATE);
        session = sharedpreferences.getBoolean(session_status, false);
        username = sharedpreferences.getString(TAG_USERNAME,null);
        if (session) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(TAG_USERNAME, username);
            startActivity(intent);
            finish();
        }

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = ti_user.getText().toString();
                String password = ti_pass.getText().toString();

                //reset indikator error
                til_user.setErrorEnabled(false);
                til_user.setError(null);
                til_pass.setErrorEnabled(false);
                til_pass.setError(null);

                //validasi form
                if(Objects.requireNonNull(ti_user.getText()).toString().isEmpty() || ti_pass.getText().length() < 8){
                    if(ti_user.getText().toString().isEmpty()){
                        til_user.setErrorEnabled(true);
                        til_user.setError("Masukan Nama Pengguna");
                    }
                    if(ti_pass.getText().length() < 8) {
                        til_pass.setErrorEnabled(true);
                        til_pass.setError("Password berisi minimal 8 karakter");
                    }
                }else {
                    try {
                        if (conMgr.getActiveNetworkInfo() !=
                                null
                                &&
                                conMgr.getActiveNetworkInfo().isAvailable()
                                &&
                                conMgr.getActiveNetworkInfo().isConnected()) {
                            checkLogin(username, password);
                        } else {
                            Toast.makeText(getApplicationContext(), "Periksa Koneksi Internet", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        googleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 1);
            }
        });

        //facebook login
        callbackManager = CallbackManager.Factory.create();
        fbSignin.setReadPermissions(Arrays.asList("email","public_profile"));
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };
        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

            }
        };
        fbSignin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.e("aa", ""+response.toString());
                                try {
                                    Toast.makeText(getApplicationContext(), "Hi, " + object.getString("name"), Toast.LENGTH_LONG).show();
                                } catch(JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "R.string.cancel_login", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplication(), "R.string.error_login", Toast.LENGTH_SHORT).show();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void checkLogin(final String username, final String password) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Tunggu Sebentar ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, ServerAPI.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Login Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    // Check for error node in json
                    if (success == 1) {
                        String username = jObj.getString(TAG_USERNAME);
                        String kd_konsumen = jObj.getString("kode_konsumen");
                        Log.e("Berhasil Masuk!", jObj.toString());
                        Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                        // menyimpan login ke session
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(session_status,true);
                        editor.putString(TAG_USERNAME, username);
                        editor.putString("kode_konsumen", kd_konsumen);
                        editor.commit();
                        // Memanggil main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(TAG_USERNAME, username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //hide keyboard when touch other view
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
