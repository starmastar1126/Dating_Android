package ru.ifsoft.chat.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import ru.ifsoft.chat.constants.Constants;


public class RefillItem extends Application implements Constants, Parcelable {

    private long id, toUserId;
    private int amount, refillType;
    private String date;

    public RefillItem() {

    }

    public RefillItem(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setToUserId(jsonData.getLong("toUserId"));
                this.setAmount(jsonData.getInt("amount"));
                this.setRefillType(jsonData.getInt("refillType"));
                this.setDate(jsonData.getString("date"));
            }

        } catch (Throwable t) {

            Log.e("Refill", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Refill", jsonData.toString());
        }
    }


    public long getId() {

        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getToUserId() {

        return this.toUserId;
    }

    public void setToUserId(long toUserId) {
        this.toUserId = toUserId;
    }

    public int getAmount() {

        return this.amount;
    }

    public void setAmount(int amount) {

        this.amount = amount;
    }

    public int getRefillType() {

        return this.refillType;
    }

    public void setRefillType(int refillType) {

        this.refillType = refillType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.toUserId);
        dest.writeInt(this.amount);
        dest.writeInt(this.refillType);
        dest.writeString(this.date);
    }

    protected RefillItem(Parcel in) {
        this.id = in.readLong();
        this.toUserId = in.readLong();
        this.amount = in.readInt();
        this.refillType = in.readInt();
        this.date = in.readString();
    }

    public static final Creator<RefillItem> CREATOR = new Creator<RefillItem>() {
        @Override
        public RefillItem createFromParcel(Parcel source) {
            return new RefillItem(source);
        }

        @Override
        public RefillItem[] newArray(int size) {
            return new RefillItem[size];
        }
    };
}
