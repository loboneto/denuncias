package neto.lobo.denuncias.views.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import neto.lobo.denuncias.R;
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
    public void onBindViewHolder(@NonNull DenunciaAdpter.ViewHolderDenuncia holder, int position) {

        if(listDenuncia != null && listDenuncia.size() > 0){
            ContentTO content = listDenuncia.get(position);

            holder.desc.setText(content.getDescription());
        }



    }

    @Override
    public int getItemCount() {
        return listDenuncia.size();
    }

    public class ViewHolderDenuncia extends RecyclerView.ViewHolder{

        public TextView desc;
        public TextView personName;

        public ViewHolderDenuncia(View itemView) {
            super(itemView);

            this.desc = itemView.findViewById(R.id.textDescriptionDenunciaAdpter);
        }
    }
}
