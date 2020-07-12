package ru.ifsoft.chat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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
import java.util.concurrent.TimeUnit;

import ru.ifsoft.chat.adapter.NotificationsListAdapter;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.dialogs.FriendRequestActionDialog;
import ru.ifsoft.chat.model.Notify;
import ru.ifsoft.chat.util.Api;
import ru.ifsoft.chat.util.CustomRequest;
import ru.ifsoft.chat.view.LineItemDecoration;

public class NotificationsFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    private ProgressDialog pDialog;

    RecyclerView mRecyclerView;
    NestedScrollView mNestedView;

    TextView mMessage;
    ImageView mSplash;

    SwipeRefreshLayout mItemsContainer;

    private ArrayList<Notify> itemsList;
    private NotificationsListAdapter itemsAdapter;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;

    private Boolean loadingComplete = false;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        initpDialog();

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new NotificationsListAdapter(getActivity(), itemsList);

            restore = savedInstanceState.getBoolean("restore");
            itemId = savedInstanceState.getInt("itemId");
            loadingComplete = savedInstanceState.getBoolean("loadingComplete");

        } else {

            itemsList = new ArrayList<Notify>();
            itemsAdapter = new NotificationsListAdapter(getActivity(), itemsList);

            restore = false;
            itemId = 0;
            loadingComplete = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        getActivity().setTitle(R.string.nav_notifications);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        mMessage = (TextView) rootView.findViewById(R.id.message);
        mSplash = (ImageView) rootView.findViewById(R.id.splash);

        mNestedView = (NestedScrollView) rootView.findViewById(R.id.nested_view);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(getActivity(), LinearLayout.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new NotificationsListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Notify item, int position) {

                switch (item.getType()) {

                    case NOTIFY_TYPE_FOLLOWER: {

                        /** Getting the fragment manager */
                        android.app.FragmentManager fm = getActivity().getFragmentManager();

                        /** Instantiating the DialogFragment class */
                        FriendRequestActionDialog alert = new FriendRequestActionDialog();

                        /** Creating a bundle object to store the selected item's index */
                        Bundle b  = new Bundle();

                        /** Storing the selected item's index in the bundle object */
                        b.putInt("position", position);

                        /** Setting the bundle object to the dialog fragment object */
                        alert.setArguments(b);

                        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

                        alert.show(fm, "alert_friend_request_action");

                        break;
                    }

                    case NOTIFY_TYPE_LIKE: {

                        Intent intent = new Intent(getActivity(), LikesActivity.class);
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_GIFT: {

                        Intent intent = new Intent(getActivity(), GiftsActivity.class);
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_IMAGE_COMMENT: {

                        Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                        intent.putExtra("itemId", item.getItemId());
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_IMAGE_COMMENT_REPLY: {

                        Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                        intent.putExtra("itemId", item.getItemId());
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_IMAGE_LIKE: {

                        Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                        intent.putExtra("itemId", item.getItemId());
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_MEDIA_APPROVE: {

                        Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                        intent.putExtra("itemId", item.getItemId());
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_MEDIA_REJECT: {

                        Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                        intent.putExtra("itemId", item.getItemId());
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_ACCOUNT_APPROVE: {

                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        startActivity(intent);

                        break;
                    }

                    case NOTIFY_TYPE_ACCOUNT_REJECT: {

                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        startActivity(intent);

                        break;
                    }

                    default: {

                        break;
                    }
                }
            }
        });

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

                        mItemsContainer.setRefreshing(true);

                        loadingMore = true;

                        getItems();
                    }
                }
            }
        });

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        if (!restore) {

            showMessage(getText(R.string.msg_loading_2).toString());

            getItems();
        }


        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            itemId = 0;
            getItems();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loadingComplete", true);
        outState.putBoolean("restore", true);
        outState.putInt("itemId", itemId);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void getItems() {

        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_NOTIFICATIONS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "NotificationsFragment Not Added to Activity");

                            return;
                        }

                        try {

                            arrayLength = 0;

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            if (!response.getBoolean("error")) {

                                App.getInstance().setNotificationsCount(0);

                                itemId = response.getInt("notifyId");

                                JSONArray notificationsArray = response.getJSONArray("notifications");

                                arrayLength = notificationsArray.length();

                                if (arrayLength > 0) {

                                    for (int i = 0; i < notificationsArray.length(); i++) {

                                        JSONObject notifyObj = (JSONObject) notificationsArray.get(i);

                                        Notify notify = new Notify(notifyObj);

                                        itemsList.add(notify);
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

                    Log.e("ERROR", "NotificationsFragment Not Added to Activity");

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
                params.put("notifyId", Integer.toString(itemId));

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

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        loadingMore = false;
        loadingComplete = true;
        mItemsContainer.setRefreshing(false);

        getActivity().invalidateOptionsMenu();
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
    }

    public void clear() {

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_NOTIFICATIONS_CLEAR, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "NotificationsFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                itemsList.clear();

                                App.getInstance().setNotificationsCount(0);

                                itemId = 0;
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            hidepDialog();

                            loadingComplete();

                            Log.d("Clear.response", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "NotificationsFragment Not Added to Activity");

                    return;
                }

                hidepDialog();

                loadingComplete();

                Log.e("Clear.error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_notifications, menu);
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (itemsAdapter.getItemCount() == 0) {

                //hide menu
                hideMenuItems(menu, false);

            } else {

                //show menu
                hideMenuItems(menu, true);
            }

        } else {

            //hide all menu items
            hideMenuItems(menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_remove_all: {

                // remove all notifications

                clear();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
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

    public void onAcceptRequest(final int position) {

        final Notify item = itemsList.get(position);

        itemsList.remove(position);
        itemsAdapter.notifyDataSetChanged();

        if (mRecyclerView.getAdapter().getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        if (App.getInstance().isConnected()) {

            Api api = new Api(getActivity());

            api.acceptFriendRequest(item.getFromUserId());

        } else {

            Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void onRejectRequest(final int position) {

        final Notify item = itemsList.get(position);

        itemsList.remove(position);
        itemsAdapter.notifyDataSetChanged();

        if (mRecyclerView.getAdapter().getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        if (App.getInstance().isConnected()) {

            Api api = new Api(getActivity());

            api.rejectFriendRequest(item.getFromUserId());

        } else {

            Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
        }
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