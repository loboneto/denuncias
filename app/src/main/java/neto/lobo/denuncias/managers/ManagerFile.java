package neto.lobo.denuncias.managers;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import neto.lobo.denuncias.util.ImagesYouubi;
import neto.lobo.denuncias.constants.ConstAndroid;
import youubi.common.constants.ConstModel;
import youubi.common.to.ImageOriginalTO;
import youubi.common.to.ImagePreviewTO;


public class ManagerFile {


    /**
     * Produz ImageOriginalTO e ImagePreviewTO
     */
    public static ImagesYouubi buildImageUbi(int requestCode, Intent data, Context context, ImageOriginalTO imageOriginalTO, ImagePreviewTO imagePreviewTO)
    {
        System.gc(); // Tenta limpar memoria

        ImagesYouubi imagesYouubi = null;

        if(imageOriginalTO == null) {
            imageOriginalTO = new ImageOriginalTO();
        }

        if(imagePreviewTO == null) {
            imagePreviewTO  = new ImagePreviewTO();
        }

        // Adiciona Metadados
        switch (requestCode)
        {
            case ConstAndroid.REQUEST_IMAGE_CAMERA_PHOTO_PROFILE:
                imageOriginalTO.setOrigin(ConstModel.FILE_ORIGIN_CAMERA);
                break;
            case ConstAndroid.REQUEST_IMAGE_UPLOAD_PHOTO_PROFILE:
                imageOriginalTO.setOrigin(ConstModel.FILE_ORIGIN_UPLOAD);
                break;
        }

        Bitmap bitmapOriginal = null;
        Bitmap bitmapPreview = null;

        Uri uriImageTaked = data.getData();


        // Pega Bitmap
        if(uriImageTaked == null) {

            Bundle bundle = data.getExtras();
            bitmapOriginal = (Bitmap) bundle.get("data");

        } else {
            try {

                bitmapOriginal = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uriImageTaked);

                // Rotaciona imagem
                int orientation = getOrientation(context, uriImageTaked);
                bitmapOriginal = rotateBitmap(bitmapOriginal, orientation);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }


        // Reduz imagem
        bitmapOriginal = ManagerFile.resizeBitmap(bitmapOriginal, ConstAndroid.IMAGE_SIZE_ORIGINAL_H, ConstAndroid.IMAGE_SIZE_ORIGINAL_W);

        // Produz Preview
        bitmapPreview = ManagerFile.resizeBitmap(bitmapOriginal, ConstAndroid.IMAGE_SIZE_PREVIEW_H, ConstAndroid.IMAGE_SIZE_PREVIEW_W);

        // Corta imagem
        bitmapPreview = cropBitmap(bitmapPreview);

        if (bitmapOriginal != null && bitmapPreview != null) {

            // bitmap -> byteArray
            ByteArrayOutputStream streamOriginal = new ByteArrayOutputStream();
            ByteArrayOutputStream streamPreview = new ByteArrayOutputStream();
            bitmapOriginal.compress(Bitmap.CompressFormat.PNG, ConstAndroid.IMAGE_QUALITY_ORIGINAL, streamOriginal);
            bitmapPreview.compress(Bitmap.CompressFormat.PNG, ConstAndroid.IMAGE_QUALITY_PREVIEW, streamPreview);
            String stringDataOriginal = Base64.encodeToString(streamOriginal.toByteArray(), Base64.NO_WRAP);
            String stringDataPreview = Base64.encodeToString(streamPreview.toByteArray(), Base64.NO_WRAP);


            // Preenche atributos de ImageOriginalTO
            imageOriginalTO.setData(stringDataOriginal);
            imageOriginalTO.setHeight(bitmapOriginal.getHeight());
            imageOriginalTO.setWidth(bitmapOriginal.getWidth());
            imageOriginalTO.setBytes(stringDataOriginal.length());
            imageOriginalTO.setTypeElem(ConstModel.ELEM_PERSON);

            // Preenche atributos de ImagePreviewTO (que sera usado nas listas)
            imagePreviewTO.setData(stringDataPreview);
            imagePreviewTO.setHeight(bitmapPreview.getHeight());
            imagePreviewTO.setWidth(bitmapPreview.getWidth());
            imagePreviewTO.setBytes(stringDataPreview.length());
            imagePreviewTO.setTypeElem(ConstModel.ELEM_PERSON);

            // Monta objeto de retorno
            imagesYouubi = new ImagesYouubi();
            imagesYouubi.setImageOriginal(imageOriginalTO);
            imagesYouubi.setImagePreview(imagePreviewTO);
            imagesYouubi.setBitmapOriginal(bitmapOriginal);
            imagesYouubi.setBitmapPreview(bitmapPreview);
        }

        return imagesYouubi;
    }



    // Metodo Atual
    public static ImageOriginalTO getImageOriginal(Context context, Uri UriImage, Bitmap original){

        Bitmap bitmapOriginal = original;
        ImageOriginalTO imageOriginalTO = null;
        //ImagePreviewTO imagePreviewTO = null;

        if(bitmapOriginal == null){

            try {
                bitmapOriginal = MediaStore.Images.Media.getBitmap(context.getContentResolver(), UriImage);
            } catch (IOException e) {
                Log.e("--->", "Catch do bitmap: " + e.getMessage());
                e.printStackTrace();
            }

        }

        if(bitmapOriginal != null) {

            ByteArrayOutputStream streamOriginal = new ByteArrayOutputStream();
            bitmapOriginal.compress(Bitmap.CompressFormat.PNG, ConstAndroid.IMAGE_QUALITY_ORIGINAL, streamOriginal);
            String stringDataOriginal = Base64.encodeToString(streamOriginal.toByteArray(), Base64.NO_WRAP);

            // Preenche atributos de ImageOriginalTO
            imageOriginalTO = new ImageOriginalTO();
            imageOriginalTO.setData(stringDataOriginal);
            imageOriginalTO.setHeight(bitmapOriginal.getHeight());
            imageOriginalTO.setWidth(bitmapOriginal.getWidth());
            imageOriginalTO.setBytes(stringDataOriginal.length());
            imageOriginalTO.setTypeElem(ConstModel.ELEM_PERSON);


        }


        return imageOriginalTO;
    }



//    public static ImagesYouubi buildImage(int requestCode, Intent data, Context context, ImageOriginalTO imageOriginalTO, ImagePreviewTO imagePreviewTO){
//
//        System.gc(); // Tenta limpar memoria
//
//        ImagesYouubi imagesYouubi = null;
//        Bitmap bitmapOriginal = null;
//        Bitmap bitmapPreview = null;
//
//        if(imageOriginalTO == null) {
//            imageOriginalTO = new ImageOriginalTO();
//        }
//
//        if(imagePreviewTO == null) {
//            imagePreviewTO  = new ImagePreviewTO();
//        }
//
//        switch (requestCode) {
//            case ConstAndroid.REQUEST_IMAGE_CAMERA_PHOTO_PROFILE:
//                imageOriginalTO.setOrigin(ConstModel.FILE_ORIGIN_CAMERA);
//                break;
//            case ConstAndroid.REQUEST_IMAGE_UPLOAD_PHOTO_PROFILE:
//                imageOriginalTO.setOrigin(ConstModel.FILE_ORIGIN_UPLOAD);
//                break;
//        }
//
//
//
//        File imageFile = getTempFile(context);
//
//        Uri selectedImage;
//        boolean isCamera = (data == null ||
//                data.getData() == null  ||
//                data.getData().toString().contains(imageFile.toString()));
//        if (isCamera) {     /** CAMERA **/
//            selectedImage = Uri.fromFile(imageFile);
//        } else {            /** ALBUM **/
//            selectedImage = data.getData();
//        }
//
//
//        bitmapOriginal = getImageResized(context, selectedImage);
//        int rotation = getRotation(context, selectedImage, isCamera);
//        bitmapOriginal = rotate(bitmapOriginal, rotation);
//
//        bitmapPreview = ManagerFile.resizeBitmap(bitmapOriginal, ConstAndroid.IMAGE_SIZE_PREVIEW_H, ConstAndroid.IMAGE_SIZE_PREVIEW_W);
//
//        ByteArrayOutputStream streamOriginal = new ByteArrayOutputStream();
//        ByteArrayOutputStream streamPreview = new ByteArrayOutputStream();
//
//        bitmapOriginal.compress(Bitmap.CompressFormat.PNG, ConstAndroid.IMAGE_QUALITY_ORIGINAL, streamOriginal);
//        bitmapPreview.compress(Bitmap.CompressFormat.PNG, ConstAndroid.IMAGE_QUALITY_PREVIEW, streamPreview);
//
//        String stringDataOriginal = Base64.encodeToString(streamOriginal.toByteArray(), Base64.NO_WRAP);
//        String stringDataPreview = Base64.encodeToString(streamPreview.toByteArray(), Base64.NO_WRAP);
//
//
//        // Preenche atributos de ImageOriginalTO
//        imageOriginalTO.setData(stringDataOriginal);
//        imageOriginalTO.setHeight(bitmapOriginal.getHeight());
//        imageOriginalTO.setWidth(bitmapOriginal.getWidth());
//        imageOriginalTO.setBytes(stringDataOriginal.length());
//        imageOriginalTO.setTypeElem(ConstModel.ELEM_PERSON);
//
//        // Preenche atributos de ImagePreviewTO (que sera usado nas listas)
//        imagePreviewTO.setData(stringDataPreview);
//        imagePreviewTO.setHeight(bitmapPreview.getHeight());
//        imagePreviewTO.setWidth(bitmapPreview.getWidth());
//        imagePreviewTO.setBytes(stringDataPreview.length());
//        imagePreviewTO.setTypeElem(ConstModel.ELEM_PERSON);
//
//        // Monta objeto de retorno
//        imagesYouubi = new ImagesYouubi();
//        imagesYouubi.setImageOriginal(imageOriginalTO);
//        imagesYouubi.setImagePreview(imagePreviewTO);
//        imagesYouubi.setBitmapOriginal(bitmapOriginal);
//        imagesYouubi.setBitmapPreview(bitmapPreview);
//
//
//
//        return imagesYouubi;
//    }
//
//    private static File getTempFile(Context context) {
//        File imageFile = new File(context.getExternalCacheDir(), "tempImage");
//        imageFile.getParentFile().mkdirs();
//        return imageFile;
//    }



    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            i++;
        } while (bm.getWidth() < 400 && i < sampleSizes.length);
        return bm;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
    }


    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }

        return rotation;
    }

    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {

            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return bmOut;
        }
        return bm;
    }

    //----------------------------------

    /**
     * Converte byte[] para Bitmap
     */
    public static Bitmap stringToBitmap(String str) {

        System.gc(); // Tenta limpar memoria

        byte[] byteArray = Base64.decode(str, Base64.NO_WRAP);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        } catch (Exception e){
            Log.e("--->", "Erro no decoder Bitmap: " + e.getMessage());
        }

        return bitmap;
    }


    /**
     * AUXILIAR: Corta imagem para ela ficar quadrada.
     */
    private static Bitmap cropBitmap(Bitmap bitmapSource)
    {
        System.gc(); // Tenta limpar memoria

        int width = bitmapSource.getWidth();
        int height = bitmapSource.getHeight();
        int crop = 0;
        Bitmap bitmapCroped = null;

        if(width > height)
        {
            crop = (width - height) / 2;
            bitmapCroped = Bitmap.createBitmap(bitmapSource, crop, 0, height, height);
        }
        else
        {
            if(height > width)
            {
                crop = (height - width) / 2;
                bitmapCroped = Bitmap.createBitmap(bitmapSource, 0, crop, width, width);
            }
            else
            {
                bitmapCroped = Bitmap.createBitmap(bitmapSource, 0, 0, height, height);
            }
        }

        System.gc(); // Tenta limpar memoria

        return bitmapCroped;
    }

    /**
     * AUXILIAR: reduz tamanho da imagem.
     */
    private static Bitmap resizeBitmap(Bitmap bitmapOriginal, float maxSideH, float maxSideW)
    {
        System.gc(); // Tenta limpar memoria

        int height = bitmapOriginal.getHeight();
        int width = bitmapOriginal.getWidth();

        int newHeight = 0;
        int newWidth = 0;

        if(height > width) {
            newWidth = (int) (width / ((float) height / (float) maxSideH ));
            newHeight = (int) maxSideH;
        }
        else {
            newHeight = (int) (height / ((float) width / (float) maxSideW ));
            newWidth = (int) maxSideW;
        }

        // calcula a escala
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // cria matrix para manipulacao
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        int x = 0;
        int y = 0;

        //realiza o resize
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOriginal, x, y, width, height, matrix, true);

        System.gc(); // Tenta limpar memoria

        return resizedBitmap ;
    }


    /**
     * AUXILIAR: Rotaciona imagem para ficar com a orientacao correta.
     */
    private static Bitmap rotateBitmap(Bitmap bitmapOriginal, float degree)
    {
        System.gc(); // Tenta limpar memoria

        Matrix matrix = new Matrix();

        matrix.postRotate(degree);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapOriginal , 0, 0, bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), matrix, true);

        System.gc(); // Tenta limpar memoria

        return rotatedBitmap;
    }

    /**
     * AUXILIAR: Obtem a orientacao da imagem.
     */
    private static int getOrientation(Context context, Uri photoUri)
    {
        System.gc(); // Tenta limpar memoria

        Cursor cursor = context.getContentResolver().query(photoUri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();

        System.gc(); // Tenta limpar memoria

        return cursor.getInt(0);
    }


    /**
     * AUXILIAR: Obtem a imagem da URL.
     */
    public static Bitmap getBitmapfromUrl(String imageUrl) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     *  Metodo de redimensionamento
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
