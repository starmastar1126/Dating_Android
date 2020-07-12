package ru.ifsoft.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.dialogs.HotgameSettingsDialog;
import ru.ifsoft.chat.model.Profile;
import ru.ifsoft.chat.util.CustomRequest;

public class HotgameFragment extends Fragment implements Constants {

    private static final String STATE_LIST = "State Adapter Data";

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    Menu MainMenu;

    TextView mMessage, mDetails;
    ImageView mSplash;

    TextView mHotgameUsername, mHotgameStatus;

    LinearLayout mSpotLight, mPermissionSpotlight;
    RelativeLayout mHotgameLayout;

    Button mGrantPermission;

    private ArrayList<Profile> itemsList;

    public ImageView mHotgamePhoto, mHotgameStamp;
    public FloatingActionButton mHotgameLike, mHotgameDislike;
    public ProgressBar mHotgameProgressBar;

    private int sex = 2, sexOrientation = 0, liked = 1, matches = 1;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loading = false;
    private Boolean viewMore = false;
    private Boolean restore = false;
    private Boolean spotlight = true;

    private int position = -1;

    private int distance = 1000;      // im miles

    int pastVisiblesItems = 0, visibleItemCount = 0, totalItemCount = 0;

    public HotgameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        setRetainInstance(true);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);

            viewMore = savedInstanceState.getBoolean("viewMore");
            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            spotlight = savedInstanceState.getBoolean("spotlight");
            itemId = savedInstanceState.getInt("itemId");
            position = savedInstanceState.getInt("position");
            distance = savedInstanceState.getInt("distance");

            sex = savedInstanceState.getInt("sex");
            liked = savedInstanceState.getInt("liked");
            matches = savedInstanceState.getInt("matches");
            sexOrientation = savedInstanceState.getInt("sexOrientation");

        } else {

            itemsList = new ArrayList<Profile>();

            restore = false;
            loading = false;
            spotlight = true;
            itemId = 0;
            position = -1;
            sexOrientation = 0;
            distance = 1000;

            sex = SEX_UNKNOWN;

            liked = 1;
            matches = 1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_hotgame, container, false);

        mHotgamePhoto = (ImageView) rootView.findViewById(R.id.hotgamePhoto);
        mHotgameStamp = (ImageView) rootView.findViewById(R.id.hotgameStamp);
        mHotgameLike = (FloatingActionButton) rootView.findViewById(R.id.fabLike);
        mHotgameDislike = (FloatingActionButton) rootView.findViewById(R.id.fabDislike);

        mHotgameProgressBar = (ProgressBar) rootView.findViewById(R.id.hotgameProgressBar);

        mHotgameLayout = (RelativeLayout) rootView.findViewById(R.id.hotgameLayout);

        mHotgameUsername = (TextView) rootView.findViewById(R.id.hotgameUsername);
        mHotgameStatus = (TextView) rootView.findViewById(R.id.hotgameStatus);

        mMessage = (TextView) rootView.findViewById(R.id.message);
        mSplash = (ImageView) rootView.findViewById(R.id.splash);

        mSpotLight = (LinearLayout) rootView.findViewById(R.id.spotlight);
        mDetails = (TextView) rootView.findViewById(R.id.openLocationSettings);

        mPermissionSpotlight = (LinearLayout) rootView.findViewById(R.id.permission_spotlight);
        mGrantPermission = (Button) rootView.findViewById(R.id.grantPermissionBtn);

        mHotgamePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Profile u = (Profile) itemsList.get(position);

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", u.getId());
                startActivity(intent);
            }
        });

        mHotgameLike.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Profile u = (Profile) itemsList.get(position);

                if (u.isMatch() || u.isMyLike()) {

                    position++;

                } else {

                    mHotgameDislike.hide();
                    mHotgameLike.hide();

                    like(u.getId());
                }

                if ((itemsList.size() - 1) < position && itemId > 1) {

                    showMessage(getText(R.string.msg_loading_2).toString());

                    getItems();

                } else if ((itemsList.size() - 1) < position) {

                    mHotgameLayout.setVisibility(View.GONE);

                    showMessage(getText(R.string.label_empty_list).toString());
                }

                updateHotgameContainer();
            }
        });

        mHotgameDislike.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                position++;

                if ((itemsList.size() - 1) < position && itemId > 1) {

                    showMessage(getText(R.string.msg_loading_2).toString());

                    getItems();

                } else if ((itemsList.size() - 1) < position) {

                    mHotgameLayout.setVisibility(View.GONE);

                    showMessage(getText(R.string.label_empty_list).toString());
                }

                updateHotgameContainer();
            }
        });

        if (itemsList.size() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        mDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), LocationActivity.class);
                startActivityForResult(i, 101);
            }
        });


        mGrantPermission.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                    }
                }
            }
        });

        updateSpotLight();

        if (!restore && App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                updateSpotLight();

            } else {

                showMessage(getText(R.string.msg_loading_2).toString());

                getItems();
            }
        }


        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateSpotLight() {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){

                showPermissionSpotlight();
                hideNoLocationSpotlight();
                hideHotgameContainer();
                hideMessage();

            } else {

                showPermissionSpotlight();
                hideNoLocationSpotlight();
                hideHotgameContainer();
                hideMessage();
            }

        } else {

            hidePermissionSpotlight();

            if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                hidePermissionSpotlight();
                hideNoLocationSpotlight();
                showHotgameContainer();

            } else {

                showNoLocationSpotlight();
                hideHotgameContainer();
                hideMessage();
            }
        }

        getActivity().invalidateOptionsMenu();
    }

    public void updateHotgameContainer() {

        if (itemsList.size() > 0 && position != -1 && (itemsList.size() - 1) >= position) {

            Profile u = (Profile) itemsList.get(position);

            mHotgameUsername.setText(u.getFullname() + ", " + Integer.toString(u.getAge()));

            if (!u.isVerify()) {

                mHotgameUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            } else {

                mHotgameUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.profile_verify_icon, 0);
            }

            if (u.getLocation().length() >  0) {

                mHotgameStatus.setVisibility(View.VISIBLE);
                mHotgameStatus.setText(u.getLocation());

            } else {

                mHotgameStatus.setVisibility(View.GONE);
            }

            if (u.isMatch() || u.isMyLike()) {

                mHotgameLike.setImageResource(R.drawable.hotgame_action_next);

            } else {

                mHotgameLike.setImageResource(R.drawable.hotgame_action_like);
            }

            if (!u.isMatch() && !u.isMyLike()) {

                mHotgameStamp.setVisibility(View.GONE);

            } else if (u.isMatch()) {

                mHotgameStamp.setVisibility(View.VISIBLE);
                mHotgameStamp.setImageResource(R.drawable.ic_hotgame_match);

            } else if (u.isMyLike() && !u.isMatch()) {

                mHotgameStamp.setVisibility(View.VISIBLE);
                mHotgameStamp.setImageResource(R.drawable.ic_hotgame_liked);
            }

            mHotgameLayout.setVisibility(View.GONE);
            mHotgameProgressBar.setVisibility(View.VISIBLE);

            if (u.getNormalPhotoUrl() != null && u.getNormalPhotoUrl().length() > 0) {

                final ImageView img = mHotgamePhoto;
                final ProgressBar progressView = mHotgameProgressBar;
                final RelativeLayout layout = mHotgameLayout;

                Picasso.with(getActivity())
                        .load(u.getNormalPhotoUrl())
                        .into(mHotgamePhoto, new Callback() {

                            @Override
                            public void onSuccess() {

                                progressView.setVisibility(View.GONE);
                                img.setVisibility(View.VISIBLE);
                                layout.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                                progressView.setVisibility(View.GONE);
                                img.setVisibility(View.VISIBLE);
                                img.setImageResource(R.drawable.profile_default_photo);
                                layout.setVisibility(View.VISIBLE);
                            }
                        });

            }

            if (!loading) {

                mHotgameLike.show();
                mHotgameDislike.show();

            } else {

                mHotgameLike.hide();
                mHotgameDislike.hide();
            }
        }
    }

    public void showHotgameContainer() {

        if (itemsList.size() - 1 < position) {

            mHotgameLayout.setVisibility(View.GONE);

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            mHotgamePhoto.setVisibility(View.VISIBLE);
            mHotgameLike.show();
            mHotgameDislike.show();

            mHotgameProgressBar.setVisibility(View.GONE);
        }

        updateHotgameContainer();
    }

    public void hideHotgameContainer() {

        mHotgameLayout.setVisibility(View.GONE);

        mHotgamePhoto.setVisibility(View.GONE);
        mHotgameLike.hide();
        mHotgameDislike.hide();

        mHotgameProgressBar.setVisibility(View.GONE);
    }

    public void showPermissionSpotlight() {

        mPermissionSpotlight.setVisibility(View.VISIBLE);
    }

    public void showNoLocationSpotlight() {

        mSpotLight.setVisibility(View.VISIBLE);
    }

    public void hidePermissionSpotlight() {

        mPermissionSpotlight.setVisibility(View.GONE);
    }

    public void hideNoLocationSpotlight() {

        mSpotLight.setVisibility(View.GONE);
    }

    public void updateItems() {

        if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

            showMessage(getText(R.string.msg_loading_2).toString());

            itemId = 0;

            getItems();
        }
    }

    @Override
    public void onStart() {

        super.onStart();

        updateSpotLight();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == getActivity().RESULT_OK) {

            updateSpotLight();

            updateItems();

        } else if (requestCode == 10001 && resultCode == getActivity().RESULT_OK) {

            updateSpotLight();

            updateItems();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                if (task.isSuccessful() && task.getResult() != null) {

                                    mLastLocation = task.getResult();

                                    Log.d("GPS", "PeopleNearby onComplete" + Double.toString(mLastLocation.getLatitude()));
                                    Log.d("GPS", "PeopleNearby onComplete" + Double.toString(mLastLocation.getLongitude()));

                                    App.getInstance().setLat(mLastLocation.getLatitude());
                                    App.getInstance().setLng(mLastLocation.getLongitude());

                                } else {

                                    Log.d("GPS", "getLastLocation:exception", task.getException());
                                }

                                updateSpotLight();

                                updateItems();
                            }
                        });
                    }

                    updateSpotLight();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        showNoLocationPermissionSnackbar();
                    }
                }

                return;
            }
        }
    }

    public void showNoLocationPermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_location_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_location_permission), Toast.LENGTH_SHORT).show();

            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("viewMore", viewMore);
        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("spotlight", spotlight);
        outState.putInt("itemId", itemId);
        outState.putInt("position", position);
        outState.putInt("sex", sex);
        outState.putInt("matches", matches);
        outState.putInt("liked", liked);
        outState.putInt("sexOrientation", sexOrientation);
        outState.putInt("distance", distance);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void onCloseHotgameSettingsDialog(int i_sex, int i_sexOrientation, int i_liked, int i_matches) {

        sex = i_sex;
        sexOrientation = i_sexOrientation;
        liked = i_liked;
        matches = i_matches;

        itemsList.clear();

        itemId = 0;

        position = -1;

        showMessage(getText(R.string.msg_loading_2).toString());

        getItems();
    }

    public void getItems() {

        if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_HOTGAME_GET, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "HotgameFragment Not Added to Activity");

                                return;
                            }

                            try {

                                arrayLength = 0;

                                if (!response.getBoolean("error")) {

                                    itemId = response.getInt("itemId");

                                    if (response.has("items")) {

                                        JSONArray usersArray = response.getJSONArray("items");

                                        arrayLength = usersArray.length();

                                        if (arrayLength > 0) {

                                            for (int i = 0; i < usersArray.length(); i++) {

                                                JSONObject userObj = (JSONObject) usersArray.get(i);

                                                Profile profile = new Profile(userObj);

                                                itemsList.add(profile);
                                            }
                                        }
                                    }

                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                loadingComplete();

                                Log.d("Success", response.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "HotgameFragment Not Added to Activity");

                        return;
                    }

                    loadingComplete();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("distance", Integer.toString(distance));
                    params.put("lat", Double.toString(App.getInstance().getLat()));
                    params.put("lng", Double.toString(App.getInstance().getLng()));
                    params.put("itemId", Long.toString(itemId));
                    params.put("sex", Integer.toString(sex));
                    params.put("sex_orientation", Integer.toString(sexOrientation));
                    params.put("liked", Integer.toString(liked));
                    params.put("matches", Integer.toString(matches));

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);

        }
    }

    public void loadingComplete() {

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        if (itemsList.size() == 0 || (itemsList.size() - 1)  < position) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();

            if (position == -1) position = 0;

            updateHotgameContainer();
            showHotgameContainer();
        }

        loading = false;
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);

        hideHotgameContainer();
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_hotgame, menu);

        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_hotgame_settings: {

                /** Getting the fragment manager */
                android.app.FragmentManager fm = getActivity().getFragmentManager();

                /** Instantiating the DialogFragment class */
                HotgameSettingsDialog alert = new HotgameSettingsDialog();

                /** Creating a bundle object to store the selected item's index */
                Bundle b  = new Bundle();

                /** Storing the selected item's index in the bundle object */
                b.putInt("hotgameGender", sex);
                b.putInt("hotgameSexOrientation", sexOrientation);
                b.putInt("hotgameLiked", liked);
                b.putInt("hotgameMatches", matches);

                /** Setting the bundle object to the dialog fragment object */
                alert.setArguments(b);

                /** Creating the dialog fragment object, which will in turn open the alert dialog window */

                alert.show(fm, "alert_dialog_hotgame_settings");

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void like(final long profileId) {

        loading = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_LIKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "HotgameFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("myLike")) {

                                    itemsList.get(position).setMyLike(response.getBoolean("myLike"));
                                }

                                if (response.has("match")) {

                                    itemsList.get(position).setMatch(response.getBoolean("match"));
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            updateHotgameContainer();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "HotgameFragment Not Added to Activity");

                    return;
                }

                loading = false;

                updateHotgameContainer();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}