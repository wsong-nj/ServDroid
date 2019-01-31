final class ahx extends agj implements ServiceConnection{
  final void b(){
    d();
    . . . . . . 
    f();
    c();
    . . . . . .
    e();
  }
  final void d(){
    localIntent = new Intent("android.media.
    MediaRouteProviderService");
    this.o = this.a.bindService(localIntent, this, 1);
  }
  final void f(){
    localahy.h.j.post(new ahz(localahy));
  }
  final void e(){
    this.a.unbindService(this);
  }
}