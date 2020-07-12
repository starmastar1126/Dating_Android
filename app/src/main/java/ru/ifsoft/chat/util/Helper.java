package ru.ifsoft.chat.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ifsoft.chat.R;

public class Helper extends Application {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    private Activity activity;

    public Helper(Activity activity) {

        this.activity = activity;
    }

    public Helper() {

    }

    public static String getGenderTitle(Context ctx, int gender) {

        switch (gender) {

            case 0: {

                return ctx.getString(R.string.label_male);
            }

            case 1: {

                return ctx.getString(R.string.label_female);
            }

            case 2: {

                return ctx.getString(R.string.label_secret);
            }

            default: {

                return ctx.getString(R.string.label_select_gender);
            }
        }
    }

    public static String getSexOrientationTitle(Context ctx, int sex_orientation) {

        switch (sex_orientation) {

            case 1: {

                return ctx.getString(R.string.sex_orientation_1);
            }

            case 2: {

                return ctx.getString(R.string.sex_orientation_2);

            }

            case 3: {

                return ctx.getString(R.string.sex_orientation_3);
            }

            case 4: {

                return ctx.getString(R.string.sex_orientation_4);
            }

            default: {

                return ctx.getString(R.string.label_select_sex_orientation);
            }
        }
    }

    public static int getGalleryGridCount(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.gallery_item_size);

        return Math.round(screenWidth / cellWidth);
    }

    public static int dpToPx(Context c, int dp) {

        Resources r = c.getResources();

        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static int getGridSpanCount(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static int getStickersGridSpanCount(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.sticker_item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static String randomString(int len) {

        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++)

            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb.toString();
    }

    public boolean isValidEmail(String email) {

    	if (TextUtils.isEmpty(email)) {

    		return false;

    	} else {

    		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    	}
    }
    
    public boolean isValidLogin(String login) {

        String regExpn = "^([a-zA-Z]{4,24})?([a-zA-Z][a-zA-Z0-9_]{4,24})$";
        CharSequence inputStr = login;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {

            return true;

        } else {

            return false;
        }
    }

    public boolean isValidSearchQuery(String query) {

        String regExpn = "^([a-zA-Z]{1,24})?([a-zA-Z][a-zA-Z0-9_]{1,24})$";
        CharSequence inputStr = query;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {

            return true;

        } else {

            return false;
        }
    }
    
    public boolean isValidPassword(String password) {

        String regExpn = "^[a-z0-9_]{6,24}$";
        CharSequence inputStr = password;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {

            return true;

        } else {

            return false;
        }
    }

    public String getRelationshipStatus(int mRelationship) {

        switch (mRelationship) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.relationship_status_1);
            }

            case 2: {

                return activity.getString(R.string.relationship_status_2);
            }

            case 3: {

                return activity.getString(R.string.relationship_status_3);
            }

            case 4: {

                return activity.getString(R.string.relationship_status_4);
            }

            case 5: {

                return activity.getString(R.string.relationship_status_5);
            }

            case 6: {

                return activity.getString(R.string.relationship_status_6);
            }

            case 7: {

                return activity.getString(R.string.relationship_status_7);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getPoliticalViews(int mPolitical) {

        switch (mPolitical) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.political_views_1);
            }

            case 2: {

                return activity.getString(R.string.political_views_2);
            }

            case 3: {

                return activity.getString(R.string.political_views_3);
            }

            case 4: {

                return activity.getString(R.string.political_views_4);
            }

            case 5: {

                return activity.getString(R.string.political_views_5);
            }

            case 6: {

                return activity.getString(R.string.political_views_6);
            }

            case 7: {

                return activity.getString(R.string.political_views_7);
            }

            case 8: {

                return activity.getString(R.string.political_views_7);
            }

            case 9: {

                return activity.getString(R.string.political_views_9);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getWorldView(int mWorld) {

        switch (mWorld) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.world_view_1);
            }

            case 2: {

                return activity.getString(R.string.world_view_2);
            }

            case 3: {

                return activity.getString(R.string.world_view_3);
            }

            case 4: {

                return activity.getString(R.string.world_view_4);
            }

            case 5: {

                return activity.getString(R.string.world_view_5);
            }

            case 6: {

                return activity.getString(R.string.world_view_6);
            }

            case 7: {

                return activity.getString(R.string.world_view_7);
            }

            case 8: {

                return activity.getString(R.string.world_view_8);
            }

            case 9: {

                return activity.getString(R.string.world_view_9);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getPersonalPriority(int mPriority) {

        switch (mPriority) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.personal_priority_1);
            }

            case 2: {

                return activity.getString(R.string.personal_priority_2);
            }

            case 3: {

                return activity.getString(R.string.personal_priority_3);
            }

            case 4: {

                return activity.getString(R.string.personal_priority_4);
            }

            case 5: {

                return activity.getString(R.string.personal_priority_5);
            }

            case 6: {

                return activity.getString(R.string.personal_priority_6);
            }

            case 7: {

                return activity.getString(R.string.personal_priority_7);
            }

            case 8: {

                return activity.getString(R.string.personal_priority_8);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getImportantInOthers(int mImportant) {

        switch (mImportant) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.important_in_others_1);
            }

            case 2: {

                return activity.getString(R.string.important_in_others_2);
            }

            case 3: {

                return activity.getString(R.string.important_in_others_3);
            }

            case 4: {

                return activity.getString(R.string.important_in_others_4);
            }

            case 5: {

                return activity.getString(R.string.important_in_others_5);
            }

            case 6: {

                return activity.getString(R.string.important_in_others_6);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getSmokingViews(int mSmoking) {

        switch (mSmoking) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.smoking_views_1);
            }

            case 2: {

                return activity.getString(R.string.smoking_views_2);
            }

            case 3: {

                return activity.getString(R.string.smoking_views_3);
            }

            case 4: {

                return activity.getString(R.string.smoking_views_4);
            }

            case 5: {

                return activity.getString(R.string.smoking_views_5);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getAlcoholViews(int mAlcohol) {

        switch (mAlcohol) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.alcohol_views_1);
            }

            case 2: {

                return activity.getString(R.string.alcohol_views_2);
            }

            case 3: {

                return activity.getString(R.string.alcohol_views_3);
            }

            case 4: {

                return activity.getString(R.string.alcohol_views_4);
            }

            case 5: {

                return activity.getString(R.string.alcohol_views_5);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getLooking(int mLooking) {

        switch (mLooking) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.you_looking_1);
            }

            case 2: {

                return activity.getString(R.string.you_looking_2);
            }

            case 3: {

                return activity.getString(R.string.you_looking_3);
            }

            default: {

                break;
            }
        }

        return "-";
    }

    public String getGenderLike(int mLike) {

        switch (mLike) {

            case 0: {

                return "-";
            }

            case 1: {

                return activity.getString(R.string.profile_like_1);
            }

            case 2: {

                return activity.getString(R.string.profile_like_2);
            }

            default: {

                break;
            }
        }

        return "-";
    }
}
