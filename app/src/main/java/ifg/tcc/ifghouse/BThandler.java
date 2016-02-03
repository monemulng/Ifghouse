package ifg.tcc.ifghouse;

/**
 * Created by Zeoraima on 18-01-2016.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.Toast;

@SuppressLint("NewApi")
public class BThandler {

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

    // Variáveis do banco de dados
    private SQLiteDatabase db;
    private CursorAdapter dataSource;
    DBhandler dbHandler;

    // Mensagens
    int connection_state;

    // Context from activity which call this class
    private Context mContext;

    public BThandler(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // Verifica se BLUETOOTH está ligado e encontra endereço do MASTER
    // Por enquanto MASTER está direcionado manualmente para o nosso BLUETOOTH
    public boolean connect(String address) throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();
        return true;
    }


    // Abre conexão com o MASTER, liga In e Output, e liga Thread de Espera de sinais
    // FUNÇÃO MAIS IMPORTANTE AQUI
    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        //msgPop("COMUNICAÇÃO ABERTA !!!");
    }

    // HANDLER - Recebe e codifica os sinais, preparar para descartá-los
    // FUNÇÃO MAIS SENSÍVEL AQUI
    void beginListenForData()
    {
        final Handler handler = new Handler();
        // código em ASCII para fim de linha '/n' = 10, estudar a melhor opção
        final byte delimiter = 35; // Sinal de hashtag para termino de msgs

        stopThread = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        final DBhandler dbHandler = new DBhandler(mContext);
        receiverThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    // O APP age aqui quando a mensagem é recebida
                                    // data é o código que será trabalhado
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {

                                        /* *******************************************************************************************
                                             TRABALHAR COM OS DADOS --- PARTE ONDE VAMOS TRABALHAR POR AGORA
                                             senha (;) String(active_pipes) (;) String(i)(??) (;) nomes[i] (;) identificacoes[i] (;) funcoes[i] (!)
                                        ****************************************************************************************** */
                                            //labelDebug.setText(data+"\n"+labelDebug.getText());

                                            dbHandler.addLogData(data);

                                            // Instala os Devices
                                            BluetoothApp app = (BluetoothApp)mContext;

                                            if (app.statusCheck == 0) {
                                                // Retira o lixo da mensagem
                                                String[] msgs = data.split("!");
                                                String[] device = msgs[0].split(";");

                                                // Tira a senha
                                                if ("100".equals(device[0])) {
                                                    boolean ok = dbHandler.addOrUpdateDevice(device[2], device[3], device[4], device[5]);
                                                    //labelDebug.setText(device[0]);
                                                    //if (ok) //msgPop("Novo device instalado!");
                                                } else {
                                                    boolean ok = dbHandler.addOrUpdateDevice(device[0], device[1], device[2], device[3]);
                                                    //if (ok) //msgPop("Novo device instalado!");
                                                }
                                            }
                                            // Recebe os Status dos Devices Instalados
                                            else if (app.statusCheck ==1)
                                            {
                                                String[] msgs = data.split("!");
                                                String[] device = msgs[0].split(";");
                                                // Grava no banco de dados os novos status
                                                if ("100".equals(device[0]))
                                                {
                                                    if (device.length==4) {
                                                        boolean ok = dbHandler.updateDevice(device[2], device[3]);
                                                    }
                                                }
                                                else
                                                {
                                                    if (device.length==2) {
                                                        boolean ok = dbHandler.updateDevice(device[0], device[1]);
                                                    }
                                                }
                                            }

                                            /* *******************************************************************************************
                                                  FIM DA PARTE DOS DADOS DO BLUETOOTH  -- CRIAR MÉTODOS PRA RESUMIR DEPOIS
                                            ****************************************************************************************** */

                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        receiverThread.start();
    }

    public void sendData(String command) throws IOException
    {
        // Método que envia comandos para o Master
        command += "\n";
        try {
            if (command.length()>5) {
                mmOutputStream.write(command.getBytes());
            }
        } catch (NullPointerException e) {
            msgErro("Erro no envio de mensagem: "+e);
        }
        // //msgPop("Mensagem enviada.");
    }

    public void pairDevices(String dv1, String dv2) throws IOException
    {
        // Método que envia comandos para o Master
        String command = "100;g;1;" + dv1 + ";" + dv2 + ";";
        sendData(command);
    }

    // Fechar conexão, sockets e para o Thread, não tem volta, por enquanto..
    void closeBT() throws IOException
    {
        stopThread = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        //msgPop("Bluetooth Desconectado.");
    }

    void msgErro(String what) {
        Log.e("BTHandler", what);
    }

}
