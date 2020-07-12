package ru.ifsoft.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ru.ifsoft.chat.adapter.FriendsSpotlightListAdapter;
import ru.ifsoft.chat.adapter.GalleryListAdapter;
import ru.ifsoft.chat.adapter.GiftsSpotlightListAdapter;
import ru.ifsoft.chat.adapter.ProfilesSpotlightListAdapter;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.dialogs.ImageChooseDialog;
import ru.ifsoft.chat.dialogs.ProfileBlockDialog;
import ru.ifsoft.chat.dialogs.ProfileReportDialog;
import ru.ifsoft.chat.model.Friend;
import ru.ifsoft.chat.model.Gift;
import ru.ifsoft.chat.model.Image;
import ru.ifsoft.chat.model.Profile;
import ru.ifsoft.chat.util.Api;
import ru.ifsoft.chat.util.CustomRequest;
import ru.ifsoft.chat.util.Helper;

public class ProfileFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";
    private static final String STATE_LIKES_SPOTLIGHT_LIST = "State Adapter Data 2";
    private static final String STATE_FRIENDS_SPOTLIGHT_LIST = "State Adapter Data 3";
    private static final String STATE_GIFTS_SPOTLIGHT_LIST = "State Adapter Data 4";

    private ProgressDialog pDialog;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 1;
    private static final int SELECT_COVER = 2;
    private static final int PROFILE_EDIT = 3;
    private static final int PROFILE_NEW_POST = 4;
    private static final int CREATE_PHOTO = 5;
    private static final int CREATE_COVER = 6;
    private static final int PROFILE_CHAT = 7;
    private static final int PROFILE_FEELINGS = 8;

    Button mFriendsSpotlightMoreButton, mGiftsSpotlightMoreButton, mLikesSpotlightMoreButton;
    TextView mFriendsSpotlightTitle, mGiftsSpotlightTitle, mLikesSpotlightTitle;
    CardView mFriendsSpotlight, mGiftsSpotlight, mLikesSpotlight;
    RecyclerView mFriendsSpotlightRecyclerView, mGiftsSpotlightRecyclerView, mLikesSpotlightRecyclerView;

    private ArrayList<Profile> likesSpotlightList;
    private ProfilesSpotlightListAdapter likesSpotlightAdapter;

    private ArrayList<Friend> friendsSpotlightList;
    private FriendsSpotlightListAdapter friendsSpotlightAdapter;

    private ArrayList<Gift> giftsSpotlightList;
    private GiftsSpotlightListAdapter giftsSpotlightAdapter;

    private int mAccountAction = 0; // 0 = choicePhoto, 1 = choiceCover

    RelativeLayout mProfileLoadingScreen, mProfileErrorScreen, mProfileDisabledScreen;

    LinearLayout mLocationContainer, mProfileInfoContainer, mProfileCountersContainer;

    LinearLayout mProfileSexOrientationContainer, mProfileAgeContainer, mProfileHeightContainer, mProfileWeightContainer, mProfileStatusContainer, mProfileJoinDateContainer, mProfileBirthDateContainer, mProfileGenderContainer, mProfileRelationshipStatusContainer, mProfilePoliticalViewsContainer, mProfileWorldViewContainer, mProfilePersonalPriorityContainer, mProfileImportantInOthersContainer, mProfileFacebookContainer, mProfileSiteContainer;
    LinearLayout mProfileSmokingViewsContainer, mProfileAlcoholViewsContainer, mProfileProfileLookingContainer, mProfileGenderLikeContainer;
    TextView mProfileSmokingViews, mProfileAlcoholViews, mProfileProfileLooking, mProfileGenderLike;
    TextView mProfileSexOrientation, mProfileAge, mProfileHeight, mProfileWeight, mProfileStatus, mProfileJoinDate, mProfileBirthDate, mProfileGender, mProfileRelationshipStatus, mProfilePoliticalViews, mProfileWorldView, mProfilePersonalPriority, mProfileImportantInOthers, mProfileFacebookUrl, mProfileSiteUrl;

    SwipeRefreshLayout mProfileRefreshLayout;
    NestedScrollView mNestedScrollView;

    CircularImageView mProfilePhoto, mProfileIcon, mProfileProIcon, mFeelingIcon;
    ImageView mProfileCover, mProfileOnlineIcon, mProfileSexOrientationImage;
    TextView mProfileLocation, mProfileFullname, mProfileUsername;
    RecyclerView mRecyclerView;
    TextView mProfileItemsCount, mProfileFriendsCount, mProfileLikesCount, mProfileGiftsCount;
    MaterialRippleLayout mProfileItemsBtn, mProfileFriendsBtn, mProfileLikesBtn, mProfileGiftsBtn;

    Button mProfileMessageBtn, mProfileActionBtn;

    Toolbar mToolbar;

    Profile profile;

    private ArrayList<Image> itemsList;

    private GalleryListAdapter itemsAdapter;

    private String selectedPhoto, selectedCover;
    private Uri outputFileUri;

    private Boolean loadingComplete = false;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;

    private String profile_mention;
    public long profile_id;
    int itemId = 0;
    int arrayLength = 0;
    int accessMode = 0;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;

    private Boolean isMainScreen = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        profile_id = i.getLongExtra("profileId", 0);
        profile_mention = i.getStringExtra("profileMention");

        if (profile_id == 0 && (profile_mention == null || profile_mention.length() == 0)) {

            profile_id = App.getInstance().getId();
            isMainScreen = true;
        }

        profile = new Profile();
        profile.setId(profile_id);

        itemsList = new ArrayList<>();
        itemsAdapter = new GalleryListAdapter(getActivity(), itemsList);

        likesSpotlightList = new ArrayList<Profile>();
        likesSpotlightAdapter = new ProfilesSpotlightListAdapter(getActivity(), likesSpotlightList);

        friendsSpotlightList = new ArrayList<Friend>();
        friendsSpotlightAdapter = new FriendsSpotlightListAdapter(getActivity(), friendsSpotlightList);

        giftsSpotlightList = new ArrayList<Gift>();
        giftsSpotlightAdapter = new GiftsSpotlightListAdapter(getActivity(), giftsSpotlightList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new GalleryListAdapter(getActivity(), itemsList);

            likesSpotlightList = savedInstanceState.getParcelableArrayList(STATE_LIKES_SPOTLIGHT_LIST);
            likesSpotlightAdapter = new ProfilesSpotlightListAdapter(getActivity(), likesSpotlightList);

            friendsSpotlightList = savedInstanceState.getParcelableArrayList(STATE_FRIENDS_SPOTLIGHT_LIST);
            friendsSpotlightAdapter = new FriendsSpotlightListAdapter(getActivity(), friendsSpotlightList);

            giftsSpotlightList = savedInstanceState.getParcelableArrayList(STATE_GIFTS_SPOTLIGHT_LIST);
            giftsSpotlightAdapter = new GiftsSpotlightListAdapter(getActivity(), giftsSpotlightList);

            itemId = savedInstanceState.getInt("itemId");

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            profile = savedInstanceState.getParcelable("profileObj");

        } else {

            itemsList = new ArrayList<>();
            itemsAdapter = new GalleryListAdapter(getActivity(), itemsList);

            likesSpotlightList = new ArrayList<Profile>();
            likesSpotlightAdapter = new ProfilesSpotlightListAdapter(getActivity(), likesSpotlightList);

            friendsSpotlightList = new ArrayList<Friend>();
            friendsSpotlightAdapter = new FriendsSpotlightListAdapter(getActivity(), friendsSpotlightList);

            giftsSpotlightList = new ArrayList<Gift>();
            giftsSpotlightAdapter = new GiftsSpotlightListAdapter(getActivity(), giftsSpotlightList);

            itemId = 0;

            restore = false;
            loading = false;
            preload = false;
        }

        if (loading) {


            showpDialog();
        }


        mProfileRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profileRefreshLayout);
        mProfileRefreshLayout.setOnRefreshListener(this);

        mProfileLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.profileLoadingScreen);
        mProfileErrorScreen = (RelativeLayout) rootView.findViewById(R.id.profileErrorScreen);
        mProfileDisabledScreen = (RelativeLayout) rootView.findViewById(R.id.profileDisabledScreen);

        mProfileInfoContainer = (LinearLayout) rootView.findViewById(R.id.profileInfoContainer);
        mProfileCountersContainer = (LinearLayout) rootView.findViewById(R.id.profileCountersContainer);

        mProfileSexOrientationContainer = (LinearLayout) rootView.findViewById(R.id.profileSexOrientationContainer);
        mProfileAgeContainer = (LinearLayout) rootView.findViewById(R.id.profileAgeContainer);
        mProfileHeightContainer = (LinearLayout) rootView.findViewById(R.id.profileHeightContainer);
        mProfileWeightContainer = (LinearLayout) rootView.findViewById(R.id.profileWeightContainer);

        mProfileSexOrientation = (TextView) rootView.findViewById(R.id.profileSexOrientation);
        mProfileAge = (TextView) rootView.findViewById(R.id.profileAge);
        mProfileHeight = (TextView) rootView.findViewById(R.id.profileHeight);
        mProfileWeight = (TextView) rootView.findViewById(R.id.profileWeight);

        mProfileSexOrientationImage = (ImageView) rootView.findViewById(R.id.profileSexOrientationImage);

        mProfileStatusContainer = (LinearLayout) rootView.findViewById(R.id.profileStatusContainer);
        mProfileJoinDateContainer = (LinearLayout) rootView.findViewById(R.id.profileJoinDateContainer);
        mProfileBirthDateContainer = (LinearLayout) rootView.findViewById(R.id.profileBirthDateContainer);
        mProfileGenderContainer = (LinearLayout) rootView.findViewById(R.id.profileGenderContainer);
        mProfileRelationshipStatusContainer = (LinearLayout) rootView.findViewById(R.id.profileRelationshipStatusContainer);
        mProfilePoliticalViewsContainer = (LinearLayout) rootView.findViewById(R.id.profilePoliticalViewsContainer);
        mProfileWorldViewContainer = (LinearLayout) rootView.findViewById(R.id.profileWorldViewContainer);
        mProfilePersonalPriorityContainer = (LinearLayout) rootView.findViewById(R.id.profilePersonalPriorityContainer);
        mProfileImportantInOthersContainer = (LinearLayout) rootView.findViewById(R.id.profileImportantInOthersContainer);
        mProfileSmokingViewsContainer = (LinearLayout) rootView.findViewById(R.id.profileSmokingViewsContainer);
        mProfileAlcoholViewsContainer = (LinearLayout) rootView.findViewById(R.id.profileAlcoholViewsContainer);
        mProfileProfileLookingContainer = (LinearLayout) rootView.findViewById(R.id.profileProfileLookingContainer);
        mProfileGenderLikeContainer = (LinearLayout) rootView.findViewById(R.id.profileGenderLikeContainer);

        mProfileFacebookContainer = (LinearLayout) rootView.findViewById(R.id.profileFacebookContainer);
        mProfileSiteContainer = (LinearLayout) rootView.findViewById(R.id.profileSiteContainer);

        mProfileStatus = (TextView) rootView.findViewById(R.id.profileStatus);
        mProfileJoinDate = (TextView) rootView.findViewById(R.id.profileJoinDate);
        mProfileBirthDate = (TextView) rootView.findViewById(R.id.profileBirthDate);
        mProfileGender = (TextView) rootView.findViewById(R.id.profileGender);
        mProfileRelationshipStatus = (TextView) rootView.findViewById(R.id.profileRelationshipStatus);
        mProfilePoliticalViews = (TextView) rootView.findViewById(R.id.profilePoliticalViews);
        mProfileWorldView = (TextView) rootView.findViewById(R.id.profileWorldView);
        mProfilePersonalPriority = (TextView) rootView.findViewById(R.id.profilePersonalPriority);
        mProfileImportantInOthers = (TextView) rootView.findViewById(R.id.profileImportantInOthers);
        mProfileSmokingViews = (TextView) rootView.findViewById(R.id.profileSmokingViews);
        mProfileAlcoholViews = (TextView) rootView.findViewById(R.id.profileAlcoholViews);
        mProfileProfileLooking = (TextView) rootView.findViewById(R.id.profileProfileLooking);
        mProfileGenderLike = (TextView) rootView.findViewById(R.id.profileGenderLike);

        mProfileFacebookUrl = (TextView) rootView.findViewById(R.id.profileFacebookUrl);
        mProfileSiteUrl = (TextView) rootView.findViewById(R.id.profileSiteUrl);

        ((ProfileActivity)getActivity()).mFabButton.hide();

        // Start prepare Friends Spotlight

        mFriendsSpotlightTitle = (TextView) rootView.findViewById(R.id.friendsSpotlightTitle);
        mFriendsSpotlightMoreButton = (Button) rootView.findViewById(R.id.friendsSpotlightMoreBtn);
        mFriendsSpotlight = (CardView) rootView.findViewById(R.id.friendsSpotlight);
        mFriendsSpotlightRecyclerView = (RecyclerView) rootView.findViewById(R.id.friendsSpotlightRecyclerView);

        mFriendsSpotlight.setVisibility(View.GONE);

        mFriendsSpotlightRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mFriendsSpotlightRecyclerView.setAdapter(friendsSpotlightAdapter);

        friendsSpotlightAdapter.setOnItemClickListener(new FriendsSpotlightListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Friend obj, int position) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", obj.getFriendUserId());
                startActivity(intent);
            }
        });

        mFriendsSpotlightMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileFriends(profile.getId());
            }
        });

        // Start prepare Gifts Spotlight

        mGiftsSpotlightTitle = (TextView) rootView.findViewById(R.id.giftsSpotlightTitle);
        mGiftsSpotlightMoreButton = (Button) rootView.findViewById(R.id.giftsSpotlightMoreBtn);
        mGiftsSpotlight = (CardView) rootView.findViewById(R.id.giftsSpotlight);
        mGiftsSpotlightRecyclerView = (RecyclerView) rootView.findViewById(R.id.giftsSpotlightRecyclerView);

        mGiftsSpotlight.setVisibility(View.GONE);

        mGiftsSpotlightRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mGiftsSpotlightRecyclerView.setAdapter(giftsSpotlightAdapter);

        giftsSpotlightAdapter.setOnItemClickListener(new GiftsSpotlightListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Gift obj, int position) {

                showProfileGifts(profile.getId());
            }
        });

        mGiftsSpotlightMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileGifts(profile.getId());
            }
        });

        // END Gifts Spotlight

        // Start prepare Likes Spotlight

        mLikesSpotlightTitle = (TextView) rootView.findViewById(R.id.likesSpotlightTitle);
        mLikesSpotlightMoreButton = (Button) rootView.findViewById(R.id.likesSpotlightMoreBtn);
        mLikesSpotlight = (CardView) rootView.findViewById(R.id.likesSpotlight);
        mLikesSpotlightRecyclerView = (RecyclerView) rootView.findViewById(R.id.likesSpotlightRecyclerView);

        mLikesSpotlight.setVisibility(View.GONE);

        mLikesSpotlightRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mLikesSpotlightRecyclerView.setAdapter(likesSpotlightAdapter);

        likesSpotlightAdapter.setOnItemClickListener(new ProfilesSpotlightListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Profile obj, int position) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", obj.getId());
                startActivity(intent);
            }
        });

        mLikesSpotlightMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileLikes(profile.getId());
            }
        });

        // END Gifts Spotlight


        mNestedScrollView = (NestedScrollView) rootView.findViewById(R.id.nestedScrollView);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getGalleryGridCount(getActivity()));
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setPadding(2, 2, 2, 2);

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {

                    if (!loadingMore && (viewMore) && !(mProfileRefreshLayout.isRefreshing())) {

                        mProfileRefreshLayout.setRefreshing(true);

                        loadingMore = true;

                        getItems();
                    }
                }
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new FriendsFragment.RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Image img = (Image) itemsList.get(position);

                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.putExtra("itemId", img.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        mProfileActionBtn = (Button) rootView.findViewById(R.id.profileActionBtn);
        mProfileMessageBtn = (Button) rootView.findViewById(R.id.profileMessageBtn);

        mProfileFullname = (TextView) rootView.findViewById(R.id.profileFullname);
        mProfileUsername = (TextView) rootView.findViewById(R.id.profileUsername);

        mProfileOnlineIcon = (ImageView) rootView.findViewById(R.id.profileOnlineIcon);

        mProfileItemsCount = (TextView) rootView.findViewById(R.id.profileItemsCount);
        mProfileFriendsCount = (TextView) rootView.findViewById(R.id.profileFriendsCount);
        mProfileLikesCount = (TextView) rootView.findViewById(R.id.profileLikesCount);
        mProfileGiftsCount = (TextView) rootView.findViewById(R.id.profileGiftsCount);

        mProfileItemsBtn = (MaterialRippleLayout) rootView.findViewById(R.id.profileItemsBtn);
        mProfileFriendsBtn = (MaterialRippleLayout) rootView.findViewById(R.id.profileFriendsBtn);
        mProfileLikesBtn = (MaterialRippleLayout) rootView.findViewById(R.id.profileLikesBtn);
        mProfileGiftsBtn = (MaterialRippleLayout) rootView.findViewById(R.id.profileGiftsBtn);

        mLocationContainer = (LinearLayout) rootView.findViewById(R.id.profileLocationContainer);
        mProfileLocation = (TextView) rootView.findViewById(R.id.profileLocation);

        mProfileFacebookContainer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!profile.getFacebookPage().startsWith("https://") && !profile.getFacebookPage().startsWith("http://")){

                    profile.setFacebookPage("http://" + profile.getFacebookPage());
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(profile.getFacebookPage()));
                startActivity(i);
            }
        });

        mProfileSiteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!profile.getInstagramPage().startsWith("https://") && !profile.getInstagramPage().startsWith("http://")){

                    profile.setInstagramPage("http://" + profile.getInstagramPage());
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(profile.getInstagramPage()));
                startActivity(i);
            }
        });

        mProfileFriendsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileFriends(profile.getId());
            }
        });

        mProfileLikesBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileLikes(profile.getId());
            }
        });

        mProfilePhoto = (CircularImageView) rootView.findViewById(R.id.profilePhoto);
        mProfileIcon = (CircularImageView) rootView.findViewById(R.id.profileIcon);
        mProfileProIcon = (CircularImageView) rootView.findViewById(R.id.profileProIcon);
        mFeelingIcon = (CircularImageView) rootView.findViewById(R.id.feelingIcon);
        mProfileCover = (ImageView) rootView.findViewById(R.id.profileCover);

        mProfileActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() == profile.getId()) {

                    getAccountSettings();

                } else {

                    if (profile.isFriend()) {

                        removeFromFriends();

                    } else {

                        if (!profile.isFollow()) {

                            addFollower();
                        }
                    }
                }
            }
        });

        ((ProfileActivity)getActivity()).mFabButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (profile.getId() == App.getInstance().getId()) {

                    Intent intent = new Intent(getActivity(), AddPhotoActivity.class);
                    startActivityForResult(intent, STREAM_NEW_POST);

                } else {

                    if (!profile.isInBlackList()) {

                        like(profile.getId());

                    } else {

                        Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mProfileMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getAccountModerateAt() != 0) {

                    if (App.getInstance().isPro() || App.getInstance().getFreeMessagesCount() > 0) {

                        if (profile.getAllowMessages() == 0 && !profile.isFriend()) {

                            Toast.makeText(getActivity(), getString(R.string.error_no_friend), Toast.LENGTH_SHORT).show();

                        } else {

                            if (!profile.isInBlackList()) {

                                Intent i = new Intent(getActivity(), ChatActivity.class);
                                i.putExtra("chatId", 0);
                                i.putExtra("profileId", profile.getId());
                                i.putExtra("withProfile", profile.getFullname());

                                i.putExtra("with_android_fcm_regId", profile.get_android_fcm_regId());
                                i.putExtra("with_ios_fcm_regId", profile.get_iOS_fcm_regId());

                                i.putExtra("with_user_username", profile.getUsername());
                                i.putExtra("with_user_fullname", profile.getFullname());
                                i.putExtra("with_user_photo_url", profile.getNormalPhotoUrl());

                                i.putExtra("with_user_state", profile.getState());
                                i.putExtra("with_user_verified", profile.getVerify());

                                startActivityForResult(i, PROFILE_CHAT);

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {

                        Toast.makeText(getActivity(), getString(R.string.msg_pro_mode_alert), Toast.LENGTH_LONG).show();
                    }

                } else {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(getText(R.string.dlg_moderation_title));

                    alertDialog.setMessage(getText(R.string.msg_account_not_moderated));
                    alertDialog.setCancelable(true);

                    alertDialog.setNegativeButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });

                    alertDialog.show();
                }
            }
        });

        mProfileGiftsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProfileGifts(profile.getId());
            }
        });


        mProfilePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (profile.getNormalPhotoUrl().length() > 0) {

                    Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                    i.putExtra("imgUrl", profile.getNormalPhotoUrl());
                    startActivity(i);
                }
            }
        });

        if (profile.getFullname() == null || profile.getFullname().length() == 0) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getData();

                Log.e("Profile", "OnReload");

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (profile.getState() == ACCOUNT_STATE_ENABLED) {

                    showContentScreen();

                    loadingComplete();
                    updateProfile();

                } else {

                    showDisabledScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("itemId", itemId);

        outState.putBoolean("restore", restore);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putParcelable("profileObj", profile);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
        outState.putParcelableArrayList(STATE_LIKES_SPOTLIGHT_LIST, likesSpotlightList);
        outState.putParcelableArrayList(STATE_FRIENDS_SPOTLIGHT_LIST, friendsSpotlightList);
        outState.putParcelableArrayList(STATE_GIFTS_SPOTLIGHT_LIST, giftsSpotlightList);
    }

    private Bitmap resize(String path){

        int maxWidth = 512;
        int maxHeight = 512;

        // create the options
        BitmapFactory.Options opts = new BitmapFactory.Options();

        //just decode the file
        opts.inJustDecodeBounds = true;
        Bitmap bp = BitmapFactory.decodeFile(path, opts);

        //get the original size
        int orignalHeight = opts.outHeight;
        int orignalWidth = opts.outWidth;

        //initialization of the scale
        int resizeScale = 1;

        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {

            final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
            final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
            resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale;
        opts.inJustDecodeBounds = false;

        //get the futur size of the bitmap
        int bmSize = (orignalWidth / resizeScale) * (orignalHeight / resizeScale) * 4;

        //check if it's possible to store into the vm java the picture
        if (Runtime.getRuntime().freeMemory() > bmSize) {

            //decode the file
            bp = BitmapFactory.decodeFile(path, opts);

        } else {

            return null;
        }

        return bp;
    }

    public void save(String outFile, String inFile) {

        try {

            Bitmap bmp = resize(outFile);

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, inFile);
            FileOutputStream fOut = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();

        } catch (Exception ex) {

            Log.e("Error", ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STREAM_NEW_POST && resultCode == getActivity().RESULT_OK && null != data) {

            profile.setPhotosCount(profile.getPhotosCount() + 1);

            itemId = 0;
            getItems();

        } else if (requestCode == SELECT_PHOTO && resultCode == getActivity().RESULT_OK && null != data) {

            Uri selectedImage = data.getData();

            selectedPhoto = getImageUrlWithAuthority(getActivity(), selectedImage, "photo.jpg");

            if (selectedPhoto != null) {

                save(selectedPhoto, "photo.jpg");

                File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "photo.jpg");

                uploadFile(METHOD_PROFILE_UPLOADPHOTO, f, UPLOAD_TYPE_PHOTO);
            }

        } else if (requestCode == SELECT_COVER && resultCode == getActivity().RESULT_OK && null != data) {

            Uri selectedImage = data.getData();

            selectedCover = getImageUrlWithAuthority(getActivity(), selectedImage, "cover.jpg");

            if (selectedCover != null) {

                save(selectedCover, "cover.jpg");

                File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "cover.jpg");

                uploadFile(METHOD_PROFILE_UPLOADCOVER, f, UPLOAD_TYPE_COVER);
            }

        } else if (requestCode == PROFILE_EDIT && resultCode == getActivity().RESULT_OK) {

            profile.setFullname(data.getStringExtra("fullname"));
            profile.setLocation(data.getStringExtra("location"));
            profile.setFacebookPage(data.getStringExtra("facebookPage"));
            profile.setInstagramPage(data.getStringExtra("instagramPage"));
            profile.setBio(data.getStringExtra("bio"));

            profile.setSex(data.getIntExtra("sex", 0));

            profile.setSexOrientation(data.getIntExtra("sexOrientation", 0));
            profile.setAge(data.getIntExtra("age", 0));
            profile.setHeight(data.getIntExtra("height", 0));
            profile.setWeight(data.getIntExtra("weight", 0));

            profile.setYear(data.getIntExtra("year", 0));
            profile.setMonth(data.getIntExtra("month", 0));
            profile.setDay(data.getIntExtra("day", 0));

            profile.setRelationshipStatus(data.getIntExtra("relationshipStatus", 0));
            profile.setPoliticalViews(data.getIntExtra("politicalViews", 0));
            profile.setWorldView(data.getIntExtra("worldView", 0));
            profile.setPersonalPriority(data.getIntExtra("personalPriority", 0));
            profile.setImportantInOthers(data.getIntExtra("importantInOthers", 0));
            profile.setViewsOnSmoking(data.getIntExtra("viewsOnSmoking", 0));
            profile.setViewsOnAlcohol(data.getIntExtra("viewsOnAlcohol", 0));
            profile.setYouLooking(data.getIntExtra("youLooking", 0));
            profile.setYouLike(data.getIntExtra("youLike", 0));

            profile.setAllowShowMyBirthday(data.getIntExtra("allowShowMyBirthday", 0));

            updateProfile();

        } else if (requestCode == PROFILE_FEELINGS && resultCode == getActivity().RESULT_OK) {


            profile.setFeeling(data.getIntExtra("feeling", 0));

            Log.e("Return", Integer.toString(profile.getFeeling()));

            updateFeeling();

        } else if (requestCode == PROFILE_NEW_POST && resultCode == getActivity().RESULT_OK) {

            getData();

        } else if (requestCode == CREATE_PHOTO && resultCode == getActivity().RESULT_OK) {

            try {

                selectedPhoto = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "photo.jpg";

                save(selectedPhoto, "photo.jpg");

                File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "photo.jpg");

                uploadFile(METHOD_PROFILE_UPLOADPHOTO, f, UPLOAD_TYPE_PHOTO);

            } catch (Exception ex) {

                Log.v("OnCameraCallBack", ex.getMessage());
            }

        } else if (requestCode == CREATE_COVER && resultCode == getActivity().RESULT_OK) {

            try {

                selectedCover = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "cover.jpg";

                save(selectedCover, "cover.jpg");

                File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "cover.jpg");

                uploadFile(METHOD_PROFILE_UPLOADCOVER, f, UPLOAD_TYPE_COVER);

            } catch (Exception ex) {

                Log.v("OnCameraCallBack", ex.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    choiceImage(mAccountAction);

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        showNoStoragePermissionSnackbar();
                    }
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void showNoStoragePermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void choiceImage(int type) {

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        ImageChooseDialog alert = new ImageChooseDialog();

        alert.show(fm, "alert_dialog_cover_choose");
    }

    public void imageFromGallery() {

        if (mAccountAction == 0) {

            photoFromGallery();

        } else {

            coverFromGallery();
        }
    }

    public void imageFromCamera() {

        if (mAccountAction == 0) {

            photoFromCamera();

        } else {

            coverFromCamera();
        }
    }

    public void photoFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_PHOTO);
    }

    public void photoFromCamera() {

        try {

            File root = new File(Environment.getExternalStorageDirectory(), APP_TEMP_FOLDER);

            if (!root.exists()) {

                root.mkdirs();
            }

            File sdImageMainDirectory = new File(root, "photo.jpg");

            outputFileUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(cameraIntent, CREATE_PHOTO);

        } catch (Exception e) {

            Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    public void coverFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_COVER);
    }

    public void coverFromCamera() {

        try {

            File root = new File(Environment.getExternalStorageDirectory(), APP_TEMP_FOLDER);

            if (!root.exists()) {

                root.mkdirs();
            }

            File sdImageMainDirectory = new File(root, "cover.jpg");

            outputFileUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(cameraIntent, CREATE_COVER);

        } catch (Exception e) {

            Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            getData();

        } else {

            mProfileRefreshLayout.setRefreshing(false);
        }
    }

    public void updateFeeling() {

        if (profile.getFeeling() > 0) {

            mFeelingIcon.setVisibility(View.VISIBLE);

            showFeeling(Constants.WEB_SITE + "feelings/" + Integer.toString(profile.getFeeling()) + ".png");

        } else {

            mFeelingIcon.setVisibility(View.GONE);
        }
    }

    public void updateProfile() {

        updateFullname();
        updateGiftsCount();
        updateFriendsCount();
        updateLikesCount();
        updateActionButton();
        updateFeeling();

        mProfileUsername.setText("@" + profile.getUsername());
        mProfileLocation.setText(profile.getLocation());

        mProfileItemsCount.setText(Integer.toString(profile.getPhotosCount()));
        mProfileFriendsCount.setText(Integer.toString(profile.getFriendsCount()));
        mProfileLikesCount.setText(Integer.toString(profile.getLikesCount()));
        mProfileGiftsCount.setText(Integer.toString(profile.getGiftsCount()));

        // Show settings button is your profile
        if (profile.getId() == App.getInstance().getId()) {

            ((ProfileActivity)getActivity()).mFabButton.show();
            ((ProfileActivity)getActivity()).mFabButton.setImageResource(R.drawable.ic_action_new);

            mProfileActionBtn.setText(R.string.action_profile_edit);

            mProfileActionBtn.setVisibility(View.VISIBLE);

            mProfileMessageBtn.setVisibility(View.GONE);

        } else {

            ((ProfileActivity)getActivity()).mFabButton.hide();

            if (!profile.isMyLike()) {

                ((ProfileActivity)getActivity()).mFabButton.setImageResource(R.drawable.ic_action_like);

                ((ProfileActivity)getActivity()).mFabButton.show();
            }

            mProfileMessageBtn.setText(R.string.action_message);

            mProfileMessageBtn.setVisibility(View.VISIBLE);

            if (profile.getAllowMessages() == 0 && !profile.isFriend()) {

                mProfileMessageBtn.setEnabled(false);

            } else {

                if (!profile.isInBlackList()) {

                    mProfileMessageBtn.setEnabled(true);

                } else {

                    mProfileMessageBtn.setEnabled(false);
                }
            }
        }

        if (profile.getLocation() != null && profile.getLocation().length() != 0) {

            mLocationContainer.setVisibility(View.VISIBLE);

        } else {

            mLocationContainer.setVisibility(View.GONE);
        }

        if (profile.getFacebookPage() != null && profile.getFacebookPage().length() != 0) {

            mProfileFacebookContainer.setVisibility(View.VISIBLE);
            mProfileFacebookUrl.setText(profile.getFacebookPage());

        } else {

            mProfileFacebookContainer.setVisibility(View.GONE);
        }

        if (profile.getInstagramPage() != null && profile.getInstagramPage().length() != 0) {

            mProfileSiteContainer.setVisibility(View.VISIBLE);
            mProfileSiteUrl.setText(profile.getInstagramPage());

        } else {

            mProfileSiteContainer.setVisibility(View.GONE);
        }

        if (profile.getBio() != null && profile.getBio().length() != 0) {

            mProfileStatusContainer.setVisibility(View.VISIBLE);
            mProfileStatus.setText(profile.getBio());

        } else {

            mProfileStatusContainer.setVisibility(View.GONE);
        }

        if (profile.getSex() == 0) {

            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_male));

        } else if (profile.getSex() == 1) {

            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_female));

        } else {

            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_secret));
        }

        switch (profile.getSexOrientation()) {

            case 1: {

                mProfileSexOrientation.setText(getString(R.string.label_sex_orientation) + ": " + getString(R.string.sex_orientation_1));
                mProfileSexOrientationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_heterosexual));

                break;
            }

            case 2: {

                mProfileSexOrientation.setText(getString(R.string.label_sex_orientation) + ": " + getString(R.string.sex_orientation_2));
                mProfileSexOrientationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_gay));

                break;
            }

            case 3: {

                mProfileSexOrientation.setText(getString(R.string.label_sex_orientation) + ": " + getString(R.string.sex_orientation_3));
                mProfileSexOrientationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_lesbian));

                break;
            }

            case 4: {

                mProfileSexOrientation.setText(getString(R.string.label_sex_orientation) + ": " + getString(R.string.sex_orientation_4));
                mProfileSexOrientationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_feature));

                break;
            }

            default: {

                mProfileSexOrientationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_feature));

                break;
            }
        }

        mProfileAge.setText(getString(R.string.label_age) + ": " + Integer.toString(profile.getAge()));
        mProfileHeight.setText(getString(R.string.label_height) + ": " + Integer.toString(profile.getHeight()) + " (" + getString(R.string.label_cm) + ")");
        mProfileWeight.setText(getString(R.string.label_weight) + ": " + Integer.toString(profile.getWeight()) + " (" + getString(R.string.label_kg) + ")");

        Helper helper = new Helper(getActivity());

        mProfileRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + helper.getRelationshipStatus(profile.getRelationshipStatus()));
        mProfilePoliticalViews.setText(getString(R.string.account_political_views) + ": " + helper.getPoliticalViews(profile.getPoliticalViews()));
        mProfileWorldView.setText(getString(R.string.account_world_view) + ": " + helper.getWorldView(profile.getWorldView()));
        mProfilePersonalPriority.setText(getString(R.string.account_personal_priority) + ": " + helper.getPersonalPriority(profile.getPersonalPriority()));
        mProfileImportantInOthers.setText(getString(R.string.account_important_in_others) + ": " + helper.getImportantInOthers(profile.getImportantInOthers()));
        mProfileSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + helper.getSmokingViews(profile.getViewsOnSmoking()));
        mProfileAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + helper.getAlcoholViews(profile.getViewsOnAlcohol()));
        mProfileProfileLooking.setText(getString(R.string.account_profile_looking) + ": " + helper.getLooking(profile.getYouLooking()));
        mProfileGenderLike.setText(getString(R.string.account_profile_like) + ": " + helper.getGenderLike(profile.getYouLike()));

        mProfileJoinDate.setText(getString(R.string.label_profile_join) + ": " + profile.getCreateDate());

        if (profile.getAllowShowMyBirthday() == 1) {

            mProfileBirthDate.setVisibility(View.VISIBLE);
            mProfileBirthDateContainer.setVisibility(View.VISIBLE);
            mProfileBirthDate.setText(getString(R.string.label_profile_birth) + ": " + profile.getBirthDate());

        } else {

            mProfileBirthDate.setVisibility(View.GONE);
            mProfileBirthDateContainer.setVisibility(View.GONE);
        }

        if (profile.getSexOrientation() == 0) {

            mProfileSexOrientationContainer.setVisibility(View.GONE);


        } else {

            mProfileSexOrientationContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getAge() == 0) {

            mProfileAgeContainer.setVisibility(View.GONE);

        } else {

            mProfileAgeContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getHeight() == 0) {

            mProfileHeightContainer.setVisibility(View.GONE);

        } else {

            mProfileHeightContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getWeight() == 0) {

            mProfileWeightContainer.setVisibility(View.GONE);

        } else {

            mProfileWeightContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getRelationshipStatus() == 0) {

            mProfileRelationshipStatusContainer.setVisibility(View.GONE);
        }

        if (profile.getPoliticalViews() == 0) {

            mProfilePoliticalViewsContainer.setVisibility(View.GONE);
        }

        if (profile.getWorldView() == 0) {

            mProfileWorldViewContainer.setVisibility(View.GONE);
        }

        if (profile.getPersonalPriority() == 0) {

            mProfilePersonalPriorityContainer.setVisibility(View.GONE);
        }

        if (profile.getImportantInOthers() == 0) {

            mProfileImportantInOthersContainer.setVisibility(View.GONE);
        }

        if (profile.getViewsOnSmoking() == 0) {

            mProfileSmokingViewsContainer.setVisibility(View.GONE);
        }

        if (profile.getViewsOnAlcohol() == 0) {

            mProfileAlcoholViewsContainer.setVisibility(View.GONE);
        }

        if (profile.getYouLooking() == 0) {

            mProfileProfileLookingContainer.setVisibility(View.GONE);
        }

        if (profile.getYouLike() == 0) {

            mProfileGenderLikeContainer.setVisibility(View.GONE);
        }

        showPhoto(profile.getLowPhotoUrl());
        showCover(profile.getNormalCoverUrl());

        showContentScreen();

        if (profile.isOnline()) {

            // User Online

            mProfileOnlineIcon.setVisibility(View.VISIBLE);

        } else {

            mProfileOnlineIcon.setVisibility(View.GONE);
        }

        if (profile.isVerify()) {

            mProfileIcon.setVisibility(View.VISIBLE);

        } else {

            mProfileIcon.setVisibility(View.GONE);
        }

        if (profile.isProMode()) {

            mProfileProIcon.setVisibility(View.VISIBLE);

        } else {

            mProfileProIcon.setVisibility(View.GONE);
        }

        // Profile Info

        mProfileInfoContainer.setVisibility(View.GONE);

        if (profile.getAllowShowMyInfo() == 0 || App.getInstance().getId() == profile.getId()) {

            mProfileInfoContainer.setVisibility(View.VISIBLE);

        } else {

            if (profile.getAllowShowMyInfo() == 1 && profile.isFriend()) {

                mProfileInfoContainer.setVisibility(View.VISIBLE);
            }
        }

        if (profile.getAllowShowMyGallery() == 0 || App.getInstance().getId() == profile.getId()) {

            if (profile.getPhotosCount() > 0 && itemsAdapter.getItemCount() != 0) {

                mRecyclerView.setVisibility(View.VISIBLE);

            } else {

                mRecyclerView.setVisibility(View.GONE);
            }

        } else {

            if (profile.getAllowShowMyGallery() == 1 && profile.isFriend()) {

                if (profile.getPhotosCount() > 0 && itemsAdapter.getItemCount() != 0) {

                    mRecyclerView.setVisibility(View.VISIBLE);

                } else {

                    mRecyclerView.setVisibility(View.GONE);
                }

            } else {

                mRecyclerView.setVisibility(View.GONE);
            }
        }

        mProfileCountersContainer.setVisibility(View.VISIBLE);

        if (this.isVisible()) {

            try {

                getActivity().invalidateOptionsMenu();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void updateFullname() {

        if (profile.getFullname() == null || profile.getFullname().length() == 0) {

            mProfileFullname.setText(profile.getUsername());
            if (!isMainScreen) getActivity().setTitle(profile.getUsername());

        } else {

            mProfileFullname.setText(profile.getFullname());
            if (!isMainScreen) getActivity().setTitle(profile.getFullname());
        }
    }

    public void getData() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile = new Profile(response);

                                if (profile.getPhotosCount() > 0) {

                                    getItems();
                                }

                                if (profile.getState() == ACCOUNT_STATE_ENABLED) {

                                    showContentScreen();

                                    updateProfile();

                                } else {

                                    showDisabledScreen();
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            if (profile.getState() == ACCOUNT_STATE_ENABLED && profile.getFriendsCount() > 0) {

                                getFriendsSpotlight();
                            }

                            if (profile.getState() == ACCOUNT_STATE_ENABLED && profile.getGiftsCount() > 0) {

                                getGiftsSpotlight();
                            }

                            if (profile.getState() == ACCOUNT_STATE_ENABLED && profile.getLikesCount() > 0) {

                                getLikesSpotlight();
                            }

                            Log.e("Profile Success",  response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                Log.e("Profile Error",  error.toString() + error.getMessage() + error.getLocalizedMessage());
                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));

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

    public void getFriendsSpotlight() {

        if (App.getInstance().getId() == profile.getId() || profile.getAllowShowMyFriends() == 0) {

            // All right. Load items

        } else {

            if (profile.getAllowShowMyFriends() == 1 && profile.isFriend()) {

                // All right. Load items

            } else {

                return;
            }
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FRIENDS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        friendsSpotlightList.clear();

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray friendsArray = response.getJSONArray("items");

                                    arrayLength = friendsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < friendsArray.length(); i++) {

                                            JSONObject userObj = (JSONObject) friendsArray.get(i);

                                            Friend item = new Friend(userObj);

                                            friendsSpotlightList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("Friends", response.toString());

                            loadingComplete();

                            updateFriendsCount();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                Log.e("getFriendsSpotlight", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));
                params.put("itemId", Integer.toString(0));
                params.put("language", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getGiftsSpotlight() {

        if (App.getInstance().getId() == profile.getId() || profile.getAllowShowMyGifts() == 0) {

            // All right. Load items

        } else {

            if (profile.getAllowShowMyGifts() == 1 && profile.isFriend()) {

                // All right. Load items

            } else {

                return;
            }
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GIFTS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        giftsSpotlightList.clear();

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray giftsArray = response.getJSONArray("items");

                                    arrayLength = giftsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < giftsArray.length(); i++) {

                                            JSONObject giftObj = (JSONObject) giftsArray.get(i);

                                            Gift item = new Gift(giftObj);

                                            giftsSpotlightList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("Gifts", response.toString());

                            loadingComplete();

                            updateGiftsCount();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                Log.e("getGiftsSpotlight", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));
                params.put("itemId", Integer.toString(0));
                params.put("language", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getLikesSpotlight() {

        if (App.getInstance().getId() == profile.getId() || profile.getAllowShowMyLikes() == 0) {

            // All right. Load items

        } else {

            if (profile.getAllowShowMyLikes() == 1 && profile.isFriend()) {

                // All right. Load items

            } else {

                return;
            }
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_FANS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        likesSpotlightList.clear();

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray likesArray = response.getJSONArray("items");

                                    arrayLength = likesArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < likesArray.length(); i++) {

                                            JSONObject profileObj = (JSONObject) likesArray.get(i);

                                            Profile item = new Profile(profileObj);

                                            likesSpotlightList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("Likes", response.toString());

                            loadingComplete();

                            updateLikesCount();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                Log.e("getLikesSpotlight", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));
                params.put("itemId", Integer.toString(0));
                params.put("language", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showFeeling(String imgUrl) {

        if (imgUrl != null && imgUrl.length() > 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(imgUrl, ImageLoader.getImageListener(mFeelingIcon, R.drawable.mood, R.drawable.mood));
        }
    }

    public void showPhoto(String photoUrl) {

        if (photoUrl != null && photoUrl.length() > 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(photoUrl, ImageLoader.getImageListener(mProfilePhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));
        }
    }

    public void showCover(String coverUrl) {

        if (coverUrl != null && coverUrl.length() > 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(mProfileCover, R.drawable.profile_default_cover, R.drawable.profile_default_cover));

            if (Build.VERSION.SDK_INT > 15) {

                mProfileCover.setImageAlpha(200);
            }
        }
    }

    public void getItems() {

        if (App.getInstance().getId() == profile.getId() || profile.getAllowShowMyGallery() == 0) {

            // All right. Load items

        } else {

            if (profile.getAllowShowMyGallery() == 1 && profile.isFriend()) {

                // All right. Load items

            } else {

                return;
            }
        }

        if (loadingMore) {

            mProfileRefreshLayout.setRefreshing(true);

        } else{

            itemId = 0;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PHOTOS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("photoId");

                                if (response.has("photos")) {

                                    JSONArray itemsArray = response.getJSONArray("photos");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            Image item = new Image(itemObj);

                                            itemsList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loadingComplete();
                            updateProfile();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                loadingComplete();
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile.getId()));
                params.put("photoId", Integer.toString(itemId));

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();
        friendsSpotlightAdapter.notifyDataSetChanged();
        giftsSpotlightAdapter.notifyDataSetChanged();
        likesSpotlightAdapter.notifyDataSetChanged();

        mProfileRefreshLayout.setRefreshing(false);

        loadingMore = false;

//        if (this.isVisible()) getActivity().invalidateOptionsMenu();
    }

    public void showLoadingScreen() {

        if (!isMainScreen) getActivity().setTitle(getText(R.string.title_activity_profile));

        mProfileRefreshLayout.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
        mProfileDisabledScreen.setVisibility(View.GONE);
//
        mProfileLoadingScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showErrorScreen() {

        if (!isMainScreen) getActivity().setTitle(getText(R.string.title_activity_profile));

        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileDisabledScreen.setVisibility(View.GONE);
        mProfileRefreshLayout.setVisibility(View.GONE);
//
        mProfileErrorScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showDisabledScreen() {

        if (profile.getState() != ACCOUNT_STATE_ENABLED) {

            //mProfileDisabledScreenMsg.setText(getText(R.string.msg_account_blocked));
        }

        getActivity().setTitle(getText(R.string.label_account_disabled));

        mProfileRefreshLayout.setVisibility(View.GONE);
        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
//
        mProfileDisabledScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showContentScreen() {

        if (!isMainScreen) {

            getActivity().setTitle(profile.getFullname());
        }

        mProfileDisabledScreen.setVisibility(View.GONE);
        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
//
        mProfileRefreshLayout.setVisibility(View.VISIBLE);
        mProfileRefreshLayout.setRefreshing(false);

        loadingComplete = true;
        restore = true;
    }

    public void action(int position) {

        final Image item = itemsList.get(position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (profile.getState() != ACCOUNT_STATE_ENABLED) {

                //hide all menu items
                hideMenuItems(menu, false);
            }

            if (App.getInstance().getId() != profile.getId()) {

                MenuItem menuItem = menu.findItem(R.id.action_profile_block);

                if (profile.isBlocked()) {

                    menuItem.setTitle(getString(R.string.action_unblock));

                } else {

                    menuItem.setTitle(getString(R.string.action_block));
                }

                menu.removeItem(R.id.action_profile_edit_feeling);
                menu.removeItem(R.id.action_profile_edit_photo);
                menu.removeItem(R.id.action_profile_edit_cover);
                menu.removeItem(R.id.action_profile_settings);

            } else {

                // your profile

                menu.removeItem(R.id.action_new_gift);
                menu.removeItem(R.id.action_profile_report);
                menu.removeItem(R.id.action_profile_block);
            }


            // If site not available - hide items

            if (!WEB_SITE_AVAILABLE) {

                menu.removeItem(R.id.action_profile_copy_url);
                menu.removeItem(R.id.action_profile_open_url);
            }

            //show all menu items
            hideMenuItems(menu, true);

        } else {

            //hide all menu items
            hideMenuItems(menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_profile_copy_url: {

                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(profile.getUsername(), API_DOMAIN + "profile.php?id=" + Long.toString(profile.getId()));
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getActivity(), getText(R.string.msg_profile_link_copied), Toast.LENGTH_SHORT).show();

                return true;
            }

            case R.id.action_profile_open_url: {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(API_DOMAIN + "profile.php?id=" + Long.toString(profile.getId())));
                startActivity(i);

                return true;
            }

            case R.id.action_new_gift: {

                selectGift(profile.getId());

                return true;
            }

            case R.id.action_profile_refresh: {

                mProfileRefreshLayout.setRefreshing(true);
                onRefresh();

                return true;
            }

            case R.id.action_profile_report: {

                profileReport();

                return true;
            }

            case R.id.action_profile_block: {

                profileBlock();

                return true;
            }

            case R.id.action_profile_edit_feeling: {

                Intent intent = new Intent(getActivity(), SelectFeelingActivity.class);
                intent.putExtra("profileId", profile.getId());
                startActivityForResult(intent, PROFILE_FEELINGS);

                return true;
            }

            case R.id.action_profile_edit_photo: {

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                    }

                } else {

                    mAccountAction = 0;

                    choiceImage(mAccountAction);
                }

                return true;
            }

            case R.id.action_profile_edit_cover: {

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                    }

                } else {

                    mAccountAction = 1;

                    choiceImage(mAccountAction);
                }

                return true;
            }

            case R.id.action_profile_settings: {

                getAccountSettings();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void profileReport() {

        /** Getting the fragment manager */
        android.app.FragmentManager fm = getActivity().getFragmentManager();

        /** Instantiating the DialogFragment class */
        ProfileReportDialog alert = new ProfileReportDialog();

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("position", 0);

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

        alert.show(fm, "alert_dialog_profile_report");
    }

    public  void onProfileReport(final int position) {

        Api api = new Api(getActivity());

        api.profileReport(profile.getId(), position);
    }

    public void profileBlock() {

        if (!profile.isBlocked()) {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            ProfileBlockDialog alert = new ProfileBlockDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putString("blockUsername", profile.getUsername());

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_profile_block");

        } else {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_REMOVE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ProfileFragment Not Added to Activity");

                                return;
                            }

                            try {

                                if (!response.getBoolean("error")) {

                                    profile.setBlocked(false);

                                    Toast.makeText(getActivity(), getString(R.string.msg_profile_removed_from_blacklist), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                loading = false;

                                hidepDialog();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "ProfileFragment Not Added to Activity");

                        return;
                    }

                    loading = false;

                    hidepDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("profileId", Long.toString(profile.getId()));

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public  void onProfileBlock() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_ADD, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setBlocked(true);

                                Toast.makeText(getActivity(), getString(R.string.msg_profile_added_to_blacklist), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile.getId()));
                params.put("reason", "example");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean uploadFile(String serverURL, File file, final int type) {

        loading = true;

        showpDialog();

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
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                    loading = false;

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

                            switch (type) {

                                case 0: {

                                    profile.setLowPhotoUrl(result.getString("lowPhotoUrl"));
                                    profile.setBigPhotoUrl(result.getString("bigPhotoUrl"));
                                    profile.setNormalPhotoUrl(result.getString("normalPhotoUrl"));

                                    App.getInstance().setPhotoUrl(result.getString("lowPhotoUrl"));

                                    break;
                                }

                                default: {

                                    profile.setNormalCoverUrl(result.getString("normalCoverUrl"));

                                    App.getInstance().setCoverUrl(result.getString("normalCoverUrl"));

                                    break;
                                }
                            }
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + response.body().string() + "\"");

                    } finally {

                        loading = false;

                        hidepDialog();

                        getData();
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }

    public void removeFromFriends() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FRIENDS_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setFriend(false);
                                profile.setFriendsCount(profile.getFriendsCount() - 1);

                                updateProfile();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("friendId", Long.toString(profile.getId()));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void addFollower() {

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_FOLLOW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setFollow(response.getBoolean("follow"));
                                profile.setFollowersCount(response.getInt("followersCount"));

                                updateProfile();

                                changeAccessMode();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void changeAccessMode() {

        if (App.getInstance().getId() == profile.getId() || profile.isFollow()) {

            accessMode = 1;

        } else {

            accessMode = 0;
        }
    }

    private void updateFriendsCount() {

        mProfileFriendsCount.setText(Integer.toString(profile.getFriendsCount()));
        mFriendsSpotlightTitle.setText(getString(R.string.label_friends) + " (" + Integer.toString(profile.getFriendsCount()) + ")");

        if (profile.getAllowShowMyFriends() == 0 || App.getInstance().getId() == profile.getId()) {

            if (profile.getFriendsCount() > 0 && friendsSpotlightAdapter.getItemCount() != 0) {

                mFriendsSpotlight.setVisibility(View.VISIBLE);

            } else {

                mFriendsSpotlight.setVisibility(View.GONE);
            }

        } else {

            mFriendsSpotlight.setVisibility(View.GONE);

            if (profile.getAllowShowMyFriends() == 1 && profile.isFriend()) {

                if (profile.getFriendsCount() > 0 && friendsSpotlightAdapter.getItemCount() != 0) {

                    mFriendsSpotlight.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void updateGiftsCount() {

        mProfileGiftsCount.setText(Integer.toString(profile.getGiftsCount()));
        mGiftsSpotlightTitle.setText(getString(R.string.label_gifts) + " (" + Integer.toString(profile.getGiftsCount()) + ")");

        if (profile.getAllowShowMyGifts() == 0 || App.getInstance().getId() == profile.getId()) {

            if (profile.getGiftsCount() > 0 && giftsSpotlightAdapter.getItemCount() != 0) {

                mGiftsSpotlight.setVisibility(View.VISIBLE);

            } else {

                mGiftsSpotlight.setVisibility(View.GONE);
            }

        } else {

            mGiftsSpotlight.setVisibility(View.GONE);

            if (profile.getAllowShowMyGifts() == 1 && profile.isFriend()) {

                if (profile.getGiftsCount() > 0 && giftsSpotlightAdapter.getItemCount() != 0) {

                    mGiftsSpotlight.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void updateLikesCount() {

        mProfileLikesCount.setText(Integer.toString(profile.getLikesCount()));
        mLikesSpotlightTitle.setText(getString(R.string.label_likes) + " (" + Integer.toString(profile.getLikesCount()) + ")");

        if (profile.getAllowShowMyLikes() == 0 || App.getInstance().getId() == profile.getId()) {

            if (profile.getLikesCount() > 0 && likesSpotlightAdapter.getItemCount() != 0) {

                mLikesSpotlight.setVisibility(View.VISIBLE);

            } else {

                mLikesSpotlight.setVisibility(View.GONE);
            }

        } else {

            mLikesSpotlight.setVisibility(View.GONE);

            if (profile.getAllowShowMyLikes() == 1 && profile.isFriend()) {

                if (profile.getLikesCount() > 0 && likesSpotlightAdapter.getItemCount() != 0) {

                    mLikesSpotlight.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void updateActionButton() {

        if (profile.getId() == App.getInstance().getId()) {

            mProfileActionBtn.setText(R.string.action_profile_edit);
            mProfileActionBtn.setEnabled(true);

        } else {

            if (profile.isFriend()) {

                mProfileActionBtn.setText(R.string.action_remove_from_friends);
                mProfileActionBtn.setEnabled(true);

            } else {

                if (profile.isFollow()) {

                    mProfileActionBtn.setText(R.string.action_pending);
                    mProfileActionBtn.setEnabled(false);

                } else {

                    mProfileActionBtn.setText(R.string.action_add_to_friends);
                    mProfileActionBtn.setEnabled(true);
                }
            }
        }
    }

    public void showProfileGifts(long profileId) {

        if (profile.getAllowShowMyGifts() == 0 || App.getInstance().getId() == profile.getId()) {

            Intent intent = new Intent(getActivity(), GiftsActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            if (profile.getAllowShowMyGifts() == 1 && profile.isFriend()) {

                Intent intent = new Intent(getActivity(), GiftsActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);

            }
        }
    }

    public void showProfileLikes(long profileId) {

        if (profile.getAllowShowMyLikes() == 0 || App.getInstance().getId() == profile.getId()) {

            Intent intent = new Intent(getActivity(), LikesActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            if (profile.getAllowShowMyLikes() == 1 && profile.isFriend()) {

                Intent intent = new Intent(getActivity(), LikesActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);

            }
        }
    }

    public void showProfileFriends(long profileId) {

        if (profile.getAllowShowMyFriends() == 0 || App.getInstance().getId() == profile.getId()) {

            Intent intent = new Intent(getActivity(), FriendsActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            if (profile.getAllowShowMyFriends() == 1 && profile.isFriend()) {

                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);

            }
        }
    }

    public void selectGift(long profileId) {

        if (!profile.isInBlackList()) {

            Intent intent = new Intent(getActivity(), SelectGiftActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
        }
    }

    public void like(final long profileId) {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_LIKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("likesCount")) {

                                    profile.setLikesCount(response.getInt("likesCount"));

                                    updateLikesCount();
                                }

                                if (response.has("myLike")) {

                                    profile.setMyLike(response.getBoolean("myLike"));
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            ((ProfileActivity)getActivity()).mFabButton.hide();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
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

    static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        public interface OnItemClickListener {

            void onItemClick(View view, int position);

            void onItemLongClick(View view, int position);
        }

        private FriendsFragment.RecyclerItemClickListener.OnItemClickListener mListener;

        private GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, FriendsFragment.RecyclerItemClickListener.OnItemClickListener listener) {

            mListener = listener;

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (childView != null && mListener != null) {

                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {

            View childView = view.findChildViewUnder(e.getX(), e.getY());

            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {

                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public void getAccountSettings() {

        Intent i = new Intent(getActivity(), AccountSettingsActivity.class);
        i.putExtra("profileId", App.getInstance().getId());
        i.putExtra("sex", profile.getSex());
        i.putExtra("year", profile.getYear());
        i.putExtra("month", profile.getMonth());
        i.putExtra("day", profile.getDay());

        i.putExtra("sexOrientation", profile.getSexOrientation());
        i.putExtra("age", profile.getAge());
        i.putExtra("height", profile.getHeight());
        i.putExtra("weight", profile.getWeight());

        i.putExtra("relationshipStatus", profile.getRelationshipStatus());
        i.putExtra("politicalViews", profile.getPoliticalViews());
        i.putExtra("worldView", profile.getWorldView());
        i.putExtra("personalPriority", profile.getPersonalPriority());
        i.putExtra("importantInOthers", profile.getImportantInOthers());
        i.putExtra("viewsOnSmoking", profile.getViewsOnSmoking());
        i.putExtra("viewsOnAlcohol", profile.getViewsOnAlcohol());
        i.putExtra("youLooking", profile.getYouLooking());
        i.putExtra("youLike", profile.getYouLike());

        i.putExtra("allowShowMyBirthday", profile.getAllowShowMyBirthday());

        i.putExtra("fullname", profile.getFullname());
        i.putExtra("location", profile.getLocation());
        i.putExtra("facebookPage", profile.getFacebookPage());
        i.putExtra("instagramPage", profile.getInstagramPage());
        i.putExtra("bio", profile.getBio());
        startActivityForResult(i, PROFILE_EDIT);
    }
}