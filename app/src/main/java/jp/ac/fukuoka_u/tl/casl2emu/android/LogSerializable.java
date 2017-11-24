package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by furusho on 2017/11/24.
 */

public class LogSerializable implements Serializable {

    public JSONObject data;

    public LogSerializable(String name,String value) {
        try {
            this.data = new JSONObject().put(name,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
