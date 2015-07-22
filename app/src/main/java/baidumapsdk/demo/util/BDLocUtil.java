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
        LocationClientOption.LocationMode.Hight_Accuracy);//��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
    option.setCoorType("bd09ll");//��ѡ��Ĭ��gcj02�����÷��صĶ�λ�������ϵ
    int span = 1000;
    option.setScanSpan(span);//��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
    option.setIsNeedAddress(true);//��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
    option.setOpenGps(true);//��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
    option.setLocationNotify(true);//��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
    option.setIsNeedLocationDescribe(
        true);//��ѡ��Ĭ��false�������Ƿ���Ҫλ�����廯�����������BDLocation.getLocationDescribe��õ�����������ڡ��ڱ����찲�Ÿ�����
    //option.setIsNeedLocationPoiList(true);//��ѡ��Ĭ��false�������Ƿ���ҪPOI�����������BDLocation.getPoiList��õ�
    //option.setIgnoreKillProcess(
    //    false);//��ѡ��Ĭ��false����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ��ɱ��
    option.SetIgnoreCacheException(false);//��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
    option.setEnableSimulateGps(false);//��ѡ��Ĭ��false�������Ƿ���Ҫ����gps��������Ĭ����Ҫ
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
