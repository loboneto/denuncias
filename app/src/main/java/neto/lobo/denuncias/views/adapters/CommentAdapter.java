package neto.lobo.denuncias.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerFile;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.to.PersonTO;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolderComment> {

    private List<String> listComments;
    private DataBaseLocal dataBaseLocal;

    private Context context;

    public CommentAdapter(List<String> listComments, Context context){

        this.listComments = listComments;
        this.context = context;

        dataBaseLocal = DataBaseLocal.getInstance(context);
    }


    @NonNull
    @Override
    public CommentAdapter.ViewHolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.adapter_comment, parent, false);

        return new ViewHolderComment(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolderComment holder, int position) {

        // indice 0: idPerson, indice 1: name, indice 2: idContent, indice 3: Calendar, indice 4: comment.
        String [] parts = listComments.get(position).split(ConstModel.SPACE_ATR);

        holder.txtVComment.setText(parts[4]);
        holder.txtVNameComment.setText(parts[1].split(" ")[0]);

    }

    @Override
    public int getItemCount() {
        return listComments.size();
    }

    public class ViewHolderComment extends RecyclerView.ViewHolder{

        public CircleImageView circleImageViewComment;
        public TextView txtVNameComment;
        public TextView txtVComment;

        public ViewHolderComment(View itemView) {
            super(itemView);

            txtVNameComment = itemView.findViewById(R.id.txtVNameComment);
            txtVComment = itemView.findViewById(R.id.txtVComment);

        }

    }
}
