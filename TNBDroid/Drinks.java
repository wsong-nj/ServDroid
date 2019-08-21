void lambda$setupViews$0(View v){
  Intent intent = new Intent("android.intent.action.VIEW");
  intent.setData(Uri.parse(this.drink.getWikipedia()));
  startActivity(intent);
    }
}