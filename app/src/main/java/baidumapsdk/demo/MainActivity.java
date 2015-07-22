package baidumapsdk.demo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import baidumapsdk.demo.util.BDLocUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class MainActivity extends AppCompatActivity {

  private TextView tvHello;
  private ListView listView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    tvHello = (TextView) findViewById(R.id.tvHello);
    listView = (ListView) findViewById(R.id.listView);

    ArrayAdapter<String> arrayAdapter =
        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
            getResources().getStringArray(R.array.array_menu));
    listView.setAdapter(arrayAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0:
            Intent intent = new Intent(MainActivity.this, PoiSearchActivity.class);

            MainActivity.this.startActivity(intent);
            break;
          default:
            break;
        }
      }
    });

    BDLocUtil.registerLocListener(new BDLocationListener() {
      @Override public void onReceiveLocation(BDLocation bdLocation) {
        tvHello.setText(BDLocUtil.getLocationInfo(bdLocation));
      }
    });

    BDLocUtil.startLocation();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
