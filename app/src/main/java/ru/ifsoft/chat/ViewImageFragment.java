package ru.ifsoft.chat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconTextView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import ru.ifsoft.chat.adapter.CommentListAdapter;
import ru.ifsoft.chat.adapter.CommentsListAdapter;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.dialogs.CommentActionDialog;
import ru.ifsoft.chat.dialogs.CommentDeleteDialog;
import ru.ifsoft.chat.dialogs.MixedCommentActionDialog;
import ru.ifsoft.chat.dialogs.MyCommentActionDialog;
import ru.ifsoft.chat.dialogs.MyPhotoActionDialog;
import ru.ifsoft.chat.dialogs.PhotoActionDialog;
import ru.ifsoft.chat.dialogs.PhotoDeleteDialog;
import ru.ifsoft.chat.dialogs.PhotoReportDialog;
import ru.ifsoft.chat.model.Comment;
import ru.ifsoft.chat.model.Image;
import ru.ifsoft.chat.util.Api;
import ru.ifsoft.chat.util.CommentInterface;
import ru.ifsoft.chat.util.CustomRequest;
import ru.ifsoft.chat.view.ResizableImageView;


public class ViewImageFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener, CommentInterface {

    private ProgressDialog pDialog;

    private MaterialRippleLayout mLikeButton;
    private ImageView mLikeIcon;

    private LinearLayout mStatusContainer;
    private ImageView mStatusIcon;
    private TextView mStatusTitle;

    private AdView mAdView;
    private Toolbar mToolbar;

    private RelativeLayout mBannerContainer;
    private LinearLayout mCommentsContainer, mLikesContainer;

    SwipeRefreshLayout mContentContainer;
    RelativeLayout mErrorScreen, mLoadingScreen, mEmptyScreen;
    LinearLayout mCommentFormContainer;
    CoordinatorLayout mContentScreen;

    EmojiconEditText mCommentText;

    private RecyclerView mRecyclerView;
    private NestedScrollView mNestedView;

    Button mRetryBtn;

    private LinearLayout mEmojiButton, mSendComment;
    ImageView mEmojiButtonIcon;

    TextView mFullnameTitle, mUsernameTitle, mModeTitle, mItemTimeAgo, mItemLikesCount, mItemCommentsCount;
    ResizableImageView mItemImg;
    CircularImageView mPhotoImage, mVerifiedIcon, mOnlineIcon;

    EmojiconTextView mItemText;

    ImageView mItemPlay;

    ImageLoader imageLoader = App.getInstance().getImageLoader();


    private ArrayList<Comment> itemsList;
    private CommentsListAdapter itemsAdapter;

    Image item = new Image();

    long itemId = 0, replyToUserId = 0;
    int arrayLength = 0;
    String commentText;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;

    EmojiconsPopup popup;

    private Boolean loadingComplete = false;

    public ViewImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        itemId = i.getLongExtra("itemId", 0);

        itemsList = new ArrayList<Comment>();
        itemsAdapter = new CommentsListAdapter(getActivity(), itemsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_image, container, false);

        popup = new EmojiconsPopup(rootView, getActivity());

        popup.setSizeForSoftKeyboard();

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mCommentText.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mCommentText.dispatchKeyEvent(event);
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if (popup.isShowing())

                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mCommentText.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mCommentText.dispatchKeyEvent(event);
            }
        });

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            replyToUserId = savedInstanceState.getLong("replyToUserId");

        } else {

            restore = false;
            loading = false;
            preload = false;

            replyToUserId = 0;
        }

        if (loading) {

            showpDialog();
        }

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        mAdView = (AdView) rootView.findViewById(R.id.adView);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mNestedView = (NestedScrollView) rootView.findViewById(R.id.nested_view);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);

        mRecyclerView.setLayoutManager(mLayoutManager);

        itemsAdapter.setOnMoreButtonClickListener(new CommentsListAdapter.OnItemMenuButtonClickListener() {

            @Override
            public void onItemClick(View v, Comment obj, int actionId, int position) {

                switch (actionId){

                    case R.id.action_remove: {

                        android.app.FragmentManager fm = getActivity().getFragmentManager();

                        CommentDeleteDialog alert = new CommentDeleteDialog();

                        Bundle b = new Bundle();
                        b.putInt("position", position);
                        b.putLong("itemId", obj.getId());

                        alert.setArguments(b);
                        alert.show(fm, "alert_dialog_comment_delete");

                        break;
                    }

                    case R.id.action_reply: {

                        if (App.getInstance().getId() != 0) {

                            replyToUserId = obj.getFromUserId();

                            mCommentText.setText("@" + obj.getFromUserUsername() + ", ");
                            mCommentText.setSelection(mCommentText.getText().length());

                            mCommentText.requestFocus();

                        }

                        break;
                    }
                }
            }
        });

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.setNestedScrollingEnabled(false);

        mStatusContainer = (LinearLayout) rootView.findViewById(R.id.status_container);
        mStatusIcon = (ImageView) rootView.findViewById(R.id.status_icon);
        mStatusTitle = (TextView) rootView.findViewById(R.id.status_label);

        mLikeButton = (MaterialRippleLayout) rootView.findViewById(R.id.like_button);
        mLikeIcon = (ImageView) rootView.findViewById(R.id.like_icon);

        mLikeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (item.isMyLike()) {

                    mLikeIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_40), android.graphics.PorterDuff.Mode.SRC_IN);

                    item.setMyLike(false);

                    item.setLikesCount(item.getLikesCount() - 1);

                } else {

                    mLikeIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.fab_like), android.graphics.PorterDuff.Mode.SRC_IN);

                    item.setMyLike(true);

                    item.setLikesCount(item.getLikesCount() + 1);
                }

                like();
            }
        });

        mLikeButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    animateIcon(mLikeIcon);
                }

                return false;
            }
        });

        mEmptyScreen = (RelativeLayout) rootView.findViewById(R.id.emptyScreen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.errorScreen);
        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loadingScreen);
        mContentContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_view);
        mContentContainer.setOnRefreshListener(this);

        mContentScreen = (CoordinatorLayout) rootView.findViewById(R.id.content_screen);
        mCommentFormContainer = (LinearLayout) rootView.findViewById(R.id.commentFormContainer);

        mBannerContainer = (RelativeLayout) rootView.findViewById(R.id.banner_container);
        mCommentsContainer = (LinearLayout) rootView.findViewById(R.id.comments_container);
        mLikesContainer = (LinearLayout) rootView.findViewById(R.id.likes_container);

        mCommentText = (EmojiconEditText) rootView.findViewById(R.id.commentText);
        mSendComment = (LinearLayout) rootView.findViewById(R.id.sendButton);
        mEmojiButton = (LinearLayout) rootView.findViewById(R.id.emojiButton);
        mEmojiButtonIcon = (ImageView) rootView.findViewById(R.id.emojiButtonIcon);

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                send();
            }
        });

        mRetryBtn = (Button) rootView.findViewById(R.id.retryBtn);

        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().isConnected()) {

                    showLoadingScreen();

                    getItem();
                }
            }
        });



        mFullnameTitle = (TextView) rootView.findViewById(R.id.fullname_label);
        mUsernameTitle = (TextView) rootView.findViewById(R.id.username_label);

        mPhotoImage = (CircularImageView) rootView.findViewById(R.id.photo_image);
        mVerifiedIcon = (CircularImageView) rootView.findViewById(R.id.verified_icon);
        mOnlineIcon = (CircularImageView) rootView.findViewById(R.id.online_icon);

        mModeTitle = (TextView) rootView.findViewById(R.id.mode_label);

        mItemText = (EmojiconTextView) rootView.findViewById(R.id.itemText);
        mItemTimeAgo = (TextView) rootView.findViewById(R.id.date_label);
        mItemLikesCount = (TextView) rootView.findViewById(R.id.likes_count_label);
        mItemCommentsCount = (TextView) rootView.findViewById(R.id.comments_count_label);

        mItemImg = (ResizableImageView) rootView.findViewById(R.id.itemImage);
        mItemPlay = (ImageView) rootView.findViewById(R.id.itemPlay);

        if (!EMOJI_KEYBOARD) {

            mEmojiButton.setVisibility(View.GONE);
        }

        mEmojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!popup.isShowing()) {

                    if (popup.isKeyBoardOpen()){

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        mCommentText.setFocusableInTouchMode(true);
                        mCommentText.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mCommentText, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

                    popup.dismiss();
                }
            }
        });

        EditTextImeBackListener er = new EditTextImeBackListener() {

            @Override
            public void onImeBack(EmojiconEditText ctrl, String text) {

                hideEmojiKeyboard();
            }
        };

        mCommentText.setOnEditTextImeBackListener(er);

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getItem();

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    loadingComplete();
                    updateItem();

                } else {

                    showLoadingScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void hideEmojiKeyboard() {

        popup.dismiss();
    }

    public void setIconEmojiKeyboard() {

        mEmojiButtonIcon.setBackgroundResource(R.drawable.ic_emoji);
    }

    public void setIconSoftKeyboard() {

        mEmojiButtonIcon.setBackgroundResource(R.drawable.ic_keyboard);
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
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
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putLong("replyToUserId", replyToUserId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            mContentContainer.setRefreshing(true);
            getItem();

        } else {

            mContentContainer.setRefreshing(false);
        }
    }

    public String getItemModeText(int postMode) {

        switch (postMode) {

            case 0: {

                return getString(R.string.label_image_for_public);
            }

            default: {

                return getString(R.string.label_image_for_friends);
            }
        }
    }

    private void showAdBanner() {

        mBannerContainer.setVisibility(View.GONE);

        if (App.getInstance().getAdmob() == ADMOB_ENABLED) {

            AdRequest adRequest = new AdRequest.Builder().build();

            mAdView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {

                    super.onAdLoaded();

                    mBannerContainer.setVisibility(View.VISIBLE);

                    Log.e("ADMOB", "onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(int i) {

                    super.onAdFailedToLoad(i);

                    mBannerContainer.setVisibility(View.GONE);

                    Log.e("ADMOB", "onAdFailedToLoad");
                }
            });

            mAdView.loadAd(adRequest);

        } else {

            Log.e("ADMOB", "ADMOB_DISABLED");

            mBannerContainer.setVisibility(View.GONE);
        }
    }

    public void updateItem() {

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        showAdBanner();
        updateCounters();
        updateStatus();

        if (item.getItemType() == Constants.GALLERY_ITEM_TYPE_VIDEO) {

            mItemPlay.setVisibility(View.VISIBLE);

        } else {

            mItemPlay.setVisibility(View.GONE);
        }

        mVerifiedIcon.setVisibility(View.GONE);
        mOnlineIcon.setVisibility(View.GONE);

        mFullnameTitle.setText(item.getFromUserFullname());
        mUsernameTitle.setText("@" + item.getFromUserUsername());

        mModeTitle.setText(getItemModeText(item.getAccessMode()));

        if (item.getFromUserVerify() == 1) {

            mVerifiedIcon.setVisibility(View.VISIBLE);
        }

        if (item.getFromUserPhotoUrl().length() != 0) {

            mPhotoImage.setVisibility(View.VISIBLE);

            imageLoader.get(item.getFromUserPhotoUrl(), ImageLoader.getImageListener(mPhotoImage, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

        } else {

            mPhotoImage.setVisibility(View.VISIBLE);
            mPhotoImage.setImageResource(R.drawable.profile_default_photo);
        }

        mPhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", item.getFromUserId());
                startActivity(intent);
            }
        });

        mFullnameTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", item.getFromUserId());
                startActivity(intent);
            }
        });

        mLikesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), LikersActivity.class);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }
        });

        if (item.isMyLike()) {

            mLikeIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.fab_like), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {

            mLikeIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_40), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        mItemTimeAgo.setText(item.getTimeAgo());
        mItemTimeAgo.setVisibility(View.VISIBLE);

        if (item.getComment().length() > 0) {

            mItemText.setText(item.getComment().replaceAll("<br>", "\n"));

            mItemText.setVisibility(View.VISIBLE);

        } else {

            mItemText.setVisibility(View.GONE);
        }

        if (item.getImgUrl().length() > 0) {

            imageLoader.get(item.getImgUrl(), ImageLoader.getImageListener(mItemImg, R.drawable.img_loading, R.drawable.img_loading));
            mItemImg.setVisibility(View.VISIBLE);

            mItemImg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (item.getItemType() == Constants.GALLERY_ITEM_TYPE_VIDEO) {

                        Intent i = new Intent(getActivity(), VideoViewActivity.class);
                        i.putExtra("videoUrl", item.getVideoUrl());
                        startActivity(i);

                    } else {

                        Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                        i.putExtra("imgUrl", item.getImgUrl());
                        startActivity(i);
                    }
                }
            });

        } else {

            mItemImg.setVisibility(View.GONE);
        }

        mItemPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), VideoViewActivity.class);
                i.putExtra("videoUrl", item.getVideoUrl());
                startActivity(i);
            }
        });
    }

    private void updateCounters() {

        mItemLikesCount.setText(Integer.toString(item.getLikesCount()));

        if (item.getLikesCount() > 0) {

            mLikesContainer.setVisibility(View.VISIBLE);
            mItemLikesCount.setVisibility(View.VISIBLE);

        } else {

            mLikesContainer.setVisibility(View.GONE);
            mItemLikesCount.setVisibility(View.GONE);
        }

        mItemCommentsCount.setText(Integer.toString(item.getCommentsCount()));

        if (item.getCommentsCount() > 0) {

            mCommentsContainer.setVisibility(View.VISIBLE);
            mItemCommentsCount.setVisibility(View.VISIBLE);

        } else {

            mCommentsContainer.setVisibility(View.GONE);
            mItemCommentsCount.setVisibility(View.GONE);
        }
    }

    private void updateStatus() {

        mStatusContainer.setVisibility(View.GONE);

        if (item.getFromUserId() == App.getInstance().getId()) {

            if (item.getRemoveAt() != 0) {

                mStatusIcon.setImageResource(R.drawable.ic_rejected);
                mStatusTitle.setText(getString(R.string.msg_gallery_item_status_rejected));

            } else {

                if (item.getModerateAt() == 0) {

                    mStatusIcon.setImageResource(R.drawable.ic_wait);
                    mStatusTitle.setText(getString(R.string.msg_gallery_item_status_wait));

                } else {

                    mStatusIcon.setImageResource(R.drawable.ic_accept);
                    mStatusTitle.setText(getString(R.string.msg_gallery_item_status_approved));
                }
            }

            mStatusContainer.setVisibility(View.VISIBLE);
        }

        mItemLikesCount.setText(Integer.toString(item.getLikesCount()));

        if (item.getLikesCount() > 0) {

            mLikesContainer.setVisibility(View.VISIBLE);
            mItemLikesCount.setVisibility(View.VISIBLE);

        } else {

            mLikesContainer.setVisibility(View.GONE);
            mItemLikesCount.setVisibility(View.GONE);
        }

        mItemCommentsCount.setText(Integer.toString(item.getCommentsCount()));

        if (item.getCommentsCount() > 0) {

            mCommentsContainer.setVisibility(View.VISIBLE);
            mItemCommentsCount.setVisibility(View.VISIBLE);

        } else {

            mCommentsContainer.setVisibility(View.GONE);
            mItemCommentsCount.setVisibility(View.GONE);
        }
    }

    public void getItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_IMAGE_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                            return;
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemsList.clear();

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            item = new Image(itemObj);

                                            updateItem();
                                        }
                                    }
                                }

                                if (response.has("comments") && item.getFromUserAllowPhotosComments() == 1) {

                                    JSONObject commentsObj = response.getJSONObject("comments");

                                    if (commentsObj.has("comments")) {

                                        JSONArray commentsArray = commentsObj.getJSONArray("comments");

                                        arrayLength = commentsArray.length();

                                        if (arrayLength > 0) {

                                            for (int i = commentsArray.length() - 1; i > -1 ; i--) {

                                                JSONObject itemObj = (JSONObject) commentsArray.get(i);

                                                Comment comment = new Comment(itemObj);

                                                itemsList.add(comment);
                                            }
                                        }
                                    }
                                }

                                loadingComplete();

                            } else {

                                showErrorScreen();
                            }

                        } catch (JSONException e) {

                            showErrorScreen();

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                    return;
                }

                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void send() {

        commentText = mCommentText.getText().toString();
        commentText = commentText.trim();

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0 && commentText.length() > 0) {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_IMAGE_COMMENTS_NEW, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                                return;
                            }

                            try {

                                if (!response.getBoolean("error")) {

                                    if (response.has("comment")) {

                                        JSONObject commentObj = (JSONObject) response.getJSONObject("comment");

                                        Comment comment = new Comment(commentObj);

                                        itemsList.add(comment);

                                        itemsAdapter.notifyDataSetChanged();

                                        mCommentText.setText("");
                                        replyToUserId = 0;

                                        mNestedView.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                // Select the last row so it will scroll into view...
                                                mNestedView.fullScroll(View.FOCUS_DOWN);

                                                item.setCommentsCount(item.getCommentsCount() + 1);

                                                updateCounters();
                                            }
                                        });
                                    }

                                    Toast.makeText(getActivity(), getString(R.string.msg_comment_has_been_added), Toast.LENGTH_SHORT).show();

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

                        Log.e("ERROR", "ViewImageFragment Not Added to Activity");

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

                    params.put("itemId", Long.toString(item.getId()));
                    params.put("commentText", commentText);

                    params.put("replyToUserId", Long.toString(replyToUserId));

                    return params;
                }
            };

            int socketTimeout = 0;//0 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void onPhotoDelete(final int position) {

        Api api = new Api(getActivity());

        api.photoDelete(item.getId());

        getActivity().finish();
    }

    public void onPhotoReport(int position, int reasonId) {

        if (App.getInstance().isConnected()) {

            Api api = new Api(getActivity());

            api.photoReport(item.getId(), reasonId);

        } else {

            Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void remove(int position) {

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        PhotoDeleteDialog alert = new PhotoDeleteDialog();

        Bundle b = new Bundle();
        b.putInt("position", 0);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_photo_delete");
    }

    public void report(int position) {

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        PhotoReportDialog alert = new PhotoReportDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);
        b.putInt("reason", 0);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_photo_report");
    }

    public void action(int position) {

        if (item.getFromUserId() == App.getInstance().getId()) {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            MyPhotoActionDialog alert = new MyPhotoActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_my_post_action");

        } else {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            PhotoActionDialog alert = new PhotoActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_post_action");
        }
    }

    public void loadingComplete() {

        itemsAdapter.notifyDataSetChanged();

        showContentScreen();

        if (mContentContainer.isRefreshing()) {

            mContentContainer.setRefreshing(false);
        }
    }

    public void showLoadingScreen() {

        preload = true;

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showEmptyScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mEmptyScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        preload = false;

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mEmptyScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        if (item.getFromUserAllowPhotosComments() == COMMENTS_DISABLED) {

            mCommentFormContainer.setVisibility(View.GONE);
        }

        loadingComplete = true;

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void commentAction(int position) {

        final Comment comment = itemsList.get(position);

        if (comment.getFromUserId() == App.getInstance().getId() && item.getFromUserId() == App.getInstance().getId()) {

            android.app.FragmentManager fm = getActivity().getFragmentManager();

            MyCommentActionDialog alert = new MyCommentActionDialog();

            Bundle b  = new Bundle();

            b.putInt("position", position);

            alert.setArguments(b);

            alert.show(fm, "alert_dialog_my_comment_action");

        } else if (comment.getFromUserId() != App.getInstance().getId() && item.getFromUserId() == App.getInstance().getId()) {

            android.app.FragmentManager fm = getActivity().getFragmentManager();

            MixedCommentActionDialog alert = new MixedCommentActionDialog();

            Bundle b = new Bundle();

            b.putInt("position", position);

            alert.setArguments(b);

            alert.show(fm, "alert_dialog_mixed_comment_action");

        } else if (comment.getFromUserId() == App.getInstance().getId() && item.getFromUserId() != App.getInstance().getId()) {

            android.app.FragmentManager fm = getActivity().getFragmentManager();

            MyCommentActionDialog alert = new MyCommentActionDialog();

            Bundle b  = new Bundle();

            b.putInt("position", position);

            alert.setArguments(b);

            alert.show(fm, "alert_dialog_my_comment_action");

        } else {

            /** Getting the fragment manager */
            android.app.FragmentManager fm = getActivity().getFragmentManager();

            /** Instantiating the DialogFragment class */
            CommentActionDialog alert = new CommentActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_comment_action");
        }
    }

    public void onCommentReply(final int position) {

        if (item.getFromUserAllowPhotosComments() == COMMENTS_ENABLED) {

            final Comment comment = itemsList.get(position);

            replyToUserId = comment.getFromUserId();

            mCommentText.setText("@" + comment.getFromUserUsername() + ", ");
            mCommentText.setSelection(mCommentText.getText().length());

            mCommentText.requestFocus();

        } else {

            Toast.makeText(getActivity(), getString(R.string.msg_comments_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    public void onCommentRemove(int position) {

        /** Getting the fragment manager */
        android.app.FragmentManager fm = getActivity().getFragmentManager();

        /** Instantiating the DialogFragment class */
        CommentDeleteDialog alert = new CommentDeleteDialog();

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("position", position);

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

        alert.show(fm, "alert_dialog_comment_delete");
    }

    public void onCommentDelete(final int position) {

        final Comment comment = itemsList.get(position);

        itemsList.remove(position);
        itemsAdapter.notifyDataSetChanged();

        Api api = new Api(getActivity());

        api.imagesCommentDelete(comment.getId());

        item.setCommentsCount(item.getCommentsCount() - 1);

        updateCounters();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_view_item, menu);

//        MainMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (App.getInstance().getId() != item.getFromUserId()) {

                menu.removeItem(R.id.action_delete);

            } else {

                menu.removeItem(R.id.action_report);
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

            case R.id.action_delete: {

                remove(0);

                return true;
            }

            case R.id.action_report: {

                report(0);

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

    public void like() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_IMAGE_LIKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                item.setLikesCount(response.getInt("likesCount"));
                                item.setMyLike(response.getBoolean("myLike"));
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            updateCounters();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                    return;
                }

                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(item.getId()));

                return params;
            }
        };

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
}