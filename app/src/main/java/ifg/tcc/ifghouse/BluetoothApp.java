package ifg.tcc.ifghouse;

import android.app.Application;

/**
 * Created by Zeoraima on 18-01-2016.
 */
public class BluetoothApp extends Application{
    private static BluetoothApp singleton;
    BThandler btHandler;

    public BluetoothApp getInstance(){
        return singleton;
    }

    /*
    Instancia o BThandler no app, mantendo assim uma instancia em background permanente
    enquanto o app estiver funcionando
    */
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        btHandler = new BThandler(getApplicationContext());
    }
    // Digitos verificadores
    int statusCheck = 0;
    int connection_state = 0;

}
