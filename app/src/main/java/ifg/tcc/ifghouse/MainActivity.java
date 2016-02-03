package ifg.tcc.ifghouse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends Activity implements View.OnClickListener{

    // Variaveis da comunicação Bluetooth
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread receiverThread;
    byte[] readBuffer;
    int readBufferPosition;
    public int statusFlag;
    volatile boolean stopThread;

    DBhandler dbHandler;

    // Variaveis da activity
    TextView labelDebug, txtView2;
    EditText cmdBox;
    Button btnFindMaster, btnSendCommand, btnDebug,
            btnFindDevices, btnPairComponents, btnExit,
            btnPopulate, btnControl;
    Switch swtConfig;

    // Variáveis do banco de dados
    private SQLiteDatabase db;
    private CursorAdapter dataSource;

    // Handler de Bluetooth, acho que vamos apagar esse
    BThandler btHandler;

    String play = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSendCommand = (Button)findViewById(R.id.btnSendCommand);
        btnFindMaster = (Button)findViewById(R.id.btnConnect);
        btnFindDevices = (Button)findViewById(R.id.btnFindDevices);
        btnPairComponents = (Button)findViewById(R.id.btnPairComponents);
        btnDebug = (Button)findViewById(R.id.btnDebug);
        btnExit = (Button)findViewById(R.id.btnExit);
        btnControl = (Button)findViewById(R.id.btnControl);

        // Grava funções do listener nos botões
        btnSendCommand.setOnClickListener(this);
        btnFindMaster.setOnClickListener(this);
        btnFindDevices.setOnClickListener(this);
        btnPairComponents.setOnClickListener(this);
        btnDebug.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnControl.setOnClickListener(this);

        swtConfig = (Switch)findViewById(R.id.swtConfig);

        BluetoothApp app = (BluetoothApp)getApplication();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Liga Bluetooth
        if(mBluetoothAdapter == null) msgPop("Não encontrei Bluetooth neste dispositivo.");
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        // Cria instância da classe DBHandler, que cuida do banco de dados
        dbHandler = new DBhandler(this);

        if (dbHandler.populateDatabase()) msgPop("Pop+");

        boolean master_on;

        try {
            Intent pegaMaster = getIntent();
            String address = pegaMaster.getStringExtra(DeviceList.EXTRA_ADDRESS);
            master_on = true;
            app.btHandler.connect(address.toString());
            msgPop("Pareado com " + address.toString());
        } catch (Exception e) {
            master_on = false;
        }

        // Listener do Switch do modo de configuração
        swtConfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    try {
                        BluetoothApp app = (BluetoothApp)getApplication();
                        app.btHandler.sendData(ConnState.ATIVA_DESCOBRIMENTO);
                        msgPop("Modo Configuração Ativado!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        msgPop("Não possível ativar modo configuração!");
                    }
                } else
                {
                    try {
                        BluetoothApp app = (BluetoothApp)getApplication();
                        app.btHandler.sendData(ConnState.DESATIVA_DESCOBRIMENTO);
                        msgPop("Modo Configuração Desligado!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        msgPop("Não possível desativar modo configuração!");
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnConnect:
                // Botão Cria Conexão
                Intent devicelist = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(devicelist, ConnState.REQUEST_CONNECT_DEVICE);

            case R.id.btnSendCommand:
                // Botão Envia Comando Digitado
                try
                {
                    BluetoothApp app = (BluetoothApp)getApplication();
                    app.btHandler.sendData(ConnState.ATIVA_DESCOBRIMENTO);
                }
                catch (IOException ex)
                {
                    msgPop("Não consegui enviar o comando..");
                }
                break;

            case R.id.btnFindDevices:
                // Recebe os componentes no banco de dados
                try {
                    BluetoothApp app = (BluetoothApp)getApplication();
                    app.statusCheck = 0;
                    app.btHandler.sendData(ConnState.RECEBE_COMPONENTES);
                    msgPop("Encontrando componentes...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnControl:
                // Botão para entrar na Activity que monitora os componentes
                try {
                    BluetoothApp app = (BluetoothApp)getApplication();
                    app.statusCheck = 1;
                    app.btHandler.sendData(ConnState.RECEBE_STATUS);
                    msgPop("Recebendo status...");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.btnPairComponents:
                // Botão Pareador de Componentes
                Intent D = new Intent(MainActivity.this, PairDevice.class);
                startActivityForResult(D, 1);
                break;

            case R.id.btnExit:
                // APAGA O BANCO DE DADOS
                dbHandler.clearDatabase();
                break;

            case R.id.btnDebug:
                // Debug de banco de dados
                Intent log = new Intent(MainActivity.this, DBdebug.class);
                startActivity(log);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String dv1 = data.getStringExtra("dv1");
                String dv2 = data.getStringExtra("dv2");
                try {
                    BluetoothApp app = (BluetoothApp)getApplication();
                    app.btHandler.pairDevices(dv1,dv2);
                    msgPop("mandei : 100;g;1;"+dv1+";"+dv2+";");
                } catch (IOException e) {
                    e.printStackTrace();
                    msgPop("PAREAMENTO ERRADO!");
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                msgPop("Não houve sucesso em selecionar os dispositivos!");
            }
        }
    }//onActivityResult


    // Economizando no Toast
    private void msgPop(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}