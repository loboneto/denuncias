package neto.lobo.denuncias.views.fragments;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerFile;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.DenunciaActivity;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.PersonContentTO;
import youubi.common.to.ResultTO;

public class DenunciaDialogFragment extends DialogFragment {

    private FragmentManager fm;
    private TextView textDesc;
    private ImageView imgVDialogDenuncia;
    private ManagerRest managerRest;
    private Button apoiarDialog;

    private ContentTO content;

    private DataBaseLocal dataBaseLocal;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        managerRest = new ManagerRest(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_denuncia_dialog,null);

        dataBaseLocal = DataBaseLocal.getInstance(getActivity());

        imgVDialogDenuncia = view.findViewById(R.id.imgVDialogDenuncia);
        textDesc = view.findViewById(R.id.textDescription);

        apoiarDialog = view.findViewById(R.id.apoiarDialog);
        apoiarDialog.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_POS);

                if(resultTO.getCode() == ConstResult.CODE_OK){
                    Toast.makeText(getContext(), "Apoiado!", Toast.LENGTH_LONG).show();
                    apoiarDialog.setTextColor(getContext().getColor(R.color.grey));
                }else{
                    resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_ZERO);

                    if(resultTO.getCode() == ConstResult.CODE_OK){
                        Toast.makeText(getContext(), "Desapoido", Toast.LENGTH_LONG).show();
                        apoiarDialog.setTextColor(getContext().getColor(R.color.colorAccent));
                    }else{
                        Toast.makeText(getContext(), "Erro!", Toast.LENGTH_LONG).show();
                    }
                }


            }
        });

        try {
            if(content.getPersonContentTO() != null){
                if(content.getPersonContentTO().getRatePerson() == 1 ){
                    apoiarDialog.setTextColor(getContext().getColor(R.color.grey));
                }
            }
        }catch (Exception e){

        }



        //Recupera o objeto
        if (getArguments() != null) {

            long idContent = getArguments().getLong("contentId");
            content = dataBaseLocal.getContent(idContent);
            //content = (ContentTO) getArguments().getSerializable("content");
        } else
            Log.d("DialogContent", "O conteúdo veio vazio");



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.go_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                         Intent intent = new Intent(getContext(), DenunciaActivity.class);
                         Bundle bun = new Bundle();
                         bun.putLong("contentId", content.getId());
                         intent.putExtras(bun);
                         getActivity().startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.back_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });



        textDesc.setText(content.getPersonTO().getNameFirst() + ": "+ content.getDescription());

        if(content.getImagePreviewPhotoTO() != null && !content.getImagePreviewPhotoTO().getData().isEmpty()){
            imgVDialogDenuncia.setImageBitmap(ManagerFile.stringToBitmap(content.getImagePreviewPhotoTO().getData()));
        }



        return builder.create();
    }

    public void setFragmentManager(FragmentManager fm){
        this.fm = fm;
    }

}
