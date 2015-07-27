package baidumapsdk.demo.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import baidumapsdk.demo.R;
import java.io.File;

/**
 * AndroidHelper
 * AUTHOR:Zhaoyy  2015/6/27
 * DESC:
 **/
public class AndroidHelper {

  private static String TAG = "AndroidHelper";

  private static Context mContext;
  private static File cacheDirPath;

  private static File splashCacheDirPath;

  private static ProgressDialog progressDialog;

  private static final int IMAGE_CACHE_SIZE = 30 * 1024 * 1024;// max cache size by byte
  private static final int CACHE_SIZE = 10 * 1024 * 1024;// max cache size by byte

  public static void init(Context context) {
    mContext = context;
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      cacheDirPath = mContext.getExternalCacheDir();
    } else {
      cacheDirPath = mContext.getCacheDir();
    }
  }

  public static String getVersionName(Context context) {
    PackageInfo info = getPackageInfo(context, context.getPackageName());

    return info == null ? null : info.versionName;
  }

  /**
   * get app version code
   */
  public static int getVersionCode(Context context) {
    PackageInfo info = getPackageInfo(context, context.getPackageName());

    return info == null ? 0 : info.versionCode;
  }

  public static PackageInfo getPackageInfo(Context context, String packageName) {
    PackageManager manager = context.getPackageManager();
    try {
      PackageInfo info =
          manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
      return info;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void showProgressDialog(Context context, String msg) {
    progressDialog = new ProgressDialog(context);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setMessage(msg);
    progressDialog.show();
  }

  public static void showProgressDialog(Context context) {
    showProgressDialog(context, "");
  }

  public static void setProgressDialogMsg(String msg) {
    if (progressDialog == null) return;

    progressDialog.setMessage(msg);
  }

  public static void hideProgressDialog() {
    if (progressDialog == null) return;

    progressDialog.dismiss();
  }

  public static int dp2px(Activity activity, float dp) {
    DisplayMetrics metrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

    return (int) (dp * metrics.density + 0.5f);
  }

  public static void sleep(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static SpannableString setSpannableTextColor(String text, String key) {
    if (!text.contains(key)) {
      throw new RuntimeException("not found key!");
    }

    int start = text.indexOf(key);
    int end = start + key.length();

    return setSpannableTextColor(text, start, end);
  }

  private static SpannableString setSpannableTextColor(String text, int start, int end) {
    if (start < 0 || end >= text.length()) {
      throw new RuntimeException("out of index");
    }

    SpannableString spannableString = new SpannableString(text);
    ForegroundColorSpan span =
        new ForegroundColorSpan(mContext.getResources().getColor(R.color.ics_blue_dark));

    spannableString.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    return spannableString;
  }

  public static void telephone_call(Context context, String num) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(Uri.parse("tel:" + num));
    context.startActivity(intent);
  }
}
