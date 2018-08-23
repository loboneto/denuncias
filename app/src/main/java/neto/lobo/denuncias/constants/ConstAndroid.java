package neto.lobo.denuncias.constants;


public class ConstAndroid {

    // YOUUBIDEPLOY

    //Host Oficial
    public final static String HOST = "edubi.ufersa.edu.br";                                  // YOUUBIDEPLOY : remoto
    public final static String PORT = "8080";							                        // YOUUBIDEPLOY
    public final static String ROOT = "youubi2_gf_sut_d2";
    public static final String VERSION_YOUUBI_PUB = "youubi2_gf_sut_d2 - 2018_07_06";	        // YOUUBIDEPLOY : versao + release de mesmo BD
    public static final String VERSION_YOUUBI_PRI = "youubi2_gf_sut_d2";				        // YOUUBIDEPLOY : chave de cada instancia

    // Host de release
//    public final static String HOST = "edubi.ufersa.edu.br";                                // YOUUBIDEPLOY : remoto
//    public final static String PORT = "8080";                                               // YOUUBIDEPLOY
//    public final static String ROOT = "youubi2_gf_sut_d2";
//    public static final String VERSION_YOUUBI_PUB = "youubi2_gf_sut_d2 - 2018_06_22";	    // YOUUBIDEPLOY : versao + release de mesmo BD
//    public static final String VERSION_YOUUBI_PRI = "youubi2_gf_sut_d2";                    // YOUUBIDEPLOY : chave de cada instancia

    //Host Teste
//    public final static String HOST = "10.215.101.67";                                        // YOUUBIDEPLOY : remoto
//    public final static String PORT = "8080";                                                 // YOUUBIDEPLOY
//    public final static String ROOT = "youubi_communic_rest";
//    public static final String VERSION_YOUUBI_PUB = "youubi2_gf_sut_d1 - 2017_05_05";         // YOUUBIDEPLOY : versao + release de mesmo BD
//    public static final String VERSION_YOUUBI_PRI = "youubi_communic_rest";                   // YOUUBIDEPLOY : chave de cada instancia

    //Pc de bruno
//    public final static String HOST = "10.143.10.159";                                        // YOUUBIDEPLOY : remoto
//    public final static String PORT = "8080";                                                 // YOUUBIDEPLOY
//    public final static String ROOT = "youubi_communic_rest";
//    public static final String VERSION_YOUUBI_PUB = "localhost250 - 2017_05_05";              // YOUUBIDEPLOY : versao + release de mesmo BD
//    public static final String VERSION_YOUUBI_PRI = "youubi_communic_rest";                   // YOUUBIDEPLOY : chave de cada instancia



    //Chave da API do Youtube
    public static final String API_KEY = "AIzaSyB2nH_QLw8-CV5b0foQqvXY0w3kEDawyUk";

    // LOG
    public final static String LOG_CONST = "--->";

    // job service
    public final static String JOB_SERVICE_ONLINE = "JOB_SERVICE_ONLINE";

    // LogOut de todos os dispositivos
    public final static int LOGOUT_ALL = -1;


    // Formas de cadastro
    public final static int AUTH_WITHOUT_MOODLE = 0;
    public final static int AUTH_WITH_MOODLE = 1;



    // Definicoes de tamanho e qualidade das imagens
    public final static int IMAGE_SIZE_ORIGINAL_H = 900; // 960
    public final static int IMAGE_SIZE_ORIGINAL_W = 500; // 540
    public final static int IMAGE_SIZE_PREVIEW_H  = 400;
    public final static int IMAGE_SIZE_PREVIEW_W  = 400;
    public final static int IMAGE_QUALITY_ORIGINAL = 100;
    public final static int IMAGE_QUALITY_PREVIEW  = 80;

    // Requests de intents
    // nos create content
    public static final int REQUEST_IMAGE_CAMERA_PHOTO_PROFILE = 1;
    public static final int REQUEST_IMAGE_UPLOAD_PHOTO_PROFILE = 2;
    public static final int REQUEST_IMAGE_CAMERA_PHOTO_COVER = 3;
    public static final int MAP_CAPTURE = 3;


    // Quantidade de elementos na lista
    public static final int LIST_SIZE = 20;


    // Quantidade de letras dos titulos e descricoes
    public final static int TRUNC_LONG = 200;
    public final static int TRUNC_SHORT = 30;


    // Definicoes de GPS
    public final static int GPS_NEAR = 2000; // 2 km
    public static final double DEFAULT_RADIUS = 0.00003;


    // Tratamento de proximidade no map
    public static final String DEFAULT_DELETE_LIST = "itemsDeleted";
    public static final String DEFAULT_ADDED_LIST = "itemsAdded";


    // Permissões no momento do login
    public final static int MULTIPLE_PERMISSIONS = 150;

    // EventBus
    public final static int MAIN_ACTIVITY = 1;
    public final static int HOME_FRAGMENT = 2;
    public final static int EVENT_BUS_CONTENT = 10;
    public final static int EVENT_BUS_PERSON = 11;
    public final static int EVENT_BUS_GROUP = 12;
    public final static int EVENT_UPDATE_PROFILE = 13;
    public final static int ACTION_COMMENT = 20;
    public final static int ACTION_PERSONADDED = 21;
    public final static int ACTION_GROUPADDED = 22;


    // Resultados de activitys entre adapters
    public final static int COMMENT_ACTIVITY = 11;
    public final static int USER_HAS_COMMENTED = 21;
    public final static int USER_HAS_NO_COMMENTED = 22;


    public final static int ACTION_MAP = 33;

    public final static int NO_OTHER_INFO = 0;
    public final static int OTHER_INFO = 1;

    //QR CODE
    public final static int REQUEST_QR_CODE_READER = 0;
    public final static String QR_CODE_ID_ENTITY = "QR_CODE_ID_ENTITY";
    public final static String QR_CODE_TYPE_ENTITY = "QR_CODE_TYPE_ENTITY";

    //CHAT
    public final static String PERSON_CHAT = "PERSON_CHAT";
    public final static int MY_MESSAGE = 0;
    public final static int THEIR_MESSAGE = 1;
    public final static int MY_MESSAGE_LINK = 2;
    public final static int THEIR_MESSAGE_LINK = 3;
    public final static int CHAT_CONTENT_SHARE_MESSAGE = 4;
    public final static String CHAT_CONTENT_SHARE = "shrContent";
    public final static String CHAT_MSG_SPACE = ":";
    public final static String CHAT_INFO_GROUP = "infoGroup";

    // BOADCAST CHAT GROUP
    public final static String GROUP_ID = "GROUP_ID";
    public final static String GROUP_CHAT_MSG = "GROUP_CHAT_MSG";
    public final static String GROUP_CHAT_MSG_UPDATE = "GROUP_CHAT_MSG_UPDATE";

    // BROADCAST MAIN
    public final static String CHANGE_ICON_EVENT = "changeIconEvent";
    public final static String CHANGE_ICON_BUNDLE = "icon";
    public final static int CHANGE_ICON_NOTIFICATION = 1;
    public final static int CHANGE_ICON_REQUEST = 2;
    public final static int CHANGE_ICON_MESSAGE = 3;


    // BROADCAST CHAT PERSON
    public final static String CHAT_PERSON = "updateMsgPerson";
    public final static String MSG_PERSON = "msgPerson";

    // TREND TOPICS
    public static String CATEGORY = "Category";
    public static String TAG = "Tag";
    public static String GENERAL = "General";
    public static String CONTEUDOS = "Conteúdos";
    public static String GRUPOS = "Grupos";



    // PERFIL DE USUARIO, ORIGEM
    public final static String PROFILE_ID = "idPerson";
    public final static String PROFILE_ORIGIN = "origem_profile";
    public final static int PROFILE_FROM_MY_PROFILE = 0;
    public final static int PROFILE_FROM_OTHER_USER = 1;


    // FEED ADAPTER LOCAL DE CARREGAMENTO
    public final static int LOAD_CONTENT_FROM_HOMETESTE = -1;
    public final static int LOAD_CONTENT_FROM_HOME = 0;
    public final static int LOAD_CONTENT_FROM_FAVORITE = 1;
    public final static int LOAD_CONTENT_FROM_SEARCH = 2;
    public final static int LOAD_CONTENT_FROM_GROUP = 3;
    public final static int LOAD_CONTENT_FROM_TRENDS = 4;
    public final static int LOAD_CONTENT_FROM_PROFILE = 5;
    public final static int LOAD_CONTENT_FROM_QRCODE = 6;
    public final static int LOAD_CONTENT_FROM_SINGLE_FEED = 7;


    // MISSÕES
    public final static int RESULT_ACTION_MISSION = 0;

    // DENUNCIAS

    public final static String BUM_PERSON_LOGIN = "UserLogin";
    public final static String PERSON_LOGIN = "User";


}
