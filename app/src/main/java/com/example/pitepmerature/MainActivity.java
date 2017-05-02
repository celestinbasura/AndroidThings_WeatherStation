package com.example.pitepmerature;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.bmx280.Bme280;
import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.ssd1306.BitmapHelper;
import com.google.android.things.contrib.driver.ssd1306.Ssd1306;
import com.google.android.things.contrib.driver.tm1637.NumericDisplay;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final int ADDRESS = 0x76;
    private static final String DISPLAY_TEMP_CLOCK = "BCM24";
    private static final String DISPLAY_TEMP_DATA = "BCM23";
    private static final String DISPLAY_PRESS_CLOCK = "BCM6";
    private static final String DISPLAY_PRESS_DATA = "BCM5";
    public static final float DISPLAY_BRIGHTNESS = 1.0f;

    private static final float BAROMETER_RANGE_SUNNY = 1010.f;
    private static final float BAROMETER_RANGE_RAINY = 990.f;

    private NumericDisplay mTempDisplay;
    private NumericDisplay mPressDisplay;
    private float mLastTemperature;
    private float mLastPressure;


    private Ssd1306 mScreen;

    private Bitmap mBitmap;

	private final PeripheralManagerService managerService = new PeripheralManagerService();
    Bme280 bmxDriver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


        try {
            mScreen = new Ssd1306(managerService.getI2cBusList().get(0));
            Log.d(TAG, "OLED connected and cleared");
            mScreen.clearPixels();
            mScreen.show();



        } catch (IOException e) {
            e.printStackTrace();
        }


        initNumerical();

		printDeviceId();

        initBME280();

		readSample();
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

        } catch (IOException e) {
            e.printStackTrace();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

               // Log.d(TAG, "Temperature: " + mLastTemperature);
               // Log.d(TAG, "Pressure: " + mLastPressure);
                //Log.d(TAG, "Humidity: " + bmxDriver.readHumidity());
                updateNumericDisplays();


                try {
                    Thread.sleep(2000);
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

        if (mLastPressure > BAROMETER_RANGE_SUNNY) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sunny_128_64);
        } else if (mLastPressure < BAROMETER_RANGE_RAINY) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rainy_128_64);
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cloud_128_64);
        }
            mScreen.clearPixels();
            BitmapHelper.setBmpData(mScreen, 32, 0, mBitmap, false);
    }
}


