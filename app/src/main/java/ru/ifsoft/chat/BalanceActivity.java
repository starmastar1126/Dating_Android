package ru.ifsoft.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.common.ActivityBase;
import ru.ifsoft.chat.util.CustomRequest;
import ru.ifsoft.chat.util.IabHelper;
import ru.ifsoft.chat.util.IabResult;
import ru.ifsoft.chat.util.Inventory;
import ru.ifsoft.chat.util.Purchase;


public class BalanceActivity extends ActivityBase implements RewardedVideoAdListener {

    Toolbar mToolbar;

    // For Google Play

    private static final String TAG = "ifsoft.inappbilling";            // Here you can write anything you like! For example: obama.white.house.billing
    private static final String TOKEN = "ifsoft.inappbilling";          // Here you can write anything you like! For example: obama.white.house.billing

    // See documentation!

    static final String ITEM_SKU_1 = "chat.ifsoft.ru.iap11";           // Change to: yourdomain.com.iap1
    static final String ITEM_SKU_2 = "chat.ifsoft.ru.iap2";            // Change to: yourdomain.com.iap2
    static final String ITEM_SKU_3 = "chat.ifsoft.ru.iap3";            // Change to: yourdomain.com.iap3
    static final String ITEM_SKU_4 = "android.test.purchased";         // Not used. For testing

    Button mBuy1Button, mBuy2Button, mBuy3Button, mBuy4Button;
    TextView mLabelCredits;

    Button mRewardedAdButton;

    private RewardedVideoAd mRewardedVideoAd;

    private Boolean loading = false;

    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

//        MpUtils.enablePaymentBroadcast(this, Manifest.permission.PAYMENT_BROADCAST_PERMISSION);

        if (savedInstanceState != null) {

            loading = savedInstanceState.getBoolean("loading");

        } else {

            loading = false;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (loading) {

            showpDialog();
        }

        mLabelCredits = (TextView) findViewById(R.id.labelCredits);

        mBuy1Button = (Button) findViewById(R.id.iap1_google_btn);
        mBuy2Button = (Button) findViewById(R.id.iap2_google_btn);
        mBuy3Button = (Button) findViewById(R.id.iap3_google_btn);
        mBuy4Button = (Button) findViewById(R.id.iap4_google_btn);      // For test Google Pay Button

        mRewardedAdButton = (Button) findViewById(R.id.rewarded_ad_btn);

        if (!GOOGLE_PAY_TEST_BUTTON) {

            mBuy4Button.setVisibility(View.GONE);
        }

        mBuy1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHelper.launchPurchaseFlow(BalanceActivity.this, ITEM_SKU_1, 10001, mPurchaseFinishedListener, TOKEN);
            }
        });

        mBuy2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHelper.launchPurchaseFlow(BalanceActivity.this, ITEM_SKU_2, 10001, mPurchaseFinishedListener, TOKEN);
            }
        });

        mBuy3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHelper.launchPurchaseFlow(BalanceActivity.this, ITEM_SKU_3, 10001, mPurchaseFinishedListener, TOKEN);
            }
        });

        mBuy4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHelper.launchPurchaseFlow(BalanceActivity.this, ITEM_SKU_4, 10001, mPurchaseFinishedListener, TOKEN);
            }
        });

        mRewardedAdButton.setVisibility(View.GONE);

        if (App.getInstance().getAllowRewardedAds() == 1) {

            mRewardedAdButton.setVisibility(View.GONE);

            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
            mRewardedVideoAd.setRewardedVideoAdListener(this);

            loadRewardedVideoAd();
        }

        mRewardedAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRewardedVideoAd.isLoaded()) {

                    mRewardedVideoAd.show();
                }
            }
        });

        update();
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) {

                // Handle error

                return;

            } else if (purchase.getSku().equals(ITEM_SKU_1)) {

                consumeItem_1();

                App.getInstance().setBalance(App.getInstance().getBalance() + 30);
                payment(30, true);

            } else if (purchase.getSku().equals(ITEM_SKU_2)) {

                consumeItem_2();

                App.getInstance().setBalance(App.getInstance().getBalance() + 70);
                payment(70, true);

            } else if (purchase.getSku().equals(ITEM_SKU_3)) {

                consumeItem_3();

                App.getInstance().setBalance(App.getInstance().getBalance() + 120);
                payment(120, true);

            } else if (purchase.getSku().equals(ITEM_SKU_4)) {

                // Test method

                consumeItem_4();

                App.getInstance().setBalance(App.getInstance().getBalance() + 100);
                payment(100, true);
            }
        }
    };

    public void consumeItem_1() {

        mHelper.queryInventoryAsync(mReceivedInventoryListener_1);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener_1 = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {

                // Handle failure

            } else {

                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU_1), mConsumeFinishedListener);
            }
        }
    };

    public void consumeItem_2() {

        mHelper.queryInventoryAsync(mReceivedInventoryListener_2);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener_2 = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {

                // Handle failure
            } else {

                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU_2), mConsumeFinishedListener);
            }
        }
    };

    public void consumeItem_3() {

        mHelper.queryInventoryAsync(mReceivedInventoryListener_3);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener_3 = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {

                // Handle failure
            } else {

                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU_3), mConsumeFinishedListener);
            }
        }
    };

    public void consumeItem_4() {

        mHelper.queryInventoryAsync(mReceivedInventoryListener_4);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener_4 = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {

                // Handle failure
            } else {

                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU_4), mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {

        public void onConsumeFinished(Purchase purchase, IabResult result) {

            if (result.isSuccess()) {

                // clickButton.setEnabled(true);

            } else {

                // handle error
            }
        }
    };

    public void payment(final int cost, final Boolean showSuccess) {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_ADD_FUNDS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("balance")) {

                                    App.getInstance().setBalance(response.getInt("balance"));
                                }

                                if (showSuccess) {

                                    success();
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            update();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("funds", Integer.toString(cost));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    // Fortumo related glue-code
    private static final int REQUEST_CODE = 1234; // Can be anything


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE) {

            if (data == null) {

                return;
            }

        } else {

            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {

                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    public void update() {

        mLabelCredits.setText(getString(R.string.label_credits) + " (" + Integer.toString(App.getInstance().getBalance()) + ")");
    }

    public void success() {

        Toast.makeText(BalanceActivity.this, getString(R.string.msg_success_purchase), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    @Override
    protected void onStart() {

        super.onStart();


        String base64EncodedPublicKey = BILLING_KEY;

        mHelper = new IabHelper(BalanceActivity.this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {

                    Log.d(TAG, "In-app Billing setup failed: " + result);

                } else {

                    Log.d(TAG, "In-app Billing is set up OK");

                    mHelper.enableDebugLogging(true, TAG);
                }
            }
        });
    }

    @Override
    protected void onPause() {

        if (mRewardedVideoAd != null) {

            mRewardedVideoAd.pause(this);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (mRewardedVideoAd != null) {

            mRewardedVideoAd.destroy(this);
        }

        super.onDestroy();

        hidepDialog();

        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override protected void onResume() {

        if (mRewardedVideoAd != null) {

            mRewardedVideoAd.resume(this);
        }

        super.onResume();

        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_deactivate, menu);
        return true;
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




    private void loadRewardedVideoAd() {

        mRewardedAdButton.setText(getString(R.string.msg_loading));

        mRewardedVideoAd.loadAd(getString(R.string.rewarded_ad_unit_id), new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem reward) {

        // Reward the user.

        Log.d("Rewarded Video", "onRewarded");

        Toast.makeText(BalanceActivity.this, getString(R.string.msg_success_rewarded), Toast.LENGTH_SHORT).show();

        mRewardedAdButton.setVisibility(View.GONE);

        App.getInstance().setBalance(App.getInstance().getBalance() + reward.getAmount());
        payment(reward.getAmount(), false);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

        Log.d("Rewarded Video", "onRewardedVideoAdLeftApplication");

        // user click by link in video ad
    }

    @Override
    public void onRewardedVideoAdClosed() {

        Log.d("Rewarded Video", "onRewardedVideoAdClosed");

        mRewardedAdButton.setVisibility(View.GONE);

        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {

        Log.d("Rewarded Video", "onRewardedVideoAdFailedToLoad");

        mRewardedAdButton.setVisibility(View.GONE);

        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLoaded() {

        Log.d("Rewarded Video", "onRewardedVideoAdLoaded");

        mRewardedAdButton.setText(getString(R.string.action_view_rewarded_video_ad));
        mRewardedAdButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRewardedVideoAdOpened() {

        Log.d("Rewarded Video", "onRewardedVideoAdOpened");
    }

    @Override
    public void onRewardedVideoStarted() {

        Log.d("Rewarded Video", "onRewardedVideoStarted");
    }

    @Override
    public void onRewardedVideoCompleted() {

        Log.d("Rewarded Video", "onRewardedVideoCompleted");
    }
}