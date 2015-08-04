package baidumapsdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
  private static final int MB = 1024 * 1024;
  private RelativeLayout ll_search;
  private ImageButton ibtn_back;
  private RadioGroup rg_type;
  private RadioButton rbtn_download;
  private RadioButton rbtn_list;
  private PinnedSectionListView listView;
  private ExpandableListView exListView;

  private MKOfflineMap offlineMap;
  private List<MKOLUpdateElement> localMapList;
  private DownLoadListAdapter downLoadListAdapter;
  private List<MKOLSearchRecord> cityList;
  private CityListAdapter cityListAdapter;

  private int checkedId = 0;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_offline);

    ll_search = (RelativeLayout) findViewById(R.id.ll_search);
    ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
    rg_type = (RadioGroup) findViewById(R.id.rg_type);
    rbtn_download = (RadioButton) findViewById(R.id.rbtn_download);
    rbtn_list = (RadioButton) findViewById(R.id.rbtn_list);
    listView = (PinnedSectionListView) findViewById(R.id.listView);
    exListView = (ExpandableListView) findViewById(R.id.ex_listView);

    offlineMap = new MKOfflineMap();
    offlineMap.init(this);

    downLoadListAdapter = new DownLoadListAdapter();
    cityListAdapter = new CityListAdapter();
    listView.setAdapter(downLoadListAdapter);
    exListView.setAdapter(cityListAdapter);

    ibtn_back.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        onBackPressed();
      }
    });

    exListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
      @Override
      public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        MKOLSearchRecord record = cityList.get(groupPosition);
        if (record.childCities == null || record.childCities.size() <= 0) {
          startOrSwitch(record.cityID);
          return true;
        }

        return false;
      }
    });

    exListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
      @Override public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
          int childPosition, long id) {
        MKOLSearchRecord record = cityList.get(groupPosition).childCities.get(childPosition);
        startOrSwitch(record.cityID);
        return true;
      }
    });

    rg_type.setOnCheckedChangeListener(this);

    rbtn_download.setChecked(true);
  }

  private void bindLocalMapList() {
    listView.setVisibility(View.VISIBLE);
    exListView.setVisibility(View.GONE);

    localMapList = offlineMap.getAllUpdateInfo();
    if (localMapList == null) {
      localMapList = new ArrayList<MKOLUpdateElement>();
    }
    //按照下载进度排序
    if (localMapList.size() > 0) {
      Collections.sort(localMapList, new Comparator<MKOLUpdateElement>() {
        @Override public int compare(MKOLUpdateElement lhs, MKOLUpdateElement rhs) {
          if (lhs.ratio == 100 && rhs.ratio < 100) {
            return 1;
          }

          if (lhs.ratio < 100 && rhs.ratio == 100) {
            return -1;
          }
          return 0;
        }
      });
    }

    // add pinned section

    MKOLUpdateElement notSection = new MKOLUpdateElement();
    notSection.cityName = "未完成下载";
    notSection.cityID = -1;
    localMapList.add(0, notSection);

    boolean found = false;
    int index = 0;
    for (int i = 1; i < localMapList.size(); i++) {
      MKOLUpdateElement element = localMapList.get(i);

      if (element.ratio == 100) {
        found = true;
        index = i;
        break;
      }
    }

    MKOLUpdateElement doneSection = new MKOLUpdateElement();
    doneSection.cityName = "下载完成";
    doneSection.cityID = -1;

    if (found) {
      localMapList.add(index, doneSection);
    } else {
      localMapList.add(doneSection);
    }

    downLoadListAdapter.setList(localMapList);
  }

  private void bindCityList(String key) {
    listView.setVisibility(View.GONE);
    exListView.setVisibility(View.VISIBLE);
    if (TextUtils.isEmpty(key)) {
      cityList = offlineMap.getOfflineCityList();
    } else {
      cityList = offlineMap.searchCity(key);
    }

    cityListAdapter.setList(cityList);
  }

  private void startOrSwitch(int cityId) {

    if (foundLocal(cityId)) {
      rbtn_download.setChecked(true);
    } else {
      startDownload(cityId);
      cityListAdapter.notifyDataSetChanged();
    }
  }

  private void startDownload(int cityId) {
    offlineMap.start(cityId);
  }

  private void pauseDownload(int cityId) {
    offlineMap.pause(cityId);
  }

  private void pauseAll() {
    for (MKOLUpdateElement element : localMapList) {
      if (element.cityID > 0) {
        pauseDownload(element.cityID);
      }
    }
  }

  private void delOffline(int cityId) {
    offlineMap.remove(cityId);
    bindLocalMapList();
  }

  @Override protected void onDestroy() {
    if (offlineMap != null) {
      offlineMap.destroy();
    }
    super.onDestroy();
  }

  @Override protected void onPause() {
    pauseAll();
    super.onPause();
  }

  @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
    this.checkedId = checkedId;
    if (checkedId == R.id.rbtn_download) {
      bindLocalMapList();
    } else {
      bindCityList("");
    }
  }

  @Override public void onGetOfflineMapState(int type, int state) {
    switch (type) {
      case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
        if (checkedId == R.id.rbtn_download) {
          bindLocalMapList();
        } else {
          localMapList = offlineMap.getAllUpdateInfo();
        }
      }
      break;
      case MKOfflineMap.TYPE_NEW_OFFLINE:
        // 有新离线地图安装
        Log.e(TAG, String.format("add offlinemap num:%d", state));
        break;
      case MKOfflineMap.TYPE_VER_UPDATE:
        // 版本更新提示
        // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

        break;
    }
  }

  private boolean foundLocal(int cityId) {
    for (MKOLUpdateElement element : localMapList) {
      if (element.cityID == cityId) {
        return true;
      }
    }
    return false;
  }

  private String getLocalInfo(int cityId) {
    MKOLUpdateElement e = null;
    for (MKOLUpdateElement element : localMapList) {
      if (element.cityID == cityId) {
        e = element;
        break;
      }
    }

    if (e == null) {
      return "";
    } else if (e.ratio == 100) {
      return "（已下载）";
    } else if (e.status == MKOLUpdateElement.DOWNLOADING) {
      return "（正在下载）";
    } else if (e.status == MKOLUpdateElement.SUSPENDED) {
      return "（暂停下载）";
    } else {
      return "（等待下载）";
    }
  }

  class CityListAdapter extends BaseExpandableListAdapter {

    private List<MKOLSearchRecord> list = new ArrayList<MKOLSearchRecord>();

    public void setList(List<MKOLSearchRecord> list) {
      this.list = list;
      notifyDataSetChanged();
    }

    @Override public int getGroupCount() {
      return list.size();
    }

    @Override public int getChildrenCount(int groupPosition) {
      ArrayList<MKOLSearchRecord> child = list.get(groupPosition).childCities;
      return child == null ? 0 : child.size();
    }

    @Override public Object getGroup(int groupPosition) {
      return list.get(groupPosition);
    }

    @Override public Object getChild(int groupPosition, int childPosition) {
      return list.get(groupPosition).childCities.get(childPosition);
    }

    @Override public long getGroupId(int groupPosition) {
      return groupPosition;
    }

    @Override public long getChildId(int groupPosition, int childPosition) {
      return groupPosition * list.size() + childPosition;
    }

    @Override public boolean hasStableIds() {
      return true;
    }

    @Override public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
        ViewGroup parent) {
      ViewHolder holder;

      if (convertView == null) {
        convertView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_off_city, null);
        holder = new ViewHolder();
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
        holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
        holder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      MKOLSearchRecord record = list.get(groupPosition);

      holder.tv_name.setText(record.cityName + getLocalInfo(record.cityID));

      holder.tv_size.setText(String.format("%.1fM  ", record.size * 1.0 / MB));

      if (record.childCities != null && record.childCities.size() > 0) {
        holder.iv_flag.setImageResource(
            isExpanded ? R.mipmap.icon_arrow_up : R.mipmap.icon_arrow_down);
      } else {
        holder.iv_flag.setImageResource(R.mipmap.localmap_citylist_download_btn_enabled);
      }

      return convertView;
    }

    @Override public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
        View convertView, ViewGroup parent) {
      ViewHolder holder;

      if (convertView == null) {
        convertView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_off_city, null);
        holder = new ViewHolder();
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
        holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
        holder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      MKOLSearchRecord record = list.get(groupPosition).childCities.get(childPosition);
      holder.tv_name.setText("    " + record.cityName + getLocalInfo(record.cityID));
      holder.tv_size.setText(String.format("%.1fM  ", record.size * 1.0 / MB));

      holder.iv_flag.setImageResource(R.mipmap.localmap_citylist_download_btn_enabled);

      return convertView;
    }

    @Override public boolean isChildSelectable(int groupPosition, int childPosition) {
      return true;
    }

    class ViewHolder {
      public TextView tv_name;
      public TextView tv_status;
      public TextView tv_size;
      public ImageView iv_flag;
    }
  }

  //
  class DownLoadListAdapter extends BaseAdapter
      implements PinnedSectionListView.PinnedSectionListAdapter {

    private List<MKOLUpdateElement> list = new ArrayList<MKOLUpdateElement>();
    private int lastType;

    public void setList(List<MKOLUpdateElement> list) {
      this.list = list;
      notifyDataSetChanged();
    }

    @Override public int getViewTypeCount() {
      return 2;
    }

    @Override public int getItemViewType(int position) {
      MKOLUpdateElement element = list.get(position);

      return element.cityID < 0 ? 1 : 0;
    }

    @Override public boolean isItemViewTypePinned(int viewType) {
      return viewType == 1;
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

      int type = getItemViewType(position);
      MKOLUpdateElement element = list.get(position);
      if (type != lastType) {
        convertView = null;
      }

      if (type == 0) {
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
          holder.ll_info = (LinearLayout) convertView.findViewById(R.id.ll_info);
          convertView.setTag(holder);
        } else {
          holder = (DownloadViewHolder) convertView.getTag();
        }

        holder.tv_name.setText(
            String.format("%1$s  %2$.1fM", element.cityName, element.serversize * 1.0 / MB));

        if (element.ratio == 100) {
          holder.tv_status.setTextColor(getResources().getColor(R.color.text_black_1));
          holder.tv_status.setText(element.update ? "有更新" : "已下载");
          holder.btn_download.setEnabled(element.update);
        } else {
          holder.tv_status.setTextColor(getResources().getColor(
              element.status == MKOLUpdateElement.DOWNLOADING ? R.color.ics_blue_dark
                  : R.color.ics_red_dark));
          String status = "";
          switch (element.status) {
            case MKOLUpdateElement.DOWNLOADING:
              status = "正在下载";
              break;
            case MKOLUpdateElement.SUSPENDED:
              status = "已暂停";
              break;
            case MKOLUpdateElement.WAITING:
              status = "等待下载";
              break;
            default:
              break;
          }
          holder.tv_status.setText(String.format("%1$s %2$d%%", status, element.ratio));
        }

        final DownloadViewHolder finalHolder = holder;
        holder.ll_info.setOnClickListener(new View.OnClickListener() {
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

        holder.btn_download.setOnClickListener(new ButtonClickListener(position));
        holder.btn_del.setOnClickListener(new ButtonClickListener(position));
      } else {
        convertView =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_textview, null);
        TextView tv = (TextView) convertView.findViewById(R.id.textView);
        tv.setText(element.cityName);
      }

      lastType = type;
      return convertView;
    }

    class ButtonClickListener implements View.OnClickListener {

      private int pos;

      public ButtonClickListener(int pos) {
        this.pos = pos;
      }

      @Override public void onClick(View v) {
        int cityId = list.get(pos).cityID;
        if (v.getId() == R.id.btn_download) {
          startDownload(cityId);
        } else {
          delOffline(cityId);
        }
      }
    }

    class DownloadViewHolder {
      public TextView tv_name;
      public TextView tv_status;
      public ImageButton ibtn_span;
      public LinearLayout ll_do;
      public Button btn_download;
      public Button btn_del;
      public LinearLayout ll_info;
    }
  }
}
