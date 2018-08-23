package neto.lobo.denuncias.views.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerFile;
import neto.lobo.denuncias.views.activities.DenunciaActivity;
import neto.lobo.denuncias.views.activities.LoginActivity;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;

public class DenunciaAdpter extends RecyclerView.Adapter<DenunciaAdpter.ViewHolderDenuncia> {

    private List<ContentTO> listDenuncia;

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


        public ViewHolderDenuncia(final View itemView) {
            super(itemView);

            this.desc = itemView.findViewById(R.id.textDescriptionDenunciaAdpter);
            this.imgDenuncia = itemView.findViewById(R.id.imageView5Adpter);
            this.data = itemView.findViewById(R.id.textDataAdpter);

        }
    }
}
