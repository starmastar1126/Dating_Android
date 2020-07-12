package ru.ifsoft.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ru.ifsoft.chat.common.ActivityBase;
import ru.ifsoft.chat.dialogs.AlcoholViewsSelectDialog;
import ru.ifsoft.chat.dialogs.GenderSelectDialog;
import ru.ifsoft.chat.dialogs.ImportantInOthersSelectDialog;
import ru.ifsoft.chat.dialogs.PersonalPrioritySelectDialog;
import ru.ifsoft.chat.dialogs.PoliticalViewsSelectDialog;
import ru.ifsoft.chat.dialogs.RelationshipStatusSelectDialog;
import ru.ifsoft.chat.dialogs.SexOrientationSelectDialog;
import ru.ifsoft.chat.dialogs.SmokingViewsSelectDialog;
import ru.ifsoft.chat.dialogs.WorldViewSelectDialog;
import ru.ifsoft.chat.dialogs.YouLikeSelectDialog;
import ru.ifsoft.chat.dialogs.YouLookingSelectDialog;

public class AccountSettingsActivity extends ActivityBase implements GenderSelectDialog.AlertPositiveListener, RelationshipStatusSelectDialog.AlertPositiveListener, PoliticalViewsSelectDialog.AlertPositiveListener, WorldViewSelectDialog.AlertPositiveListener, PersonalPrioritySelectDialog.AlertPositiveListener, ImportantInOthersSelectDialog.AlertPositiveListener, SmokingViewsSelectDialog.AlertPositiveListener, AlcoholViewsSelectDialog.AlertPositiveListener, YouLookingSelectDialog.AlertPositiveListener, YouLikeSelectDialog.AlertPositiveListener, SexOrientationSelectDialog.AlertPositiveListener {

    Toolbar mToolbar;

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ru.ifsoft.chat.R.layout.activity_account_settings);

        mToolbar = (Toolbar) findViewById(ru.ifsoft.chat.R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {

            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

        } else {

            fragment = new AccountSettingsFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(ru.ifsoft.chat.R.id.container_body, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(ru.ifsoft.chat.R.menu.menu_account_settings, menu);
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

    @Override
    public void onSexOrientationSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getSexOrientation(position);
    }

    @Override
    public void onGenderSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getGender(position);
    }

    @Override
    public void onRelationshipStatusSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getRelationshipStatus(position);
    }

    @Override
    public void onPoliticalViewsSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getPoliticalViews(position);
    }

    @Override
    public void onWorldViewSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getWorldView(position);
    }

    @Override
    public void onPersonalPrioritySelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getPersonalPriority(position);
    }

    @Override
    public void onImportantInOthersSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getImportantInOthers(position);
    }

    @Override
    public void onSmokingViewsSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getSmokingViews(position);
    }

    @Override
    public void onAlcoholViewsSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getAlcoholViews(position);
    }

    @Override
    public void onYouLookingSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getYouLooking(position);
    }

    @Override
    public void onYouLikeSelect(int position) {

        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getYouLike(position);
    }
}
