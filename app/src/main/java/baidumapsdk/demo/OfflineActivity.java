package baidumapsdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import baidumapsdk.demo.view.PinnedSectionListView;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * OfflineActivity
 *
 * @author Mislead
 *         DATE: 2015/8/3
 *         DESC:
 **/
public class OfflineActivity extends Activity
    implements RadioGroup.OnCheckedChangeListener, MKOfflineMapListener {

  private static String TAG = "OfflineActivity";
  private RelativeLayout ll_search;
  private ImageButton ibtn_back;
  private RadioGroup rg_type;
  private RadioButton rbtn_download;
  private RadioButton rbtn_list;
  private baidumapsdk.demo.view.PinnedSectionListView listView;

  private MKOfflineMap offlineMap;
  private List<MKOLUpdateElement> localMapList;
  private DownLoadListAdapter downLoadListAdapter;
  private List<MKOLSearchRecord> cityList;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_offline);

    ll_search = (RelativeLayout) findViewById(R.id.ll_search);
    ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
    rg_type = (RadioGroup) findViewById(R.id.rg_type);
    rbtn_download = (RadioButton) findViewById(R.id.rbtn_download);
    rbtn_list = (RadioButton) findViewById(R.id.rbtn_list);
    listView = (baidumapsdk.demo.view.PinnedSectionListView) findViewById(R.id.listView);

    offlineMap = new MKOfflineMap();
    offlineMap.init(this);

    downLoadListAdapter = new DownLoadListAdapter();

    rg_type.setOnCheckedChangeListener(this);

    rbtn_download.setChecked(true);
  }

  private void bindLocalMapList() {
    listView.setAdapter(downLoadListAdapter);
    localMapList = offlineMap.getAllUpdateInfo();
    if (localMapList == null) {
      localMapList = new ArrayList<MKOLUpdateElement>();
    }
    //按照下载进度排序
    if (localMapList.size() > 0) {
      Collections.sort(localMapList, new Comparator<MKOLUpdateElement>() {
        @Override public int compare(MKOLUpdateElement lhs, MKOLUpdateElement rhs) {
          return lhs.ratio - rhs.ratio;
        }
      });
    }

    downLoadListAdapter.setList(localMapList);
  }

  private void bindCityList(String key) {
    if (TextUtils.isEmpty(key)) {
      cityList = offlineMap.getOfflineCityList();
    } else {
      cityList = offlineMap.searchCity(key);
    }
  }

  @Override protected void onDestroy() {
    if (offlineMap != null) {
      offlineMap.destroy();
    }
    super.onDestroy();
  }

  @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
    if (checkedId == R.id.rbtn_download) {
      bindLocalMapList();
    } else {
      bindCityList("");
    }
  }

  @Override public void onGetOfflineMapState(int i, int i1) {

  }

  class DownLoadListAdapter extends BaseAdapter
      implements PinnedSectionListView.PinnedSectionListAdapter {

    private List<MKOLUpdateElement> list;

    public void setList(List<MKOLUpdateElement> list) {
      this.list = list;
      notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) {
      return 1;
    }

    @Override public boolean isItemViewTypePinned(int viewType) {
      return viewType == 2;
    }

    @Override public int getCount() {
      return list.size();
    }

    @Override public Object getItem(int position) {
      return list.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
      DownloadViewHolder holder = null;

      if (convertView == null) {
        convertView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, null);

        holder = new DownloadViewHolder();
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
        holder.ibtn_span = (ImageButton) convertView.findViewById(R.id.ibtn_span);
        holder.ll_do = (LinearLayout) convertView.findViewById(R.id.ll_do);
        holder.btn_download = (Button) convertView.findViewById(R.id.btn_download);
        holder.btn_del = (Button) convertView.findViewById(R.id.btn_del);
        convertView.setTag(holder);
      } else {
        holder = (DownloadViewHolder) convertView.getTag();
      }

      MKOLUpdateElement element = list.get(position);
      holder.tv_name.setText(
          String.format("%1$s  %2$.1fM", element.cityName, element.size / 1024.0));

      holder.tv_status.setText("ratio:" + element.ratio + " status:" + element.status);

      final DownloadViewHolder finalHolder = holder;
      holder.ibtn_span.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (finalHolder.ll_do.getVisibility() == View.VISIBLE) {
            finalHolder.ll_do.setVisibility(View.GONE);
            finalHolder.ibtn_span.setImageResource(R.mipmap.icon_arrow_down);
          } else {
            finalHolder.ll_do.setVisibility(View.VISIBLE);
            finalHolder.ibtn_span.setImageResource(R.mipmap.icon_arrow_up);
          }
        }
      });

      return convertView;
    }

    class DownloadViewHolder {
      public TextView tv_name;
      public TextView tv_status;
      public ImageButton ibtn_span;
      public LinearLayout ll_do;
      public Button btn_download;
      public Button btn_del;
    }
  }
}
