package ifg.tcc.ifghouse;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends Activity {

    Button btnSearchDevices;
    ListView listDevices;

    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        btnSearchDevices = (Button)findViewById(R.id.button_scan);
        listDevices = (ListView)findViewById(R.id.list_devices);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        // Liga o bluetooth - (Caso já não esteja ligado)
        if(myBluetooth == null)
        {
            //  Não possui Bluetooth
            msg("Seu periférico não possui Bluetooth");
        }
        else if(!myBluetooth.isEnabled())
        {
            // Pedir permissão para ligar o Bluetooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
    }

    public void procura(View v){
        listaDevicesPareados();
    }

    public void voltaPainel(View v) {
        Intent exit = new Intent(DeviceList.this, MainActivity.class);
        startActivity(exit);
    }

    // Método para colocar os nomes e endereços no ListView
    private void listaDevicesPareados()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); // Pega os nomes e endereços
            }
        }
        else {
            msg("Não encontramos nenhum dispositivo bluetooth na área...");
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.simplelist, list);
        listDevices.setAdapter(adapter);
        listDevices.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    // Método para quando escolher um device, enviar endereço para o painel de comando
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        // ItemClick generico para ListViews ( muito interessante )
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Pega o endereço MAC do device, são os últimos 17 chars
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Cria o intent para enviar os dados para o Painel de Controle
            Intent i = new Intent(DeviceList.this, MainActivity.class);

            // Muda de activity e envia o endereço: EXTRA_ADDRESS
            i.putExtra(EXTRA_ADDRESS, address); //
            startActivity(i);
        }
    };

    // Economizando no Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

}
