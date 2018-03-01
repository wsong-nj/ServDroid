public abstract class SearchActionVerificationClientService extends Service{
  private final Intent mServiceIntent = new Inten("com.google.android.
    googlequicksearchbox.SEARCH_ACTION_VERIFICATION_SERVICE").
    setPackage("com.google.android.googlequicksearchbox");
  public final void onCreate(){
    bindService(this.mServiceIntent,
    this.mSearchActionVerificationServiceConnection, 1);
  }	
  protected final void onHandleIntent(Intent paramIntent){
    if ((bool2) && (this.mIRemoteService.
      isSearchAction(localIntent,localBundle))){
        performAction(localIntent, bool1, localBundle);
        return;
    }
  }
  public final void onDestroy(){	
    unbindService(this.mSearchActionVerificationServiceConnection);
  }
}




