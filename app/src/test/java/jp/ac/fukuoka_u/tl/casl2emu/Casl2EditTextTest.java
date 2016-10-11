package jp.ac.fukuoka_u.tl.casl2emu;

import android.content.Context;
import android.test.InstrumentationTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by furusho on 2016/10/07.
 */
public class Casl2EditTextTest extends InstrumentationTestCase{
    @Before
    public void setUp() throws Exception {

    }
    private Context getApplicationContext() {
        return this.getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Test
    public void getChar() throws Exception {

        Casl2EditText text = new Casl2EditText(getApplicationContext(),1);
    }

    @Test
    public void getShort() throws Exception {

    }

}