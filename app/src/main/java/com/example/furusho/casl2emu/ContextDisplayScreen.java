package com.example.furusho.casl2emu;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.furusho.casl2emu.databinding.ActivityBinaryEditScreenBinding;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icepick.Icepick;

import static android.R.layout.simple_list_item_1;

public class ContextDisplayScreen extends BaseActivity implements LoaderCallbacks{

    InputText it;
    ListView listView;
    Casl2Memory memory;
    Casl2Register register;
    Casl2Emulator emulator;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> stringArrayList;


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
                .setTitle("テキスト入力ダイアログ: "+String.format(Locale.US,"%04X",position*4 & 0xFFFF)+" - "+String.format(Locale.US,"%04X",position*4+3& 0xFFFF))
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //入力した文字をトースト出力する
                        String upperedString =editView.getText().toString().toUpperCase();
                        Pattern pattern = Pattern.compile(getString(R.string.memory_row_pattern));
                        Matcher matcher = pattern.matcher(upperedString);
                        if (matcher.matches()) {
                            Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
                            char[] chars = getHexChars(upperedString," ");
                            memory.setMemoryArray(chars, position*4);
                            //stringArrayList.set(position,String.format(Locale.US ,"%02X %02X %02X %02X %02X %02X %02X %02X",
                            //        chars[0] & 0xFFFF, chars[1] & 0xFFFF, chars[2] & 0xFFFF, chars[3] &
                            //                0xFFFF, chars[4] & 0xFFFF, chars[5] & 0xFFFF, chars[6] & 0xFFFF, chars[7] & 0xFFFF));
                            stringArrayList.remove(position);
                            //arrayAdapter.remove("00 00 00 00 00 00 00 00");
                            arrayAdapter.insert(String.format(Locale.US ,"%04X %04X %04X %04X",
                                    chars[0] & 0xFFFF, chars[1] & 0xFFFF, chars[2] & 0xFFFF, chars[3] & 0xFFFF),position);
                            //arrayAdapter.addAll(stringArrayList);
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

    private String getWord(int offset, CharSequence line) {
        String ret="";
        if((offset+1)%3==1){//一桁目ならoffset-1をとる
            ret= String.valueOf(line.charAt(offset))+String.valueOf(line.charAt(offset+1));
        }else if((offset+1)%3==2){
            ret= String.valueOf(line.charAt(offset-1))+String.valueOf(line.charAt(offset));

        }
        return ret;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_edit_screen);
        Icepick.restoreInstanceState(this,savedInstanceState);

        memory = Casl2Memory.getInstance();
        register = Casl2Register.getInstance();
        emulator= Casl2Emulator.getInstance();

        final ActivityBinaryEditScreenBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_binary_edit_screen);
        char[] test = new char[]{0x000F,0,9,8,78,7,5,0xabcd};

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
        String initialString = "5101 0002 0001 0000"+" "+getString(R.string.short_zerofill);
        char[]tmp = getHexChars(initialString," ");
        memory.setMemory(tmp);
        final char[] a = memory.getMemory();


        listView = (ListView)findViewById(R.id.memory_list);
        localSetMemoryAdapter(a,0);
        listView.setOnItemClickListener(showTextEditDialog);

        binding.runbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emulator.run();
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

    }
    private void localSetMemoryAdapter(char[] chars,int count) {
        startListTask(chars,count);
    }


    public AdapterView.OnItemClickListener getShowTextEditDialog(){
        return showTextEditDialog;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this,outState);
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
                        .setTitle("テキスト入力ダイアログ")
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
                                    Toast.makeText(ContextDisplayScreen.this, upperedString, Toast.LENGTH_LONG).show();
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

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader loader, Object data) {

        if(arrayAdapter ==null) {
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

class ContextEditDialog extends AlertDialog{

    protected ContextEditDialog(final Context context, final HexEditText hexEditTextText) {
        super(context);
    }
        /*
        this.Builder(getOwnerActivity());
        this.setIcon(android.R.drawable.ic_dialog_info);
        this.setTitle("テキスト入力ダイアログ");
            //setViewにてビューを設定します。
        this.setView(hexEditTextText);
        this.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //入力した文字をトースト出力する
                String upperedString =hexEditTextText.getText().toString().toUpperCase();
                Pattern pattern = Pattern.compile(context.getString(R.string.memory_row_pattern));
                Matcher matcher = pattern.matcher(upperedString);
                if (matcher.matches()) {
                    hexEditTextText.setText(upperedString);
                }else {
                }

            }
        });
        this.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .show();
    }
*/
}
