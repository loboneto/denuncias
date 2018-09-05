package neto.lobo.denuncias.views.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerFile;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.DenunciaActivity;
import neto.lobo.denuncias.views.activities.LoginActivity;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.PersonContentTO;
import youubi.common.to.ResultTO;

public class DenunciaAdpter extends RecyclerView.Adapter<DenunciaAdpter.ViewHolderDenuncia> {

    private List<ContentTO> listDenuncia;
    private boolean hasSupport;

    private ManagerRest managerRest;
    private ManagerPreferences managerPreferences;
    private DataBaseLocal dataBaseLocal;
    private Context context;

    public DenunciaAdpter(List<ContentTO> list, Context context){
        this.listDenuncia = list;
        this.context = context;

        this.dataBaseLocal = DataBaseLocal.getInstance(context);
        this.managerRest = new ManagerRest(context);
        this.managerPreferences = new ManagerPreferences(context);
    }
    @NonNull
    @Override
    public DenunciaAdpter.ViewHolderDenuncia onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.adpter_denuncia, parent, false);

        ViewHolderDenuncia denuncia = new ViewHolderDenuncia(view);

        return denuncia;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(final DenunciaAdpter.ViewHolderDenuncia holder, int position) {

        if(listDenuncia != null && listDenuncia.size() > 0){

            final ContentTO content = listDenuncia.get(position);

            holder.desc.setText(content.getPersonTO().getNameFirst()+": "+ content.getDescription());
            holder.data.setText(content.getDateCreation());
            holder.imgDenuncia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(holder.itemView.getContext(), DenunciaActivity.class);
                    Bundle bun = new Bundle();
                    bun.putLong("contentId", content.getId());
                    intent.putExtras(bun);
                    holder.itemView.getContext().startActivity(intent);
                }
            });

            if(content.getImagePreviewPhotoTO() != null && !content.getImagePreviewPhotoTO().getData().isEmpty()){
                holder.imgDenuncia.setImageBitmap(ManagerFile.stringToBitmap(content.getImagePreviewPhotoTO().getData()));
            }




            PersonContentTO personContentTO = dataBaseLocal.getPersonContent(managerPreferences.getId(), content.getId());
            if(personContentTO != null){

                if(personContentTO.getRatePerson() == ConstModel.RATE_POS){
                    // Tem apoiado
                    hasSupport = true;
                    holder.apoiarAdpter.setBackground(context.getResources().getDrawable(R.drawable.ic_apoiado));

                } else {
                    // nao apoiou
                    hasSupport = false;
                }

            }



            holder.apoiarAdpter.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {

                    if(hasSupport){

                        ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_ZERO);

                        if (resultTO.getCode() == ConstResult.CODE_OK) {

                            hasSupport = false;

                            Toast.makeText(context, "Desapoido", Toast.LENGTH_LONG).show();
                            holder.apoiarAdpter.setBackground(context.getResources().getDrawable(R.drawable.ic_apoio));

                            PersonContentTO personContentTO = (PersonContentTO) resultTO.getObject();
                            dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());

                        } else {
                            Toast.makeText(context, "Erro ao retirar apoio!", Toast.LENGTH_LONG).show();
                        }

                    } else {

                        ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_POS);

                        if (resultTO.getCode() == ConstResult.CODE_OK) {

                            hasSupport = true;

                            Toast.makeText(context, "Apoiado!", Toast.LENGTH_LONG).show();
                            holder.apoiarAdpter.setBackground(context.getResources().getDrawable(R.drawable.ic_apoiado));

                            PersonContentTO personContentTO = (PersonContentTO) resultTO.getObject();
                            dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());

                        } else {
                            if(resultTO.getCode() == 1017){
                                Toast.makeText(context, "Você não pode apoiar sua denuncia" + resultTO.getDescription(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Erro ao apoiar! " + resultTO.getDescription(), Toast.LENGTH_LONG).show();
                            }

                            Log.e("--->", "Result: " + resultTO.getCode() + " msg: " + resultTO.getDescription());
                        }
                    }

//                    ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_POS);
//
//                    if(resultTO.getCode() == ConstResult.CODE_OK){
//                        Toast.makeText(holder.itemView.getContext(), "Apoiado!", Toast.LENGTH_LONG).show();
//                        holder.apoiarAdpter.setTextColor(holder.itemView.getContext().getColor(R.color.grey));
//                    }else{
//                        resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_ZERO);
//
//                        if(resultTO.getCode() == ConstResult.CODE_OK){
//                            Toast.makeText(holder.itemView.getContext(), "Desapoido", Toast.LENGTH_LONG).show();
//                            holder.apoiarAdpter.setTextColor(holder.itemView.getContext().getColor(R.color.colorAccent));
//                        }else{
//                            Toast.makeText(holder.itemView.getContext(), "Erro!", Toast.LENGTH_LONG).show();
//                        }
//                    }


                }
            });

            if(content.getPersonContentTO() != null)
                if(content.getPersonContentTO().getRatePerson() == 1 ){
                    holder.apoiarAdpter.setTextColor(holder.itemView.getContext().getColor(R.color.grey));
                }
        }



    }

    @Override
    public int getItemCount() {
        return listDenuncia.size();
    }

    public class ViewHolderDenuncia extends RecyclerView.ViewHolder{

        public TextView desc;
        public TextView data;
        public ImageView imgDenuncia;
        public Button apoiarAdpter;


        public ViewHolderDenuncia(final View itemView) {
            super(itemView);

            this.desc = itemView.findViewById(R.id.textDescriptionDenunciaAdpter);
            this.imgDenuncia = itemView.findViewById(R.id.imageView5Adpter);
            this.data = itemView.findViewById(R.id.textDataAdpter);
            this.apoiarAdpter = itemView.findViewById(R.id.apoiarAdpter);

        }
    }
}
