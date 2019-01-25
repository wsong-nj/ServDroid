public class NCBlackListActivity{
  public final void run(){
    Intent intent = new Intent(context, NotificationManagerService
    .class);
    context.bindService(intent, aqL.dYV, 1);
    oj();
    arF();
    ...
    dZm.aqC();       
  }
}
public class NotificationManagerService extends Service {
  public IBinder onBind(Intent intent) {
    ...
    return this.dZm;
  }
}
