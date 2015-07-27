package baidumapsdk.demo;

import android.app.Activity;
import android.os.Bundle;
import baidumapsdk.demo.model.MyPoiInfo;
import baidumapsdk.demo.util.BDLocUtil;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import java.util.List;

/**
 * MapActivity
 *
 * @author Mislead
 *         DATE: 2015/7/27
 *         DESC:
 **/
public class MapActivity extends Activity {

  private static String TAG = "MapActivity";

  private MapView mapView;
  private BaiduMap baiduMap;
  private boolean isFirst = true;

  private List<MyPoiInfo> data;

  private int marker_icon_ids[] = new int[] {
      R.mipmap.icon_marka, R.mipmap.icon_markb, R.mipmap.icon_markc, R.mipmap.icon_markd,
      R.mipmap.icon_marke, R.mipmap.icon_markf, R.mipmap.icon_markg, R.mipmap.icon_markh,
      R.mipmap.icon_marki, R.mipmap.icon_markj
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    data = (List<MyPoiInfo>) getIntent().getSerializableExtra("data");

    setContentView(R.layout.activity_map);
    mapView = (MapView) findViewById(R.id.mapview);
    baiduMap = mapView.getMap();
    // 显示定位图层
    baiduMap.setMyLocationEnabled(true);

    mapView.showScaleControl(true);

    setMyLocOnMap();
  }

  /**
   * 设置地图中显示当前位置，如果是第一次，则动画移动到当前位置
   */
  private void setMyLocOnMap() {
    BDLocation location = BDLocUtil.getLastLocation();
    MyLocationData.Builder builder = new MyLocationData.Builder().latitude(location.getLatitude())
        .longitude(location.getLongitude())
        .accuracy(location.getRadius())
        .direction(location.getDirection());
    baiduMap.setMyLocationData(builder.build());

    if (isFirst) {
      isFirst = false;
      // 地图移动到当前位置，设置初试比例尺为15（20-3）
      MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(
          new LatLng(location.getLatitude(), location.getLongitude()), 15);

      baiduMap.animateMapStatus(update);
    }
  }

  @Override protected void onResume() {
    if (mapView != null) mapView.onResume();
    super.onResume();
  }

  @Override protected void onPause() {
    if (mapView != null) mapView.onPause();
    super.onPause();
  }

  @Override protected void onDestroy() {
    if (mapView != null) {
      mapView.onDestroy();
      mapView = null;
    }
    super.onDestroy();
  }
}
