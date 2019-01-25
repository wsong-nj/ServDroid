public class ArtMonitorImpl{
  public void startMonitoring(){
    ...
    this.mContext.bindService(new Intent(this.mContext, 
    ArtDownloadService.class), this.mServiceConnection, 5);
    ...    	
  }	   
}