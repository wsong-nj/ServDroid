public class OemIntentReceiver extends BroadcastReceiver{
  public static void a(Context paramContext){
    localIntent.setClassName("com.twitter.twitteroemhelper",
     "com.twitter.twitteroemhelper.OemHelperService");
    paramContext.startService(localIntent);
  }
}
