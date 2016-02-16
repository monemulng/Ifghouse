package ifg.tcc.ifghouse;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

public class ShowDevices extends AppCompatActivity {

    TextView txtSense1, txtSense2;
    Switch swtLampada;
    String lampAdress;
    int sensorcount = 0;
    DBhandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_devices);

        txtSense1 = (TextView) findViewById(R.id.txtSense1);
        txtSense2 = (TextView) findViewById(R.id.txtSense2);
        swtLampada = (Switch) findViewById(R.id.swtLampada);

        // Cria instância da classe DBHandler, que cuida do banco de dados
        dbHandler = new DBhandler(this);

        Cursor dispositivos = dbHandler.getAllPluggedDevices();

        while (dispositivos.moveToNext()) {
            int tipo = dispositivos.getInt(1);    // ENDEREÇO
            int end = dispositivos.getInt(2);    // ENDEREÇO
            //msgPop("Wht? : "+String.valueOf(tipo)+", "+String.valueOf(end)+", "+String.valueOf(enda)+", "+String.valueOf(ende)+", "+String.valueOf(endi)+", "+" ...");
            String[] texto = null;
            switch (tipo) {
                case 2: // LAMPADA
                    if (dispositivos.getString(5)=="1") {
                        swtLampada.setChecked(true);
                    } else {
                        swtLampada.setChecked(false);
                    }
                    swtLampada.setText("Lâmpada LED");
                    lampAdress = "100;c;"+end+";";
                    swtLampada.setVisibility(View.VISIBLE);
                    break;
                case 7: // SENSOR HIGROMETRO
                    texto = dispositivos.getString(5).split("%");
                    txtSense1.setText("Humidade: "+texto[0].toString()+" %");
                    txtSense2.setText("Temperatura: "+texto[1].toString()+" ºC");
                    txtSense1.setVisibility(View.VISIBLE);
                    txtSense2.setVisibility(View.VISIBLE);
                    break;
            }
        }
        dispositivos.close();

        swtLampada.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        BluetoothApp app = (BluetoothApp) getApplication();
                        app.btHandler.sendData(lampAdress + "1;");
                    } catch (IOException e) {
                        e.printStackTrace();
                        msgPop("Problemas com a lâmpada!");
                    }
                } else {
                    try {
                        BluetoothApp app = (BluetoothApp) getApplication();
                        app.btHandler.sendData(lampAdress+"0;");
                    } catch (IOException e) {
                        e.printStackTrace();
                        msgPop("Problemas com a lâmpada!");
                    }
                }
            }
        }); // */
    }

    // Economizando no Toast
    private void msgPop(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    // Botão voltar para Main
    public void ShowGoMain(View v){
        Intent T = new Intent(ShowDevices.this, MainActivity.class);
        startActivity(T);
    }
}