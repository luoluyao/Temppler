package com.temppler;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;


public class TempplerActivity extends ActionBarActivity {


    private SeekBar valueIn;
    private TextView valueOut;
    private boolean done = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        valueIn = (SeekBar) this.findViewById(R.id.valueIn);
        valueOut = (TextView) this.findViewById(R.id.valueOut);

        valueIn.setOnSeekBarChangeListener(OSBCL);
        valueIn.setProgress(400);
        valueOut.setText("0");

        Thread t = new Thread(new OutDACrunnable());
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void onStop(){
        done = true;
        System.exit(0);
    }



    private SeekBar.OnSeekBarChangeListener OSBCL = new SeekBar.OnSeekBarChangeListener(){
        public void onProgressChanged(SeekBar sb, int progress, boolean u){
            valueOut.setText(progress+" Hz");
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };




    class OutDACrunnable implements Runnable {

        private short[] generatedSnd;
        private int sampleR = 44100;
        private double ph = 0;
        private int amp = 10000;


        public void run() {
            generatedSnd = new short[AudioTrack.getMinBufferSize(sampleR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)];
            System.out.println("Buffer-length: "+ generatedSnd.length);
            for(int i = 0; i < generatedSnd.length; i++){
                generatedSnd[i] = 0;
            }

            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleR,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.length, AudioTrack.MODE_STREAM);

            audioTrack.play();

            while (! done) {
                genTone(valueIn.getProgress()); //;valueIn.getProgress());

                //System.out.println(val);


                audioTrack.write(generatedSnd, 0, generatedSnd.length);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            audioTrack.release();
        }


        private void genTone(float freq) {
            for (int i = 0; i < generatedSnd.length; i++) {
                generatedSnd[i] = (short) (Math.sin(ph) * (double)amp);


                ph += 2*Math.PI*freq/sampleR;
                //ph %= 2*Math.PI;

            }
            // Log.d(MyTag, "genTone: done");
        }
    }
}
