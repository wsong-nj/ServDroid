public final class GoogleDriveActivity extends apf implements c.b, e.a{
  public final void onCreate(Bundle paramBundle){
    Intent localIntent = getIntent();
    onNewIntent(localIntent);
    . . . . . . 
    getApplicationContext().bindService(new Intent(this, 
      GoogleDriveService.class), this.af, 1);
  }
  protected final void onNewIntent(Intent paramIntent){
    m();
  }
  final void m(){
    Intent localIntent = new Intent(this, GoogleDriveService.class);
    getApplicationContext().startService(localIntent);
  }
}
