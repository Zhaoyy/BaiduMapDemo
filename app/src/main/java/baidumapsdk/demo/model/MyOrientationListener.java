package baidumapsdk.demo.model;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * MyOrientationListener
 *
 * @author Mislead
 *         DATE: 2015/7/28
 *         DESC:
 **/
public class MyOrientationListener implements SensorEventListener {

  private static String TAG = "MyOrientationListener";
  private Context context;
  private SensorManager manager;
  private Sensor sensor; // 备选方向传感器
  private Sensor aSensor;//加速度传感器
  private Sensor mSensor;//磁场传感器

  private float aValues[] = new float[3];
  private float mValues[] = new float[3];

  private float lastO;

  private OnOritentationListener onOritentationListener;

  public void setOnOritentationListener(OnOritentationListener onOritentationListener) {
    this.onOritentationListener = onOritentationListener;
  }

  public MyOrientationListener(Context context) {
    this.context = context;
    manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    if (manager != null) {
      aSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      mSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

      if (aSensor == null || mSensor == null) {
        sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        aSensor = null;
        mSensor = null;
      }
    }
  }

  public void start() {

    if (manager != null) {
      if (sensor != null) {
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
      } else if (aSensor != null && mSensor != null) {
        manager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
      }
    }
  }

  public void stop() {
    if (manager != null) {
      manager.unregisterListener(this);
    }
  }

  @Override public void onSensorChanged(SensorEvent event) {

    float x;

    if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
      x = event.values[SensorManager.DATA_X];
    } else {
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        aValues = event.values;
      } else {
        mValues = event.values;
      }

      x = getOrientations();

      if (x < 0) {
        x = 360 + x;
      }
    }

    if (onOritentationListener != null && Math.abs(lastO - x) > 15 && Math.abs(lastO - x) < 345) {
      onOritentationListener.getOrientation(x);
      lastO = x;
    }
  }

  private float getOrientations() {
    float values[] = new float[3];
    float R[] = new float[9];
    SensorManager.getRotationMatrix(R, null, aValues, mValues);
    SensorManager.getOrientation(R, values);

    return (float) Math.toDegrees(values[0]);
  }

  @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  public interface OnOritentationListener {
    void getOrientation(float orientation);
  }
}
