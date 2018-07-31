package neto.lobo.denuncias.common;

import android.app.ProgressDialog;
import android.content.Context;

import neto.lobo.denuncias.views.activities.LoginActivity;
import youubi.common.to.ContentTO;

public class Common {

    public static ContentTO contentSelected;


    public void loading(boolean input, Context context){
        final ProgressDialog progressDialog = new ProgressDialog(context);

        if(input){
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Carregando...");
            progressDialog.show();
        }else{
            progressDialog.cancel();
        }
    }
}
