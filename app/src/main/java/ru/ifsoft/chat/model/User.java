package ru.ifsoft.chat.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.util.CustomRequest;


public class User extends Application implements Constants, Parcelable {

    private long id;

    private int sex_orientation, age, height, weight, pro, state, sex, year, month, day, verify, lastAuthorize;

    private double distance = 0;

    private String username, fullname, lowPhotoUrl, bigPhotoUrl, normalPhotoUrl, normalCoverUrl, location, bio, lastAuthorizeDate, lastAuthorizeTimeAgo;

    private Boolean online = false;

    public User() {


    }

    public User(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setState(jsonData.getInt("state"));
                this.setSex(jsonData.getInt("sex"));
                this.setYear(jsonData.getInt("year"));
                this.setMonth(jsonData.getInt("month"));
                this.setDay(jsonData.getInt("day"));
                this.setUsername(jsonData.getString("username"));
                this.setFullname(jsonData.getString("fullname"));
                this.setLocation(jsonData.getString("location"));
                this.setBio(jsonData.getString("status"));
                this.setVerify(jsonData.getInt("verify"));

                this.setLowPhotoUrl(jsonData.getString("lowPhotoUrl"));
                this.setNormalPhotoUrl(jsonData.getString("normalPhotoUrl"));
                this.setBigPhotoUrl(jsonData.getString("bigPhotoUrl"));

                this.setNormalCoverUrl(jsonData.getString("normalCoverUrl"));

                this.setOnline(jsonData.getBoolean("online"));

                this.setLastActive(jsonData.getInt("lastAuthorize"));
                this.setLastActiveDate(jsonData.getString("lastAuthorizeDate"));
                this.setLastActiveTimeAgo(jsonData.getString("lastAuthorizeTimeAgo"));

                if (jsonData.has("distance")) {

                    this.setDistance(jsonData.getDouble("distance"));
                }

                if (jsonData.has("pro")) {

                    this.setProMode(jsonData.getInt("pro"));
                }

                if (jsonData.has("sex_orientation")) {

                    this.setSexOrientation(jsonData.getInt("sex_orientation"));
                }

                if (jsonData.has("height")) {

                    this.setHeight(jsonData.getInt("height"));
                }

                if (jsonData.has("weight")) {

                    this.setWeight(jsonData.getInt("weight"));
                }
            }

        } catch (Throwable t) {

            Log.e("Profile", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Profile", jsonData.toString());
        }
    }

    public void setId(long profile_id) {

        this.id = profile_id;
    }

    public long getId() {

        return this.id;
    }

    public void setState(int profileState) {

        this.state = profileState;
    }

    public int getState() {

        return this.state;
    }

    public void setSex(int sex) {

        this.sex = sex;
    }

    public int getSex() {

        return this.sex;
    }

    public void setYear(int year) {

        this.year = year;
    }

    public int getYear() {

        return this.year;
    }

    public void setMonth(int month) {

        this.month = month;
    }

    public int getMonth() {

        return this.month;
    }

    public void setDay(int day) {

        this.day = day;
    }

    public int getDay() {

        return this.day;
    }

    public void setVerify(int profileVerify) {

        this.verify = profileVerify;
    }

    public int getVerify() {

        return this.verify;
    }

    public void setProMode(int proMode) {

        this.pro = proMode;
    }

    public int getProMode() {

        return this.pro;
    }

    public Boolean isVerify() {

        if (this.verify > 0) {

            return true;
        }

        return false;
    }

    public Boolean isProMode() {

        if (this.pro > 0) {

            return true;
        }

        return false;
    }

    public void setAge(int age) {

        this.age = age;
    }

    public int getAge() {

        return this.age;
    }

    public void setSexOrientation(int sex_orientation) {

        this.sex_orientation = sex_orientation;
    }

    public int getSexOrientation() {

        return this.sex_orientation;
    }

    public void setHeight(int height) {

        this.height = height;
    }

    public int getHeight() {

        return this.height;
    }

    public void setWeight(int weight) {

        this.weight = weight;
    }

    public int getWeight() {

        return this.weight;
    }

    public void setUsername(String profile_username) {

        this.username = profile_username;
    }

    public String getUsername() {

        return this.username;
    }

    public void setFullname(String profile_fullname) {

        this.fullname = profile_fullname;
    }

    public String getFullname() {

        return this.fullname;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    public String getLocation() {

        if (this.location == null) {

            this.location = "";
        }

        return this.location;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public String getBio() {

        return this.bio;
    }

    public void setLowPhotoUrl(String lowPhotoUrl) {

        this.lowPhotoUrl = lowPhotoUrl.replace("/../", "/");
    }

    public String getLowPhotoUrl() {

        return this.lowPhotoUrl;
    }

    public void setBigPhotoUrl(String bigPhotoUrl) {

        this.bigPhotoUrl = bigPhotoUrl.replace("/../", "/");
    }

    public String getBigPhotoUrl() {

        return this.bigPhotoUrl;
    }

    public void setNormalPhotoUrl(String normalPhotoUrl) {

        this.normalPhotoUrl = normalPhotoUrl.replace("/../", "/");
    }

    public String getNormalPhotoUrl() {

        return this.normalPhotoUrl;
    }

    public void setNormalCoverUrl(String normalCoverUrl) {

        this.normalCoverUrl = normalCoverUrl;
    }

    public String getNormalCoverUrl() {

        return this.normalCoverUrl;
    }

    public void setLastActive(int lastAuthorize) {

        this.lastAuthorize = lastAuthorize;
    }

    public int getLastActive() {

        return this.lastAuthorize;
    }

    public void setDistance(double distance) {

        this.distance = distance;
    }

    public double getDistance() {

        return this.distance;
    }

    public void setLastActiveDate(String lastAuthorizeDate) {

        this.lastAuthorizeDate = lastAuthorizeDate;
    }

    public String getLastActiveDate() {

        return this.lastAuthorizeDate;
    }

    public String getBirthDate() {

        int tMonth = this.month + 1;

        return this.day + "/" + tMonth + "/" + this.year;
    }

    public void setLastActiveTimeAgo(String lastAuthorizeTimeAgo) {

        this.lastAuthorizeTimeAgo = lastAuthorizeTimeAgo;
    }

    public String getLastActiveTimeAgo() {

        return this.lastAuthorizeTimeAgo;
    }

    public void setOnline(Boolean online) {

        this.online = online;
    }

    public Boolean isOnline() {

        return this.online;
    }



































    public void addFollower() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_FOLLOW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.getBoolean("error") == false) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

//                            Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(getId()));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.sex_orientation);
        dest.writeInt(this.age);
        dest.writeInt(this.height);
        dest.writeInt(this.weight);
        dest.writeInt(this.pro);
        dest.writeInt(this.state);
        dest.writeInt(this.sex);
        dest.writeInt(this.year);
        dest.writeInt(this.month);
        dest.writeInt(this.day);
        dest.writeInt(this.verify);
        dest.writeInt(this.lastAuthorize);
        dest.writeDouble(this.distance);
        dest.writeString(this.username);
        dest.writeString(this.fullname);
        dest.writeString(this.lowPhotoUrl);
        dest.writeString(this.bigPhotoUrl);
        dest.writeString(this.normalPhotoUrl);
        dest.writeString(this.normalCoverUrl);
        dest.writeString(this.location);
        dest.writeString(this.bio);
        dest.writeString(this.lastAuthorizeDate);
        dest.writeString(this.lastAuthorizeTimeAgo);
        dest.writeValue(this.online);
    }

    protected User(Parcel in) {
        this.id = in.readLong();
        this.sex_orientation = in.readInt();
        this.age = in.readInt();
        this.height = in.readInt();
        this.weight = in.readInt();
        this.pro = in.readInt();
        this.state = in.readInt();
        this.sex = in.readInt();
        this.year = in.readInt();
        this.month = in.readInt();
        this.day = in.readInt();
        this.verify = in.readInt();
        this.lastAuthorize = in.readInt();
        this.distance = in.readDouble();
        this.username = in.readString();
        this.fullname = in.readString();
        this.lowPhotoUrl = in.readString();
        this.bigPhotoUrl = in.readString();
        this.normalPhotoUrl = in.readString();
        this.normalCoverUrl = in.readString();
        this.location = in.readString();
        this.bio = in.readString();
        this.lastAuthorizeDate = in.readString();
        this.lastAuthorizeTimeAgo = in.readString();
        this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
