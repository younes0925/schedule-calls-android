package com.example.treesa.autocallandendcall;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            callAndEndCall((String) msg.obj);

        }
    };
    AlarmBroadcastReceiver_ receiver;
    PowerManager.WakeLock mWl;
    PowerManager mPm;
    private View.OnClickListener clickListener;
    private Button add;
    private Button time;
    private Button start;
    private Button callingTime;
    private Button period;
    private RecyclerView recyclerView;
    private PhonesAdapter adapter;
    private List<Phones> list;
    private AlarmManager alarm;
    PendingIntent pendingIntent;
    private Button cancel;
    Switch logcall, call24x7, dialImmediately;
    PhoneListViewModel phoneListViewModel;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        phoneListViewModel = ViewModelProviders.of(this).get(PhoneListViewModel.class);

        start = findViewById(R.id.btn_phone2);
        add = findViewById(R.id.add);
        time= findViewById(R.id.time);
        cancel = findViewById(R.id.cancel);
        recyclerView = findViewById(R.id.recycle_view);
        period = findViewById(R.id.sleep);
        callingTime = findViewById(R.id.calling_time);
        logcall = findViewById(R.id.LogCalls);
        call24x7 = findViewById(R.id.Call24x7);
        dialImmediately = findViewById(R.id.DialImmediately);
        prepereSwitches();

        mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWl = mPm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myservice");
        mWl.acquire(5000);

        adapter = new PhonesAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // observe for notes data changes
        phoneListViewModel.getAllNotes().observe(this, new Observer<List<Phones>>() {
            @Override
            public void onChanged(@Nullable List<Phones> phones) {
                //add notes to adapter
                adapter.addPhones(phones);
                list = phones ;
            }
        });

        clickListener = (View v) -> {
            switch (v.getId()) {
                case R.id.cancel:
                    finish();
                    System.exit(0);
                    break;
                case R.id.time:
                    AlertDialog.Builder builder4 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater4 = getLayoutInflater();
                    View view1 = inflater4.inflate(R.layout.from_to, null);
                    builder4.setView(view1);
                    builder4.setPositiveButton("determine", (dialog, which) -> {
                        EditText from = view1.findViewById(R.id.from);
                        EditText to = view1.findViewById(R.id.to);
                        String inputNumberFrom = from.getText().toString();
                        String inputNumberTo = to.getText().toString();
                        DateFormat sdf = new SimpleDateFormat("hh:mm");
                        Date fromTime = null;
                        Date toTime= null;;
                        try {
                            fromTime = sdf.parse(inputNumberFrom);
                            toTime = sdf.parse(inputNumberTo);

                            if(! inputNumberFrom.matches("(?:[0-1][0-9]|2[0-4]):[0-5]\\d")){
                                Toast.makeText(this, " The time you entered was not valid", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(! inputNumberTo.matches("(?:[0-1][0-9]|2[0-4]):[0-5]\\d")){
                                Toast.makeText(this, " The time you entered was not valid", Toast.LENGTH_SHORT).show();
                                return;
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (fromTime.after(toTime) && toTime.before(fromTime)) {
                            Toast.makeText(this, "Out of range", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SharedPreferences sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("from", inputNumberFrom);
                        edit.putString("to", inputNumberTo);

                        edit.commit();
                        Toast.makeText(MainActivity.this, "we start calling on " + inputNumberFrom + " we end calling on "+inputNumberTo , Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    });
                    builder4.create().show();
                    break;

                case R.id.add:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View inflate = inflater.inflate(R.layout.dialog_list_of_contact, null);
                    builder.setView(inflate);
                    builder.setPositiveButton("determine", (dialog, which) -> {
                        EditText input = inflate.findViewById(R.id.numberWithSms);
                        String inputNumber = input.getText().toString();


                        setContactWithSMSInDB(inputNumber);
                        input = inflate.findViewById(R.id.numberWithOutSms);
                        inputNumber = input.getText().toString();
                        setContactWithoutSmsInDB(inputNumber);


                        Toast.makeText(MainActivity.this, "Added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                    builder.create().show();
                    break;
                case R.id.sleep:
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater2 = getLayoutInflater();
                    View view = inflater2.inflate(R.layout.dialog_sleep, null);
                    builder2.setView(view);
                    builder2.setPositiveButton("determine", (dialog, which) -> {
                        EditText input = view.findViewById(R.id.input);
                        String inputNumber = input.getText().toString();
                        int period2 = Integer.valueOf(inputNumber);
                        if (period2 > 5 || period2 < 60) {
                            Toast.makeText(this, "Out of range", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SharedPreferences sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putInt("sleep", Integer.valueOf(inputNumber));
                        edit.commit();
                        Toast.makeText(MainActivity.this, period2 + "Hour/time, restart after the period is modified", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    });
                    builder2.create().show();
                    break;
                case R.id.calling_time:
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater3 = getLayoutInflater();
                    View view2 = inflater3.inflate(R.layout.dialog_callling_time, null);
                    builder3.setView(view2);
                    builder3.setPositiveButton("determine", (dialog, which) -> {
                        EditText input = view2.findViewById(R.id.input);
                        String inputNumber = input.getText().toString();
                        int calling_time = Integer.valueOf(inputNumber);
                        if (calling_time > 60 || calling_time < 5) {
                            Toast.makeText(this, "Out of range", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SharedPreferences sp1 = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp1.edit();
                        edit.putInt("calling_time", Integer.valueOf(inputNumber));
                        edit.commit();
                        Toast.makeText(MainActivity.this, calling_time + "Seconds/time, restart after modification", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    });
                    builder3.create().show();
                    break;
                case R.id.btn_phone2:
                    SharedPreferences sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                    int DialImmediately = sp.getInt("DialImmediately", 0);
                    int Call24x7 = sp.getInt("Call24x7", 0);

                    if(DialImmediately>0 && Call24x7>0 ){
                        callImedialtyWithRepeat();
                    }
                    else if(DialImmediately>0 && Call24x7==0 ){
                        callImedialtywithOutRepeat();
                    }
                    else if(DialImmediately==0 && Call24x7==0){
                        callAfterwithoutRepeat();
                    }
                    else  {
                        callAfterwithRepeat();
                    }

                    break;
                default:
            }
        };


        add.setOnClickListener(clickListener);
        start.setOnClickListener(clickListener);
        period.setOnClickListener(clickListener);
        callingTime.setOnClickListener(clickListener);
        cancel.setOnClickListener(clickListener);
        time.setOnClickListener(clickListener);
        logcall.setOnCheckedChangeListener(this);;
        call24x7.setOnCheckedChangeListener(this);
        dialImmediately.setOnCheckedChangeListener(this);

    }

    public void deleteItem(int position){
        Phones phones = list.get(position);
        phoneListViewModel.deleteNote(phones);
        list.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(MainActivity.this, "successfully deleted", Toast.LENGTH_SHORT).show();

    }
    public void setContactWithoutSmsInDB(String contacts){
        String[] splited = contacts.split("/+");
        Date createdAt ;
        for(int i=0;i<splited.length;i++){
            if(splited[i].equals("/") || splited[i].equals("") ||  splited[i].equals(" ")){continue;}
            Phones phones = new Phones();
            phones.setNumber(splited[i]);
            phones.setWithsms(false+"");
            phoneListViewModel.addNote(phones);


        }

        adapter.notifyDataSetChanged();
    }

    public void callImedialtyWithRepeat(){

        Toast.makeText(MainActivity.this, "start working Imedialty with daily repeat ", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> {
            new Clock(list).start();


        }, 1000 * 5);

    }
    public void callImedialtywithOutRepeat(){

        Toast.makeText(MainActivity.this, "we start   Imedialty without repeat", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> {
            new Clock(list).start();


        }, 1000 * 5);

    }


    public void callAfterwithRepeat(){

        SharedPreferences sp1 = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
        String from = sp1.getString("from", "19:00");
        String to = sp1.getString("to", "22:00");

        DateFormat sdf = new SimpleDateFormat("hh:mm");
        Date fromTime = null;
        Date toTime= null;;

        try {
            fromTime = sdf.parse(from);
            toTime = sdf.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, " we start  on "+from+ " to "+to+" with repeat", Toast.LENGTH_SHORT).show();


        Date finalFromTime1 = fromTime;
        new Handler().postDelayed(() -> {
            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent();
            intent.setAction("action.CALL_EVERY_PHONE_NUMBER");
            pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                    100, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // rtc Wake up cpu from 1970
            Calendar timeOff9 = Calendar.getInstance();
            timeOff9.set(Calendar.HOUR_OF_DAY, finalFromTime1.getHours());
            timeOff9.set(Calendar.MINUTE, finalFromTime1.getMinutes());
            timeOff9.set(Calendar.SECOND, 0);

            alarm.set(AlarmManager.RTC_WAKEUP, timeOff9.getTimeInMillis(),pendingIntent);

            alarm.setRepeating(AlarmManager.RTC_WAKEUP,
                    timeOff9.getTimeInMillis(),AlarmManager.INTERVAL_DAY , pendingIntent);

            receiver = new AlarmBroadcastReceiver_();
            registerReceiver(receiver, new IntentFilter("action.CALL_EVERY_PHONE_NUMBER"));

        }, 1000 * fromTime.getSeconds());

    }

    public void callAfterwithoutRepeat()  {

        SharedPreferences sp1 = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
        String from = sp1.getString("from", "19:00");
        String to = sp1.getString("to", "22:00");

        DateFormat sdf = new SimpleDateFormat("hh:mm");
        Date fromTime = null;
        Date toTime= null;;

        try {
            fromTime = sdf.parse(from);
            toTime = sdf.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, " we start  on "+from+ " to "+to+" without repeat", Toast.LENGTH_SHORT).show();


        Date finalFromTime1 = fromTime;
        new Handler().postDelayed(() -> {
            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent();
            intent.setAction("action.CALL_EVERY_PHONE_NUMBER");
            pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                    100, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // rtc Wake up cpu from 1970
            Calendar timeOff9 = Calendar.getInstance();
            timeOff9.set(Calendar.HOUR_OF_DAY, finalFromTime1.getHours());
            timeOff9.set(Calendar.MINUTE, finalFromTime1.getMinutes());
            timeOff9.set(Calendar.SECOND, 0);

            alarm.set(AlarmManager.RTC_WAKEUP, timeOff9.getTimeInMillis(),pendingIntent);

            receiver = new AlarmBroadcastReceiver_();
            registerReceiver(receiver, new IntentFilter("action.CALL_EVERY_PHONE_NUMBER"));


        }, 1000 * 5);
    }


    public PhoneListViewModel getmPhoneListViewModel() {
        return phoneListViewModel;
    }

    public void setContactWithSMSInDB(String contactWithSms){
        String[] splited = contactWithSms.split("/+");
        Date createdAt ;
        for(int i=0;i<splited.length;i++){
            if(splited[i].equals("/") || splited[i].equals("") ||  splited[i].equals(" ")){continue;}
            Phones phones = new Phones();
            phones.setNumber(splited[i]);
            phones.setWithsms(true+"");
            phoneListViewModel.addNote(phones);


            // sendMessage(new Contact(splited[0], createdAt ,true ,false ,false))
        }

        adapter.notifyDataSetChanged();
    }



    public void prepereSwitches() {
        SharedPreferences sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
        int DialImmediately = sp.getInt("DialImmediately", 0);
        int Call24x7 = sp.getInt("Call24x7", 0);
        int Logcall = sp.getInt("Logcall", 0);

        logcall.setChecked(Logcall > 0 ? true : false);
        call24x7.setChecked(Call24x7 > 0 ? true : false);
        dialImmediately.setChecked(DialImmediately > 0 ? true : false);

    }

    public void callAndEndCall(String phone) {


        SharedPreferences sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
        int calling_time = sp.getInt("calling_time", 5);
        Toast.makeText(MainActivity.this, "calling_time " + calling_time, Toast.LENGTH_SHORT).show();


        try {
            // Start making calls directly
            Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
            Toast.makeText(MainActivity.this, "dial number！", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {


                Log.e("MainActivity", "Ready to hang up --- " + phone);
                try {
                    // Automatically hang up after 5 seconds delay
                    // First get the TelephonyManager
                    TelephonyManager telMag = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    Class<TelephonyManager> c = TelephonyManager.class;

                    // Then go to the private method getITelephony in the TelephonyManager to get the ITelephony object.对象
                    Method mthEndCall = null;

                    mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
                    //Allow access to private methods
                    mthEndCall.setAccessible(true);
                    final Object obj = mthEndCall.invoke(telMag, (Object[]) null);

                    // Then use the ITelephony object to reflect the endCall method inside, hang up the phone
                    Method mt = obj.getClass().getMethod("endCall");
                    //Allow access to private methods
                    mt.setAccessible(true);
                    mt.invoke(obj);
                    Toast.makeText(MainActivity.this, "hang up the phone！", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, (calling_time+5) * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWl.isHeld()) {
            mWl.release();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences sp ;
        SharedPreferences.Editor edit ;
        switch(buttonView.getId()){
            case R.id.LogCalls:
                sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                edit = sp.edit();
                edit.putInt("Logcall", Integer.valueOf(isChecked == true ? 1 : 0));
                edit.commit();
                Toast.makeText(MainActivity.this, "Log Calls option modified", Toast.LENGTH_LONG).show();
                break;

            case R.id.Call24x7:
                sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                edit = sp.edit();
                edit.putInt("Call24x7", Integer.valueOf(isChecked == true ? 1 : 0));
                edit.commit();
                Toast.makeText(MainActivity.this, "Call 24x7 option modified", Toast.LENGTH_LONG).show();
                break;
            case R.id.DialImmediately:
                sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
                edit = sp.edit();
                edit.putInt("DialImmediately", Integer.valueOf(isChecked == true ? 1 : 0));
                edit.commit();
                Toast.makeText(MainActivity.this, "Dial Immediately option modified", Toast.LENGTH_LONG).show();
                break;
            default:
    }
}


    class AlarmBroadcastReceiver_ extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("AlarmBroadcastReceiver_","onReceive Start the call thread ;)");

            new Clock(list).start();


        }
    }

    class Clock extends Thread {
        Date fixeTime = new Date();
        List<Phones> phones;

        public Clock(List<Phones> phones) {
            this.phones = phones;
        }

        @Override
        public void run() {
            SharedPreferences sp = getSharedPreferences("auto_call", Context.MODE_PRIVATE);
            int sleep_ = sp.getInt("sleep", 20);
            int calling_time = sp.getInt("calling_time", 5);

            for (Phones phone : phones) {
                Date currTime = new Date();

                    Message obtain = Message.obtain();
                    obtain.obj = phone.getNumber();
                    handler.sendMessage(obtain);
                    Log.i("cur_", ";)" + getSecond(System.currentTimeMillis()));

                    SystemClock.sleep((sleep_) * 1000 );

            }
        }
    }

    public int getSecond(long milliseconds){
        return  (int) (milliseconds / 1000) % 60 ;
    }
}

/*                if (fixeTime.after(currTime)) {
                    Message obtain = Message.obtain();
                    obtain.obj = phone.getNumber();
                    handler.sendMessage(obtain);
                    Log.i("cur_", ";)" + getSecond(System.currentTimeMillis()));

                    SystemClock.sleep(20 * 1000);
                }
                */