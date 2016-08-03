package br.com.tairoroberto.tairoqrcodegeo.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tairo on 02/08/16.
 */
public class Registro implements Parcelable {
    private long id;
    private String content;
    private String type;
    private String latitude;
    private String longitude;

    public Registro() {
    }

    public Registro(long id, String content, String type, String latitude, String longitude) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected Registro(Parcel in) {
        id = in.readLong();
        content = in.readString();
        type = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public static final Creator<Registro> CREATOR = new Creator<Registro>() {
        @Override
        public Registro createFromParcel(Parcel in) {
            return new Registro(in);
        }

        @Override
        public Registro[] newArray(int size) {
            return new Registro[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeString(type);
        dest.writeString(latitude);
        dest.writeString(longitude);
    }
}
