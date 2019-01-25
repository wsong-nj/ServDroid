public class OverlayService extends Service{
  public static void a(Context paramContext){
    paramContext.startService(new Intent(paramContext, OverlayService.
      class));
  }
  public int onStartCommand(Intent paramIntent, int paramInt1, int 
    paramInt2){
    return;
  }
  public static void b(Context paramContext){
    paramContext.stopService(new Intent(paramContext, OverlayService.
      class));
  }
}