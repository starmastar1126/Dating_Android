package ru.ifsoft.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.ifsoft.chat.adapter.AdvancedPeopleListAdapter;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.model.Profile;
import ru.ifsoft.chat.util.CustomRequest;
import ru.ifsoft.chat.util.Helper;
import ru.ifsoft.chat.view.RangeSeekBar;

public class SearchFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    SearchView searchView = null;

    private RecyclerView mRecyclerView;
    private NestedScrollView mNestedView;

    TextView mMessage, mHeaderText, mHeaderSettings;
    ImageView mSplash;

    LinearLayout mHeaderContainer;

    SwipeRefreshLayout mItemsContainer;

    private ArrayList<Profile> itemsList;
    private AdvancedPeopleListAdapter itemsAdapter;

    public String queryText, currentQuery, oldQuery;

    public int itemCount;
    private int userId = 0;

    private int sex_orientation = 0, gender = 3, online = 0, moderation = 1, photo = 0, pro_mode = 0, age_from = 18, age_to = 105;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;
    private Boolean preload = true;

    int pastVisiblesItems = 0, visibleItemCount = 0, totalItemCount = 0;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new AdvancedPeopleListAdapter(getActivity(), itemsList);

            currentQuery = queryText = savedInstanceState.getString("queryText");

            viewMore = savedInstanceState.getBoolean("viewMore");

            restore = savedInstanceState.getBoolean("restore");
            preload = savedInstanceState.getBoolean("preload");
            itemId = savedInstanceState.getInt("itemId");
            userId = savedInstanceState.getInt("userId");
            itemCount = savedInstanceState.getInt("itemCount");

            gender = savedInstanceState.getInt("gender");
            sex_orientation = savedInstanceState.getInt("sex_orientation");
            online = savedInstanceState.getInt("online");
            moderation = savedInstanceState.getInt("moderation");
            photo = savedInstanceState.getInt("photo");
            pro_mode = savedInstanceState.getInt("pro_mode");
            age_from = savedInstanceState.getInt("age_from");
            age_to = savedInstanceState.getInt("age_to");

        } else {

            itemsList = new ArrayList<Profile>();
            itemsAdapter = new AdvancedPeopleListAdapter(getActivity(), itemsList);

            currentQuery = queryText = "";

            restore = false;
            preload = true;
            itemId = 0;
            userId = 0;
            itemCount = 0;

            readData();
        }

        mHeaderContainer = (LinearLayout) rootView.findViewById(R.id.container_header);
        mHeaderText = (TextView) rootView.findViewById(R.id.headerText);
        mHeaderSettings = (TextView) rootView.findViewById(R.id.headerSettings);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        mMessage = (TextView) rootView.findViewById(R.id.message);
        mSplash = (ImageView) rootView.findViewById(R.id.splash);

        mNestedView = (NestedScrollView) rootView.findViewById(R.id.nested_view);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getGridSpanCount(getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY < oldScrollY) { // up


                }

                if (scrollY > oldScrollY) { // down


                }

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {

                    if (!loadingMore && (viewMore) && !(mItemsContainer.isRefreshing())) {

                        if (preload) {

                            loadingMore = true;

                            preload();

                        } else {

                            currentQuery = getCurrentQuery();

                            if (currentQuery.equals(oldQuery)) {

                                loadingMore = true;

                                search();
                            }
                        }
                    }
                }
            }
        });

        itemsAdapter.setOnItemClickListener(new AdvancedPeopleListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Profile item, int position) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", item.getId());
                startActivity(intent);
            }
        });

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        if (queryText.length() == 0) {

            if (mRecyclerView.getAdapter().getItemCount() == 0) {

                showMessage(getString(R.string.label_search_start_screen_msg));
                mHeaderText.setVisibility(View.GONE);

            } else {

                if (preload) {

                    mHeaderText.setVisibility(View.GONE);

                } else {

                    mHeaderText.setVisibility(View.VISIBLE);
                    mHeaderText.setText(getText(R.string.label_search_results) + " " + Integer.toString(itemCount));
                }

                hideMessage();
            }

        } else {

            if (mRecyclerView.getAdapter().getItemCount() == 0) {

                showMessage(getString(R.string.label_search_results_error));
                mHeaderText.setVisibility(View.GONE);

            } else {

                mHeaderText.setVisibility(View.VISIBLE);
                mHeaderText.setText(getText(R.string.label_search_results) + " " + Integer.toString(itemCount));

                hideMessage();
            }
        }

        mHeaderSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getSearchSettings();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {

            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (isAdded()) {

                        if (!restore) {

                            if (preload) {

                                preload();
                            }
                        }
                    }
                }
            }, 300);
        }
    }

    @Override
    public void onRefresh() {

        currentQuery = queryText;

        currentQuery = currentQuery.trim();

        if (App.getInstance().isConnected() && currentQuery.length() != 0) {

            userId = 0;
            search();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    public String getCurrentQuery() {

        String searchText = searchView.getQuery().toString();
        searchText = searchText.trim();

        return searchText;
    }

    public void searchStart() {

        preload = false;

        currentQuery = getCurrentQuery();

        if (App.getInstance().isConnected()) {

            userId = 0;
            search();

        } else {

            Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("viewMore", viewMore);
        outState.putString("queryText", queryText);
        outState.putBoolean("restore", true);
        outState.putBoolean("preload", preload);
        outState.putInt("itemId", itemId);
        outState.putInt("userId", userId);
        outState.putInt("itemCount", itemCount);

        outState.putInt("gender", gender);
        outState.putInt("sex_orientation", sex_orientation);
        outState.putInt("moderation", moderation);
        outState.putInt("online", online);
        outState.putInt("photo", photo);
        outState.putInt("pro_mode", pro_mode);
        outState.putInt("age_from", age_from);
        outState.putInt("age_to", age_to);

        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

//        MenuInflater menuInflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.options_menu_main_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {

            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }

        if (searchView != null) {

            searchView.setQuery(queryText, false);

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setIconified(false);

            SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchAutoComplete.setHint(getText(R.string.placeholder_search));
            searchAutoComplete.setHintTextColor(getResources().getColor(R.color.white));

            searchView.clearFocus();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {

                    queryText = newText;

                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {

                    queryText = query;
                    searchStart();

                    return false;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void search() {

        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_SEARCH, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "SearchFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemCount = response.getInt("itemCount");
                                oldQuery = response.getString("query");
                                userId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray usersArray = response.getJSONArray("items");

                                    arrayLength = usersArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < usersArray.length(); i++) {

                                            JSONObject profileObj = (JSONObject) usersArray.get(i);

                                            Profile u = new Profile(profileObj);

                                            itemsList.add(u);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loadingComplete();

                            Log.e("response", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "SearchFragment Not Added to Activity");

                    return;
                }

                loadingComplete();

                Toast.makeText(getActivity(), getString(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("query", currentQuery);
                params.put("userId", Integer.toString(userId));
                params.put("gender", Integer.toString(gender));
                params.put("online", Integer.toString(online));
                params.put("photo", Integer.toString(photo));
                params.put("pro", Integer.toString(pro_mode));
                params.put("ageFrom", Integer.toString(age_from));
                params.put("ageTo", Integer.toString(age_to));
                params.put("sex_orientation", Integer.toString(sex_orientation));
                params.put("moderation", Integer.toString(moderation));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void preload() {

        if (preload) {

            mItemsContainer.setRefreshing(true);

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_SEARCH_PRELOAD, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!isAdded() || getActivity() == null) {

                                    Log.e("ERROR", "SearchFragment Not Added to Activity");

                                    return;
                                }

                                if (!loadingMore) {

                                    itemsList.clear();
                                }

                                arrayLength = 0;

                                if (!response.getBoolean("error")) {

                                    itemId = response.getInt("itemId");

                                    if (response.has("items")) {

                                        JSONArray usersArray = response.getJSONArray("items");

                                        arrayLength = usersArray.length();

                                        if (arrayLength > 0) {

                                            for (int i = 0; i < usersArray.length(); i++) {

                                                JSONObject profileObj = (JSONObject) usersArray.get(i);

                                                Profile u = new Profile(profileObj);

                                                itemsList.add(u);
                                            }
                                        }
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                loadingComplete();

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "SearchFragment Not Added to Activity");

                        return;
                    }

                    loadingComplete();
                    Toast.makeText(getActivity(), getString(R.string.error_data_loading), Toast.LENGTH_LONG).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("itemId", Integer.toString(itemId));
                    params.put("gender", Integer.toString(gender));
                    params.put("online", Integer.toString(online));
                    params.put("photo", Integer.toString(photo));
                    params.put("pro", Integer.toString(pro_mode));
                    params.put("ageFrom", Integer.toString(age_from));
                    params.put("ageTo", Integer.toString(age_to));
                    params.put("sex_orientation", Integer.toString(sex_orientation));
                    params.put("moderation", Integer.toString(moderation));

                    return params;
                }
            };

            jsonReq.setRetryPolicy(new RetryPolicy() {

                @Override
                public int getCurrentTimeout() {

                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {

                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void loadingComplete() {

        restore = true;

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();

        loadingMore = false;

        mItemsContainer.setRefreshing(false);

        if (mRecyclerView.getAdapter().getItemCount() == 0) {

            showMessage(getString(R.string.label_search_results_error));

            if (isAdded()) {

                mHeaderText.setVisibility(View.GONE);
            }

        } else {

            hideMessage();

            if (isAdded()) {

                if (preload) {

                    mHeaderText.setVisibility(View.GONE);

                } else {

                    mHeaderText.setVisibility(View.VISIBLE);

                    mHeaderText.setText(getText(R.string.label_search_results) + " " + Integer.toString(itemCount));
                }
            }
        }
    }

    public void showMessage(String message) {

        if (isAdded()) {

            mMessage.setText(message);
            mMessage.setVisibility(View.VISIBLE);

            mSplash.setVisibility(View.VISIBLE);
        }
    }

    public void hideMessage() {

        if (isAdded()) {

            mMessage.setVisibility(View.GONE);

            mSplash.setVisibility(View.GONE);
        }
    }

    public void getSearchSettings() {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(getText(R.string.label_search_settings_dialog_title));

        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_search_settings, null);

        b.setView(view);

        final RadioButton mAnyGenderRadio = (RadioButton) view.findViewById(R.id.radio_gender_any);
        final RadioButton mMaleGenderRadio = (RadioButton) view.findViewById(R.id.radio_gender_male);
        final RadioButton mFemaleGenderRadio = (RadioButton) view.findViewById(R.id.radio_gender_female);
        final RadioButton mSecretGenderRadio = (RadioButton) view.findViewById(R.id.radio_gender_secret);

        final RadioButton mAnySexOrientationRadio = (RadioButton) view.findViewById(R.id.radio_sex_orientation_any);
        final RadioButton mHeterosexualSexOrientationRadio = (RadioButton) view.findViewById(R.id.radio_sex_orientation_heterosexual);
        final RadioButton mGaySexOrientationRadio = (RadioButton) view.findViewById(R.id.radio_sex_orientation_gay);
        final RadioButton mLesbianSexOrientationRadio = (RadioButton) view.findViewById(R.id.radio_sex_orientation_lesbian);
        final RadioButton mBisexualSexOrientationRadio = (RadioButton) view.findViewById(R.id.radio_sex_orientation_bisexual);

        final CheckBox mOnlineCheckBox = (CheckBox) view.findViewById(R.id.checkbox_online);
        final CheckBox mPhotoCheckBox = (CheckBox) view.findViewById(R.id.checkbox_photo);
        final CheckBox mProCheckBox = (CheckBox) view.findViewById(R.id.checkbox_pro);
        final CheckBox mModerationCheckBox = (CheckBox) view.findViewById(R.id.checkbox_moderation);

        final RangeSeekBar<Integer> mAgeSeekBar = view.findViewById(R.id.age_seekbar);

        switch (gender) {

            case 0: {

                mMaleGenderRadio.setChecked(true);

                break;
            }

            case 1: {

                mFemaleGenderRadio.setChecked(true);

                break;
            }

            case 2: {

                mSecretGenderRadio.setChecked(true);

                break;
            }

            default: {

                mAnyGenderRadio.setChecked(true);

                break;
            }
        }

        switch (sex_orientation) {

            case 0: {

                mAnySexOrientationRadio.setChecked(true);

                break;
            }

            case 1: {

                mHeterosexualSexOrientationRadio.setChecked(true);

                break;
            }

            case 2: {

                mGaySexOrientationRadio.setChecked(true);

                break;
            }

            case 3: {

                mLesbianSexOrientationRadio.setChecked(true);

                break;
            }

            default: {

                mBisexualSexOrientationRadio.setChecked(true);

                break;
            }
        }

        mOnlineCheckBox.setChecked(false);
        mPhotoCheckBox.setChecked(false);
        mProCheckBox.setChecked(false);
        mModerationCheckBox.setChecked(false);

        if (online > 0) {

            mOnlineCheckBox.setChecked(true);
        }

        if (photo > 0) {

            mPhotoCheckBox.setChecked(true);
        }

        if (pro_mode > 0) {

            mProCheckBox.setChecked(true);
        }

        if (moderation > 0) {

            mModerationCheckBox.setChecked(true);
        }

        mAgeSeekBar.setSelectedMinValue(age_from);
        mAgeSeekBar.setSelectedMaxValue(age_to);

        b.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                // age

                age_from = mAgeSeekBar.getSelectedMinValue();
                age_to = mAgeSeekBar.getSelectedMaxValue();

                // Gender

                if (mAnyGenderRadio.isChecked()) {

                    gender = 3;
                }

                if (mMaleGenderRadio.isChecked()) {

                    gender = 0;
                }

                if (mFemaleGenderRadio.isChecked()) {

                    gender = 1;
                }

                if (mSecretGenderRadio.isChecked()) {

                    gender = 2;
                }

                // Sex orientation

                if (mAnySexOrientationRadio.isChecked()) {

                    sex_orientation = 0;
                }

                if (mHeterosexualSexOrientationRadio.isChecked()) {

                    sex_orientation = 1;
                }

                if (mGaySexOrientationRadio.isChecked()) {

                    sex_orientation = 2;
                }

                if (mLesbianSexOrientationRadio.isChecked()) {

                    sex_orientation = 3;
                }

                if (mBisexualSexOrientationRadio.isChecked()) {

                    sex_orientation = 4;
                }

                //

                if (mOnlineCheckBox.isChecked()) {

                    online = 1;

                } else {

                    online = 0;
                }

                if (mPhotoCheckBox.isChecked()) {

                    photo = 1;

                } else {

                    photo = 0;
                }

                if (mProCheckBox.isChecked()) {

                    pro_mode = 1;

                } else {

                    pro_mode = 0;
                }

                if (mModerationCheckBox.isChecked()) {

                    moderation = 1;

                } else {

                    moderation = 0;
                }

                // Save filters settings

                saveData();

                // Reload items list

                String q = getCurrentQuery();

                if (preload) {

                    itemId = 0;

                    preload();

                } else {

                    if (q.length() > 0) {

                        searchStart();
                    }
                }
            }
        });

        b.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        AlertDialog d = b.create();

        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        d.show();
    }

    private void readData() {

        gender = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_gender), 3); // 3 = all
        sex_orientation = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_sex_orientation), 0); // 0 = all
        online = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_online), 0);
        photo = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_photo), 0);
        pro_mode = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_pro), 0);
        moderation = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_moderation), 1);
        age_from = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_age_from), 18);
        age_to = App.getInstance().getSharedPref().getInt(getString(R.string.settings_search_age_to), 105);
    }

    public void saveData() {

        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_gender), gender).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_sex_orientation), sex_orientation).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_online), online).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_photo), photo).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_pro), pro_mode).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_moderation), moderation).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_age_from), age_from).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_search_age_to), age_to).apply();
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