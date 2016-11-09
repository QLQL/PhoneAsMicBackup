package com.qlgc.phoneasmic;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ShortBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static String IP_ADDRESS = "192.168.0.2";//"10.33.17.190";//"131.227.95.234";//"10.64.8.78";
    //private MediaRecorder mRecorder = null;
    private static String mFileName = null;
    private static String mFileNamedelete = null;
    //private static Boolean recordMode = Boolean.FALSE;
    private com.androidplot.xy.XYPlot mySimpleXYPlot;

    public Number[] buffer_plot = new Number[50];
    //public static DatagramSocket socket;
    AudioRecord recorder;
    private TextView textView;
    private EditText myEditText;

    private int sampleRate = 48000;//44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    //private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    //private int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = false;
    private boolean Server_aktiv = true;
    private boolean playMode = true;
    private boolean saveMode = false;

    AudioTrack player;
    private int playBufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, audioFormat);

//    ShortBuffer mSamples; // the samples to play
//    int mNumSamples; // number of samples to play

    public MainActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/QL_Record";
        mFileNamedelete = Environment.getExternalStorageDirectory().getAbsolutePath()+"/OnlyTest";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.finishButton);
        btn.setEnabled(false);

        udpListening(); // started the udp listening thread

        mySimpleXYPlot = (com.androidplot.xy.XYPlot) findViewById(R.id.mySimpleXYPlot);
        textView = (TextView) findViewById(R.id.textView);
        myEditText = (EditText) findViewById(R.id.ipAddress);


        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series

        // same as above
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 200, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                null,                                   // fill color (none)
                new PointLabelFormatter(Color.WHITE));                           // text color

        // add a new series' to the xyplot:
        mySimpleXYPlot.addSeries(series1, series1Format);

        // same as above:
        mySimpleXYPlot.addSeries(series2,
                new LineAndPointFormatter(
                        Color.rgb(0, 0, 200),
                        Color.rgb(0, 0, 100),
                        null,
                        new PointLabelFormatter(Color.WHITE)));

    }


    /** Called when the user clicks the Start button */
    public void startRecording(View view) {
        // Do something in response
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Start recording......");

        /*
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recordMode = Boolean.TRUE;
        mRecorder.start();
        */


        //testStreaming();

        status = true;
        startStreaming();


        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setEnabled(false);
        EditText EditTextField = (EditText) findViewById(R.id.ipAddress);
        EditTextField.setEnabled(false);
        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setEnabled(true);

        //mRecorder.stop();
        //mRecorder.release();
        //mRecorder = null;
    }

    public void finishRecording(View view) {
        // Do something in response
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Finish recording......");

        /*
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        recordMode = Boolean.FALSE;
        */


        status = false;


        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setEnabled(true);
        EditText EditTextField = (EditText) findViewById(R.id.ipAddress);
        EditTextField.setEnabled(true);
        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setEnabled(false);
    }

    /*Exit the Apps*/
    public void exitApp(View view) {
        finish();
        System.exit(0);
    }


    public void startStreaming()
    {

        final Handler handler = new Handler();
        Thread udpSendThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //while(true){
                    // pause the programme for 1ooo milliseconds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    try {

                        // create new UDP socket
                        DatagramSocket socket = new DatagramSocket();
                        Log.d("UDP", "Socket Created");

                        final byte[] buffer = new byte[minBufSize];
                        final short[] shortBuffer = new short[minBufSize/2];
                        Log.d("UDP","Buffer created of size " + minBufSize);

                        // First get the IP address from the text field 10.64.8.78
                        handler.post(new Runnable(){
                                 public void run() {
                                     // IP_ADDRESS = myEditText.getText().toString();
                                     // TODO add some grammar check to the IP_ADDRESS
                                     Log.w("UDP","IP address " + IP_ADDRESS);
                                 }
                            });
                        // get server name

                        InetAddress serverAddr = InetAddress.getByName(IP_ADDRESS);
                        Log.w("UDP", "Connecting "+IP_ADDRESS);

                        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);
                        if(recorder.getState() != AudioRecord.STATE_INITIALIZED){
                            Log.w("VS", "Recorder cannot initialize");
                            return;
                        }
                        Log.w("VS", "Recorder initialized");

                        if (playMode) {
                            player = new AudioTrack(AudioManager.STREAM_MUSIC,
                                    sampleRate,
                                    AudioFormat.CHANNEL_OUT_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    playBufSize,
                                    AudioTrack.MODE_STREAM);
                            player.setPlaybackRate(sampleRate);
                            Log.w("VS", "Player initialized");
                            player.play();
                        }


                        recorder.startRecording();

                        File f = new File(mFileName);
                        FileOutputStream dos = new FileOutputStream(mFileName);


                        while (status) {
                            //new Random().nextBytes(sendData);

                            //try {
                            //    Thread.sleep(10000);
                            //} catch (InterruptedException e) {
                            //    e.printStackTrace();
                            //}


                            // int read = recorder.read(buffer, 0, buffer.length);

                            int read = recorder.read(shortBuffer, 0, shortBuffer.length);
                            byte byte1, byte2;
                            for (int i=0;i<shortBuffer.length;i++) {
                                byte1 = (byte) (shortBuffer[i]&0xFF); // the low byte
                                byte2 = (byte) ((shortBuffer[i]>>8)&0xFF); // the high byte
                                buffer[i*2] = byte1;
                                buffer[i*2+1] = byte2;
                                //short shortrecover = (short) ((byte2<<8)+byte1&0xFF);
//                                dos.writeByte(shortBuffer[i] & 0xFF);
//                                dos.writeByte((shortBuffer[i] >> 8) & 0xFF);
                            }

                            if (saveMode) {
                                if (read > 10)
                                    dos.write(buffer);
                            }



                            if (playMode) {
                                player.write(shortBuffer, 0, read);
                            }

                            //short[] tmpp = new short[16];
                            //int read2 = recorder.read(tmpp, 0, tmpp.length);
                            //Log.w("UDP", "C: Sending: " + Arrays.toString(tmpp));

                            Log.w("UDP", "Read " + Integer.toString(read));
                            Log.w("UDP", "C: read in: " + Arrays.toString(Arrays.copyOfRange(shortBuffer, 0, 50)));

                            // create a UDP packet with data and its destination ip & port
                            DatagramPacket packet = new DatagramPacket(buffer, read*2, serverAddr, 50007);
                            Log.w("UDP", "C: Sending the current frame");
                            Log.w("UDP", "C: Sending: " + Arrays.toString(Arrays.copyOfRange(buffer, 0, 50)));


                            int SampleN = 128;
                            int step = 4;
                            final int[] EngeryBuffer = new int[read/(SampleN*step)];
                            //final int[] EngeryBuffer = new int[100/SampleN];
                            int ii = 0, jj = 0;

                            //byte[] tmp = packet.getData();
                            for(int k = 0; k < SampleN*step*EngeryBuffer.length; k+=step){
                                EngeryBuffer[ii] += Math.abs(shortBuffer[k]);
                                jj++;
                                if(jj==SampleN){
                                    ii++;
                                    jj = 0;
                                }
                            }

                            Log.w("UDP", "C: Current frame energy: " + Arrays.toString(EngeryBuffer));


                            // The energy buffer to be plotted.
                            // TODO use a list and a pointer for this buffer so the shifting is unnecessary, all we need is just to copy the current frame energy to the buffer
                            int shiftN = EngeryBuffer.length;
                            for (int i = 0; i <buffer_plot.length-shiftN; i++) {
                                buffer_plot[i] = buffer_plot[i+shiftN];
                            }
                            int shiftN2 = buffer_plot.length-shiftN;
                            for (int i = shiftN2; i<buffer_plot.length; i++) {
                                buffer_plot[i] = (Number) EngeryBuffer[i-shiftN2];
                            }

                            // Now plot the energy
                            // initialize our XYPlot reference:
                            // mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);

                            //TextView textView = (TextView) findViewById(R.id.textView);
                            //textView.setText("Sending: '" + new String(sendData));
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    textView.setText(Arrays.toString(EngeryBuffer));
//                                }
//                            });

//                            handler.post(new Runnable(){
//                                 public void run() {
//                                     textView.setText(Arrays.toString(EngeryBuffer));
//                                 }
//                            });


                            handler.post(new Runnable(){
                                @Override
                                public void run() {
                                    //textView.setText(Arrays.toString(EngeryBuffer));
                                    textView.setText(Integer.toString(EngeryBuffer[0]));

                                }
                            });

                            handler.post(new Runnable(){
                                @Override
                                public void run() {
                                    //textView.setText("Sending data");
                                    // Create a couple arrays of y-values to plot:
                                    // Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
                                    Number[] series1Numbers = buffer_plot;
                                    XYSeries series1 = new SimpleXYSeries(
                                            Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                                            SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                                            "Series1");                             // Set the display title of the series

                                    // Create a formatter to use for drawing a series using LineAndPointRenderer:
                                    LineAndPointFormatter series1Format = new LineAndPointFormatter(
                                            Color.rgb(0, 200, 0),                   // line color
                                            Color.rgb(0, 100, 0),                   // point color
                                            null,                                   // fill color (none)
                                            new PointLabelFormatter(Color.WHITE));                           // text color

                                    mySimpleXYPlot.clear();
                                    // add a new series' to the xyplot:
                                    //Log.w("UDP", "Now update the plot");
                                    mySimpleXYPlot.addSeries(series1, series1Format);
                                    //Log.w("UDP", "Now update the plot2");
                                    mySimpleXYPlot.setRangeBoundaries(0,25000,BoundaryMode.FIXED);
                                    mySimpleXYPlot.redraw();

                                }
                            });


                            try {
                                // send the UDP packet
                                socket.send(packet);
                            } catch (Exception e) {
                                Log.w("UDP", "C: Sending just failed");
                            }

                        }


                        try {
                            dos.flush();
                            dos.close();
                            Log.d("VS", "IO released");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        socket.close();
                        Log.d("VS","Socket released");
                        if (playMode){
                            player.release();
                            Log.d("VS","Player released");
                        }
                        recorder.stop();
                        recorder.release();
                        Log.d("VS","Recorder released");

                    } catch (Exception e) {
                        Log.w("UDP", "C: Error", e);
                        //e.printStackTrace();
                    }

                //}
            }

        });

        //start the streaming thread
        udpSendThread.start();
    }


    public void testStreaming()
    {

        final Handler handler = new Handler();
        Thread testudpSendThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //while(true){
                // pause the programme for 1ooo milliseconds
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                final byte[] DeleteBuffer = new byte[131072];
                byte byte1, byte2;
                for (int i=0;i<65536;i++) {
                    short tmp = (short) (i-32768);
                    byte1 = (byte) (tmp&0xFF); // the low byte
                    byte2 = (byte) ((tmp>>8)&0xFF); // the high byte
                    DeleteBuffer[i*2] = byte1;
                    DeleteBuffer[i*2+1] = byte2;
//            if (i%1000==0){
//                Log.w("Test",Short.toString(tmp));
//            }
                }

                try {
                    DatagramSocket socket = new DatagramSocket();
                    InetAddress serverAddr = InetAddress.getByName(IP_ADDRESS);

                    byte[] tmpp = new byte[2048];
                    for(int i=0;i<64;i++) {
                        //DatagramPacket packet = new DatagramPacket(DeleteBuffer, 131072, serverAddr, 50007);
                        //new Random().nextBytes(tmpp);
                        tmpp = Arrays.copyOfRange(DeleteBuffer,i*2048,(i+1)*2048);
                        DatagramPacket packet = new DatagramPacket(tmpp, 2048, serverAddr, 50007);
                        Log.w("Test","Packet generated");
                        try {
                            // send the UDP packet
                            socket.send(packet);
                            Log.w("UDP", "C: Packet sent!");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.w("UDP", "C: Sending just failed");
                        }

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                    }
                    socket.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(new File(mFileNamedelete));
                    os.write(DeleteBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        });

        //start the streaming thread
        testudpSendThread.start();
    }


    public void udpListening()
    {

        final Handler handler = new Handler();
        Thread udpListenStartThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // pause the programme for 1ooo milliseconds
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                int port = 12345;
                byte[] message = new byte[8000]; //You must set the incoming message's maximum size

                try {
                    int Frame = 0;
                    InetAddress serverAddr = InetAddress.getByName(IP_ADDRESS);
                    if (serverAddr.isReachable(1000))
                        Log.w("Test", "host is reachable");
                    else
                        Log.w("Test", "host is not reachable");


                    // It is very important that the socket need to be bind, otherwise, it cannot receive any packet
                    DatagramChannel channel = DatagramChannel.open();
                    DatagramSocket ds = channel.socket();// <-- create an unbound socket first
                    ds.setReuseAddress(true);
                    ds.bind(new InetSocketAddress(port)); // <-- now bind it
                    ds.setSoTimeout(2000);

                    DatagramPacket dp = new DatagramPacket(message, message.length);
                    while (Server_aktiv){
                        try {
                            Log.w("UDP", "Try to receive data");
                            ds.receive(dp);
                            //Log.d("Udp","Roger, received");
                            Frame++;
                            Log.d("Udp",String.valueOf(Frame)+" received "+String.valueOf(dp.getLength())+" data");


                            // show the frame number and the length in the screen
                            final int LL = dp.getLength();
                            final int FFrame = Frame;
                            handler.post(new Runnable(){
                                public void run() {
                                    textView.setText(String.valueOf(FFrame)+"-----"+String.valueOf(LL));
                                }
                            });

                        } catch (SocketTimeoutException e) {
                            // resend
                            Log.w("Test","Continue to listen");
                            continue;
                        } catch (IOException e) {
                            Log.e(" UDP ", "error");
                            e.printStackTrace();
                        }
                    }
                    ds.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        });

        //start the listener
        udpListenStartThread.start();
    }



}

