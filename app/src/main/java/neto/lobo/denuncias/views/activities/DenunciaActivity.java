package neto.lobo.denuncias.views.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.renderscript.Sampler;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerFile;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.adapters.CommentAdapter;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.PersonContentTO;
import youubi.common.to.ResultTO;
import youubi.common.tools.StringTools;

public class DenunciaActivity extends AppCompatActivity {

    private DataBaseLocal dataBaseLocal;
    private ManagerPreferences managerPreferences;
    private ManagerRest managerRest;

    private ContentTO content;
    private boolean hasSupport;

    private TextView name;
    private TextView description;
    private TextView commentsEmpty;
    private TextView data;
    private ImageView imgVDenuncia;
    private EditText textComment;
    private Button btnApoiar;
    private CircleImageView photoProfile;


    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<String> comments = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);

        dataBaseLocal = DataBaseLocal.getInstance(this);
        managerPreferences = new ManagerPreferences(this);
        managerRest = new ManagerRest(this);

        name = findViewById(R.id.textNamePerson);
        description = findViewById(R.id.textDescriptionDenuncia);
        commentsEmpty = findViewById(R.id.textCommentsEmpty);
        data = findViewById(R.id.textData);
        imgVDenuncia = findViewById(R.id.imgVDenuncia);
        textComment = findViewById(R.id.textComment);
        photoProfile = findViewById(R.id.photoProfile);



        recyclerView = findViewById(R.id.recyclerViewComments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentAdapter = new CommentAdapter(comments, this);
        recyclerView.setAdapter(commentAdapter);


        long contentId = getIntent().getExtras().getLong("contentId");

        if (contentId != 0) {

            content = dataBaseLocal.getContent(contentId);

            if(managerPreferences.getNameNick() != ""){
                photoProfile.setImageResource(getAvatar());
            }

            name.setText(content.getPersonTO().getNameFirst());
            description.setText(content.getDescription());
            data.setText(content.getDateCreation());

            if(content.getImagePreviewPhotoTO() != null && !content.getImagePreviewPhotoTO().getData().isEmpty())
                imgVDenuncia.setImageBitmap(ManagerFile.stringToBitmap(content.getImagePreviewPhotoTO().getData()));


            if(content.getListComment() != null && !content.getListComment().isEmpty()) {

                List<String> listComments = content.getListComment();
                comments.addAll(listComments);

                commentAdapter.notifyDataSetChanged();

            } else {
                commentsEmpty.setVisibility(View.VISIBLE);
            }

            btnApoiar = findViewById(R.id.btnApoiar);
            PersonContentTO personContentTO = dataBaseLocal.getPersonContent(managerPreferences.getId(), content.getId());
            if(personContentTO != null){

                if(personContentTO.getRatePerson() == ConstModel.RATE_POS){
                    // Tem apoiado
                    hasSupport = true;
                    btnApoiar.setBackground(getResources().getDrawable(R.drawable.ic_apoiado));

                } else {
                    // nao apoiou
                    hasSupport = false;
                }

            }



        } else{
            Log.d("DialogContent", "O conteúdo veio vazio");
            finish();
        }


    }

    public void back(View view){
        finish();
    }

    public void toProfile(View view){
        Intent profile = new Intent(this, ProfileActivity.class);
        Bundle bun =  new Bundle();

        bun.putLong("personId", content.getPersonTO().getId());
        profile.putExtras(bun);

//        bun.putSerializable("person", content.getPersonTO());
//        profile.putExtra("person", bun);
        startActivity(profile);
    }

    public void sendComment(View v){

        String comment = textComment.getText().toString().trim();

        if(!comment.isEmpty()){

            long id = managerPreferences.getId();
            String firstName = managerPreferences.getNameFirst();
            String lastName = managerPreferences.getNameLast();

            final String commentComplete = StringTools.buildComment(id, firstName, lastName, content.getId(), comment, content.getTypeElem());

            new Thread() {

                @Override
                public void run() {
                    Looper.prepare();

                    ResultTO resultTO = managerRest.commentContent(content.getId(), commentComplete);

                    if(resultTO.getCode() == ConstResult.CODE_OK){

                        PersonContentTO personContentTO = (PersonContentTO) resultTO.getObject();
                        dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());

                        comments.add(commentComplete);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textComment.setText("");
                                commentsEmpty.setVisibility(View.INVISIBLE);
                                commentAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Mensagem de erro


                            }
                        });
                    }

                }
            }.start();


        }

    }

    public void apoiar(View v){

        if(hasSupport){

            ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_ZERO);

            if (resultTO.getCode() == ConstResult.CODE_OK) {

                hasSupport = false;

                Toast.makeText(this, "Desapoido", Toast.LENGTH_LONG).show();
                btnApoiar.setBackground(getResources().getDrawable(R.drawable.ic_apoio));

                PersonContentTO personContentTO = (PersonContentTO) resultTO.getObject();
                dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());

            } else {
                Toast.makeText(this, "Erro ao retirar apoio!", Toast.LENGTH_LONG).show();
            }

        } else {

            ResultTO resultTO = managerRest.rateContent(content.getId(), ConstModel.RATE_POS);

            if (resultTO.getCode() == ConstResult.CODE_OK) {

                hasSupport = true;

                Toast.makeText(this, "Apoiado!", Toast.LENGTH_LONG).show();
                btnApoiar.setBackground(getResources().getDrawable(R.drawable.ic_apoiado));

                PersonContentTO personContentTO = (PersonContentTO) resultTO.getObject();
                dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());

            } else {
                if(resultTO.getCode() == 1017){
                    Toast.makeText(this, "Você não pode apoiar sua denuncia" + resultTO.getDescription(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Erro ao apoiar! " + resultTO.getDescription(), Toast.LENGTH_LONG).show();
                }

                Log.e("--->", "Result: " + resultTO.getCode() + " msg: " + resultTO.getDescription());
            }
        }


    }

    private int getAvatar(){
        Resources resources = this.getResources();
        return resources.getIdentifier(managerPreferences.getNameNick(), "drawable", this.getPackageName());
    }

}
