package neto.lobo.denuncias.util;

import android.graphics.Bitmap;

import youubi.common.to.ImageOriginalTO;
import youubi.common.to.ImagePreviewTO;



public class ImagesYouubi {

    private ImageOriginalTO imageOriginal;
    private ImagePreviewTO imagePreview;
    private Bitmap bitmapOriginal;
    private Bitmap bitmapPreview;


    public ImagesYouubi() {
        imageOriginal = null;
        imagePreview  = null;
        bitmapOriginal = null;
        bitmapPreview  = null;
    }

    //------------------------------------------------------------------------------
    // Getters and Setters
    //------------------------------------------------------------------------------
    public ImageOriginalTO getImageOriginal() {
        return imageOriginal;
    }
    public void setImageOriginal(ImageOriginalTO imageOriginal) {
        this.imageOriginal = imageOriginal;
    }

    public ImagePreviewTO getImagePreview() {
        return imagePreview;
    }
    public void setImagePreview(ImagePreviewTO imagePreview) {
        this.imagePreview = imagePreview;
    }

    public Bitmap getBitmapOriginal() {
        return bitmapOriginal;
    }
    public void setBitmapOriginal(Bitmap bitmapOriginal) {
        this.bitmapOriginal = bitmapOriginal;
    }

    public Bitmap getBitmapPreview() {
        return bitmapPreview;
    }
    public void setBitmapPreview(Bitmap bitmapPreview) {
        this.bitmapPreview = bitmapPreview;
    }

}