package neto.lobo.denuncias.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.views.activities.DenunciaActivity;

public class DenunciaAlertDialog extends AlertDialog {
    protected DenunciaAlertDialog(@NonNull Context context) {
        super(context);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState, View view, final Activity activity) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view)
                .setPositiveButton(R.string.go_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(), "deu certo", Toast.LENGTH_LONG).show();
                        /*Intent intent = new Intent(activity, DenunciaActivity.class);
                        activity.startActivity(intent);*/
                    }
                })
                .setNegativeButton(R.string.back_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
