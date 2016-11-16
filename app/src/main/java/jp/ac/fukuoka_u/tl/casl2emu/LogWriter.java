package jp.ac.fukuoka_u.tl.casl2emu;

import android.app.Application;
import android.content.Context;
import android.support.annotation.RequiresPermission;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.FileOutputStream;

/**
 * Created by furusho on 2016/11/09.
 */

public class LogWriter {


    Context context;

    public LogWriter(Context context) {
        this.context = context;
    }

    WriterOutputStream writerOutputStream;

}
