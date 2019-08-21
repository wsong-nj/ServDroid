public static void download(final Activity context, Wallpaper wallpaper, 
  final boolean apply){
    mContextCache = context;
    mWallpaperCache = wallpaper;
    mApplyCache = apply;
    if (Assent.isPermissionGranted(AssentBase.WRITE_EXTERNAL_STORAGE)
      ||apply){
        File saveFolder;
        String name;
        String extension = wallpaper.url.toLowerCase(Locale.getDefault()).
        endsWith(".png") ? "png" : "jpg";  
      ......
    }
}