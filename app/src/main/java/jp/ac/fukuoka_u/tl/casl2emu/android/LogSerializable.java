package jp.ac.fukuoka_u.tl.casl2emu.android;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 行動履歴用のクラス
 *
 */
public class LogSerializable implements Serializable {

    public String data;

    public LogSerializable(String name, String value) {
        try {
            data = new JSONObject().put(name,value).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
