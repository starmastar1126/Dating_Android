package ru.ifsoft.chat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.common.ActivityBase;
import ru.ifsoft.chat.util.CustomRequest;
import ru.ifsoft.chat.util.Helper;

public class RegisterActivity extends ActivityBase {

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    private Toolbar mToolbar;

    private String selectedPhotoImg = "";
    private Uri selectedImage;
    private Uri outputFileUri;

    private ViewPager mViewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout mMarkersLayout;
    private TextView[] markers;
    private int[] screens;
    private Button mButtonBack, mButtonFinish;

    private RelativeLayout mNavigator;

    // Screen 0

    private EditText mUsername, mFullname, mPassword, mEmail, mReferrer;
    private LinearLayout mFacebookAuthContainer;

    private TextView mButtonRegularAuth, mButtonTerms;

    private LoginButton mFacebookAuth;

    private Button mButtonContinue;

    // Screen 1

    private Button mButtonChoosePhoto;
    private CircularImageView mPhoto;

    // Screen 2

    private Button mButtonChooseAge;

    // Screen 3

    private Button mButtonChooseGender, mButtonChooseSexOrientation;


    // Screen 3, 4 and 5

    private ImageView mImage;

    // Screen 4

    private Button mButtonGrantLocationPermission;

    //

    private int age = 0, gender = 2, sex_orientation = 0; // gender: 0 - male; 1 = female; 2 = secret
    private String username = "", password = "", email = "", language = "en", fullname = "", photo_url = "", referrer = "", facebook_id = "";

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (AccessToken.getCurrentAccessToken()!= null) LoginManager.getInstance().logOut();

        callbackManager = CallbackManager.Factory.create();

        Intent i = getIntent();
        facebook_id = i.getStringExtra("facebookId");

        if (facebook_id == null) {

            facebook_id = "";
        }

        setContentView(R.layout.activity_register);

        if (savedInstanceState != null) {

            age = savedInstanceState.getInt("age");
            gender = savedInstanceState.getInt("gender");
            sex_orientation = savedInstanceState.getInt("sex_orientation");

            username = savedInstanceState.getString("username");
            password = savedInstanceState.getString("password");
            email = savedInstanceState.getString("email");
            fullname = savedInstanceState.getString("fullname");
            referrer = savedInstanceState.getString("referrer");
            facebook_id = savedInstanceState.getString("facebook_id");
            selectedPhotoImg = savedInstanceState.getString("selectedPhotoImg");
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mMarkersLayout = (LinearLayout) findViewById(R.id.layout_markers);

        mNavigator = (RelativeLayout) findViewById(R.id.navigator_layout);
        mNavigator.setVisibility(View.GONE);

        mButtonBack = (Button) findViewById(R.id.button_back);
        mButtonFinish = (Button) findViewById(R.id.button_next);

        screens = new int[]{
                R.layout.register_screen_1,
                R.layout.register_screen_2,
                R.layout.register_screen_3,
                R.layout.register_screen_4,
                R.layout.register_screen_5};

        addMarkers(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        mViewPager.setAdapter(myViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        mViewPager.beginFakeDrag();

        mButtonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);

                updateView();
            }
        });

        mButtonFinish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int current = mViewPager.getCurrentItem();

                if (current < screens.length - 1) {

                    switch (current) {

                        case 1: {

                            if (selectedPhotoImg.length() != 0) {

                                mViewPager.setCurrentItem(current + 1);

                            } else {

                                Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_2_msg), Toast.LENGTH_SHORT).show();
                                animateIcon(mPhoto);
                            }

                            break;
                        }

                        case 2: {

                            if (age > 17) {

                                mViewPager.setCurrentItem(current + 1);

                            } else {

                                Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_3_msg), Toast.LENGTH_SHORT).show();
                                animateIcon(mImage);
                            }

                            break;
                        }

                        case 3: {

                            if (sex_orientation != 0) {

                                mViewPager.setCurrentItem(current + 1);

                            } else {

                                Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_4_msg), Toast.LENGTH_SHORT).show();
                                animateIcon(mImage);
                            }

                            break;
                        }

                        default: {

                            mViewPager.setCurrentItem(current + 1);

                            break;
                        }
                    }

                    updateView();

                } else {

                    signup();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("age", age);
        outState.putInt("gender", gender);
        outState.putInt("sex_orientation", sex_orientation);

        outState.putString("username", username);
        outState.putString("password", password);
        outState.putString("email", email);
        outState.putString("fullname", fullname);
        outState.putString("referrer", referrer);
        outState.putString("facebook_id", facebook_id);
        outState.putString("selectedPhotoImg", selectedPhotoImg);
    }

    private void updateView() {

        int current = mViewPager.getCurrentItem();

        setStatusBarColor(this, current);
        //setToolBarColor(current);

        mToolbar.setVisibility(View.GONE);

        mNavigator.setVisibility(View.VISIBLE);

        switch (current) {

            case 0: {

                mToolbar.setVisibility(View.VISIBLE);
                mNavigator.setVisibility(View.GONE);

                if (username.length() != 0) {

                    mUsername.setText(username);
                }

                if (fullname.length() != 0) {

                    mFullname.setText(fullname);
                }

                if (password.length() != 0) {

                    mPassword.setText(password);
                }

                if (email.length() != 0) {

                    mEmail.setText(email);
                }

                mReferrer.setText(referrer);

                if (!FACEBOOK_AUTHORIZATION) {

                    mFacebookAuthContainer.setVisibility(View.GONE);
                    mFacebookAuth.setVisibility(View.GONE);

                } else {

                    if (facebook_id.length() != 0) {

                        mFacebookAuthContainer.setVisibility(View.VISIBLE);
                        mFacebookAuth.setVisibility(View.GONE);

                    } else {

                        mFacebookAuthContainer.setVisibility(View.GONE);
                        mFacebookAuth.setVisibility(View.VISIBLE);
                    }
                }

                break;
            }

            case 2: {

                if (age != 0) {

                    mButtonChooseAge.setText(getString(R.string.action_choose_age) + ": " + Integer.toString(age));

                } else {

                    mButtonChooseAge.setText(getString(R.string.action_choose_age));
                }

                break;
            }

            case 3: {

                mButtonChooseGender.setText(getString(R.string.action_choose_gender) + ": " + Helper.getGenderTitle(this, gender));

                if (sex_orientation != 0) {

                    mButtonChooseSexOrientation.setText(getString(R.string.action_choose_sex_orientation) + ": " + Helper.getSexOrientationTitle(this, sex_orientation));

                } else {

                    mButtonChooseSexOrientation.setText(getString(R.string.action_choose_sex_orientation));
                }

                break;
            }

            case 4: {

                if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mButtonGrantLocationPermission.setEnabled(false);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access_success);

                } else {

                    mButtonGrantLocationPermission.setEnabled(true);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access);
                }

                break;
            }

            default: {

                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO_IMG && resultCode == RESULT_OK && null != data) {

            selectedImage = data.getData();

            selectedPhotoImg = getImageUrlWithAuthority(this, selectedImage, "photo.jpg");

            try {

                if (save(selectedPhotoImg, "photo.jpg")) {

                    selectedPhotoImg = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "photo.jpg";

                    mPhoto.setImageURI(null);
                    mPhoto.setImageURI(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(selectedPhotoImg)));

                } else {

                    mPhoto.setImageURI(null);
                    mPhoto.setImageResource(R.drawable.profile_default_photo);
                    selectedPhotoImg = "";
                }

            } catch (Exception e) {

                mPhoto.setImageURI(null);
                mPhoto.setImageResource(R.drawable.profile_default_photo);
                selectedPhotoImg = "";

                Log.e("OnSelectPhotoImage", e.getMessage());
            }

        } else if (requestCode == CREATE_PHOTO_IMG && resultCode == RESULT_OK) {

            try {

                selectedPhotoImg = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "photo.jpg";

                save(selectedPhotoImg, "photo.jpg");

                mPhoto.setImageURI(null);
                mPhoto.setImageURI(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(selectedPhotoImg)));

//                mContainerImg.setVisibility(View.VISIBLE);

            } catch (Exception ex) {

                mPhoto.setImageURI(null);
                mPhoto.setImageResource(R.drawable.profile_default_photo);
                selectedPhotoImg = "";

                Log.v("OnCameraCallBack", ex.getMessage());
            }

        } else if (requestCode == 10001) {

            if (mViewPager.getCurrentItem() == 4) {

                updateView();
            }
        }
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Granted

                    choiceImage();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Denied

                        showNoStoragePermissionSnackbar();
                    }
                }

                return;
            }

            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Granted

                    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                if (task.isSuccessful() && task.getResult() != null) {

                                    mLastLocation = task.getResult();

                                    App.getInstance().setLat(mLastLocation.getLatitude());
                                    App.getInstance().setLng(mLastLocation.getLongitude());

                                } else {

                                    Log.d("GPS", "getLastLocation:exception", task.getException());
                                }
                            }
                        });
                    }

                    animateIcon(mImage);

                    mButtonGrantLocationPermission.setEnabled(false);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access_success);

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Denied

                        showNoLocationPermissionSnackbar();

                        mButtonGrantLocationPermission.setEnabled(true);
                        mButtonGrantLocationPermission.setText(R.string.action_grant_access);
                    }
                }

                return;
            }

        }
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void showNoStoragePermissionSnackbar() {

        Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(RegisterActivity.this, getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void showNoLocationPermissionSnackbar() {

        Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_location_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(RegisterActivity.this, getString(R.string.label_grant_location_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public Bitmap resizeBitmap(String photoPath) {

        Log.e("Image", "resizeBitmap()");

        int targetW = 512;
        int targetH = 512;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;

        scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated from  API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    public Boolean save(String outFile, String inFile) {

        Boolean status = true;

        try {

            Bitmap bmp = resizeBitmap(outFile);

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, inFile);
            FileOutputStream fOut = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();

        } catch (Exception ex) {

            status = false;

            Log.e("Error", ex.getMessage());
        }

        return status;
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri, String fileName) {

        InputStream is = null;

        if (uri.getAuthority() != null) {

            try {

                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                return writeToTempImageAndGetPathUri(context, bmp, fileName).toString();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (is != null) {

                        is.close();
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage, String fileName) {

        String file_path = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER;
        File dir = new File(file_path);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {

            FileOutputStream fos = new FileOutputStream(file);

            inImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {

            Toast.makeText(inContext, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + fileName;
    }

    public int getColorWrapper(Context context, int id) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return context.getColor(id);

        } else {

            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    public void setStatusBarColor(Activity act, int index) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            switch (index) {

                case 0: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.statusBarColor));

                    break;
                }

                case 1: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_2));

                    break;
                }

                case 2: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_3));

                    break;
                }

                case 3: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_4));

                    break;
                }

                case 4: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_5));

                    break;
                }

                default: {

                    window.setStatusBarColor(Color.TRANSPARENT);

                    break;
                }
            }
        }
    }

    private void addMarkers(int currentPage) {

        markers = new TextView[screens.length];

        mMarkersLayout.removeAllViews();

        for (int i = 0; i < markers.length; i++) {

            markers[i] = new TextView(this);
            markers[i].setText(Html.fromHtml("&#8226;"));
            markers[i].setTextSize(35);
            markers[i].setTextColor(getResources().getColor(R.color.grey_90));
            mMarkersLayout.addView(markers[i]);
        }

        if (markers.length > 0)

            markers[currentPage].setTextColor(getResources().getColor(R.color.white));
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            addMarkers(position);

            if (position == screens.length - 1) {

                mButtonFinish.setText(getString(R.string.action_finish));

            } else {

                mButtonFinish.setText(getString(R.string.action_next));
            }

            switch (position) {

                case 0: {

                    setStatusBarColor(RegisterActivity.this, 0);

                    break;
                }

                case 1: {

                    setStatusBarColor(RegisterActivity.this, 1);

                    break;
                }

                case 2: {

                    setStatusBarColor(RegisterActivity.this, 2);

                    break;
                }

                case 3: {

                    setStatusBarColor(RegisterActivity.this, 3);

                    break;
                }

                case 4: {

                    setStatusBarColor(RegisterActivity.this, 4);

                    break;
                }

                default: {

                    break;
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(screens[position], container, false);
            container.addView(view);

            switch (position) {

                case 0: {

                    mUsername = (EditText) view.findViewById(R.id.username_edit);
                    mFullname = (EditText) view.findViewById(R.id.fullname_edit);
                    mPassword = (EditText) view.findViewById(R.id.password_edit);
                    mEmail = (EditText) view.findViewById(R.id.email_edit);
                    mReferrer = (EditText) view.findViewById(R.id.referrer_edit);

                    mFacebookAuthContainer = (LinearLayout) view.findViewById(R.id.facebook_auth_container);

                    mFacebookAuth = (LoginButton) view.findViewById(R.id.button_facebook_login);
                    mFacebookAuth.setReadPermissions("public_profile"); // "email",

                    // Registering CallbackManager with the LoginButton
                    mFacebookAuth.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                        @Override
                        public void onSuccess(LoginResult loginResult) {

                            // Retrieving access token using the LoginResult
                            AccessToken accessToken = loginResult.getAccessToken();

                            useLoginInformation(accessToken);
                        }

                        @Override
                        public void onCancel() {

                        }
                        @Override
                        public void onError(FacebookException error) {

                        }
                    });

                    mButtonRegularAuth = (TextView) view.findViewById(R.id.button_regular_auth);
                    mButtonTerms = (TextView) view.findViewById(R.id.button_terms);

                    mButtonContinue = (Button) view.findViewById(R.id.button_continue);

                    mButtonContinue.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            hideKeyboard();

                            next();
                        }
                    });

                    mButtonRegularAuth.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            facebook_id = "";

                            updateView();
                        }
                    });

                    mButtonTerms.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(RegisterActivity.this, WebViewActivity.class);
                            i.putExtra("url", METHOD_APP_TERMS);
                            i.putExtra("title", getText(R.string.signup_label_terms_and_policies));
                            startActivity(i);
                        }
                    });

                    mUsername.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            if (App.getInstance().isConnected() && check_username()) {

                                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECKUSERNAME, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {

                                                    if (response.getBoolean("error")) {

                                                        mUsername.setError(getString(R.string.error_login_taken));
                                                    }

                                                } catch (JSONException e) {

                                                    e.printStackTrace();

                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        Log.e("Username()", error.toString());

                                    }
                                }) {

                                    @Override
                                    protected Map<String, String> getParams() {
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put("username", username);

                                        return params;
                                    }
                                };

                                App.getInstance().addToRequestQueue(jsonReq);
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });

                    mFullname.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            check_fullname();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    mPassword.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            check_password();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    mEmail.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            if (App.getInstance().isConnected() && check_email()) {

                                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECK_EMAIL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {

                                                    if (response.getBoolean("error")) {

                                                        mEmail.setError(getString(R.string.error_email_taken));
                                                    }

                                                } catch (JSONException e) {

                                                    e.printStackTrace();

                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        Log.e("Email()", error.toString());

                                    }
                                }) {

                                    @Override
                                    protected Map<String, String> getParams() {
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put("email", email);

                                        return params;
                                    }
                                };

                                App.getInstance().addToRequestQueue(jsonReq);
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });

                    break;
                }

                case 1: {

                    mPhoto = (CircularImageView) view.findViewById(R.id.photo_image);

                    if (selectedPhotoImg != null && selectedPhotoImg.length() > 0) {

                        mPhoto.setImageURI(FileProvider.getUriForFile(RegisterActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(selectedPhotoImg)));
                    }

                    mButtonChoosePhoto = (Button) view.findViewById(R.id.button_choose_photo);

                    mButtonChoosePhoto.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            selectPhoto();
                        }
                    });

                    break;
                }

                case 2: {

                    mImage = (ImageView) view.findViewById(R.id.age_image);

                    mButtonChooseAge = (Button) view.findViewById(R.id.button_choose_age);

                    mButtonChooseAge.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            choiceAge();
                        }
                    });

                    break;
                }

                case 3: {

                    mImage = (ImageView) view.findViewById(R.id.image);

                    mButtonChooseGender = (Button) view.findViewById(R.id.button_choose_gender);
                    mButtonChooseSexOrientation = (Button) view.findViewById(R.id.button_choose_sexual_orientation);

                    mButtonChooseGender.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            choiceGender();
                        }
                    });

                    mButtonChooseSexOrientation.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            choiceSexOrientation();
                        }
                    });

                    break;
                }

                case 4: {

                    mImage = (ImageView) view.findViewById(R.id.image);
                    mButtonGrantLocationPermission = (Button) view.findViewById(R.id.button_grant_location_permission);

                    mButtonGrantLocationPermission.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            grantLocationPermission();
                        }
                    });
                }
            }

            updateView();

            return view;
        }

        @Override
        public int getCount() {

            return screens.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            View view = (View) object;
            container.removeView(view);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed(){

        finish();
    }

    private void useLoginInformation(AccessToken accessToken) {

        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/

        showpDialog();

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {

            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {

                    if (object.has("id")) {

                        facebook_id = object.getString("id");
                    }

                    if (object.has("name")) {

                        fullname = object.getString("name");
                    }

                    if (object.has("email")) {

                        email = object.getString("email");
                    }

                } catch (JSONException e) {

                    Log.e("Facebook Login", "Could not parse malformed JSON: \"" + object.toString() + "\"");

                } finally {

                    if (AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();

                    if (!facebook_id.equals("")) {

                        signinByFacebookId();

                    } else {

                        hidepDialog();
                    }
                }
            }
        });

        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        // parameters.putString("fields", "id,name,email,picture.width(200)");
        parameters.putString("fields", "id, name");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    public void signinByFacebookId() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGINBYFACEBOOK, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                go();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                Toast.makeText(RegisterActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_DEACTIVATED) {

                                Toast.makeText(RegisterActivity.this, getText(R.string.msg_account_deactivated), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            if (!facebook_id.equals("")) {

                                mFacebookAuth.setVisibility(View.GONE);

                                mFacebookAuthContainer.setVisibility(View.VISIBLE);

                                updateView();
                            }
                        }

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Facebook Login", "Error");

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("facebookId", facebook_id);
                params.put("clientId", CLIENT_ID);

                params.put("gcm_regId", App.getInstance().getGcmToken());
                params.put("lang", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void animateIcon(ImageView icon) {

        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(175);
        scale.setInterpolator(new LinearInterpolator());

        icon.startAnimation(scale);
    }

    private void selectPhoto() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
            }

        } else {

            choiceImage();
        }
    }

    private void choiceImage() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_PHOTO_IMG);

                        break;
                    }

                    default: {

                        try {

                            File root = new File(Environment.getExternalStorageDirectory(), APP_TEMP_FOLDER);

                            if (!root.exists()) {

                                root.mkdirs();
                            }

                            File sdImageMainDirectory = new File(root, "photo.jpg");
                            outputFileUri = FileProvider.getUriForFile(RegisterActivity.this, BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory);

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(cameraIntent, CREATE_PHOTO_IMG);

                        } catch (Exception e) {

                            Toast.makeText(RegisterActivity.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    }
                }

            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceAge() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        for (int i = 18; i < 101; i++) {

            arrayAdapter.add(Integer.toString(i));
        }

        builderSingle.setTitle(getText(R.string.register_screen_3_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                age = which + 18;

                updateView();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceGender() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        arrayAdapter.add(getString(R.string.label_male));
        arrayAdapter.add(getString(R.string.label_female));
        arrayAdapter.add(getString(R.string.label_secret));

        builderSingle.setTitle(getText(R.string.action_choose_gender));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                gender = which;

                updateView();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceSexOrientation() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        arrayAdapter.add(getString(R.string.sex_orientation_1));
        arrayAdapter.add(getString(R.string.sex_orientation_2));
        arrayAdapter.add(getString(R.string.sex_orientation_3));
        arrayAdapter.add(getString(R.string.sex_orientation_4));

        builderSingle.setTitle(getText(R.string.action_choose_sex_orientation));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                sex_orientation = which + 1;

                updateView();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void grantLocationPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)){

                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

            } else {

                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            }
        }
    }

    private void next() {

        if (verifyRegForm()) {

            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);

            updateView();
        }
    }

    private void signup() {

        showpDialog();

        File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "photo.jpg");

        uploadFile(METHOD_ACCOUNT_UPLOADPHOTO, f);
    }

    private void go() {

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public Boolean verifyRegForm() {

        username = mUsername.getText().toString();
        fullname = mFullname.getText().toString();
        password = mPassword.getText().toString();
        email = mEmail.getText().toString();
        referrer = mReferrer.getText().toString();

        mUsername.setError(null);
        mFullname.setError(null);
        mPassword.setError(null);
        mEmail.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            mUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            mUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            mUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (fullname.length() == 0) {

            mFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            mFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        if (password.length() == 0) {

            mPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (email.length() == 0) {

            mEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            mEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        return true;
    }

    public Boolean check_username() {

        username = mUsername.getText().toString();

        Helper helper = new Helper();

        if (username.length() == 0) {

            mUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            mUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            mUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mUsername.setError(null);

        return  true;
    }

    public Boolean check_fullname() {

        fullname = mFullname.getText().toString();

        if (fullname.length() == 0) {

            mFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            mFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        mFullname.setError(null);

        return  true;
    }

    public Boolean check_password() {

        password = mPassword.getText().toString();

        Helper helper = new Helper();

        if (password.length() == 0) {

            mPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mPassword.setError(null);

        return true;
    }

    public Boolean check_email() {

        email = mEmail.getText().toString();

        Helper helper = new Helper();

        if (email.length() == 0) {

            mEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            mEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mEmail.setError(null);

        return true;
    }

    private void hideKeyboard() {

        View view = this.getCurrentFocus();

        if (view != null) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public Boolean uploadFile(String serverURL, File file) {

        final OkHttpClient client = new OkHttpClient();

        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        try {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                    hidepDialog();

                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            photo_url = result.getString("lowPhotoUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        showpDialog();

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SIGNUP, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                                        if (App.getInstance().authorize(response)) {

                                            Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                                            go();

                                        } else {

                                            switch (App.getInstance().getErrorCode()) {

                                                case 300 : {

                                                    mViewPager.setCurrentItem(0);

                                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_login_taken), Toast.LENGTH_SHORT).show();

                                                    break;
                                                }

                                                case 301 : {

                                                    mViewPager.setCurrentItem(0);

                                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_email_taken), Toast.LENGTH_SHORT).show();

                                                    break;
                                                }

                                                case 500 : {

                                                    Toast.makeText(RegisterActivity.this, getString(R.string.label_multi_account_msg), Toast.LENGTH_SHORT).show();

                                                    break;
                                                }

                                                default: {

                                                    Log.e("Profile", "Could not parse malformed JSON: \"" + response.toString() + "\"");
                                                    break;
                                                }
                                            }
                                        }

                                        hidepDialog();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.e("signup()", error.toString());

                                hidepDialog();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("username", username);
                                params.put("fullname", fullname);
                                params.put("password", password);
                                params.put("photo", photo_url);
                                params.put("email", email);
                                params.put("referrer", referrer);
                                params.put("language", language);
                                params.put("facebookId", facebook_id);
                                params.put("sex", Integer.toString(gender));
                                params.put("age", Integer.toString(age));
                                params.put("sex_orientation", Integer.toString(sex_orientation));
                                params.put("clientId", CLIENT_ID);
                                params.put("gcm_regId", App.getInstance().getGcmToken());

                                return params;
                            }
                        };

                        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                        jsonReq.setRetryPolicy(policy);

                        App.getInstance().addToRequestQueue(jsonReq);
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            hidepDialog();
        }

        return false;
    }
}
