package baidumapsdk.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import baidumapsdk.demo.model.MyPoiInfo;
import baidumapsdk.demo.util.AndroidHelper;
import baidumapsdk.demo.util.ToastHelper;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * PoiSearchActivity
 *
 * @author Mislead
 *         DATE: 2015/7/21
 *         DESC:
 **/
public class PoiSearchActivity extends AppCompatActivity implements View.OnClickListener {

  private static String TAG = "PoiSearchActivity";
  private PoiSearch poiSearch;

  private LinearLayout ll_search;
  private ImageButton ibtn_back;
  private TextView tv_title;
  private ImageButton ibtn_map;
  private ListView listView;

  private MyResultAdapter adapter;

  private String key;
  private String city;
  private String district;
  private LatLng cLatLng;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search_result);
    ll_search = (LinearLayout) findViewById(R.id.ll_search);
    ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
    tv_title = (TextView) findViewById(R.id.tv_title);
    ibtn_map = (ImageButton) findViewById(R.id.ibtn_map);
    listView = (ListView) findViewById(R.id.listView);
    ibtn_back.setOnClickListener(this);
    ibtn_map.setOnClickListener(this);

    Intent intent = getIntent();
    key = intent.getStringExtra("key");
    city = intent.getStringExtra("city");
    district = intent.getStringExtra("district");
    double clat = intent.getDoubleExtra("clat", 0);
    double clon = intent.getDoubleExtra("clon", 0);

    cLatLng = new LatLng(clat, clon);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 查询详情
        PoiInfo info = adapter.getData().get(position);

        gotoMap(info);
        //poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(info.uid));
      }
    });

    adapter = new MyResultAdapter();
    listView.setAdapter(adapter);

    setTitle();

    initPoiSearch();
  }

  private void setTitle() {
    StringBuilder sb = new StringBuilder(key);
    if (!TextUtils.isEmpty(city)) {
      sb.append(" ").append(city);
    }

    if (!TextUtils.isEmpty(district)) {
      sb.append(" ").append(district);
    }

    tv_title.setText(sb.toString());
  }

  private void initPoiSearch() {
    poiSearch = PoiSearch.newInstance();
    poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
      @Override public void onGetPoiResult(PoiResult poiResult) {
        Log.e(TAG, poiResult.error + "");

        if (poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
          adapter.setData(poiResult.getAllPoi());
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

    if (TextUtils.isEmpty(district)) {
      // 附近检索
      poiSearch.searchNearby(new PoiNearbySearchOption().keyword(key)
          .location(cLatLng)
          .pageCapacity(10)
          .pageNum(0)
          .radius(30 * 1000)
          .sortType(PoiSortType.distance_from_near_to_far));
    } else {
      // 市内检索
      poiSearch.searchInCity(
          new PoiCitySearchOption().city(district).keyword(key).pageCapacity(10) //每页数据量
              .pageNum(0));   // 页码
    }
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

  private void gotoMap(PoiInfo info) {
    List<MyPoiInfo> data = new ArrayList<MyPoiInfo>();
    data.add(new MyPoiInfo(info));

    gotoMap(data);
  }

  private void gotoMap(List<MyPoiInfo> data) {
    Intent intent = getMapIntent();

    intent.putExtra("data", (Serializable) data);

    this.startActivity(intent);
  }

  private Intent getMapIntent() {
    Intent intent = new Intent(this, MapActivity.class);
    intent.putExtra("clat", cLatLng.latitude);
    intent.putExtra("clon", cLatLng.longitude);

    return intent;
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

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.ibtn_back:
        onBackPressed();
        break;
      default:
        break;
    }
  }

  class MyResultAdapter extends BaseAdapter {

    private List<PoiInfo> data = new ArrayList<PoiInfo>();

    public void setData(List<PoiInfo> data) {
      this.data = data;
      notifyDataSetChanged();
    }

    public List<PoiInfo> getData() {
      return data;
    }

    @Override public int getCount() {
      return data.size();
    }

    @Override public Object getItem(int position) {
      return data.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;

      if (convertView == null) {
        convertView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poi_result, null);
        holder = new ViewHolder();
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);
        holder.tv_addr = (TextView) convertView.findViewById(R.id.tv_addr);
        holder.tv_goto = (TextView) convertView.findViewById(R.id.tv_goto);
        holder.tv_tel = (TextView) convertView.findViewById(R.id.tv_tel);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      PoiInfo info = data.get(position);
      holder.tv_name.setText(info.name);
      holder.tv_addr.setText(info.address);
      holder.tv_goto.setOnClickListener(new GotoListener(position));
      holder.tv_tel.setOnClickListener(new TelListener(position));

      if (TextUtils.isEmpty(info.phoneNum)) {
        holder.tv_tel.setTextColor(getResources().getColor(R.color.text_black_3));
        holder.tv_tel.setEnabled(false);
      } else {
        holder.tv_tel.setTextColor(getResources().getColor(R.color.text_black_2));
        holder.tv_tel.setEnabled(true);
      }

      double distance = DistanceUtil.getDistance(cLatLng, info.location);

      if (distance > 1000) {
        holder.tv_distance.setText(String.format("%.2f千米", distance / 100.0));
      } else {
        holder.tv_distance.setText(String.format("%d米", (int) distance));
      }

      return convertView;
    }

    class GotoListener implements View.OnClickListener {

      private int pos;

      public GotoListener(int pos) {
        this.pos = pos;
      }

      @Override public void onClick(View v) {
        ToastHelper.toastShort(PoiSearchActivity.this, "pos:" + pos);
      }
    }

    class TelListener implements View.OnClickListener {

      private int pos;

      public TelListener(int pos) {
        this.pos = pos;
      }

      @Override public void onClick(View v) {
        AndroidHelper.telephone_call(PoiSearchActivity.this, data.get(pos).phoneNum);
      }
    }

    class ViewHolder {
      public TextView tv_name;
      public TextView tv_distance;
      public TextView tv_addr;
      public TextView tv_goto;
      public TextView tv_tel;
    }
  }
}
