package com.example.pitepmerature;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;

import com.example.pitepmerature.DataHolders.MusicNotes;
import com.example.pitepmerature.Drivers.Graphics;
import com.example.pitepmerature.Drivers.LedControl;
import com.example.pitepmerature.WebServer.HttpdServer;
import com.example.pitepmerature.font.CodePage437;
import com.example.pitepmerature.font.Font;
import com.google.android.things.contrib.driver.bmx280.Bme280;
import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.ssd1306.BitmapHelper;
import com.google.android.things.contrib.driver.ssd1306.Ssd1306;
import com.google.android.things.contrib.driver.tm1637.NumericDisplay;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final int ADDRESS = 0x76;
    private static final String DISPLAY_TEMP_CLOCK = "BCM24";
    private static final String DISPLAY_TEMP_DATA = "BCM23";
    private static final String DISPLAY_PRESS_CLOCK = "BCM6";
    private static final String DISPLAY_PRESS_DATA = "BCM5";
    private static final String SPEAKER_PWM_PIN = "PWM1";
    private static final String BUTTON_INPUT = "BCM17";
    public static final float DISPLAY_BRIGHTNESS = 1.0f;

    private static final long PLAYBACK_NOTE_DELAY = 160L;

    private static final float BAROMETER_RANGE_SUNNY = 1013.0f;
    private static final float BAROMETER_RANGE_RAINY = 1110.f;

    private NumericDisplay mTempDisplay;
    private NumericDisplay mPressDisplay;
    private Speaker mSpeaker;
    private Ssd1306 mScreen;
    private Bme280 bmxDriver;
    private ButtonInputDriver mButton;

    private static final int HANDLER_MSG_SHOW = 1;
    private static final int HANDLER_MSG_STOP = 2;
    private static final int FRAME_DELAY_MS = 125;

    private LedControl ledControl;

    private int index;
    private final HandlerThread handlerThread = new HandlerThread("FrameThread");
    private Handler handler;


    private float mLastTemperature;
    private float mLastPressure;
    private float mLastHumidity;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private HttpdServer httpdserver;

    private Bitmap mBitmap;
    private int index3 = 0;
    DateFormat dateFormat;
    DateFormat timeFormat;
    long startTime;
    final String DEGREE  = "\u00b0";
    int startTicker = 0;
    String text = "Loading";

	private final PeripheralManagerService managerService = new PeripheralManagerService();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);





        startTime = System.currentTimeMillis();


        dateFormat = new SimpleDateFormat("dd/MM/YYYY");
        timeFormat = new SimpleDateFormat("HH:mm:ss");


        try {
            mScreen = new Ssd1306(managerService.getI2cBusList().get(0));
            Log.d(TAG, "OLED connected and cleared");
            mScreen.clearPixels();
            mScreen.show();


            mButton = new ButtonInputDriver(BUTTON_INPUT,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_A // the keycode to send
            );
            mButton.register();
            Log.d(TAG, "Button is registerd:");

            httpdserver = new HttpdServer();
            httpdserver.start();


        } catch (IOException e) {
            e.printStackTrace();
        }




    //Sensor readouts and OLED updates are kept on same thread for IIC bus stability
        //Speaker player is on separate thread since there is no link to other data

        initSpeaker();

        initLedMatrix();

        mHandlerThread = new HandlerThread("speaker-playback");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(mSpeakerRunnable);

        initNumerical();

		printDeviceId();

        initBME280();

		readSample();

	}


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Any Button Pressed");
        if (keyCode == KeyEvent.KEYCODE_A) {
            Log.d(TAG, "Button Pressed");
            return true; // indicate we handled the event
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "Button Released");
        if (keyCode == KeyEvent.KEYCODE_A) {
            Log.d(TAG, "Button Released");
            return true; // indicate we handled the event
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onStart() {
        super.onStart();
        handler.sendEmptyMessage(HANDLER_MSG_SHOW);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.sendEmptyMessage(HANDLER_MSG_STOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mScreen.clearPixels();
            mScreen.close();
            bmxDriver.close();
            mPressDisplay.clear();
            mPressDisplay.close();
            mTempDisplay.clear();
            mTempDisplay.close();
            handlerThread.quitSafely();

            ledControl.close();

            mSpeaker.stop();
            mSpeaker.close();
            mSpeaker = null;
            mButton.unregister();
            mButton.close();
            httpdserver.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            handler = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mSpeakerRunnable);
            mHandlerThread.quitSafely();
        }
    }


    private void initLedMatrix(){

        try {
            ledControl = new LedControl("SPI0.0");
            //ledControl.setIntensity(0);
            ledControl.setIntensity(1);
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.smiley);
            ledControl.draw(bmp);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LED matrix", e);
        }



       /* handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != HANDLER_MSG_SHOW) {
                    return;
                }

                try {
                    byte[] frame = Invaders.FRAMES[index];
                    for (int i = 0; i < frame.length; i++) {
                        ledControl.setRow(i, frame[i]);
                    }

                    index = (index + 1) % Invaders.FRAMES.length;
                    handler.sendEmptyMessageDelayed(HANDLER_MSG_SHOW, FRAME_DELAY_MS);
                } catch (IOException e) {
                    Log.e(TAG, "Error displaying frame", e);
                }
            }
        };*/

    }

    private void initSpeaker(){

        try {
            mSpeaker = new Speaker(SPEAKER_PWM_PIN);
            Log.d(TAG, "Speaker init");
            mSpeaker.stop(); // in case the PWM pin was enabled already
        } catch (IOException e) {
            Log.e(TAG, "Error initializing speaker");
            return; // don't initilize the handler
        }



    }

    private void initNumerical(){
        //Init the LED numeric displays

        try {
            mTempDisplay = new NumericDisplay(
                    DISPLAY_TEMP_DATA,
                    DISPLAY_TEMP_CLOCK);
            mTempDisplay.setBrightness(DISPLAY_BRIGHTNESS);
            mTempDisplay.setColonEnabled(true);


            mPressDisplay = new NumericDisplay(
                    DISPLAY_PRESS_DATA,
                    DISPLAY_PRESS_CLOCK);
            mPressDisplay.setBrightness(DISPLAY_BRIGHTNESS);
            mPressDisplay.setColonEnabled(false);



        } catch (IOException e) {
            Log.e(TAG, "Error on Numeric display");
        }


    }

	private void printDeviceId() {
		List<String> deviceList = managerService.getI2cBusList();
		if (deviceList.isEmpty()) {
			Log.i(TAG, "No I2C bus available on this device.");
		} else {
			Log.i(TAG, "List of available devices: " + deviceList);
		}
		I2cDevice device = null;
		try {
			device = managerService.openI2cDevice(deviceList.get(0), ADDRESS);

            if(Integer.toHexString(device.readRegByte(0xD0)).equals("60")){
                Log.d(TAG, "Device ID byte: 0x60 Sensor is BME280");
            }
            if(Integer.toHexString(device.readRegByte(0xD0)).equals("58")){
                Log.d(TAG, "Device ID byte: 0x58 Sensor is BMP280");
            }
			Log.d(TAG, "Device ID byte: 0x" + Integer.toHexString(device.readRegByte(0xD0)));
		} catch (IOException|RuntimeException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			try {
				device.close();
			} catch (Exception ex) {
				Log.d(TAG, "Error closing device");
			}
		}
	}


	private void initBME280() {
        try {
            bmxDriver = new Bme280(managerService.openI2cDevice(managerService.getI2cBusList().get(0), ADDRESS));
            bmxDriver.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            bmxDriver.setPressureOversampling(Bmx280.OVERSAMPLING_1X);
            bmxDriver.setHumidityOversampling(Bmx280.OVERSAMPLING_1X);
            bmxDriver.setMode(Bme280.MODE_NORMAL);
        } catch (IOException e) {
            Log.e(TAG, "Error during IO", e);
            // error reading temperature
        }
    }


	private void readSample() {

            while(true) {
                try {
                    mLastTemperature = bmxDriver.readTemperature();
                    mLastPressure = bmxDriver.readPressure();
                    mLastHumidity = bmxDriver.readHumidity();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateNumericDisplays();
                httpdserver.setTemp(mLastTemperature/100.0f);
                httpdserver.setPress(mLastPressure);

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e){

                }
            }


    }

    private void updateNumericDisplays(){

        mLastTemperature = mLastTemperature*100;

        try {
            mTempDisplay.display(String.format("%4d", (int)mLastTemperature));
            mPressDisplay.display(String.format("%4d", (int)mLastPressure));

            drawWeatherToOLED();
            mScreen.show();

        } catch (IOException e) {
            Log.e(TAG, "Error setting numeric displays");
        }


    }

    private void drawWeatherToOLED() {

        Date date = new Date();
        Font font = new CodePage437();


        int height = 64;
        int width = 128;
        TextPaint paint = new TextPaint();
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
        Bitmap textAsBitmap = Bitmap.createBitmap(width,height,ARGB_8888);
        Canvas canvas = new Canvas(textAsBitmap);


        mScreen.clearPixels();

        if((startTime + 5000) < System.currentTimeMillis()){

            if (mLastPressure > BAROMETER_RANGE_SUNNY) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sunny_128_64);
            } else if (mLastPressure < BAROMETER_RANGE_RAINY) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rainy_128_64);
            } else {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cloud_128_64);
            }


            //height = 42;
            paint.setTextSize(21f);
            canvas.drawText(String.format("%.1f"+ DEGREE + "C", (mLastTemperature/100)), 0, 21, paint);
            //BitmapHelper.setBmpData(mScreen, 0, 0, textAsBitmap, true);


            paint.setTypeface(Typeface.create("Arial", Typeface.NORMAL));
            paint.setTextSize(12f);
            canvas.drawText(String.format("%.1fhPa", mLastPressure), 0,33 /*0.5f * height*/, paint);


            //Graphics.drawTextNew(mScreen,0,1,font,String.format("%.1f*C", (mLastTemperature/100)), 2.0f);

            //Graphics.drawTextNew(mScreen,0,22, font, String.format("%4dhPa", Math.round(mLastPressure)),1.3f);

            Graphics.line(mScreen,0,37,61,37);
            Graphics.line(mScreen,0,36,61,36);

            date.setTime(System.currentTimeMillis() + 7200000L);

            paint.setTextSize(13f);
            paint.setTypeface(Typeface.create("Arial", Typeface.BOLD));

            canvas.drawText(timeFormat.format(date), 0,51 /*0.5f * height*/, paint);

            paint.setTextSize(12f);
            paint.setTypeface(Typeface.create("Arial", Typeface.NORMAL));
            canvas.drawText(dateFormat.format(date), 0,64 /*0.5f * height*/, paint);
            //Graphics.drawTextNew(mScreen,0,41,font, timeFormat.format(date), 1.3f);
            //Graphics.drawTextNew(mScreen,0,52,font, dateFormat.format(date), 1.1f);

            BitmapHelper.setBmpData(mScreen, 0, 0, textAsBitmap, true);
            BitmapHelper.setBmpData(mScreen, 64, 0, mBitmap, false);
        }else{

            //mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
            //BitmapHelper.setBmpData(mScreen, 16, 0, mBitmap, false);
          /*  Graphics.drawChar(mScreen,0,0,font,bytes[0], 1);
            Graphics.drawChar(mScreen,6,0,font,bytes[1], 1);
            Graphics.drawChar(mScreen,12,0,font,bytes[2], 1);
            Graphics.drawChar(mScreen,18,0,font,bytes[3], 1);*/

            if(startTicker == 1){
                text = "Loading.";
            }
            if(startTicker == 5){
                text = "Loading..";
            }
            if(startTicker == 8){
                text = "Loading...";
            }
            startTicker++;


            paint.setTextSize(20f);
            canvas.drawText(text, 0, 0.5f * height, paint);
            BitmapHelper.setBmpData(mScreen, 0, 5, textAsBitmap, true);

            //Graphics.drawTextNew(mScreen,0,25,font,text, 2.0f);

        }



//        Graphics.text(mScreen,0,29,new CodePage850(), "CPU Stat");
//        Graphics.text(mScreen,0,39,new CodePage850(), String.format("Cores:%1d", getNumCores()));
//        Graphics.text(mScreen,0,49,new CodePage850(), String.format("Usage:%.2f%%", readUsage()));

    }


    private Runnable mSpeakerRunnable = new Runnable() {

        @Override
        public void run() {
            playSpeaker();
        }
    };

    private void playSpeaker(){
        if (mSpeaker == null) {
            Log.d(TAG, "Speaker Null");
            return;
        }
        while ((startTime + 5000) > System.currentTimeMillis()) {//Skip play


            index3++;
            try {
                if (index3 == MusicNotes.DRAMATIC_THEME.length) {
                    // reached the end
                    mSpeaker.stop();
                    index3 = -200;
                } else {

                    if(index3 >= 0){
                        double note = MusicNotes.DRAMATIC_THEME[index3];
                        if (note > 0) {
                            mSpeaker.play(note);
                            Log.d(TAG, "Speaker Playing");
                        } else {
                            mSpeaker.stop();
                        }
                    }

                }
            } catch (IOException e) {
                Log.e(TAG, "Error playing speaker", e);
            }


            try {
                Thread.sleep(PLAYBACK_NOTE_DELAY);
            } catch (InterruptedException e){

            }

        }
    }


    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    private float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }


}


