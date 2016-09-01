package com.example.furusho.casl2emu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.furusho.casl2emu.databinding.ActivityBinaryEditScreenBinding;
import com.google.common.primitives.Chars;

import org.apache.commons.lang.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icepick.Icepick;

import static android.R.layout.simple_list_item_1;

public class ContextDisplayScreen extends BaseActivity implements LoaderCallbacks,Runnable{

    InputText it;
    ListView listView;
    Casl2Memory memory;
    Casl2Register register;
    Casl2Emulator emulator;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> stringArrayList;
    OutputBuffer outputBuffer;
    HttpURLConnection connection;
    private final static String casl2filedirectory = Environment.getExternalStorageDirectory().getPath()+"CASL2Emu";
    private static final int REQUEST_WRITE_STORAGE = 112;


    private final AdapterView.OnItemClickListener showTextEditDialog = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String msg = String.valueOf(listView.getItemAtPosition(position));
            showTextDialog(msg,position);
        }

    };

    private void showTextDialog(String text, final int position) {
        final HexEditText editView = new HexEditText(ContextDisplayScreen.this,1);
        editView.setText(text);
        new AlertDialog.Builder(ContextDisplayScreen.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(R.layout.input_text_dialog)
                .setTitle("メモリを編集: "+String.format(Locale.US,"0x%04X",position*4 & 0xFFFF)+" - "+String.format(Locale.US,"0x%04X",position*4+3& 0xFFFF))
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //入力した文字をトースト出力する
                        String upperedString =editView.getText().toString().toUpperCase();
                        Pattern pattern = Pattern.compile(getString(R.string.memory_row_pattern));
                        Matcher matcher = pattern.matcher(upperedString);
                        if (matcher.matches()) {
                            //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                            char[] chars = getHexChars(upperedString," ");
                            memory.setMemoryArray(chars, position*4);
                            stringArrayList.remove(position);
                            //arrayAdapter.remove("00 00 00 00 00 00 00 00");
                            arrayAdapter.insert(String.format(Locale.US ,"%04X %04X %04X %04X",
                                    chars[0] & 0xFFFF, chars[1] & 0xFFFF, chars[2] & 0xFFFF, chars[3] & 0xFFFF),position);
                            arrayAdapter.notifyDataSetChanged();

                        }else {
                            Toast.makeText(ContextDisplayScreen.this, "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private char[] getHexChars(String s,String separeter) {
        String[] stmp = s.split(separeter);
        char[] tmp= new char[stmp.length];
        for(int i=0;i<stmp.length;i++){
           tmp[i] = (char)Integer.parseInt(stmp[i],16);
        }
        return tmp;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_edit_screen);
        Icepick.restoreInstanceState(this,savedInstanceState);

        memory = Casl2Memory.getInstance();
        register = Casl2Register.getInstance();
        emulator= Casl2Emulator.getInstance(getApplicationContext());

        final ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        char[] test = new char[]{0,0,9,8,78,7,1,2};
        //String initialString = "F000 FF06 0314 1592 0000 8100 0000 0003 0001 0001 0020 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        //String initialString = "F000 FF06 0314 1592 0000 8100 0000 0003 0001 0001 0020 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        String initialString = "F000 FF04 0001 1592 0000 8100 0000 0003 0001 0001 0020 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);

        register.setGr(test);
        binding.setCasl2Register(register);
        binding.gr0.setOnClickListener(showWordDialog(binding,0));
        binding.gr1.setOnClickListener(showWordDialog(binding,1));
        binding.gr2.setOnClickListener(showWordDialog(binding,2));
        binding.gr3.setOnClickListener(showWordDialog(binding,3));
        binding.gr4.setOnClickListener(showWordDialog(binding,4));
        binding.gr5.setOnClickListener(showWordDialog(binding,5));
        binding.gr6.setOnClickListener(showWordDialog(binding,6));
        binding.gr7.setOnClickListener(showWordDialog(binding,7));
        binding.pc.setOnClickListener(showWordDialog(binding,8));
        binding.sp.setOnClickListener(showWordDialog(binding,9));
        binding.of.setOnClickListener(showWordDialog(binding,10));
        binding.sf.setOnClickListener(showWordDialog(binding,11));
        binding.zf.setOnClickListener(showWordDialog(binding,12));
        //String initialString = "F000 FF02 F000 FF01 0001 0064 0064 0064 0001 0002 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        //String initialString = "8314 1592 F000 FF01 0001 0064 0064 0064 0001 0002 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        char[]tmp = getHexChars(initialString," ");
        memory.setMemory(tmp);
        final char[] a = memory.getMemory();


        listView = binding.memoryList;
        localSetMemoryAdapter(a,0);
        listView.setOnItemClickListener(showTextEditDialog);

        binding.runbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.run();
                startListTask(new char[0],0);
            }
        });
        binding.stepbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.stepOver();
                startListTask(new char[0],0);
            }
        });
        binding.waitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.waitEmu();
            }
        });

        binding.outputscreenbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),OutputScreen.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==5657&&resultCode==RESULT_OK){

            List<String> openfilename=data.getData().getPathSegments();
            byte[] loaddata = new byte[131098];
            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream;
            try {
                fileInputStream=new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath(),
                        openfilename.get(1).split(":")[1]));
                fileInputStream.read(loaddata);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(int i = 0;i<8;i++)
                register.setGr(Chars.fromBytes(loaddata[2*i], loaddata[2*i+1]), i);
            register.setPc(Chars.fromBytes(loaddata[8*2],loaddata[8*2+1]));
            register.setSp(Chars.fromBytes(loaddata[9*2],loaddata[9*2+1]));
            for(int i = 0;i<3;i++)
                register.setFr(Chars.fromBytes(loaddata[2*(10+i)],loaddata[2*(10+i)+1]),i);
            for(int i =0;i<65536;i++)
                memory.setMemoryWithoutNotifying(Chars.fromBytes(loaddata[2*(13+i)],loaddata[2*(13+i)+1]),i);
            localSetMemoryAdapter(memory.getMemory(),0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String filename=getString(R.string.default_file_name);
        final EditText editView = new EditText(getApplicationContext());
        byte[] bytes = new byte[0];
        switch(item.getItemId()){
            case R.id.action_jump:
                editView.setTextColor(Color.BLACK);
                new AlertDialog.Builder(ContextDisplayScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle(getString(R.string.jump_dialog_text))
                        //setViewにてビューを設定します。
                        .setView(editView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String position = editView.getText().toString();
                                listView.setSelection(Integer.parseInt(position,16)/4);

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                break;
            case R.id.action_load:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent,5657);
               //Chars.
                //register.setGr();
                break;
            case R.id.action_teishutu:
                /*ステップ1 :接続URLを決める。
                    ステップ5 :リクエストボディの書き込みを行う。(POSTのみ行う)
                    ステップ6 :レスポンスの読み出しを行う。
                    ステップ7 :コネクションを閉じる。*/


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        URL url;
                        connection = null;
                        DataOutputStream outputStream = null;
                        try {
                            //ステップ1 :接続URLを決める。
                            url = new URL(getString(R.string.server_address));
                            // ステップ2 :URLへのコネクションを取得する。
                            connection = (HttpURLConnection)url.openConnection();
                            //ステップ3 :接続設定(メソッドの決定,タイムアウト値,ヘッダー値等)を行う。
                            connection.setDoOutput(true);
                            connection.setDoInput(true);
                            connection.setReadTimeout(10000 /* milliseconds */);
                            connection.setConnectTimeout(15000 /* milliseconds */);
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Connection", "Keep-Alive");
                            connection.setRequestProperty("Cache-Control", "no-cache");
                            connection.setRequestProperty(
                                    "Content-Type", "multipart/form-data;boundary=" + getString(R.string.boundary));
                            //ステップ4 :コネクションを開く

                            //ステップ5 :リクエストボディの書き込みを行う。(POSTのみ行う)
                            outputStream = new DataOutputStream(connection.getOutputStream());
                            outputStream.writeBytes(getString(R.string.twohyphens) + getString(R.string.boundary) + getString(R.string.crlf));
                            outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                                    "data" + "\";filename=\"" +
                                    "data.cl2" + "\"" + getString(R.string.crlf));
                            outputStream.writeBytes(getString(R.string.crlf));


                            byte[] loaddata = new byte[131098];
                            FileInputStream fileInputStream = null;
                            BufferedInputStream bufferedInputStream;
                            fileInputStream=new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath(),
                                        getString(R.string.default_file_name)));
                            fileInputStream.read(loaddata);

                            outputStream.write(loaddata);
                            outputStream.writeBytes(getString(R.string.crlf));
                            outputStream.writeBytes(getString(R.string.twohyphens) + getString(R.string.boundary) + getString(R.string.crlf));

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if(outputStream!=null){

                                try {
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(connection!=null) {
                                connection.disconnect();
                            }
                        }

                    }
                }).start();


                break;
            case R.id.action_save:
                editView.setText(filename);
                editView.setTextColor(Color.BLACK);
                new AlertDialog.Builder(ContextDisplayScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle("ファイル名入力: ")
                        //setViewにてビューを設定します。
                        .setView(editView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String savedfielname = editView.getText().toString();
                                char[] casl2data;
                                casl2data = Chars.concat(ArrayUtils.add(ArrayUtils.add(register.getGr(),register.getPc()),register.getSp()),register.getFr(),memory.getMemory());
                                byte[]savedata = toBytes(casl2data);
                                FileOutputStream fileOutputStream;

                                try {
                                    File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/CASL2Emu");
                                    boolean hasPermission = (ContextCompat.checkSelfPermission(ContextDisplayScreen.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                                    if (!hasPermission) {
                                        ActivityCompat.requestPermissions(ContextDisplayScreen.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_WRITE_STORAGE);
                                    }
                                    if(!dir.exists()){
                                        dir.mkdir();
                                    }
                                    File file = new File(Environment.getExternalStorageDirectory().getPath()+"/CASL2Emu",filename);
                                    fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(savedata);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private byte[] toBytes(char[] chars) {

        byte[] bytes =  new byte[chars.length*2];
        for(int i=0;i<chars.length;i++){
           bytes[i*2]= (byte) (chars[i] >> 8);
           bytes[i*2+1]= (byte) (chars[i]&0x00FF);
        }
        return bytes;
    }

    private void localSetMemoryAdapter(char[] chars, int positon) {
        startListTask(chars,positon);
    }


    public AdapterView.OnItemClickListener getShowTextEditDialog(){
        return showTextEditDialog;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this,outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //private void showToast() {
    //    Toast.makeText(ContextDisplayScreen.this,"is touched!!",Toast.LENGTH_SHORT).show();
    //}
    private void showToast(String offset) {
        Toast.makeText(ContextDisplayScreen.this,offset+" is touched!!",Toast.LENGTH_SHORT).show();
    }
    private View.OnClickListener showWordDialog(final ActivityBinaryEditScreenBinding binding, final int id) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HexEditText hexEditText;
                if(id<10){
                    hexEditText = new HexEditText(ContextDisplayScreen.this,1);
                }else {
                    hexEditText = new HexEditText(ContextDisplayScreen.this,2);
                }
                TextView textview = (TextView) v;
                //Log.d("dbg",textview.getAccessibilityClassName().toString());
                hexEditText.setText(textview.getText());
                new AlertDialog.Builder(ContextDisplayScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle("レジスタを編集")
                        //setViewにてビューを設定します。
                        .setView(hexEditText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String upperedString = hexEditText.getText().toString().toUpperCase();
                                Pattern pattern;
                                if(id<10) {
                                    pattern = Pattern.compile(getString(R.string.word_pattern_wo_space));
                                }else {
                                    pattern = Pattern.compile(getString(R.string.boolean_pattern));
                                }
                                Matcher matcher = pattern.matcher(upperedString);
                                if (matcher.matches()) {
                                   //Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                                    switch (id){
                                        case 0:
                                            binding.gr0.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),0);
                                            break;
                                        case 1:
                                            binding.gr1.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),1);
                                            break;
                                        case 2:
                                            binding.gr2.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),2);
                                            break;
                                        case 3:
                                            binding.gr3.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),3);
                                            break;
                                        case 4:
                                            binding.gr4.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),4);
                                            break;
                                        case 5:
                                            binding.gr5.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),5);
                                            break;
                                        case 6:
                                            binding.gr6.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),6);
                                            break;
                                        case 7:
                                            binding.gr7.setText(upperedString);
                                            register.setGr((char)Integer.parseInt(upperedString,16),7);
                                            break;
                                        case 8:
                                            binding.pc.setText(upperedString);
                                            register.setPc((char)Integer.parseInt(upperedString,16));
                                            break;
                                        case 9:
                                            binding.sp.setText(upperedString);
                                            register.setSp((char) Integer.parseInt(upperedString,16));
                                            break;
                                        case 10:
                                            binding.of.setText(upperedString);
                                            register.setFr((char) Integer.parseInt(upperedString,16),0 );
                                            break;
                                        case 11:
                                            binding.sf.setText(upperedString);
                                            register.setFr((char) Integer.parseInt(upperedString,16),1 );
                                            break;
                                        case 12:
                                            binding.zf.setText(upperedString);
                                            register.setFr((char) Integer.parseInt(upperedString,16),2 );
                                            break;
                                    }
                                } else {
                                    Toast.makeText(ContextDisplayScreen.this, "適切な文字列を入力してください", Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

            }
        };
    }
    @Override
    public void run() {

    }


    private void startListTask(char[]cs,int i){
       Bundle bundle=new Bundle();
        bundle.putCharArray("cs",cs);
        bundle.putInt("position",i);
        getLoaderManager().initLoader(0,bundle,this).forceLoad();
    }
    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        char[]cs = new char[0];
        int i = 0;
        if(args!=null){
            cs = args.getCharArray("cs");
            i = args.getInt("position");
        }
        return new ListDisplayTaskLoader(this,cs,i);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        if(arrayAdapter == null) {
            stringArrayList = (ArrayList<String>)data;
            arrayAdapter = new CustomArrayAdapter(listView.getContext(),
                    simple_list_item_1,
                    stringArrayList,
                    Typeface.MONOSPACE);
            listView.setAdapter(arrayAdapter);
        }else {
            arrayAdapter.clear();
            arrayAdapter.addAll((ArrayList<String>)data);
            arrayAdapter.notifyDataSetChanged();
        }

    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {

    }
}

class HexEditText extends EditText {
    public HexEditText(Context context, int i) {
        super(context);
        this.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        switch (i){
            case 1:
                this.setKeyListener(DigitsKeyListener.getInstance(context.getString(R.string.a_to_f_0_to_9)));
                break;
            case 2:
                this.setKeyListener(DigitsKeyListener.getInstance(context.getString(R.string.zero_or_one)));
                break;
        }
        this.setTypeface(Typeface.MONOSPACE);
    }
}

