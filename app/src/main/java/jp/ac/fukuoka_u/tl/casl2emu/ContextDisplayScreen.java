package jp.ac.fukuoka_u.tl.casl2emu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Chars;

import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icepick.Icepick;
import jp.ac.fukuoka_u.tl.casl2emu.databinding.ActivityBinaryEditScreenBinding;

import static android.R.layout.simple_list_item_1;

public class ContextDisplayScreen extends BaseActivity implements LoaderCallbacks,Runnable{

    ListView listView;
    Casl2Register register;
    private static final int REQUEST_WRITE_STORAGE = 112;
    BroadcastReceiver refreshReceiver;


    private final AdapterView.OnItemClickListener showTextEditDialog = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String msg = String.valueOf(listView.getItemAtPosition(position));
            showTextDialog(msg,position);
        }

    };

    private void showTextDialog(String text, final int rownum) {
        final Casl2EditText editView = new Casl2EditText(ContextDisplayScreen.this,1);
        final InputMethodManager inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        editView.setText(text);
        editView.setMaxLines(1);
        editView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(editView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    return true;
                }
                return false;
            }
        });
        new AlertDialog.Builder(ContextDisplayScreen.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(R.layout.input_text_dialog)
                .setTitle("メモリを編集: "+String.format(Locale.US,"0x%04X",rownum*4 & 0xFFFF)+" - "+String.format(Locale.US,"0x%04X",rownum*4+3& 0xFFFF))
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //入力した文字をトースト出力する
                        String upperedString =editView.getText().toString().toUpperCase();
                        Pattern pattern = Pattern.compile(getString(R.string.memory_row_pattern));
                        Matcher matcher = pattern.matcher(upperedString);
                        if (matcher.matches()) {
                            char[] chars = Casl2EditText.getHexChars(upperedString," ");
                            memory.setMemoryArray(chars, rownum*4);
                            refreshMemoryPane(rownum);

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

    private void refreshMemoryPane(int rownum) {
        stringArrayList.remove(rownum);
        arrayAdapter.insert(String.format(Locale.US ,"%04X %04X %04X %04X",
                memory.getMemory(4*rownum) & 0xFFFF, memory.getMemory(4*rownum+1) & 0xFFFF,
                memory.getMemory(4*rownum+2) & 0xFFFF, memory.getMemory(4*rownum+3) & 0xFFFF),rownum);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void refreshMemory(char[] data, char position) {
        super.refreshMemory(data, position);
        char memoryRowPosition = (char) ((position/4)*4);
        refreshMemoryPane(memoryRowPosition);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_edit_screen);
        Icepick.restoreInstanceState(this,savedInstanceState);

        register = Casl2Register.getInstance();

        final ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        char[] initialState;
        String initialString;
        //OUTデモ
        //initialState = new char[]{0,0,0,0,0,0,0x000F,2};
        //initialString = "F000 FF00 4675 6B75 6461 690A 2837 3930 213F 2900"+" "+getString(R.string.short_zerofill);

        //図形描画デモ
        //initialState = new char[]{0,0,0,0,0x001D,0x0016,0x0010,0x000b};
        //initialString = "F000 FF02 1476 F000 FF02 1475 F000 FF02 1474 F000 FF02 0001 0064 0064 0064 0001 0002 00C8 00C8 0190 0190 0000 0003 0190 0190 01F4 01F4 0002 000a 0004 0100 0100 0003 0010"+" "+getString(R.string.short_zerofill);
        //音楽再生デモ
        //initialState = new char[]{0,0,0,0,0x009,0x0008,0x0001,0x0007};
        //initialString = "F000 FF04 1475 F000 FF04 1474 F000 FF04 0001 0002 0003 F000 FF02 0001 0064 0064 0064 0001 0002 00C8 00C8 0190 0190 0000 0003 0190 0190 01F4 01F4 0002 000a 0004 0100 0100 0003 0010"+" "+getString(R.string.short_zerofill);
        //String initialString = "F000 FF06 0314 1592 0000 8100 0000 0003 0001 0001 0020 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        //String initialString = "F000 FF06 0314 1592 0000 8100 0000 0003 0001 0001 0020 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        //音楽再生デモ
        //initialState = new char[]{0,0,9,8,78,7,1,2};
        //initialString = "F000 FF04 0001 1592 0000 8100 0000 0003 0001 0001 0020 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        //STデモ
        //initialState = new char[]{0,0,0,0,0,0,0x000F,2};
        //initialString = "1100 0002 4675 6B75 6461 690A 2837 3930 213F 2900"+" "+getString(R.string.short_zerofill);
        //INPUTデモ
        //initialState = new char[]{0,0,0,0,0,0,0x000F,2};
        //initialString = "F000 FF0E 4675 6B75 6461 690A 2837 3930 213F 2900"+" "+getString(R.string.short_zerofill);
        //ASYNCINPUTデモ
        //initialState = new char[]{0,0,0,0,0,1,0x0001,2};
        //initialString = "F000 FF10 4675 6B75 6461 690A 2837 3930 213F 2900"+" "+getString(R.string.short_zerofill);

        initialState = new char[]{0,0,0,0,0,0,0,0};
        initialString = "0000 0000 0000 0000";

        register.setGr(initialState);
        binding.setCasl2Register(register);
        binding.gr0.setOnClickListener(showWordDialog(binding,0));
        binding.gr0.setOnLongClickListener(jumpAddress(binding,0));
        binding.gr1.setOnClickListener(showWordDialog(binding,1));
        binding.gr1.setOnLongClickListener(jumpAddress(binding,1));
        binding.gr2.setOnClickListener(showWordDialog(binding,2));
        binding.gr2.setOnLongClickListener(jumpAddress(binding,2));
        binding.gr3.setOnClickListener(showWordDialog(binding,3));
        binding.gr3.setOnLongClickListener(jumpAddress(binding,3));
        binding.gr4.setOnClickListener(showWordDialog(binding,4));
        binding.gr4.setOnLongClickListener(jumpAddress(binding,4));
        binding.gr5.setOnClickListener(showWordDialog(binding,5));
        binding.gr5.setOnLongClickListener(jumpAddress(binding,5));
        binding.gr6.setOnClickListener(showWordDialog(binding,6));
        binding.gr6.setOnLongClickListener(jumpAddress(binding,6));
        binding.gr7.setOnClickListener(showWordDialog(binding,7));
        binding.gr7.setOnLongClickListener(jumpAddress(binding,7));
        binding.pc.setOnClickListener(showWordDialog(binding,8));
        binding.pc.setOnLongClickListener(jumpAddress(binding,8));
        binding.sp.setOnClickListener(showWordDialog(binding,9));
        binding.sp.setOnLongClickListener(jumpAddress(binding,9));
        binding.of.setOnClickListener(showWordDialog(binding,10));
        binding.sf.setOnClickListener(showWordDialog(binding,11));
        binding.zf.setOnClickListener(showWordDialog(binding,12));
        //String initialString = "8314 1592 F000 FF01 0001 0064 0064 0064 0001 0002 00C8 00C8 0190 0190 0000"+" "+getString(R.string.short_zerofill);
        char[]tmp = Casl2EditText.getHexChars(initialString," ");
        memory.setMemory(tmp);


        listView = binding.memoryList;
        localSetMemoryAdapter(memory.getMemory(),0);
        listView.setOnItemClickListener(showTextEditDialog);

        binding.runbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.run(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(getString(R.string.intervalkey),1000));
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
    protected void onResume() {
        super.onResume();
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshMemoryPane(intent.getCharExtra(getString(R.string.BUTTON_INPUT_ADDRESS),(char)0)/4);
            }
        };
        IntentFilter filter = new IntentFilter(getString(R.string.action_memory_refresh));
        registerReceiver(refreshReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(refreshReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        if(data!=null){
            if(data.getData().getPathSegments().get(1).contains(sharedPreferences.getString("userid","null"))) {
                if(requestCode==1114&&resultCode==RESULT_OK) {
                        startDataSendTask(data, sharedPreferences);
                }
                else if(requestCode==5657&&resultCode==RESULT_OK){

                    List<String> openfilename=data.getData().getPathSegments();
                    byte[] loaddata = new byte[131098];
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream=new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath(),
                                openfilename.get(1).split(":")[1]));
                        fileInputStream.read(loaddata);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for(int i = 0;i<8;i++) register.setGr(Chars.fromBytes(loaddata[2*i], loaddata[2*i+1]), i);
                    register.setPc(Chars.fromBytes(loaddata[8*2],loaddata[8*2+1]));
                    register.setSp(Chars.fromBytes(loaddata[9*2],loaddata[9*2+1]));
                    for(int i = 0;i<3;i++) register.setFr(Chars.fromBytes(loaddata[2*(10+i)],loaddata[2*(10+i)+1]),i);
                    for(int i =0;i<65536;i++) memory.setMemoryWithoutNotifying(Chars.fromBytes(loaddata[2*(13+i)],loaddata[2*(13+i)+1]),i);
                    localSetMemoryAdapter(memory.getMemory(),0);
                    String[] lastopenedfilenamestr= openfilename.get(1).split("(.*/)([^/].)");
                    String lastopenedfilename= lastopenedfilenamestr[lastopenedfilenamestr.length-1];
                    Log.d("asa",lastopenedfilename);

                }

            }else {
                Toast.makeText(this,"["+sharedPreferences.getString("userid","USERID")+"]フォルダ内のファイルを指定してください。",Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startDataSendTask(Intent data, SharedPreferences sharedPreferences) {
        Intent intent = new Intent(getApplicationContext(),DataSendTask.class);
        List<String> openfilename=data.getData().getPathSegments();
        ArrayList<String> localfile= new ArrayList<String >();
        localfile.add(getString(R.string.server_address));
        localfile.add("21");
        localfile.add(openfilename.get(1).split(":")[1]);
        localfile.add(sharedPreferences.getString("userid","null"));
        localfile.add(sharedPreferences.getString("password","null"));
        localfile.add("true");
        localfile.add(Environment.getExternalStorageDirectory().getPath()+"/"+openfilename.get(1).split(":")[1]);
        intent.putExtra("data",localfile);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String filename=getString(R.string.default_file_name);
        final EditText editView = new EditText(getApplicationContext());
        byte[] bytes = new byte[0];
        Intent intent;
        final SharedPreferences preferences;
        switch(item.getItemId()){
            case R.id.execution_interval:
                final Casl2EditText text = new Casl2EditText(getApplicationContext(),3);
                text.setInputType(InputType.TYPE_CLASS_NUMBER);
                preferences = PreferenceManager.getDefaultSharedPreferences(this);
                text.setText(Integer.toString(preferences.getInt(getString(R.string.intervalkey),1000)));
                new AlertDialog.Builder(this)
                        .setTitle("実行間隔を入力してください。[ms]")
                        .setView(R.layout.input_text_dialog)
                        .setView(text)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String data = text.getText().toString();
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(getString(R.string.intervalkey),Integer.parseInt(data));
                                editor.commit();

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            case R.id.output_initialize:
                OutputBuffer.getInstance().setData("");
                OutputBuffer.getInstance().setDrawObjectArray(new ArrayList<Casl2Figure>());
                break;
            case R.id.action_jump:
                final Casl2EditText memory_position = new Casl2EditText(getApplicationContext(),1);
                memory_position.setTextColor(Color.BLACK);
                new AlertDialog.Builder(ContextDisplayScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle(getString(R.string.jump_dialog_text))
                        //setViewにてビューを設定します。
                        .setView(memory_position)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String position = memory_position.getText().toString();
                                if(position.matches("\\w\\w\\w\\w")) {
                                    listView.setSelection(Integer.parseInt(position, 16) / 4);
                                }else{
                                    Toast.makeText(ContextDisplayScreen.this,"適切な文字列を入力してください",Toast.LENGTH_SHORT).show();
                                }


                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                break;
            case R.id.action_load:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent,5657);
                break;
            case R.id.action_submit:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent,1114);
                break;
            case R.id.action_save:
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if(preferences.contains("LastSavedFileName")){
                    editView.setText(preferences.getString("LastSavedFileName",""));
                }else{
                    editView.setText(filename);
                }
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
                                String save_filename = editView.getText().toString();
                                char[] casl2data;
                                casl2data = Chars.concat(ArrayUtils.add(ArrayUtils.add(register.getGr(),register.getPc()),register.getSp()),register.getFr(),memory.getMemory());
                                byte[]savedata = toBytes(casl2data);
                                FileOutputStream fileOutputStream;

                                try {
                                    SharedPreferences sharedPreferences = PreferenceManager.
                                            getDefaultSharedPreferences(getApplicationContext());
                                    String dirname =Environment.getExternalStorageDirectory().getPath()+
                                            getString(R.string.app_directory_name)+
                                            "/"+ sharedPreferences.getString("userid","null");
                                    File dir = new File(dirname);
                                    boolean hasPermission = (ContextCompat.checkSelfPermission(ContextDisplayScreen.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                                    if (!hasPermission) {
                                        ActivityCompat.requestPermissions(ContextDisplayScreen.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_WRITE_STORAGE);
                                    }
                                    if(!dir.exists()){
                                        dir.mkdirs();
                                    }
                                    File file = new File(dirname,save_filename);
                                    fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(savedata);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("LastSavedFileName",save_filename);
                                    editor.commit();
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
    private View.OnLongClickListener jumpAddress(final ActivityBinaryEditScreenBinding binding, final int id){

        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (id){
                    case 0:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr0.getText()), 16) / 4);
                        break;
                    case 1:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr1.getText()), 16) / 4);
                        break;
                    case 2:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr2.getText()), 16) / 4);
                        break;
                    case 3:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr3.getText()), 16) / 4);
                        break;
                    case 4:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr4.getText()), 16) / 4);
                        break;
                    case 5:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr5.getText()), 16) / 4);
                        break;
                    case 6:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr6.getText()), 16) / 4);
                        break;
                    case 7:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.gr7.getText()), 16) / 4);
                        break;
                    case 8:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.pc.getText()), 16) / 4);
                        break;
                    case 9:
                        listView.setSelection(Integer.parseInt(String.valueOf(binding.sp.getText()), 16) / 4);
                        break;
                }
                return true;
            }
        };
    }
    private View.OnClickListener showWordDialog(final ActivityBinaryEditScreenBinding binding, final int id) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Casl2EditText casl2EditText;
                final InputMethodManager inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(id<10){
                    casl2EditText = new Casl2EditText(ContextDisplayScreen.this,1);
                }else {
                    casl2EditText = new Casl2EditText(ContextDisplayScreen.this,2);
                }
                TextView textview = (TextView) v;
                //Log.d("dbg",textview.getAccessibilityClassName().toString());
                casl2EditText.setText(textview.getText());
                casl2EditText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                            //キーボードを閉じる
                            inputMethodManager.hideSoftInputFromWindow(casl2EditText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
                new AlertDialog.Builder(ContextDisplayScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(R.layout.input_text_dialog)
                        .setTitle("レジスタを編集")
                        //setViewにてビューを設定します。
                        .setView(casl2EditText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //入力した文字をトースト出力する
                                String upperedString = casl2EditText.getText().toString().toUpperCase();
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
        return new ListDisplayTask(this,cs,i);
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

