package baidumapsdk.demo;

import android.app.Application;
import baidumapsdk.demo.util.BDLocUtil;
import com.baidu.mapapi.SDKInitializer;

/**
 * MyApplication
 *
 * @author Mislead
 *         DATE: 2015/7/21
 *         DESC:
 **/
public class MyApplication extends Application {

  private static String TAG = "MyApplication";

  @Override public void onCreate() {
    super.onCreate();
    BDLocUtil.init(getApplicationContext());
    SDKInitializer.initialize(getApplicationContext());
  }
}
