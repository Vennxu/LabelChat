package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ekuater.labelchat.util.L;

import java.util.Random;

/**
 * 获取加速度传感器加速度计算扔动作的最大力度和角度
 *
 * @author LinYong
 */
public class ThrowStrengthCollector {

    private static final String TAG = ThrowStrengthCollector.class.getSimpleName();

    private static final double FULL_ACCELERATION = 30.0D;
    private static final double THRESHOLD_ACCELERATION = 15.0D;

    private static final int MSG_EMULATE_COLLECTION = 101;

    private static final int[] AZIMUTH_ARRAY = {
            0,
            45,
            90,
            135,
            180,
            225,
            270,
            315,
    };

    public interface Listener {
        public void onCollectDone(float strength, double azimuth);
    }

    private interface ListenerNotifier {
        public void notify(Listener listener);
    }

    private static Random sRandom;

    private static synchronized void newRandom() {
        if (sRandom == null) {
            sRandom = new Random();
        }
    }

    private static Random getRandom() {
        if (sRandom == null) {
            newRandom();
        }
        return sRandom;
    }

    private final Listener mListener;
    private final SensorManager mSensorManager;
    private final Sensor mGSensor;
    private final Sensor mMagneticSensor;
    private final float[] mGData = new float[3];
    private final float[] mMData = new float[3];
    private final float[] mR = new float[16];
    private final float[] gravity = new float[3];
    private final float[] linear_acceleration = new float[3];
    private final SensorEventListener mOrientationListener
            = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final int type = event.sensor.getType();
            final float[] data;

            switch (type) {
                case Sensor.TYPE_ACCELEROMETER:
                    data = mGData;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    data = mMData;
                    break;
                default:
                    return;
            }

            float[] orientations = new float[3];
            System.arraycopy(event.values, 0, data, 0, 3);
            SensorManager.getRotationMatrix(mR, null, mGData, mMData);
            SensorManager.getOrientation(mR, orientations);

            double azimuth = Math.toDegrees(orientations[0]) % 360;
            mAzimuth = (azimuth < 0) ? 360 + azimuth : azimuth;

            switch (type) {
                case Sensor.TYPE_ACCELEROMETER:
                    onNewAccelerationData(data);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private double mAzimuth;
    private double mCollectAzimuth;
    private double mMaxAcceleration = 0.0D;
    private boolean mCollecting = false;

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_EMULATE_COLLECTION:
                    emulateCollection();
                    break;
                default:
                    break;
            }
        }
    }

    private final Handler mHandler;

    public ThrowStrengthCollector(Context context, Listener listener) {
        mListener = listener;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mGSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mGSensor != null) {
            mSensorManager.registerListener(mOrientationListener, mGSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mMagneticSensor != null) {
            mSensorManager.registerListener(mOrientationListener, mMagneticSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        mHandler = new MainHandler(context.getMainLooper());
    }

    public void start() {
        mMaxAcceleration = 0.0D;
        mCollectAzimuth = getCollectedAzimuth();

        if (mGSensor != null) {
            mCollecting = true;
        } else {
            Message msg = mHandler.obtainMessage(MSG_EMULATE_COLLECTION);
            mHandler.sendMessageDelayed(msg, 1000);
        }
    }

    public void stop() {
        stopSensor();
    }

    public void destroy() {
        if (mGSensor != null) {
            mSensorManager.unregisterListener(mOrientationListener, mGSensor);
        }
        if (mMagneticSensor != null) {
            mSensorManager.unregisterListener(mOrientationListener, mMagneticSensor);
        }
    }

    private void stopSensor() {
        if (mGSensor != null) {
            mCollecting = false;
        } else {
            mHandler.removeMessages(MSG_EMULATE_COLLECTION);
        }
    }

    private void onNewAccelerationData(float[] values) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];
        linear_acceleration[0] = values[0] - gravity[0];
        linear_acceleration[1] = values[1] - gravity[1];
        linear_acceleration[2] = values[2] - gravity[2];

        if (mCollecting) {
            double acceleration = 0.0f;

            for (float a : linear_acceleration) {
                acceleration += Math.pow(a, 2);
            }

            acceleration = Math.sqrt(acceleration);
            onNewAcceleration(acceleration);
        }
    }

    private void onNewAcceleration(double acceleration) {
        acceleration -= THRESHOLD_ACCELERATION;
        mMaxAcceleration = Math.max(mMaxAcceleration, acceleration);

        if (mMaxAcceleration > 0 && acceleration < 0) {
            stopSensor();
            float strength = mMaxAcceleration >= FULL_ACCELERATION ? 1.0F
                    : (float) (mMaxAcceleration / FULL_ACCELERATION);
            notifyCollectDone(strength, mCollectAzimuth);
            L.v(TAG, "onNewAcceleration(), acceleration=%1$f, strength=%2$f, azimuth=%3$f",
                    mMaxAcceleration, strength, mAzimuth);
        }
    }

    private double getCollectedAzimuth() {
        return (mGSensor != null && mMagneticSensor != null)
                ? mAzimuth : randomAzimuth();
    }

    private int randomAzimuth() {
        return AZIMUTH_ARRAY[getRandom().nextInt(AZIMUTH_ARRAY.length)];
    }

    private void emulateCollection() {
        notifyCollectDone(getRandom().nextFloat(), mCollectAzimuth);
    }

    private void notifyListener(ListenerNotifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }

    private void notifyCollectDone(float strength, double azimuth) {
        notifyListener(new CollectDoneNotifier(strength, azimuth));
    }

    private static class CollectDoneNotifier implements ListenerNotifier {

        private final float strength;
        private final double azimuth;

        public CollectDoneNotifier(float strength, double azimuth) {
            this.strength = strength;
            this.azimuth = azimuth;
        }

        @Override
        public void notify(Listener listener) {
            listener.onCollectDone(strength, azimuth);
        }
    }
}
