package baidumapsdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import baidumapsdk.demo.util.AndroidHelper;
import baidumapsdk.demo.util.BDLocUtil;
import baidumapsdk.demo.util.ToastHelper;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private LinearLayout ll_search;
  private ImageButton ibtn_back;
  private EditText et_search;
  private Button btn_search;
  private Button btn_oil;
  private Button btn_park;
  private Button btn_hotel;
  private Button btn_food;
  private ListView listView;

  private BDLocation currentLoc;
  private MHandler mHandler = new MHandler();

  private SuggestionSearch suggestionSearch;
  private List<SuggestionResult.SuggestionInfo> suggestionInfos =
      new ArrayList<SuggestionResult.SuggestionInfo>();

  private SuggestionAdapter adapter;
  private String key = "";

  private Intent intent;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ll_search = (LinearLayout) findViewById(R.id.ll_search);
    ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
    et_search = (EditText) findViewById(R.id.et_search);
    btn_search = (Button) findViewById(R.id.btn_search);
    btn_oil = (Button) findViewById(R.id.btn_oil);
    btn_park = (Button) findViewById(R.id.btn_park);
    btn_hotel = (Button) findViewById(R.id.btn_hotel);
    btn_food = (Button) findViewById(R.id.btn_food);
    listView = (ListView) findViewById(R.id.listView);

    ibtn_back.setOnClickListener(this);
    btn_search.setOnClickListener(this);
    btn_oil.setOnClickListener(this);
    btn_park.setOnClickListener(this);
    btn_hotel.setOnClickListener(this);
    btn_food.setOnClickListener(this);

    et_search.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override public void afterTextChanged(Editable s) {
        key = s.toString();
        if (TextUtils.isEmpty(key)) {
          loadSearchHistory();
          return;
        }

        suggestionSearch.requestSuggestion(
            new SuggestionSearchOption().keyword(key).city(currentLoc.getCity()));
      }
    });

    ArrayAdapter<String> arrayAdapter =
        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
            getResources().getStringArray(R.array.array_menu));
    listView.setAdapter(arrayAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SuggestionResult.SuggestionInfo info =
            (SuggestionResult.SuggestionInfo) adapter.getItem(position);

        gotoSearchResult(info);
      }
    });

    adapter = new SuggestionAdapter();
    listView.setAdapter(adapter);

    suggestionSearch = SuggestionSearch.newInstance();

    suggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
      @Override public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult == null
            || suggestionResult.getAllSuggestions() == null) {
          return;
        }

        suggestionInfos = suggestionResult.getAllSuggestions();

        adapter.setData(suggestionInfos);
      }
    });

    BDLocUtil.startLocation();

    new CheckLocThread().start();
  }

  private void loadSearchHistory() {
    // todo: load search history
    suggestionInfos = new ArrayList<SuggestionResult.SuggestionInfo>();
    adapter.setData(suggestionInfos);
  }

  private void gotoSearchResult(String key) {

    if (TextUtils.isEmpty(key)) {
      ToastHelper.toastShort(this, "请填写要查询的内容");
      return;
    }

    Intent intent = getPoiResultIntent();
    intent.putExtra("key", key);

    startActivity(intent);
  }

  private void gotoSearchResult(SuggestionResult.SuggestionInfo info) {
    Intent intent = getPoiResultIntent();
    intent.putExtra("key", info.key);
    intent.putExtra("city", info.city);
    intent.putExtra("district", info.district);

    startActivity(intent);
  }

  private Intent getPoiResultIntent() {
    if (intent == null) {
      intent = new Intent(this, PoiSearchActivity.class);
      if (currentLoc != null) {
        intent.putExtra("clat", currentLoc.getLatitude());
        intent.putExtra("clon", currentLoc.getLongitude());
      }
    }
    return intent;
  }

  private void getLocation() {

    currentLoc = BDLocUtil.getLastLocation();

    if (currentLoc == null || currentLoc.getLatitude() < 1 || currentLoc.getLongitude() < 1) {
      Toast.makeText(this, "获取当前位置失败，请检查是否禁用应用获取位置的权限！", Toast.LENGTH_SHORT).show();
      et_search.setEnabled(false);
      return;
    }

    et_search.setEnabled(true);
    et_search.requestFocus();
    if (TextUtils.isEmpty(currentLoc.getStreet())) {
      et_search.setHint(String.format("在%s附件搜索", currentLoc.getCity()));
    } else {
      et_search.setHint(String.format("在%s附件搜索", currentLoc.getStreet()));
    }
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.ibtn_back:
        onBackPressed();
        break;
      case R.id.btn_search:
        gotoSearchResult(key);
        break;
      case R.id.btn_food:
        gotoSearchResult("饭店");
        break;
      case R.id.btn_hotel:
        gotoSearchResult("旅馆");
        break;
      case R.id.btn_oil:
        gotoSearchResult("加油站");
        break;
      case R.id.btn_park:
        gotoSearchResult("停车场");
        break;
      default:
        break;
    }
  }

  @Override protected void onDestroy() {
    if (suggestionSearch != null) {
      suggestionSearch.destroy();
    }
    super.onDestroy();
  }

  class SuggestionAdapter extends BaseAdapter {

    private List<SuggestionResult.SuggestionInfo> data =
        new ArrayList<SuggestionResult.SuggestionInfo>();

    public void setData(List<SuggestionResult.SuggestionInfo> data) {
      this.data = data;
      notifyDataSetChanged();
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

      ViewHolder holder = null;

      if (convertView == null) {
        convertView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_suggestion, parent, false);
        holder = new ViewHolder();
        holder.tv_key = (TextView) convertView.findViewById(R.id.tv_key);
        holder.tv_detail = (TextView) convertView.findViewById(R.id.tv_detail);
        holder.iv = (ImageView) convertView.findViewById(R.id.iv);

        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      SuggestionResult.SuggestionInfo info = data.get(position);

      if (TextUtils.isEmpty(key)) {
        holder.tv_key.setText(info.key);
      } else {
        if (info.key.contains(key)) {
          holder.tv_key.setText(AndroidHelper.setSpannableTextColor(info.key, key));
        } else {
          holder.tv_key.setText(info.key);
        }
      }


      String detail = info.city + info.district;

      if (TextUtils.isEmpty(detail)) {
        holder.tv_detail.setVisibility(View.GONE);
        holder.iv.setImageResource(R.mipmap.common_icon_searchbox_magnifier_new);
      } else {
        holder.tv_detail.setVisibility(View.VISIBLE);
        holder.tv_detail.setText(detail);
        holder.iv.setImageResource(R.mipmap.icon_menzhi);
      }

      return convertView;
    }

    class ViewHolder {
      public TextView tv_key;
      public TextView tv_detail;
      public ImageView iv;
    }
  }

  class CheckLocThread extends Thread {
    private static final int MAX_TRY_NUM = 30;

    @Override public void run() {
      int t = 0;
      while (!BDLocUtil.hasGetLocation() && t < MAX_TRY_NUM) {
        AndroidHelper.sleep(1000);
        t++;
      }

      mHandler.sendEmptyMessage(0);
    }
  }

  class MHandler extends Handler {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case 0:
          getLocation();
          break;
        default:
          break;
      }
    }
  }
}
