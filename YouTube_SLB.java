class jth extends BroadcastReceiver{
  public void onReceive(Context paramContext, Intent paramIntent){
    localIntent.setComponent(new ComponentNam(localContext,"com.
    google.android.gms.analytics.AnalyticsService"));
    localContext.startService(localIntent);
  }
}
