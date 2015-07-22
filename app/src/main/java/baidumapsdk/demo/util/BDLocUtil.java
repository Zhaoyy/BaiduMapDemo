package baidumapsdk.demo.util;

import android.content.Context;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * BDLocUtil
 *
 * @author Mislead
 *         DATE: 2015/7/21
 *         DESC:
 **/
public class BDLocUtil {

  private static String TAG = "BDLocUtil";

  private static LocationClient locationClient;

  private static LocationClientOption option = new LocationClientOption();

  private static BDLocation lastLocation = null;

  private static BDLocationListener defaultLocListener = new BDLocationListener() {
    @Override public void onReceiveLocation(BDLocation bdLocation) {

      switch (bdLocation.getLocType()) {
        case BDLocation.TypeGpsLocation: // 61
        case BDLocation.TypeNetWorkLocation: //161
        case BDLocation.TypeOffLineLocation://66

          lastLocation = bdLocation;

          break;
        default:// fail
          break;
      }
    }
  };

  public static void init(Context context) {
    if (locationClient == null) {
      locationClient = new LocationClient(context);
    }
    initOptions();
    locationClient.setLocOption(option);

    locationClient.registerLocationListener(defaultLocListener);
  }

  public static void registerLocListener(BDLocationListener listener) {
    locationClient.registerLocationListener(listener);
  }

  private static void initOptions() {
    option.setLocationMode(
        LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
    option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
    int span = 1000;
    option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
    option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
    option.setOpenGps(true);//可选，默认false,设置是否使用gps
    option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
    option.setIsNeedLocationDescribe(
        true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
    //option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
    //option.setIgnoreKillProcess(
    //    false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
    option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
    option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
  }

  public static boolean hasGetLocation() {
    return lastLocation != null;
  }

  public static BDLocation getLastLocation() {
    return lastLocation;
  }

  public static void startLocation() {
    locationClient.start();
  }

  public static void stop() {
    locationClient.stop();
  }

  public static void requestLocation() {
    locationClient.requestLocation();
  }

  public static String getLocationInfo(BDLocation location) {
    StringBuilder sb = new StringBuilder();
    sb.append("type:" + location.getLocType() + "\n")
        .append("lat:" + location.getLatitude() + "\n")
        .append("lon:" + location.getLongitude() + "\n")
        .append("addr:" + location.getAddrStr() + "\n")
        .append("city:" + location.getCity() + "\n")
        .append("district:" + location.getDistrict() + "\n")
        .append("street:" + location.getStreet());

    return sb.toString();
  }
}
