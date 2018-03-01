public class MessageService extends Service{
  public void a(Context paramContext){
    Intent intent=new Intent(paramContext.this, MessageService.class);
    startService(intent);
  }
  public void b(Context paramContext){
    Intent intent=new Intent(paramContext.this, MessageService.class);
    startService(intent);
  }
  public void m(Context paramContext){
    Intent intent=new Intent(paramContext.this, MessageService.class);
    startService(intent);
  }
  public int onStartCommand(Intent paramIntent,int paramInt1,int paramInt2){
    . . . . . .
    stopSelf();
    return 1;
  }
}
