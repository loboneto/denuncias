package neto.lobo.denuncias.managers;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import neto.lobo.denuncias.constants.ConstAndroid;
import youubi.client.help.connection.FacadeRestAndroid;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstResult;
import youubi.common.to.CategoryTO;
import youubi.common.to.ContentTO;
import youubi.common.to.ContextoTO;
import youubi.common.to.FileOriginalTO;
import youubi.common.to.GroupTO;
import youubi.common.to.ImageOriginalTO;
import youubi.common.to.MedalTO;
import youubi.common.to.MissionTO;
import youubi.common.to.PersonContentTO;
import youubi.common.to.PersonGroupTO;
import youubi.common.to.PersonPersonTO;
import youubi.common.to.PersonTO;
import youubi.common.to.RequestParams;
import youubi.common.to.ResultTO;


public class ManagerRest {

    public static Queue<RequestParams> requestParamsList;

    private ManagerPreferences managerPreferences;
    private ManagerContexto managerContext;
    private DataBaseLocal dataBaseLocal;
    private FacadeRestAndroid facadeRestAndroid;
    private Context contextAndroid;
    private NetworkInfo networkInfo;
    private ConnectivityManager connectivityManager;

    /**
     * Constutor.
     */
    public ManagerRest(Context contextAndroid)
    {
        if(requestParamsList == null){
            requestParamsList = new LinkedList<RequestParams>();
        }

        this.managerPreferences = new ManagerPreferences(contextAndroid);
        this.managerContext = new ManagerContexto(contextAndroid);
        this.dataBaseLocal = DataBaseLocal.getInstance(contextAndroid);

        this.facadeRestAndroid = FacadeRestAndroid.getInstance( contextAndroid, false,false, false, ConstAndroid.HOST, ConstAndroid.PORT, ConstAndroid.ROOT );
        this.contextAndroid = contextAndroid;

        connectivityManager = (ConnectivityManager) contextAndroid.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        // Tenta limpar memoria
        System.gc();
    }


    //################################################################################################
    // Util
    //################################################################################################

    /**
     * Verifica se o dispositivo esta conectado na internet.
     */
    private boolean checkConnection() {

        connectivityManager = (ConnectivityManager) contextAndroid.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean result = false;

        if (networkInfo != null) {
            result = networkInfo.isConnectedOrConnecting();
        }

        return result;
    }

    // envio de buffer
    public void sendBuffer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                RequestParams params;
                ResultTO resultTO;

                while(requestParamsList.size() != 0){
                    params = null;
                    resultTO = null;

                    if(checkConnection()){
                        Log.e(ConstAndroid.LOG_CONST, "Enviando... "+requestParamsList.size());
                        params = requestParamsList.poll();
                        if(params != null){
                            resultTO = facadeRestAndroid.requestResult(params);
                            if (resultTO.getCode() == ConstResult.CODE_OK) {

                                saveToBaseLocal(resultTO, params);

                                Log.e(ConstAndroid.LOG_CONST, "Enviado... "+(requestParamsList.size()+1));
                            }else{
                                requestParamsList.add(params);
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void saveToBaseLocal(ResultTO resultTO, RequestParams params){
        if(params.getClassResult() == PersonContentTO.class){
            Log.e(ConstAndroid.LOG_CONST, params.toString());
            PersonContentTO personContentTO = (PersonContentTO) resultTO.getObject();
            dataBaseLocal.storePersonContent(personContentTO, personContentTO.getIdPerson(), personContentTO.getIdContent());
        } else if(params.getClassResult() == PersonGroupTO.class){
            Log.e(ConstAndroid.LOG_CONST, params.toString());
            PersonGroupTO personGroupTO = (PersonGroupTO) resultTO.getObject();
            dataBaseLocal.storePersonGroup(personGroupTO, personGroupTO.getPersonTO().getId(), personGroupTO.getGroupTO().getId());
        } else if(params.getClassResult() == PersonPersonTO.class){
            Log.e(ConstAndroid.LOG_CONST, params.toString());
            PersonPersonTO personPersonTO = (PersonPersonTO) resultTO.getObject();
            dataBaseLocal.storePersonPerson(personPersonTO, personPersonTO.getIdPerson1(), personPersonTO.getIdPerson2());
        }
    }

    //################################################################################################
    // POST
    //################################################################################################

    //***********************************************************************************************
    // Session  2
    //***********************************************************************************************

    public ResultTO login(String email, String passPlain) {
        ResultTO resultLogin = new ResultTO();
        resultLogin.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection()) {

            ContextoTO contextoTO = managerContext.buildContexto();

            resultLogin = facadeRestAndroid.login(email, passPlain, contextoTO);
            int codeLogin = resultLogin.getCode();

            if(codeLogin == ConstResult.CODE_OK) {

                PersonTO personLoginTO = (PersonTO) resultLogin.getObject();
                String token = personLoginTO.getToken();

                long idPerson = personLoginTO.getId();

                // Atualiza preferencias iniciais
                managerPreferences.loginSession(idPerson, email, passPlain, personLoginTO.getNameFirst(), personLoginTO.getNameLast(), true, true, token);

            }
        }

        return resultLogin;
    }

    public ResultTO logout() {

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if( managerPreferences.getFlagLogin()  ) {

            if(checkConnection() ) {

                ContextoTO contextoTO = managerContext.buildContexto();

                // Registra logout no Server
                resultTO = facadeRestAndroid.logout(managerPreferences.getTokenAPI(), managerPreferences.getId(), contextoTO.getTypeDevice(), contextoTO);
                int code = resultTO.getCode();

                if( code == ConstResult.CODE_OK ) {

                    // Salva nas preferencias: flagLogin (false)
                    managerPreferences.logoutSession();

                    // BD Local: Deleta tudo
                    dataBaseLocal.deleteALL();

                }
            }
        }

        return resultTO;
    }

    public ResultTO logoutAll() {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if( managerPreferences.getFlagLogin()  )
        {
            if(checkConnection() )
            {
                ContextoTO contextoTO = managerContext.buildContexto();

                // Registra logout no Server
                resultTO = facadeRestAndroid.logout(managerPreferences.getTokenAPI(), managerPreferences.getId(), ConstAndroid.LOGOUT_ALL, contextoTO);
                int code = resultTO.getCode();

                if( code == ConstResult.CODE_OK ) {

                    // Salva nas preferencias: flagLogin (false)
                    managerPreferences.logoutSession();

                    // BD Local: Deleta tudo
                    dataBaseLocal.deleteALL();

                }
            }
        }

        return resultTO;
    }

    public ResultTO logoutDevice(int device){

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {

            ContextoTO contextoTO = managerContext.buildContexto();

            resultTO = facadeRestAndroid.logout(managerPreferences.getTokenAPI(), managerPreferences.getId(), device, contextoTO);

        }


        return resultTO;
    }

    public ResultTO authAccountPersonMoodle(String user, String password, String accountInstitutionUrl, long institutionId){

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection()){
            resultTO = facadeRestAndroid.authAccountPerson(user, password, accountInstitutionUrl, institutionId);
        }

        return resultTO;
    }

    public ResultTO getListInstitution(){

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection()){

            resultTO = facadeRestAndroid.getListInstitution();

        }

        return resultTO;
    }


    public ResultTO getListAccountInstitution(long idInstitutionSelected){
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection()){
            resultTO = facadeRestAndroid.getListAccountInstitution(managerPreferences.getTokenAPI(), managerPreferences.getId(), idInstitutionSelected);
        }

        return resultTO;
    }

    //***********************************************************************************************
    // Person  11
    //***********************************************************************************************

    public ResultTO createPerson(String tokenAdmin, long idAdmin, PersonTO person, String passPlain, ImageOriginalTO imageOriginal, ImageOriginalTO imageOriginalCover)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() )
        {
            ContextoTO contextoTO = managerContext.buildContexto();

            // Criptografa senha antes de transmitir (somente se o campo foi preenchido)
            if( passPlain != null  &&  !passPlain.isEmpty() )
            {
                //byte[] passEncoded = SecurityTools.encodeRSA(passPlain, contextAndroid.getResources().openRawResource(R.raw.publickey) );
                //person.setPassCripted(passEncoded);
                person.setPassPlain(passPlain);
            }

            resultTO = facadeRestAndroid.createPerson(tokenAdmin, idAdmin, person, imageOriginal, imageOriginalCover, contextoTO);
        }

        return resultTO;
    }

    public ResultTO recoveryPass(String emailUser, String strCalendarBirthDate )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.recoveryPass(emailUser, strCalendarBirthDate, contextoTO);
        }

        return resultTO;
    }

    public ResultTO editPerson(PersonTO person, String passPlain, ImageOriginalTO imageOriginal, ImageOriginalTO imageOriginalCover)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() )
        {
            ContextoTO contextoTO = managerContext.buildContexto();

            // Criptografa senha antes de transmitir (somente se o campo foi preenchido)
            if( passPlain != null  &&  !passPlain.isEmpty() )
            {
                //byte[] passEncoded = SecurityTools.encodeRSA(passPlain, contextAndroid.getResources().openRawResource(R.raw.publickey) );
                //person.setPassCripted(passEncoded);
                person.setPassPlain(passPlain);
            }

            resultTO = facadeRestAndroid.editPerson(managerPreferences.getTokenAPI(), person.getId(), person, imageOriginal, imageOriginalCover, contextoTO);
        }

        return resultTO;
    }

    public ResultTO pingPerson(int typeContexto)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            contextoTO.setTypeAction(typeContexto);
            resultTO = facadeRestAndroid.pingPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), contextoTO.getTypeAction(), contextoTO);
        }

        return resultTO;
    }

    public ResultTO removePerson(long idPerson)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removePerson(managerPreferences.getTokenAPI(), idPerson, managerPreferences.getId(), contextoTO);
        }

        return resultTO;
    }

    public ResultTO viewPerson(long idPerson2) {

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.viewPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO rejectRecommPerson(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.rejectRecommPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO sendMessagePerson(long idPerson2, String messages) {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if (checkConnection()) {
            facadeRestAndroid.setFlagOffline(false);
            resultTO = facadeRestAndroid.sendMessagePerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, messages, contextoTO);
        } else {
            facadeRestAndroid.setFlagOffline(true);
            resultTO = facadeRestAndroid.sendMessagePerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, messages, contextoTO);
            requestParamsList.add(resultTO.getRequestParams());
        }

        return resultTO;
    }

    public ResultTO checkInPerson(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.checkInPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO warnPerson(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.warnPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO searchPerson(String param, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.searchPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), param, pageSize, page, contextoTO);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // PersonTO  friendship  5
    //------------------------------------------------------------------------------------------------

    public ResultTO requestFriendship(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.requestFriendship(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }
        return resultTO;
    }

    public ResultTO cancelRequestFriendship(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.cancelRequestFriendship(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO rejectFriendship(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            Log.e("--->", "RejectFriendship: ");
            resultTO = facadeRestAndroid.rejectFriendship(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO acceptFriendship(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {

//            Log.e("--->", "Conteudo no managerRest");
//            Log.e("--->", "Token: " + managerPreferences.getTokenAPI());
//            Log.e("--->", "Meu Id: " + managerPreferences.getId());
//            Log.e("--->", "Id da Pessoa: " + idPerson2);


            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.acceptFriendship(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeFriendship(long idPerson2)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() )
        {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeAddedPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson2, contextoTO);
        }

        return resultTO;
    }

    //***********************************************************************************************
    // Content  13
    //***********************************************************************************************

    public ResultTO createContent(ContentTO content, ImageOriginalTO imageOriginal, FileOriginalTO fileOriginal)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if(checkConnection() ) {
            facadeRestAndroid.setFlagOffline(false);
            resultTO = facadeRestAndroid.createContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), content, imageOriginal, fileOriginal, contextoTO);
        }else {
            facadeRestAndroid.setFlagOffline(true);
            resultTO = facadeRestAndroid.createContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), content, imageOriginal, fileOriginal, contextoTO);
            requestParamsList.add(resultTO.getRequestParams());
        }

        return resultTO;
    }

    public ResultTO editContent(ContentTO content, ImageOriginalTO imageOriginal, FileOriginalTO fileOriginal)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.editContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), content, imageOriginal, fileOriginal, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO viewContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.viewContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO rejectRecommContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.rejectRecommContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO addContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.addContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeAddedContent(long idContent){

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeAddedContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }


    public ResultTO rateContent(long idContent, int rate)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if(checkConnection() ) {
            facadeRestAndroid.setFlagOffline(false);
            resultTO = facadeRestAndroid.rateContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, rate, contextoTO);
        }else {
            facadeRestAndroid.setFlagOffline(true);
            resultTO = facadeRestAndroid.rateContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, rate, contextoTO);
            requestParamsList.add(resultTO.getRequestParams());
        }

        return resultTO;
    }

    public ResultTO commentContent(long idContent, String comment)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if(checkConnection() ) {
            facadeRestAndroid.setFlagOffline(false);
            resultTO = facadeRestAndroid.commentContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, comment, contextoTO);
        }else {
            facadeRestAndroid.setFlagOffline(true);
            resultTO = facadeRestAndroid.commentContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, comment, contextoTO);
            requestParamsList.add(resultTO.getRequestParams());
        }

        return resultTO;
    }

    public ResultTO removeCommentContent(long idContent, String commentComplete)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeCommentContent( managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, commentComplete, contextoTO );
        }

        return resultTO;
    }

    public ResultTO answerContent(long idContent, String answer)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.answerContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, answer, contextoTO);
        }

        return resultTO;
    }

    public ResultTO checkInContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.checkInContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO warnContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.warnContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO searchContent(String param, int contentType, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.searchContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), param, contentType, pageSize, page, contextoTO);
        }

        return resultTO;
    }


    //***********************************************************************************************
    // GroupTO  11
    //***********************************************************************************************

    public ResultTO createGroup(GroupTO group, ImageOriginalTO imageOriginal, ImageOriginalTO imageOriginalCover)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.createGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), group, imageOriginal, imageOriginalCover, contextoTO);
        }

        return resultTO;
    }

    public ResultTO editGroup(GroupTO group, ImageOriginalTO imageOriginal, ImageOriginalTO imageOriginalCover)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.editGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), group, imageOriginal, imageOriginalCover, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO viewGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.viewGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO rejectRecommGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.rejectRecommGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO addGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.addGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeAddedGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeAddedGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO rateGroup(long idGroup, int rate)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.rateGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, rate, contextoTO);
        }

        return resultTO;
    }

    public ResultTO sendMessageGroup(long idGroup, String message)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if(checkConnection() ) {
            facadeRestAndroid.setFlagOffline(false);
            resultTO = facadeRestAndroid.sendMessageGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, message, contextoTO);
        }else {
            Log.e(ConstAndroid.LOG_CONST, "buffer send message group");
            facadeRestAndroid.setFlagOffline(true);
            resultTO = facadeRestAndroid.sendMessageGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, message, contextoTO);
            requestParamsList.add(resultTO.getRequestParams());
        }

        return resultTO;
    }

    public ResultTO checkInGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.checkInGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO warnGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.warnGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO searchGroup(String param, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.searchGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), param, pageSize, page, contextoTO);
        }

        return resultTO;
    }

    //--------------------------------------------------------------------------------
    // Group Elements 6
    //--------------------------------------------------------------------------------

    public ResultTO createContentInGroup(long idGroup, ContentTO content, ImageOriginalTO imageOriginal, FileOriginalTO fileOriginal)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        ContextoTO contextoTO = managerContext.buildContexto();

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.createContentInGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, content, imageOriginal, fileOriginal, contextoTO);
        }

        return resultTO;
    }

    public ResultTO addContentInGroup(long idGroup, long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.addContentInGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, idContent, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeContentFromGroup(long idGroup, long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeContentFromGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, idContent, contextoTO);
        }

        return resultTO;
    }

    //--------------------------------------------------------------------------------
    // Group Membership  4
    //--------------------------------------------------------------------------------

    public ResultTO requestMembership(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.requestMembership(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO cancelRequestMembership(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.cancelRequestMembership(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO acceptMembership(long idPersonInterested, long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.acceptMembership(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonInterested, idGroup, contextoTO);
        }

        return resultTO;
    }

    public ResultTO rejectMembership(long idPersonInterested, long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.rejectMembership(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonInterested, idGroup, contextoTO);
        }

        return resultTO;
    }

    //***********************************************************************************************
    // AccountTO  2
    //***********************************************************************************************
//    public ResultTO createAccount(AccountTO accountTO)
//    {
//        ResultTO resultTO = new ResultTO();
//        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            ContextoTO contextoTO = managerContext.buildContexto();
//            resultTO = facadeRestAndroid.createAccount( managerPreferences.getTokenAPI(), managerPreferences.getId(), accountTO, contextoTO );
//        }
//
//        return resultTO;
//    }

//    public ResultTO removeAccount(long idAccount)
//    {
//        ResultTO resultTO = new ResultTO();
//        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            ContextoTO contextoTO = managerContext.buildContexto();
//            resultTO = facadeRestAndroid.removeAccount( managerPreferences.getTokenAPI(), managerPreferences.getId(), idAccount, contextoTO );
//        }
//
//        return resultTO;
//    }

    //***********************************************************************************************
    // MedalTO  4
    //***********************************************************************************************

    public ResultTO createMedal(MedalTO medalTO, ImageOriginalTO imageOriginalTO)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.createMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), medalTO, imageOriginalTO, contextoTO);
        }

        return resultTO;
    }

    public ResultTO editMedal(MedalTO medalTO, ImageOriginalTO imageOriginalTO)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.editMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), medalTO, imageOriginalTO, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeMedal(long idMedal)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMedal, contextoTO);
        }

        return resultTO;
    }

//    public ResultTO addMedal(long idMedal)
//    {
//        ResultTO resultTO = new ResultTO();
//        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            ContextoTO contextoTO = managerContext.buildContexto();
//            resultTO = facadeRestAndroid.addMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMedal, contextoTO);
//        }
//
//        return resultTO;
//    }


    //***********************************************************************************************
    // MissionTO  5
    //***********************************************************************************************

    public ResultTO createMission(MissionTO missionTO, ImageOriginalTO imageOriginalTO)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.createMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), missionTO, imageOriginalTO, contextoTO);
        }

        return resultTO;
    }

    public ResultTO editMission(MissionTO missionTO, ImageOriginalTO imageOriginalTO)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.editMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), missionTO, imageOriginalTO, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeMission(long idMission)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission, contextoTO);
        }

        return resultTO;
    }

    public ResultTO checkInMission(long idMission)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.checkInMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeAddedMission(long idMission) {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeAddedMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission, contextoTO);
        }

        return resultTO;
    }



//    public ResultTO addMission(long idMission)
//    {
//        ResultTO resultTO = new ResultTO();
//        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            ContextoTO contextoTO = managerContext.buildContexto();
//            resultTO = facadeRestAndroid.addMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission, contextoTO);
//        }
//
//        return resultTO;
//    }

    //***********************************************************************************************
    // CategoryTO  3
    //***********************************************************************************************

    public ResultTO createCategory(CategoryTO categoryTO)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.createCategory(managerPreferences.getTokenAPI(), managerPreferences.getId(), categoryTO, contextoTO);
        }

        return resultTO;
    }

    public ResultTO editCategory(CategoryTO categoryTO)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.editCategory(managerPreferences.getTokenAPI(), managerPreferences.getId(), categoryTO, contextoTO);
        }

        return resultTO;
    }

    public ResultTO removeCategory(long idCategory)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            ContextoTO contextoTO = managerContext.buildContexto();
            resultTO = facadeRestAndroid.removeCategory(managerPreferences.getTokenAPI(), managerPreferences.getId(), idCategory, contextoTO);
        }

        return resultTO;
    }


    //################################################################################################
    // GET
    //################################################################################################

    //***********************************************************************************************
    // Person 6
    //***********************************************************************************************

    public ResultTO getPerson(long idPerson)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson);
        }

        return resultTO;
    }

    public ResultTO getPerson(String email)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPerson(managerPreferences.getTokenAPI(), email);
        }

        return resultTO;
    }

    public ResultTO getListPerson()
    {
        ResultTO resultListTO = new ResultTO();
        resultListTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultListTO = facadeRestAndroid.getListPerson(managerPreferences.getTokenAPI(), managerPreferences.getId() );
        }

        return resultListTO;
    }

    public ResultTO getListPersonRanking(int sort, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonRanking( managerPreferences.getTokenAPI(), managerPreferences.getId(), sort, pageSize, page);
        }

        return resultTO;
    }

    public ResultTO getListPerson(int pageSize, int page, double lat, double lon)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPerson( managerPreferences.getTokenAPI(), managerPreferences.getId(), lat, lon, pageSize, page );
        }

        return resultTO;
    }

    public ResultTO getListPerson(String listStrIDV)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPerson( managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV );
        }

        return resultTO;
    }

    //-------------------------------------------------------------------------------------------------------
    // RecommPerson  1
    //-------------------------------------------------------------------------------------------------------
    public ResultTO getListRecommPerson(int sort, int pageSize, int page )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListRecommPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), sort, pageSize, page);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // PersonContent  3
    //------------------------------------------------------------------------------------------------

    // whatObjects = escolher qual dos objetos quer, utilizar ConstModel.WHAT_OBJECTS_
    // 0 = Nenhum, 1 = objeto 1, 2 = objeto 2, 3 = todos, 4 = nenhum ao final da constante

    public ResultTO getPersonContent(long idPersonTarget, long idContent, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPersonContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idContent, whatObjects );
        }

        return resultTO;
    }

    public ResultTO getListPersonContent(long idPersonTarget, long idContent, int relation, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idContent, relation, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonContent(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }


    //------------------------------------------------------------------------------------------------
    // PersonGroup  3
    //------------------------------------------------------------------------------------------------
    public ResultTO getPersonGroup(long idPersonTarget, long idGroup, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPersonGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idGroup, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonGroup(long idPersonTarget, long idGroup, int relation, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idGroup, relation, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonGroup(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // PersonPerson  3
    //------------------------------------------------------------------------------------------------
    public ResultTO getPersonPerson(long idPerson1, long idPerson2, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPersonPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson1, idPerson2, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonPerson(long idPerson1, long idPerson2, int relation, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson1, idPerson2, relation, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonPerson(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonPerson(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }

    //-------------------------------------------------------------------------------------------------------
    // PersonMedal  3
    //-------------------------------------------------------------------------------------------------------

    public ResultTO getPersonMedal(long idPersonTarget, long idMedal, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPersonMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idMedal, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonMedal(long idPersonTarget, long idMedal, int relation, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idMedal, relation, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListPersonMedal(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonMedal(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }

    //-------------------------------------------------------------------------------------------------------
    // PersonMission  3
    //-------------------------------------------------------------------------------------------------------

    public ResultTO getPersonMission(long idPersonTarget, long idMission, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getPersonMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idMission, whatObjects);
        }

        return resultTO;
    }


    public ResultTO getListPersonMission(long idPersonTarget, long idMission, int relation, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idMission, relation, whatObjects);
        }

        return resultTO;
    }


    public ResultTO getListPersonMission(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // PersonTag  1
    //------------------------------------------------------------------------------------------------
    public ResultTO getListPersonTag(long idPersonTarget, long idTag, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListPersonTag(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget, idTag, whatObjects);
        }

        return resultTO;
    }

    //***********************************************************************************************
    // ContentTO 5
    //***********************************************************************************************

    public ResultTO getContent(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent);
        }

        return resultTO;
    }

    public ResultTO getListContent(long idPersonTarget)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget);
        }

        return resultTO;
    }

    public ResultTO getListContentByCategory(long idContent)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContentByCategory(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent);
        }

        return resultTO;
    }

    public ResultTO getListContentRanking(int sort, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContentRanking( managerPreferences.getTokenAPI(), managerPreferences.getId(), sort, pageSize, page);
        }

        return resultTO;
    }

    public ResultTO getListContent(int pageSize, int page, double lat, double lon)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContent( managerPreferences.getTokenAPI(), managerPreferences.getId(), lat, lon, pageSize, page);
        }

        return resultTO;
    }

    public ResultTO getListContent(String listStrIDV)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContent( managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV );
        }

        return resultTO;
    }

    //-------------------------------------------------------------------------------------------------------
    // RecommContent  1
    //-------------------------------------------------------------------------------------------------------
    public ResultTO getListRecommContent(int sort, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListRecommContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), sort, pageSize, page);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // ContentTag  1
    //------------------------------------------------------------------------------------------------
    public ResultTO getListContentTag(long idContent, long idTag, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContentTag(managerPreferences.getTokenAPI(), managerPreferences.getId(), idContent, idTag, whatObjects);
        }

        return resultTO;
    }


    //***********************************************************************************************
    // GroupTO  5
    //***********************************************************************************************
    public ResultTO getGroup(long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup);
        }

        return resultTO;
    }

    public ResultTO getListGroup(long idPersonTarget)
    {
        ResultTO resultListTO = new ResultTO();
        resultListTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultListTO = facadeRestAndroid.getListGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget);
        }

        return resultListTO;
    }

    public ResultTO getListGroupRanking(int sort, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListGroupRanking(managerPreferences.getTokenAPI(), managerPreferences.getId(), sort, pageSize, page);
        }

        return resultTO;
    }

    public ResultTO getListGroup(int pageSize, int page, double lat, double lon )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), lat, lon, pageSize, page);
        }

        return resultTO;
    }

    public ResultTO getListGroup(String listStrIDV )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // RecommGroup  1
    //------------------------------------------------------------------------------------------------
    public ResultTO getListRecommGroup(int sort, int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListRecommGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), sort, pageSize, page);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // GroupContentTO  3
    //------------------------------------------------------------------------------------------------
    public ResultTO getGroupContent(long idGroup, long idContent, long idPerson, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getGroupContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, idContent, idPerson, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListGroupContent(long idGroup, long idContent, long idPerson, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListGroupContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, idContent, idPerson, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListGroupContent(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListGroupContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }


    //------------------------------------------------------------------------------------------------
    // GroupTagTO  1
    //------------------------------------------------------------------------------------------------
    public ResultTO getListGroupTag(long idGroup, long idTag, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListGroupTag(managerPreferences.getTokenAPI(), managerPreferences.getId(), idGroup, idTag, whatObjects);
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // RequestGroup  3
    //------------------------------------------------------------------------------------------------
    public ResultTO getRequestGroup(long idPersonInter, long idPersonAuthor, long idGroup)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getRequestGroup( managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonInter, idPersonAuthor, idGroup );
        }

        return resultTO;
    }

    public ResultTO getListRequestGroup(long idPersonInter, long idPersonAuthor, long idGroup)
    {
        ResultTO resultListTO = new ResultTO();
        resultListTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultListTO = facadeRestAndroid.getListRequestGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonInter, idPersonAuthor, idGroup);
        }

        return resultListTO;
    }

    public ResultTO getListRequestGroup(String listStrIDV )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListRequestGroup(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV);
        }

        return resultTO;
    }

    //************************************************************************************************
    // Medal  3
    //************************************************************************************************
    public ResultTO getMedal(long idMedal)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getMedal( managerPreferences.getTokenAPI(), managerPreferences.getId(), idMedal );
        }

        return resultTO;
    }

    public ResultTO getListMedal()
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMedal( managerPreferences.getTokenAPI(), managerPreferences.getId() );
        }

        return resultTO;
    }

    public ResultTO getListMedal(String listStrIDV )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMedal( managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV );
        }

        return resultTO;
    }

    //************************************************************************************************
    // Mission  4
    //************************************************************************************************
    public ResultTO getMission(long idMission)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission);
        }

        return resultTO;
    }

    public ResultTO getListMission(long idPersonTarget)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMission(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget);
        }

        return resultTO;
    }

    public ResultTO getListMission(int pageSize, int page, double lat, double lon )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMission( managerPreferences.getTokenAPI(), managerPreferences.getId(), lat, lon, pageSize, page );
        }

        return resultTO;
    }

    public ResultTO getListMission(String listStrIDV )
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMission( managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV );
        }

        return resultTO;
    }

    //------------------------------------------------------------------------------------------------
    // MissionContentTO  3
    //------------------------------------------------------------------------------------------------
    public ResultTO getMissionContent(long idMission, long idContent, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getMissionContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission, idContent, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListMissionContent(long idMission, long idContent, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMissionContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), idMission, idContent, whatObjects);
        }

        return resultTO;
    }

    public ResultTO getListMissionContent(String listStrIDV, int whatObjects)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListMissionContent(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV, whatObjects);
        }

        return resultTO;
    }


    //************************************************************************************************
    // AccountTO  2
    //************************************************************************************************
//    public ResultTO getAccount(long idAccount)
//    {
//        ResultTO resultTO = new ResultTO();
//        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            resultTO = facadeRestAndroid.getAccount( managerPreferences.getTokenAPI(), managerPreferences.getId(), idAccount );
//        }
//
//        return resultTO;
//    }

//    public ResultTO getListAccount()
//    {
//        ResultTO resultListTO = new ResultTO();
//        resultListTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            resultListTO = facadeRestAndroid.getListAccount( managerPreferences.getTokenAPI(), managerPreferences.getId() );
//        }
//
//        return resultListTO;
//    }

    //************************************************************************************************
    // TagTO  1
    //************************************************************************************************
    public ResultTO getListTag(int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListTag( managerPreferences.getTokenAPI(), managerPreferences.getId(), pageSize, page);
        }

        return resultTO;
    }

    //************************************************************************************************
    // ContextoTO  2
    //************************************************************************************************
    public ResultTO getContextoLast(long idPersonTarget)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getContextoLast(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget);
        }

        return resultTO;
    }

    public ResultTO getListContexto(long personTarget, int pageSize, int page, int type)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListContexto(managerPreferences.getTokenAPI(), managerPreferences.getId(), personTarget, type, pageSize, page );
        }

        return resultTO;
    }

    //************************************************************************************************
    // TimesUserTO  1
    //************************************************************************************************
    public ResultTO getTimesUser(long idPersonTarget)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getTimesUser(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPersonTarget);
        }

        return resultTO;
    }

    //************************************************************************************************
    // TimesElemTO  1
    //************************************************************************************************
    public ResultTO getTimesElem(long idElem, int by)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getTimesElem(managerPreferences.getTokenAPI(), managerPreferences.getId(), idElem, by);
        }

        return resultTO;
    }

    //************************************************************************************************
    // TimesRelationTO  1
    //************************************************************************************************
    public ResultTO getTimesRelation(long idPerson, long idElem, int by)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getTimesRelation(managerPreferences.getTokenAPI(), managerPreferences.getId(), idPerson, idElem, by);
        }

        return resultTO;
    }

    //************************************************************************************************
    // DeviceTO  1
    //************************************************************************************************
//    public ResultTO getListDevice()
//    {
//        ResultTO resultListTO = new ResultTO();
//        resultListTO.setCode(ConstResult.CODE_FAIL_INTERNET);
//
//        if(checkConnection() ) {
//            resultListTO = facadeRestAndroid.getListDevice( managerPreferences.getTokenAPI(), managerPreferences.getId() );
//        }
//
//        return resultListTO;
//    }

    //************************************************************************************************
    // FileOriginalTO  1
    //************************************************************************************************
    public ResultTO getFileOriginal(long idFileOriginal)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getFileOriginal( managerPreferences.getTokenAPI(), managerPreferences.getId(), idFileOriginal );
        }

        return resultTO;
    }

    //************************************************************************************************
    // ImageOriginalTO  1
    //************************************************************************************************
    public ResultTO getImageOriginal(long idImageOriginal, int qualidade) {
        // Const Model
        // IMAGE_QUALITY_MEDIUM
        // IMAGE_QUALITY_HIGH

        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getImageOriginal( managerPreferences.getTokenAPI(), managerPreferences.getId(), idImageOriginal, qualidade);
        }

        return resultTO;
    }

    //************************************************************************************************
    // NotificationTO 3
    //************************************************************************************************
    public ResultTO getListNotification(int pageSize, int page)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListNotification( managerPreferences.getTokenAPI(), managerPreferences.getId(), pageSize, page);
        }

        return resultTO;
    }

    public ResultTO getNotification(int type, long elementId, int elementType)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getNotification( managerPreferences.getTokenAPI(), managerPreferences.getId(), type, elementId, elementType );
        }

        return resultTO;
    }

    public ResultTO getListNotification(String listStrIDV)
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListNotification(managerPreferences.getTokenAPI(), managerPreferences.getId(), listStrIDV);
        }

        return resultTO;
    }

    //************************************************************************************************
    // SyncTO  1
    //************************************************************************************************
    public ResultTO getSync()
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getSync(managerPreferences.getTokenAPI(), managerPreferences.getId());
        }

        return resultTO;
    }

    //************************************************************************************************
    // CategoryTO  1
    //************************************************************************************************
    public ResultTO getListCategory()
    {
        ResultTO resultTO = new ResultTO();
        resultTO.setCode(ConstResult.CODE_FAIL_INTERNET);

        if(checkConnection() ) {
            resultTO = facadeRestAndroid.getListCategory();
        }

        return resultTO;
    }



}
