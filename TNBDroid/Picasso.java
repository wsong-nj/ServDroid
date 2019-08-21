public RequestCreator load(String path){
        if (path == null){
            return new RequestCreator(this, null, 0);
        }
        if (path.trim().length() != 0){
            return load(Uri.parse(path));
        }
        throw new IllegalArgumentException("Path must not be empty.");
}
