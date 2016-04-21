package comp590proj4.chenchik.comp590proj4;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BeanManager.getInstance().startDiscovery(bdl);
    }

    BeanDiscoveryListener bdl  = new BeanDiscoveryListener() {
        @Override
        public void onBeanDiscovered(Bean bean, int rssi) {
            Log.v("DANILA", "Bean Discovered, its name: " + bean.getDevice().getName() + " its rssi is: " + rssi );
            //b = bean;
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
                        Log.v("Result", "acceleration is: x " + result.x() +" y "+ result.y() +" z "+ result.z());

                    }
                });
            //}
        }

        @Override
        public void onConnectionFailed() {
            Log.v("CONNECT FAILED", "---");
        }

        @Override
        public void onDisconnected() {
            Log.v("CONNECT DISCONNECTED", "---");
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
        }

    };
    public void startLoop(View v){
        keepLooping = true;
        if(b.isConnected()) {
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

    public void stopLoop(View v){
        //t.stop();
        keepLooping = false;
    }

    public void startAccelReading(Bean bean){
        if (accel == null) {
            bean.readAcceleration(new Callback<Acceleration>() {
                @Override
                public void onResult(Acceleration result) {
                    Log.v("Result", "acceleration is: x " + result.x() + " y " + result.y() + " z " + result.z());
                    double[] d = {result.x(), result.y(), result.z()};
                    vals.add(d);
                    accel = result;
                    Date dNow = new Date( );
                    SimpleDateFormat ft = new SimpleDateFormat("ssmmm");
                    dates.add(Integer.parseInt(ft.format(dNow)));


                }
            });
        }

        bean.readAcceleration(new Callback<Acceleration>() {
            @Override
            public void onResult(Acceleration result) {
                Log.v("Result", "acceleration is: x " + result.x() + " y " + result.y() + " z " + result.z());
                double[] d = {result.x(), result.y(), result.z()};
                vals.add(d);
                Log.v("result arr", vals.get(0)[0]+"");
                Date dNow = new Date( );
                SimpleDateFormat ft = new SimpleDateFormat("ssmmm");
                dates.add(Integer.parseInt(ft.format(dNow)));
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
    //function: copies the bytes received from the server int the retr_files folder
    public void createFile(View v) {
        String youFilePath = Environment.getExternalStorageDirectory().toString()+"/lobstartestwithdate.txt";
        Log.v("FILEPATH", youFilePath);
        Log.v("first input", vals.get(0)[0]+"");
        Log.v("first date", dates.get(0)+"");
        File fout = new File(youFilePath);
        FileOutputStream out = null;
        if (vals.size() > 0) {
            try {
                out = new FileOutputStream(fout);
                int c;
                //read in the file byte by byte
                for (int i = 0; i < vals.size(); i++) {
                    out.write((int)vals.get(i)[0]);
                    Log.v("i is:", i+"");
                    Log.v("FIRST VAL IS", "" + (int) vals.get(i)[0]);
                    out.write((int)vals.get(i)[1]);
                    out.write((int)vals.get(i)[2]);
                    out.write(dates.get(i));
                    //out.write((int)vals.get(i));
                }

                if (out != null) {
                    out.close();
//                    dataSocket.close();
                }
            } catch (IOException theExcept) {
                theExcept.printStackTrace();
            }
        }
    }

}
