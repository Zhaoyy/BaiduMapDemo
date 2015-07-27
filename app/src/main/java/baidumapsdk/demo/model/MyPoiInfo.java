package baidumapsdk.demo.model;

import com.baidu.mapapi.search.core.PoiInfo;
import java.io.Serializable;

/**
 * MyPoiInfo
 *
 * @author Mislead
 *         DATE: 2015/7/27
 *         DESC:
 **/
public class MyPoiInfo implements Serializable {

  private static String TAG = "MyPoiInfo";

  public String name;
  public String uid;
  public String address;
  public String city;
  public String phoneNum;
  public String postCode;
  public PoiInfo.POITYPE type;
  public double lat;
  public double lng;
  public boolean hasCaterDetails;
  public boolean isPano;

  public MyPoiInfo() {
  }

  public MyPoiInfo(PoiInfo info) {
    this.name = info.name;
    this.uid = info.uid;
    this.address = info.address;
    this.city = info.city;
    this.phoneNum = info.phoneNum;
    this.postCode = info.postCode;
    this.type = info.type;
    this.lat = info.location.latitude;
    this.lng = info.location.longitude;
    this.hasCaterDetails = info.hasCaterDetails;
    this.isPano = info.isPano;
  }
}
