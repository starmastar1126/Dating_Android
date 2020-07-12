package ru.ifsoft.chat.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import ru.ifsoft.chat.constants.Constants;


public class Profile extends Application implements Constants, Parcelable {

    private long id;

    private int feeling, sex_orientation = 0, age = 0, height, weight, state, sex = 3, year, month, day, verify, pro, itemsCount, likesCount, giftsCount, friendsCount, followingsCount, followersCount, photosCount, matchesCount, allowPhotosComments, allowShowMyBirthday, allowMessages, lastAuthorize;

    private double distance = 0;

    private int relationshipStatus, politicalViews, worldView, personalPriority, importantInOthers, viewsOnSmoking, viewsOnAlcohol, youLooking, youLike;

    private int allowShowMyInfo, allowShowMyGallery, allowShowMyFriends, allowShowMyLikes, allowShowMyGifts, allowShowMyAge, allowShowMySexOrientation, allowShowOnline;

    private String username, fullname, lowPhotoUrl, bigPhotoUrl, normalPhotoUrl, normalCoverUrl, location, facebookPage, instagramPage, bio, lastAuthorizeDate, lastAuthorizeTimeAgo, createDate;

    private Boolean blocked = false;

    private Boolean inBlackList = false;

    private Boolean follower = false;

    private Boolean friend = false;

    private Boolean match = false;

    private Boolean follow = false;

    private Boolean online = false;

    private Boolean myLike = false;

    private String android_fcm_regId = "";
    private String ios_fcm_regId = "";

    private String last_visit = "";

    public Profile() {


    }

    public Profile(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setRelationshipStatus(jsonData.getInt("iStatus"));
                this.setPoliticalViews(jsonData.getInt("iPoliticalViews"));
                this.setWorldView(jsonData.getInt("iWorldView"));
                this.setPersonalPriority(jsonData.getInt("iPersonalPriority"));
                this.setImportantInOthers(jsonData.getInt("iImportantInOthers"));
                this.setViewsOnSmoking(jsonData.getInt("iSmokingViews"));
                this.setViewsOnAlcohol(jsonData.getInt("iAlcoholViews"));
                this.setYouLooking(jsonData.getInt("iLooking"));
                this.setYouLike(jsonData.getInt("iInterested"));

                this.setId(jsonData.getLong("id"));
                this.setState(jsonData.getInt("state"));
                this.setSex(jsonData.getInt("sex"));
                this.setYear(jsonData.getInt("year"));
                this.setMonth(jsonData.getInt("month"));
                this.setDay(jsonData.getInt("day"));
                this.setUsername(jsonData.getString("username"));
                this.setFullname(jsonData.getString("fullname"));
                this.setLocation(jsonData.getString("location"));
                this.setFacebookPage(jsonData.getString("fb_page"));
                this.setInstagramPage(jsonData.getString("instagram_page"));
                this.setBio(jsonData.getString("status"));
                this.setVerify(jsonData.getInt("verify"));

                this.setLowPhotoUrl(jsonData.getString("lowPhotoUrl"));
                this.setNormalPhotoUrl(jsonData.getString("normalPhotoUrl"));
                this.setBigPhotoUrl(jsonData.getString("bigPhotoUrl"));

                this.setNormalCoverUrl(jsonData.getString("normalCoverUrl"));

                this.setFriendsCount(jsonData.getInt("friendsCount"));
                this.setLikesCount(jsonData.getInt("likesCount"));
                this.setGiftsCount(jsonData.getInt("giftsCount"));
                this.setPhotosCount(jsonData.getInt("photosCount"));

                this.setAllowPhotosComments(jsonData.getInt("allowPhotosComments"));
                this.setAllowMessages(jsonData.getInt("allowMessages"));
                this.setAllowShowMyBirthday(jsonData.getInt("allowShowMyBirthday"));

                this.setAllowShowMyInfo(jsonData.getInt("allowShowMyInfo"));
                this.setAllowShowMyGallery(jsonData.getInt("allowShowMyGallery"));
                this.setAllowShowMyFriends(jsonData.getInt("allowShowMyFriends"));
                this.setAllowShowMyLikes(jsonData.getInt("allowShowMyLikes"));
                this.setAllowShowMyGifts(jsonData.getInt("allowShowMyGifts"));

                this.setInBlackList(jsonData.getBoolean("inBlackList"));
                this.setFollower(jsonData.getBoolean("follower"));
                this.setFriend(jsonData.getBoolean("friend"));
                this.setFollow(jsonData.getBoolean("follow"));
                this.setOnline(jsonData.getBoolean("online"));
                this.setBlocked(jsonData.getBoolean("blocked"));
                this.setMyLike(jsonData.getBoolean("myLike"));

                this.setLastActive(jsonData.getInt("lastAuthorize"));
                this.setLastActiveDate(jsonData.getString("lastAuthorizeDate"));
                this.setLastActiveTimeAgo(jsonData.getString("lastAuthorizeTimeAgo"));

                this.setCreateDate(jsonData.getString("createDate"));

                if (jsonData.has("match")) {

                    this.setMatch(jsonData.getBoolean("match"));
                }

                if (jsonData.has("matchesCount")) {

                    this.setMatchesCount(jsonData.getInt("matchesCount"));
                }

                if (jsonData.has("distance")) {

                    this.setDistance(jsonData.getDouble("distance"));
                }

                if (jsonData.has("pro")) {

                    this.setProMode(jsonData.getInt("pro"));
                }

                if (jsonData.has("gcm_regid")) {

                    this.set_android_fcm_regId(jsonData.getString("gcm_regid"));
                }

                if (jsonData.has("ios_fcm_regid")) {

                    this.set_iOS_fcm_regId(jsonData.getString("ios_fcm_regid"));
                }

                if (jsonData.has("age")) {

                    this.setAge(jsonData.getInt("age"));
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

                if (jsonData.has("allowShowMyAge")) {

                    this.setAllowShowMyAge(jsonData.getInt("allowShowMyAge"));
                }

                if (jsonData.has("allowShowMySexOrientation")) {

                    this.setAllowShowMySexOrientation(jsonData.getInt("allowShowMySexOrientation"));
                }

                if (jsonData.has("allowShowOnline")) {

                    this.setAllowShowOnline(jsonData.getInt("allowShowOnline"));
                }

                if (jsonData.has("feeling")) {

                    this.setFeeling(jsonData.getInt("feeling"));
                }
            }

        } catch (Throwable t) {

            Log.e("Profile", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Profile", jsonData.toString());
        }
    }

    public void setRelationshipStatus(int relationshipStatus) {

        this.relationshipStatus = relationshipStatus;
    }

    public int getRelationshipStatus() {

        return this.relationshipStatus;
    }

    public void setPoliticalViews(int politicalViews) {

        this.politicalViews = politicalViews;
    }

    public int getPoliticalViews() {

        return this.politicalViews;
    }

    public void setWorldView(int worldView) {

        this.worldView = worldView;
    }

    public int getWorldView() {

        return this.worldView;
    }

    public void setPersonalPriority(int personalPriority) {

        this.personalPriority = personalPriority;
    }

    public int getPersonalPriority() {

        return this.personalPriority;
    }

    public void setImportantInOthers(int importantInOthers) {

        this.importantInOthers = importantInOthers;
    }

    public int getImportantInOthers() {

        return this.importantInOthers;
    }

    public void setViewsOnSmoking(int viewsOnSmoking) {

        this.viewsOnSmoking = viewsOnSmoking;
    }

    public int getViewsOnSmoking() {

        return this.viewsOnSmoking;
    }

    public void setViewsOnAlcohol(int viewsOnAlcohol) {

        this.viewsOnAlcohol = viewsOnAlcohol;
    }

    public int getViewsOnAlcohol() {

        return this.viewsOnAlcohol;
    }

    public void setYouLooking(int youLooking) {

        this.youLooking = youLooking;
    }

    public int getYouLooking() {

        return this.youLooking;
    }

    public void setYouLike(int youLike) {

        this.youLike = youLike;
    }

    public int getYouLike() {

        return this.youLike;
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

    public void setFacebookPage(String facebookPage) {

        this.facebookPage = facebookPage;
    }

    public String getFacebookPage() {

        return this.facebookPage;
    }

    public void setInstagramPage(String instagramPage) {

        this.instagramPage = instagramPage;
    }

    public String getInstagramPage() {

        return this.instagramPage;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public String getBio() {

        return this.bio;
    }

    public void setLowPhotoUrl(String lowPhotoUrl) {

        this.lowPhotoUrl = lowPhotoUrl;
    }

    public String getLowPhotoUrl() {

        return this.lowPhotoUrl;
    }

    public void setBigPhotoUrl(String bigPhotoUrl) {

        this.bigPhotoUrl = bigPhotoUrl;
    }

    public String getBigPhotoUrl() {

        return this.bigPhotoUrl;
    }

    public void setNormalPhotoUrl(String normalPhotoUrl) {

        this.normalPhotoUrl = normalPhotoUrl;
    }

    public String getNormalPhotoUrl() {

        if (this.normalPhotoUrl == null) {

            this.normalPhotoUrl = "";
        }

        return this.normalPhotoUrl;
    }

    public void setNormalCoverUrl(String normalCoverUrl) {

        this.normalCoverUrl = normalCoverUrl;
    }

    public String getNormalCoverUrl() {

        return this.normalCoverUrl;
    }

    public void setFollowersCount(int followersCount) {

        this.followersCount = followersCount;
    }

    public int getFollowersCount() {

        return this.followersCount;
    }

    public void setFriendsCount(int friendsCount) {

        this.friendsCount = friendsCount;
    }

    public int getFriendsCount() {

        return this.friendsCount;
    }

    public void setItemsCount(int itemsCount) {

        this.itemsCount = itemsCount;
    }

    public int getItemsCount() {

        return this.itemsCount;
    }

    public void setLikesCount(int likesCount) {

        this.likesCount = likesCount;
    }

    public int getLikesCount() {

        return this.likesCount;
    }

    public void setGiftsCount(int giftsCount) {

        this.giftsCount = giftsCount;
    }

    public int getGiftsCount() {

        return this.giftsCount;
    }

    public void setPhotosCount(int photosCount) {

        this.photosCount = photosCount;
    }

    public int getPhotosCount() {

        return this.photosCount;
    }

    public void setMatchesCount(int matchesCount) {

        this.matchesCount = matchesCount;
    }

    public int getMatchesCount() {

        return this.matchesCount;
    }

    public void setFollowingsCount(int followingsCount) {

        this.followingsCount = followingsCount;
    }

    public int getFollowingsCount() {

        return this.followingsCount;
    }

    public void setAllowPhotosComments(int allowPhotosComments) {

        this.allowPhotosComments = allowPhotosComments;
    }

    public int getAllowPhotosComments() {

        return this.allowPhotosComments;
    }

    public void setAllowMessages(int allowMessages) {

        this.allowMessages = allowMessages;
    }

    public int getAllowMessages() {

        return this.allowMessages;
    }

    public void setAllowShowMyBirthday(int allowShowMyBirthday) {

        this.allowShowMyBirthday = allowShowMyBirthday;
    }

    public int getAllowShowMyBirthday() {

        return this.allowShowMyBirthday;
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

    public void setCreateDate(String createDate) {

        this.createDate = createDate;
    }

    public String getCreateDate() {

        return this.createDate;
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

    public void setFeeling(int feeling) {

        this.feeling = feeling;
    }

    public int getFeeling() {

        return this.feeling;
    }

    public void setBlocked(Boolean blocked) {

        this.blocked = blocked;
    }

    public Boolean isBlocked() {

        return this.blocked;
    }

    public void setFollower(Boolean follower) {

        this.follower = follower;
    }

    public Boolean isFollower() {

        return this.follower;
    }

    public void setFriend(Boolean friend) {

        this.friend = friend;
    }

    public Boolean isFriend() {

        return this.friend;
    }

    public void setMatch(Boolean match) {

        this.match = match;
    }

    public Boolean isMatch() {

        return this.match;
    }

    public void setFollow(Boolean follow) {

        this.follow = follow;
    }

    public Boolean isFollow() {

        return this.follow;
    }

    public void setOnline(Boolean online) {

        this.online = online;
    }

    public Boolean isOnline() {

        return this.online;
    }

    public void setMyLike(Boolean myLike) {

        this.myLike = myLike;
    }

    public Boolean isMyLike() {

        return this.myLike;
    }

    public void setInBlackList(Boolean inBlackList) {

        this.inBlackList = inBlackList;
    }

    public Boolean isInBlackList() {

        return this.inBlackList;
    }

    // Privacy

    public void setAllowShowMyAge(int allowShowMyAge) {

        this.allowShowMyAge = allowShowMyAge;
    }

    public int getAllowShowMyAge() {

        return this.allowShowMyAge;
    }

    public void setAllowShowMySexOrientation(int allowShowMySexOrientation) {

        this.allowShowMySexOrientation = allowShowMySexOrientation;
    }

    public int getAllowShowMySexOrientation() {

        return this.allowShowMySexOrientation;
    }

    public void setAllowShowOnline(int allowShowOnline) {

        this.allowShowOnline = allowShowOnline;
    }

    public int getAllowShowOnline() {

        return this.allowShowOnline;
    }

    public void setAllowShowMyInfo(int allowShowMyInfo) {

        this.allowShowMyInfo = allowShowMyInfo;
    }

    public int getAllowShowMyInfo() {

        return this.allowShowMyInfo;
    }

    public void setAllowShowMyGallery(int allowShowMyGallery) {

        this.allowShowMyGallery = allowShowMyGallery;
    }

    public int getAllowShowMyGallery() {

        return this.allowShowMyGallery;
    }

    public void setAllowShowMyFriends(int allowShowMyFriends) {

        this.allowShowMyFriends = allowShowMyFriends;
    }

    public int getAllowShowMyFriends() {

        return this.allowShowMyFriends;
    }

    public void setAllowShowMyLikes(int allowShowMyLikes) {

        this.allowShowMyLikes = allowShowMyLikes;
    }

    public int getAllowShowMyLikes() {

        return this.allowShowMyLikes;
    }

    public void setAllowShowMyGifts(int allowShowMyGifts) {

        this.allowShowMyGifts = allowShowMyGifts;
    }

    public int getAllowShowMyGifts() {

        return this.allowShowMyGifts;
    }

    public void set_android_fcm_regId(String android_fcm_regId) {

        this.android_fcm_regId = android_fcm_regId;
    }

    public String get_android_fcm_regId() {

        return this.android_fcm_regId;
    }

    public void set_iOS_fcm_regId(String ios_fcm_regId) {

        this.ios_fcm_regId = ios_fcm_regId;
    }

    public String get_iOS_fcm_regId() {

        return this.ios_fcm_regId;
    }

    // For guests only

    public void setLastVisit(String last_visit) {

        this.last_visit = last_visit;
    }

    public String getLastVisit() {

        if (this.last_visit == null) {

            this.last_visit = "";
        }

        return this.last_visit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.feeling);
        dest.writeInt(this.sex_orientation);
        dest.writeInt(this.age);
        dest.writeInt(this.height);
        dest.writeInt(this.weight);
        dest.writeInt(this.state);
        dest.writeInt(this.sex);
        dest.writeInt(this.year);
        dest.writeInt(this.month);
        dest.writeInt(this.day);
        dest.writeInt(this.verify);
        dest.writeInt(this.pro);
        dest.writeInt(this.itemsCount);
        dest.writeInt(this.likesCount);
        dest.writeInt(this.giftsCount);
        dest.writeInt(this.friendsCount);
        dest.writeInt(this.followingsCount);
        dest.writeInt(this.followersCount);
        dest.writeInt(this.photosCount);
        dest.writeInt(this.matchesCount);
        dest.writeInt(this.allowPhotosComments);
        dest.writeInt(this.allowShowMyBirthday);
        dest.writeInt(this.allowMessages);
        dest.writeInt(this.lastAuthorize);
        dest.writeDouble(this.distance);
        dest.writeInt(this.relationshipStatus);
        dest.writeInt(this.politicalViews);
        dest.writeInt(this.worldView);
        dest.writeInt(this.personalPriority);
        dest.writeInt(this.importantInOthers);
        dest.writeInt(this.viewsOnSmoking);
        dest.writeInt(this.viewsOnAlcohol);
        dest.writeInt(this.youLooking);
        dest.writeInt(this.youLike);
        dest.writeInt(this.allowShowMyInfo);
        dest.writeInt(this.allowShowMyGallery);
        dest.writeInt(this.allowShowMyFriends);
        dest.writeInt(this.allowShowMyLikes);
        dest.writeInt(this.allowShowMyGifts);
        dest.writeInt(this.allowShowMyAge);
        dest.writeInt(this.allowShowMySexOrientation);
        dest.writeInt(this.allowShowOnline);
        dest.writeString(this.username);
        dest.writeString(this.fullname);
        dest.writeString(this.lowPhotoUrl);
        dest.writeString(this.bigPhotoUrl);
        dest.writeString(this.normalPhotoUrl);
        dest.writeString(this.normalCoverUrl);
        dest.writeString(this.location);
        dest.writeString(this.facebookPage);
        dest.writeString(this.instagramPage);
        dest.writeString(this.bio);
        dest.writeString(this.lastAuthorizeDate);
        dest.writeString(this.lastAuthorizeTimeAgo);
        dest.writeString(this.createDate);
        dest.writeValue(this.blocked);
        dest.writeValue(this.inBlackList);
        dest.writeValue(this.follower);
        dest.writeValue(this.friend);
        dest.writeValue(this.match);
        dest.writeValue(this.follow);
        dest.writeValue(this.online);
        dest.writeValue(this.myLike);
        dest.writeString(this.android_fcm_regId);
        dest.writeString(this.ios_fcm_regId);
        dest.writeString(this.last_visit);
    }

    protected Profile(Parcel in) {
        this.id = in.readLong();
        this.feeling = in.readInt();
        this.sex_orientation = in.readInt();
        this.age = in.readInt();
        this.height = in.readInt();
        this.weight = in.readInt();
        this.state = in.readInt();
        this.sex = in.readInt();
        this.year = in.readInt();
        this.month = in.readInt();
        this.day = in.readInt();
        this.verify = in.readInt();
        this.pro = in.readInt();
        this.itemsCount = in.readInt();
        this.likesCount = in.readInt();
        this.giftsCount = in.readInt();
        this.friendsCount = in.readInt();
        this.followingsCount = in.readInt();
        this.followersCount = in.readInt();
        this.photosCount = in.readInt();
        this.matchesCount = in.readInt();
        this.allowPhotosComments = in.readInt();
        this.allowShowMyBirthday = in.readInt();
        this.allowMessages = in.readInt();
        this.lastAuthorize = in.readInt();
        this.distance = in.readDouble();
        this.relationshipStatus = in.readInt();
        this.politicalViews = in.readInt();
        this.worldView = in.readInt();
        this.personalPriority = in.readInt();
        this.importantInOthers = in.readInt();
        this.viewsOnSmoking = in.readInt();
        this.viewsOnAlcohol = in.readInt();
        this.youLooking = in.readInt();
        this.youLike = in.readInt();
        this.allowShowMyInfo = in.readInt();
        this.allowShowMyGallery = in.readInt();
        this.allowShowMyFriends = in.readInt();
        this.allowShowMyLikes = in.readInt();
        this.allowShowMyGifts = in.readInt();
        this.allowShowMyAge = in.readInt();
        this.allowShowMySexOrientation = in.readInt();
        this.allowShowOnline = in.readInt();
        this.username = in.readString();
        this.fullname = in.readString();
        this.lowPhotoUrl = in.readString();
        this.bigPhotoUrl = in.readString();
        this.normalPhotoUrl = in.readString();
        this.normalCoverUrl = in.readString();
        this.location = in.readString();
        this.facebookPage = in.readString();
        this.instagramPage = in.readString();
        this.bio = in.readString();
        this.lastAuthorizeDate = in.readString();
        this.lastAuthorizeTimeAgo = in.readString();
        this.createDate = in.readString();
        this.blocked = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.inBlackList = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.follower = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.friend = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.match = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.follow = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.myLike = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.android_fcm_regId = in.readString();
        this.ios_fcm_regId = in.readString();
        this.last_visit = in.readString();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
