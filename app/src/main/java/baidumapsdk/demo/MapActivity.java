package baidumapsdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import baidumapsdk.demo.model.MyOrientationListener;
import baidumapsdk.demo.model.MyPoiInfo;
import baidumapsdk.demo.util.BDLocUtil;
import baidumapsdk.demo.util.ToastHelper;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * MapActivity
 *
 * @author Mislead
 *         DATE: 2015/7/27
 *         DESC:
 **/
public class MapActivity extends Activity
    implements BDLocationListener, MyOrientationListener.OnOritentationListener,
    View.OnClickListener {

  private static String TAG = "MapActivity";

  private MapView mapView;
  private BaiduMap baiduMap;
  private LinearLayout ll_search;
  private ImageButton ibtn_back;
  private TextView tv_title;
  private RelativeLayout rl_info;
  private Button btn_do;
  private TextView tv_name;
  private TextView tv_distance;
  private TextView tv_addr;
  private boolean isFirst = true;
  private int type = 1; // 1-标注位置，2-路线规划

  private List<MyPoiInfo> data;
  private MyPoiInfo toInfo;

  private RoutePlanSearch rpSearch;

  private BDLocation cLoc;

  private DrivingRouteOverlay drivingRouteOverlay;

  private OverlayManager overlayManager;

  private BitmapDescriptor currentIcon;
  private MyOrientationListener myOrientationListener;

  private float mOrientation;
  private String title;

  private int marker_icon_ids[] = new int[] {
      R.mipmap.icon_marka, R.mipmap.icon_markb, R.mipmap.icon_markc, R.mipmap.icon_markd,
      R.mipmap.icon_marke, R.mipmap.icon_markf, R.mipmap.icon_markg, R.mipmap.icon_markh,
      R.mipmap.icon_marki, R.mipmap.icon_markj
  };

  private int marker_focus_icon_ids[] = new int[] {
      R.mipmap.icon_focus_marka, R.mipmap.icon_focus_markb, R.mipmap.icon_focus_markc,
      R.mipmap.icon_focus_markd, R.mipmap.icon_focus_marke, R.mipmap.icon_focus_markf,
      R.mipmap.icon_focus_markg, R.mipmap.icon_focus_markh, R.mipmap.icon_focus_marki,
      R.mipmap.icon_focus_markj
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    type = getIntent().getIntExtra("type", 1);

    title = getIntent().getStringExtra("title");

    setContentView(R.layout.activity_map);

    mapView = (MapView) findViewById(R.id.mapview);
    ll_search = (LinearLayout) findViewById(R.id.ll_search);
    ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
    tv_title = (TextView) findViewById(R.id.tv_title);
    rl_info = (RelativeLayout) findViewById(R.id.rl_info);
    btn_do = (Button) findViewById(R.id.btn_do);
    tv_name = (TextView) findViewById(R.id.tv_name);
    tv_distance = (TextView) findViewById(R.id.tv_distance);
    tv_addr = (TextView) findViewById(R.id.tv_addr);

    ibtn_back.setOnClickListener(this);
    btn_do.setOnClickListener(this);

    if (!TextUtils.isEmpty(title)) {
      tv_title.setText(title);
    }

    baiduMap = mapView.getMap();
    // 显示定位图层
    baiduMap.setMyLocationEnabled(true);

    baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
      @Override public void onMapLoaded() {
        if (drivingRouteOverlay != null) {
          drivingRouteOverlay.zoomToSpan();
        }

        if (overlayManager != null) {
          overlayManager.zoomToSpan();
        }
      }
    });

    baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
      @Override public void onMapClick(LatLng latLng) {
        if (type == 1) {
          rl_info.setVisibility(View.GONE);
        }
      }

      @Override public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
      }
    });

    mapView.showScaleControl(true);

    BDLocUtil.registerLocListener(this);
    BDLocUtil.requestLocation();

    myOrientationListener = new MyOrientationListener(this);
    myOrientationListener.setOnOritentationListener(this);
    myOrientationListener.start();

    if (type == 1) {
      // add marker
      data = (List<MyPoiInfo>) getIntent().getSerializableExtra("data");
      addMarker2Map();
    } else {
      // route plan
      toInfo = (MyPoiInfo) getIntent().getSerializableExtra("to");

      requestRoutePlan(toInfo);
    }
  }

  /**
   * 设置当前位置，如果是第一次则动画移动到当前位置
   */
  private void setMyLocOnMap() {

    MyLocationData.Builder builder = new MyLocationData.Builder().latitude(cLoc.getLatitude())
        .longitude(cLoc.getLongitude())
        .accuracy(cLoc.getRadius())
        .direction(mOrientation);
    baiduMap.setMyLocationData(builder.build());

    if (currentIcon == null) {
      currentIcon = BitmapDescriptorFactory.fromResource(R.mipmap.main_icon_nav);
    }

    baiduMap.setMyLocationConfigeration(
        new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true,
            currentIcon));

    if (isFirst && type == 1) {
      isFirst = false;
      // 设置默认比例尺15（20-3）
      MapStatusUpdate update =
          MapStatusUpdateFactory.newLatLngZoom(new LatLng(cLoc.getLatitude(), cLoc.getLongitude()),
              15);

      baiduMap.animateMapStatus(update);
    }
  }

  private void addMarker2Map() {

    List<OverlayOptions> optionses = new ArrayList<OverlayOptions>();

    for (int i = 0; i < data.size(); i++) {

      MyPoiInfo info = data.get(i);
      BitmapDescriptor descriptor =
          BitmapDescriptorFactory.fromResource(marker_icon_ids[i % marker_icon_ids.length]);
      OverlayOptions oo = new MarkerOptions().position(new LatLng(info.lat, info.lng))
          .icon(descriptor)
          .zIndex(i)
          .title(info.name);
      optionses.add(oo);
    }

    overlayManager = new MarkerOverLayManager(baiduMap, optionses);
    overlayManager.addToMap();
    // add maker click listener
    baiduMap.setOnMarkerClickListener(overlayManager);
  }

  // 请求线路规划
  private void requestRoutePlan(final MyPoiInfo to) {

    if (to == null) return;

    if (rpSearch == null) {
      rpSearch = RoutePlanSearch.newInstance();
    }

    rpSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
      @Override public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

      }

      @Override public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

      }

      @Override public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if (drivingRouteResult == null
            || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
          ToastHelper.toastShort(MapActivity.this, "查询失败，请重试！");
          return;
        }

        if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {

          type = 2;

          baiduMap.clear();
          drivingRouteOverlay = new DrivingRouteOverlay(baiduMap);
          baiduMap.setOnMarkerClickListener(drivingRouteOverlay);
          drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));
          drivingRouteOverlay.addToMap();
          drivingRouteOverlay.zoomToSpan();

          showInfoView(to);
        }
      }
    });

    if (cLoc == null) {
      cLoc = BDLocUtil.getLastLocation();
    }

    PlanNode fromNode = PlanNode.withLocation(new LatLng(cLoc.getLatitude(), cLoc.getLongitude()));
    PlanNode toNode = PlanNode.withLocation(new LatLng(to.lat, to.lng));

    rpSearch.drivingSearch(new DrivingRoutePlanOption().from(fromNode).to(toNode));
  }

  private void showInfoView(MyPoiInfo info) {
    rl_info.setVisibility(View.VISIBLE);

    tv_name.setText(info.name);
    tv_addr.setText(info.address);

    double distance = DistanceUtil.getDistance(new LatLng(cLoc.getLatitude(), cLoc.getLongitude()),
        new LatLng(info.lat, info.lng));

    if (distance > 1000) {
      tv_distance.setText(String.format("%.2f千米", distance / 100.0));
    } else {
      tv_distance.setText(String.format("%d米", (int) distance));
    }

    btn_do.setText(type == 1 ? "前往" : "详情");

    rl_info.setTag(type == 1 ? info : null);
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

    BDLocUtil.unregisterLocListener(this);
    myOrientationListener.stop();

    if (mapView != null) {
      mapView.onDestroy();
      mapView = null;
    }

    if (rpSearch != null) {
      rpSearch.destroy();
      rpSearch = null;
    }
    super.onDestroy();
  }

  @Override public void onReceiveLocation(BDLocation bdLocation) {
    cLoc = bdLocation;
    setMyLocOnMap();
  }

  @Override public void getOrientation(float orientation) {
    if (cLoc != null) {
      mOrientation = orientation;
      setMyLocOnMap();
    }
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.ibtn_back:
        onBackPressed();
        break;
      case R.id.btn_do:

        if (type == 1) {
          requestRoutePlan((MyPoiInfo) rl_info.getTag());
        } else {

        }

        break;
      default:
        break;
    }
  }

  class MarkerOverLayManager extends OverlayManager {

    private List<OverlayOptions> optionses;
    private Marker lastMarker = null;

    public MarkerOverLayManager(BaiduMap baiduMap, List<OverlayOptions> optionses) {
      super(baiduMap);
      baiduMap.clear();
      this.optionses = optionses;
    }

    @Override public List<OverlayOptions> getOverlayOptions() {
      return optionses;
    }

    @Override public boolean onMarkerClick(Marker marker) {

      setMarkerIcon(lastMarker, false);
      setMarkerIcon(marker, true);
      lastMarker = marker;
      MyPoiInfo info = data.get(marker.getZIndex());

      showInfoView(info);

      return true;
    }

    private void setMarkerIcon(Marker marker, boolean focused) {

      if (marker == null) return;

      int pos = marker.getZIndex();

      BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(
          focused ? marker_focus_icon_ids[pos % marker_focus_icon_ids.length]
              : marker_icon_ids[pos % marker_icon_ids.length]);

      marker.setIcon(descriptor);
    }

    @Override public boolean onPolylineClick(Polyline polyline) {
      return false;
    }
  }
}
