package org.techtown.dotoristagram.util;

import android.net.Uri;

public class imageEditItem {



    private Uri image_uri = null;   //갤러리 용 uri경로


    public imageEditItem(Uri image_uri) {
        this.image_uri = image_uri;
    }



    public Uri getImage_uri() {
        return image_uri;
    }



    public void setImage_uri(Uri image_uri) {
        this.image_uri = image_uri;
    }


}


