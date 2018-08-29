package neto.lobo.denuncias.views.adapters;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.DenunciaActivity;
import neto.lobo.denuncias.views.activities.LoginActivity;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ResultTO;

public class DenunciaAdpter extends RecyclerView.Adapter<DenunciaAdpter.ViewHolderDenuncia> {

    private List<ContentTO> listDenuncia;
    public ManagerRest managerRest;

    public DenunciaAdpter(List<ContentTO> list){
        this.listDenuncia = list;
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

            holder.apoiarAdpter.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    managerRest = new ManagerRest(holder.itemView.getContext());
                    ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_POS);

                    if(resultTO.getCode() == ConstResult.CODE_OK){
                        Toast.makeText(holder.itemView.getContext(), "Apoiado!", Toast.LENGTH_LONG).show();
                        holder.apoiarAdpter.setTextColor(holder.itemView.getContext().getColor(R.color.grey));
                    }else{
                        resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_ZERO);

                        if(resultTO.getCode() == ConstResult.CODE_OK){
                            Toast.makeText(holder.itemView.getContext(), "Desapoido", Toast.LENGTH_LONG).show();
                            holder.apoiarAdpter.setTextColor(holder.itemView.getContext().getColor(R.color.colorAccent));
                        }else{
                            Toast.makeText(holder.itemView.getContext(), "Erro!", Toast.LENGTH_LONG).show();
                        }
                    }


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
