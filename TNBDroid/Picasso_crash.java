public View getView(int position, View convertView, 
  ViewGroup parent){
    Picasso.with(this.context)
      .load((String) this.appStoreLinks.get(position))
      .placeholder((int) C0487R.drawable
      .progress_animation).noFade()
      .resize((int) this.scrWidth)/2, (int) this.newHeight)
      .centerCrop().into(calculatorImage);
   ......
}

