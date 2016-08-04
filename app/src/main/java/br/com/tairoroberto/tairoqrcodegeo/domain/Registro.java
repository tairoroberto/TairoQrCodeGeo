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
    private double latitude;
    private double longitude;

    public Registro() {
    }

    public Registro(long id, String content, String type, double latitude, double longitude) {
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
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeString(type);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
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
}
