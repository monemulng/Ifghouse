package ifg.tcc.ifghouse;

/**
 * Created by Zeoraima on 18-01-2016.
 */
public class ConnState {
    // Indicaçao de como esta a conexão
    public static final int STATE_NONE = 0;       	// we're doing nothing
    public static final int STATE_LISTEN = 1;     	// now listening for incoming connections
    public static final int STATE_CONNECTING = 2; 	// now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  	// now connected to a remote device
    public static final int STATE_NULL = -1;  	 	// now service is null

    // Comandos
    public static final String ATIVA_DESCOBRIMENTO = "100;m;1;";
    public static final String DESATIVA_DESCOBRIMENTO = "100;m;0;";
    public static final String RECEBE_COMPONENTES = "100;s;0;";
    public static final String RECEBE_STATUS = "100;s;1;";

    // Mensagens de erro
    public static final int NOT_FOUND = 55;

    // Tipos de mensagens enviadas como resposta
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Códigos de requisição de intent
    public static final int REQUEST_CONNECT_DEVICE = 384;
    public static final int REQUEST_ENABLE_BT = 385;


    // Nomes recebidos do serviço
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    public static final boolean DEVICE_ANDROID = true;
    public static final boolean DEVICE_OTHER = false;

    // Retorno do extra do intent
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

}