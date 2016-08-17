package com.example.furusho.casl2emu;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by furusho on 2016/08/17.
 */

public class ListDisplayTaskLoader extends AsyncTaskLoader {
    private char[] chars;
    private int position;
    public ListDisplayTaskLoader(Context context ,char[] cs,int i) {
        super(context);
        chars=cs;
        position=i;
    }

    /**
     * Called on a worker thread to perform the actual load and to return
     * the result of the load operation.
     * <p>
     * Implementations should not deliver the result directly, but should return them
     * from this method, which will eventually end up calling {@link #deliverResult} on
     * the UI thread.  If implementations need to process the results on the UI thread
     * they may override {@link #deliverResult} and do so there.
     * <p>
     * To support cancellation, this method should periodically check the value of
     * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
     * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
     * directly instead of polling {@link #isLoadInBackgroundCanceled}.
     * <p>
     * When the load is canceled, this method may either return normally or throw
     * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
     * result object, if any.
     *
     * @return The result of the load operation.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Override
    public ArrayList<String> loadInBackground() {
        ArrayList<String> stringArrayList = new ArrayList<String>();
        for(int i = position; i< chars.length/8; i++){
            stringArrayList.add(String.format(Locale.US ,"%02X %02X %02X %02X %02X %02X %02X %02X",
                chars[8*i] & 0xFFFF, chars[8*i+1] & 0xFFFF, chars[8*i+2] & 0xFFFF, chars[8*i+3] &
                0xFFFF, chars[8*i+4] & 0xFFFF, chars[8*i+5] & 0xFFFF, chars[8*i+6] & 0xFFFF, chars[8*i+7] & 0xFFFF));
        }
        return stringArrayList;
    }


    private Object _data;


    public ListDisplayTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(Object data) {
        if (isReset()) {
            return;
        }
        _data = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (_data != null) {
            deliverResult(_data);
        }

        if (takeContentChanged() || _data == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        _data = null;
    }

}
