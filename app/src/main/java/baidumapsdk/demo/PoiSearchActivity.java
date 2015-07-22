package baidumapsdk.demo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import baidumapsdk.demo.util.BDLocUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import java.util.ArrayList;
import java.util.List;

/**
 * PoiSearchActivity
 *
 * @author Mislead
 *         DATE: 2015/7/21
 *         DESC:
 **/
public class PoiSearchActivity extends AppCompatActivity {

  private static String TAG = "PoiSearchActivity";
  private PoiSearch poiSearch;

  private ListView listView;

  private List<PoiInfo> data;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    listView = (ListView) findViewById(R.id.listView);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 查询详情
        poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(data.get(position).uid));
      }
    });

    BDLocation location = BDLocUtil.getLastLocation();

    poiSearch = PoiSearch.newInstance();
    poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
      @Override public void onGetPoiResult(PoiResult poiResult) {
        Log.e(TAG, poiResult.error + "");

        if (poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
          List<String> list = new ArrayList<String>();
          data = poiResult.getAllPoi();

          for (PoiInfo info : poiResult.getAllPoi()) {
            list.add(info.name + "-" + info.address);
          }
          ArrayAdapter<String> arrayAdapter =
              new ArrayAdapter<String>(PoiSearchActivity.this, android.R.layout.simple_list_item_1,
                  list);
          listView.setAdapter(arrayAdapter);
        } else if (poiResult.error == PoiResult.ERRORNO.ST_EN_TOO_NEAR) {
          Toast.makeText(PoiSearchActivity.this, "就在附近，好好找找", Toast.LENGTH_SHORT).show();
        }
      }

      @Override public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        Log.e(TAG, poiDetailResult.error + "");
        if (poiDetailResult.error == PoiResult.ERRORNO.NO_ERROR) {
          showInfoDialog(poiDetailResult);
        } else if (poiDetailResult.error == PoiResult.ERRORNO.ST_EN_TOO_NEAR) {
          Toast.makeText(PoiSearchActivity.this, "就在附近，好好找找", Toast.LENGTH_SHORT).show();
        }
      }
    });

    // 市内检索
    //poiSearch.searchInCity(
    //    new PoiCitySearchOption().city(location.getCity()).keyword("加油站").pageCapacity(20) //每页数据量
    //        .pageNum(0));   // 页码

    // 附近检索
    poiSearch.searchNearby(new PoiNearbySearchOption().keyword("加油站")
        .location(new LatLng(location.getLatitude(), location.getLongitude()))
        .pageCapacity(10)
        .pageNum(0)
        .radius(100 * 1000)
        .sortType(PoiSortType.distance_from_near_to_far));
  }

  private String getPoiDetailInfo(PoiDetailResult result) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getName() + "\n")
        .append(result.getAddress() + "\n")
        .append("lat:" + result.getLocation().latitude + "\n")
        .append("lon:" + result.getLocation().longitude + "\n");
    return sb.toString();
  }

  private void showInfoDialog(PoiDetailResult result) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(result.getName())
        .setCancelable(true)
        .setMessage(getPoiDetailInfo(result));
    builder.show();
  }

  @Override protected void onStop() {
    super.onStop();
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    if (poiSearch != null) {
      poiSearch.destroy();
    }
  }
}
