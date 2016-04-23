package comp590proj4.chenchik.comp590proj4;

import comp590proj4.chenchik.comp590proj4.R;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
//import android.R;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.punchthrough.bean.sdk.message.Acceleration;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.Callback;
import com.punchthrough.bean.sdk.message.ScratchBank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Bean b;
    private ArrayList<Bean> beans = new ArrayList<Bean>();
    private Acceleration accel = null;
    private Timestamp accelTime = null;
    boolean keepLooping = false;
    private ArrayList<double[]> vals = new ArrayList<double[]>();
    private ArrayList<Integer> dates = new ArrayList<Integer>();
    private int activitySerialNum = 1;
    private int positionSerialNum = 1;
    private int serialNum = 1;
    //Button btn = (Button) findViewById(R.id.standing);
    //RadioButton stand = (RadioButton) findViewById(R.id.standingButton);

    private RadioButton[] activities;

    private RadioButton[] positions;
    String fileName = "";
    boolean lobstarConnected = false;

    //HashMap<String, Integer> fileMap = new HashMap<>();

    int[][] fileNamearr = new int[3][6];
    //to find positions in array
    int STANDING = 0;
    int SITTING = 1;
    int WALKING = 2;
    int RUNNING = 3;
    int UPSTAIRS = 4;
    int DOWNSTAIRS = 5;

    int WAIST = 0;
    int WRIST = 1;
    int SHOW = 2;

    String currentActivity = "";

    /*String[] fileNames = {
        "STANDING_WAIST_0",
            "SITTING_WAIST_0",
            "WALKING_WAIST_0",
            "RUNNING_WAIST_0",
            "UPSTAIRS_WAIST_0",
            "DOWNSTAIRS_WAIST_0",
            "STANDING_WRIST_0",
            ""

    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BeanManager.getInstance().startDiscovery(bdl);
        activities = new RadioButton[]{
                (RadioButton) findViewById(R.id.standingButton),
                (RadioButton) findViewById(R.id.sittingButton),
                (RadioButton) findViewById(R.id.walkingButton),
                (RadioButton) findViewById(R.id.runningButton),
                (RadioButton) findViewById(R.id.upstairsButton),
                (RadioButton) findViewById(R.id.downstairsButton)
        };

        positions = new RadioButton[]{
                (RadioButton) findViewById(R.id.waistButton),
                (RadioButton) findViewById(R.id.wristButton),
                (RadioButton) findViewById(R.id.shoeButton)
        };


        //updateFileName();
    }

    BeanDiscoveryListener bdl  = new BeanDiscoveryListener() {
        @Override
        public void onBeanDiscovered(Bean bean, int rssi) {
            Log.v("DANILA", "Bean Discovered, its name: " + bean.getDevice().getName() + " its rssi is: " + rssi );
            //b = bean;
            showPop(bean.getDevice().getName()+" Discovered" + " its rssi is: " + rssi);
            beans.add(bean);
        }

        @Override
        public void onDiscoveryComplete() {
            for(Bean bean : beans){
                if(bean.getDevice().getName().equals("LobstarBean")){
                    b = bean;

                    break;
                }
            }
            if(b != null){
                b.connect(getApplicationContext(), blsnr);
            }
            else{
                Log.v("LOBSTARBEAN", "NOT DISCOVERD");
            }
        }
    };

    BeanListener blsnr = new BeanListener() {
        @Override
        public void onConnected() {
            //for(int i = 0; i < 200; i++){
                b.readAcceleration(new Callback<Acceleration>() {
                    @Override
                    public void onResult(Acceleration result) {
                        Log.v("Result", "acceleration is: x " + result.x() + " y " + result.y() + " z " + result.z());
                        showPop("LobstarBean Connected");
                        TextView t = (TextView) findViewById(R.id.isConnected);
                        t.setText("lobstarbean connected");
                        lobstarConnected = true;


                    }
                });
            //}
        }

        @Override
        public void onConnectionFailed() {
            Log.v("CONNECT FAILED", "---");
            showPop("connection failed");
            TextView t = (TextView) findViewById(R.id.isConnected);
            t.setText("connection failed");
        }

        @Override
        public void onDisconnected() {
            Log.v("CONNECT DISCONNECTED", "---");
            showPop("lobstar disconnected");
            TextView t = (TextView) findViewById(R.id.isConnected);
            t.setText("disconnected");
        }

        @Override
        public void onSerialMessageReceived(byte[] data) {

        }

        @Override
        public void onScratchValueChanged(ScratchBank bank, byte[] value) {

        }

        @Override
        public void onError(BeanError error) {
            Log.v("SOME KIND OF ERROR", "");
            showPop("some kind error");
            TextView t = (TextView) findViewById(R.id.isConnected);
            t.setText("Some kind of error");
        }

    };
    public void startLoop(View v){
        if(lobstarConnected) {
            keepLooping = true;
            if (b.isConnected()) {
                Thread t = new Thread() {
                    public void run() {
                        while (keepLooping) {
                            try {
                                startAccelReading(b);
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }
                };
                t.start();
            }
        }
        else{
            showPop("you are not connected to lobstarBean yet");
        }




    }

    public void stopLoop(View v){
        //t.stop();
        keepLooping = false;
    }

    public void startAccelReading(Bean bean){
        /*if (accel == null) {
            bean.readAcceleration(new Callback<Acceleration>() {
                @Override
                public void onResult(Acceleration result) {
                    Log.v("Result", "acceleration is: x " + result.x() + " y " + result.y() + " z " + result.z());
                    double[] d = {result.x(), result.y(), result.z()};
                    vals.add(d);
                    accel = result;
                    Date dNow = new Date( );
                    SimpleDateFormat ft = new SimpleDateFormat("ssSSS");
                    dates.add(Integer.parseInt(ft.format(dNow)));


                }
            });
        }*/

        bean.readAcceleration(new Callback<Acceleration>() {
            @Override
            public void onResult(Acceleration result) {
                Log.v("Result", "acceleration is: x " + result.x() + " y " + result.y() + " z " + result.z());
                double[] d = {result.x(), result.y(), result.z()};
                vals.add(d);
                Log.v("result arr", vals.get(0)[0] + "");
                Date dNow = new Date( );
                SimpleDateFormat ft = new SimpleDateFormat("ssSSS");
                dates.add(Integer.parseInt(ft.format(dNow)));
                TextView seconds = (TextView) findViewById(R.id.timerText);
                String sec = new SimpleDateFormat("ss").toString();
                String ms = new SimpleDateFormat("SSS").toString();
                seconds.setText(dNow.toString());
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

    public static void writeFile1(View v) throws IOException {
        File fout = new File("out.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (int i = 0; i < 10; i++) {
            bw.write("something");
            bw.newLine();
        }

        bw.close();
    }

    public void checkActivity(View v){
        RadioButton rb = (RadioButton) findViewById(v.getId());
        for(int i = 0; i < activities.length; i++){
            if(rb.getId() != activities[i].getId()){
                activities[i].setChecked(false);
            }
            else{
                activities[i].setChecked(true);
                activitySerialNum = i;
                //updateSerialNum(i + 1, positionSerialNum);
                //updateFileName();
            }
        }

    }
    public void checkPosition(View v){
        RadioButton rb = (RadioButton) findViewById(v.getId());
        for(int i = 0; i < positions.length; i++){
            if(rb.getId() != positions[i].getId()){
                positions[i].setChecked(false);
            }
            else{
                positions[i].setChecked(true);
                positionSerialNum = i;
                //updateSerialNum(activitySerialNum, i + 1);
                //updateFileName();
            }
        }
    }
    public void updateSerialNum(int act, int pos){
        serialNum = act*pos;
    }

    public void updateFileName(){
        //TextView t = (TextView) findViewById(R.id.fileName);
        String activity = activities[activitySerialNum].getText().toString();
        String position = positions[positionSerialNum].getText().toString();
        fileNamearr[positionSerialNum][activitySerialNum]++;
        int fileNum = fileNamearr[positionSerialNum][activitySerialNum];
        fileName = activity+ "_" + position+ "_" + fileNum + ".txt";
        currentActivity = activity;

        //t.setText("/"+currentActivity+"/"+fileName);


    }

    //public String getFileName(){

    //}
    //function: copies the bytes received from the server int the retr_files folder
    public void createFile(View v) {
        updateFileName();
        //String youFilePath = Environment.getExternalStorageDirectory().toString()+"/"+fileName;

        String youFilePath = Environment.getExternalStorageDirectory().toString()+"/"+currentActivity+"/"+fileName;
        TextView tv = (TextView) findViewById(R.id.lastFileMadeText);
        tv.setText(fileName);
        Log.v("FILEPATH", youFilePath);
        Log.v("first input", vals.get(0)[0]+"");
        Log.v("first date", dates.get(0)+"");
        File fout = new File(youFilePath);
        //FileOutputStream out = null;
        FileWriter outWriter = null;
        if (vals.size() > 0) {
            try {
          //      out = new FileOutputStream(fout);
                outWriter = new FileWriter(fout);
                int c;
                //read in the file byte by byteg
                for (int i = 0; i < vals.size(); i++) {
            //        out.write((int)vals.get(i)[0]);
                    String s = dates.get(i)+ " " + vals.get(i)[0] + " " + vals.get(i)[1] + " " + vals.get(i)[2] + "\n";
                    outWriter.write(s);
                    Log.v("i is:", i + "");
                    Log.v("FIRST VAL IS", "" + (int) vals.get(i)[0]);
                    Log.v("dates", dates.get(i)+"");
            //        out.write((int) vals.get(i)[1]);
            //        out.write((int) vals.get(i)[2]);
            //        out.write(dates.get(i));
                    outWriter.flush();

                    //out.write((int)vals.get(i));
                }

                dates.clear();
                vals.clear();

                if (outWriter != null) {
                    outWriter.close();

//                    dataSocket.close();
                }
            } catch (IOException theExcept) {
                theExcept.printStackTrace();
            }
        }
    }

    public void showPop(String s){
        //context is a the window object of the application, we want to put something on top of the current window object
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        //toast is an alert/popup
        Toast toast = Toast.makeText(context, s, duration);
        toast.show();
    }


}
