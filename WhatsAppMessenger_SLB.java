public static class ExternalMediaStateReceiver extends 
BroadcastReceiver{
  public void onReceive(Context paramContext, Intent paramIntent){
    paramContext.startService(paramIntent.setClass(paramContext,
    ExternalMediaManager.class));
  }
}
