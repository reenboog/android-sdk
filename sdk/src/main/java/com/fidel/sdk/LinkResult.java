package com.fidel.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by reenboog on 8/1/17.
 */

public class LinkResult implements Parcelable {
    public String id;

    public String created;
    public String updated;

    public String type;
    public String scheme;

    public String programId;

    public Boolean mapped = false;
    public Boolean live = false;

    public String lastNumbers;

    public int expYear;
    public int expMonth;
    public String expDate;

    public String countryCode;
    public String accountId;

    public LinkResult(String id) {
        this.id = id;
    }

    private LinkResult(Parcel src) {
        id = src.readString();

        created = src.readString();
        updated = src.readString();

        type = src.readString();
        scheme = src.readString();

        programId = src.readString();

        mapped = src.readInt() != 0;
        live = src.readInt() != 0;

        lastNumbers = src.readString();

        expYear = src.readInt();
        expMonth = src.readInt();

        expDate = src.readString();

        countryCode = src.readString();
        accountId = src.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);

        dest.writeString(created);
        dest.writeString(updated);

        dest.writeString(type);
        dest.writeString(scheme);

        dest.writeString(programId);

        dest.writeInt(mapped ? 1 : 0);
        dest.writeInt(live ? 1 : 0);

        dest.writeString(lastNumbers);

        dest.writeInt(expYear);
        dest.writeInt(expMonth);

        dest.writeString(expDate);

        dest.writeString(countryCode);
        dest.writeString(accountId);
    }

    public static final Parcelable.Creator<LinkResult> CREATOR = new Parcelable.Creator<LinkResult>() {

        @Override
        public LinkResult createFromParcel(Parcel source) {
            return new LinkResult(source);
        }

        @Override
        public LinkResult[] newArray(int size) {
            return new LinkResult[size];
        }
    };


}
