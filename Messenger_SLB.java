public class MpActivity extends Activity{
  public final void a(g paramg){
    startService(new Intent(this, MpService.class));
     . . . . . 
    if(b()){
      stopService(new Intent(this, MpService.class));
    }
    else{
       . . . . .
    }
  }
}
