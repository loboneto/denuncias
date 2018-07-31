package neto.lobo.denuncias.views.fragments;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import neto.lobo.denuncias.R;
import youubi.common.to.ContentTO;

public class DenunciaDialogFragment extends DialogFragment {
    private FragmentManager fm;
    private TextView textDesc;
    private ContentTO content;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Recupera o objeto
        if (getArguments() != null) {
            content = (ContentTO) getArguments().getSerializable("content");
        } else
            Log.d("DialogContent", "O conteúdo veio vazio");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_teste,null);
        builder.setView(view)
                .setPositiveButton(R.string.back_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.go_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        this.textDesc = view.findViewById(R.id.textDescription);

        this.textDesc.setText(content.getPersonTO().getNameFirst() + ": "+
                                content.getDescription());

        return builder.create();
    }

    public void setFragmentManager(FragmentManager fm){
        this.fm = fm;
    }
}
