package ifg.tcc.ifghouse;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DBdebug extends Activity implements View.OnClickListener {

    Button btnLoadLog;
    ListView logList;
    TextView textView22;

    private SQLiteDatabase db;
    DBhandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbdebug);

        btnLoadLog = (Button) findViewById(R.id.btnLoadLog);
        logList = (ListView) findViewById(R.id.logList);
        textView22 = (TextView) findViewById(R.id.textView22);

        // Cria instância da classe DBHandler, que cuida do banco de dados
        dbHandler = new DBhandler(this);

        btnLoadLog.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Botão carrega log
            case R.id.btnLoadLog:
                //executa consulta geral de todos os registros cadastrados no banco de dados
                ArrayList list = new ArrayList();

                //Cursor c = dbHandler.getAllPluggedDevices();
                Cursor c = dbHandler.getLogData();
                //Cursor c = dbHandler.getAllHouseDevice();

                String strmaster = "";
                int n = 1;
                if (c.getCount()>0) {
                    while (c.moveToNext()) {
                        // Plugged
                        //String strGo1 = String.valueOf(n)+": " + c.getString(2)+ "-" + c.getString(1) +"-"+ c.getString(7) + "-" + c.getString(5) +"-"+ c.getString(3) + "\n";

                        // Logdata
                        String strGo1 = String.valueOf(n)+": " + c.getString(1) + "\n";

                        // House devices
                        // String strGo1 = String.valueOf(n)+": " + c.getString(1)+ "-" + c.getString(2) + "-" + c.getString(3) + "-" + c.getString(4) +"-"+ c.getString(5) + "\n";
                        strmaster = strmaster + strGo1;
                        n++;
                    }
                    textView22.setText(strmaster);
                } else textView22.setText("Vazio");
                c.close();
/*
                String query = "SELECT * FROM logData";
                db = dbHandler.getWritableDatabase();
                Cursor logs = db.rawQuery(query, null);
                db.close();
                if (logs.getCount() == 0) {
                    msgPop("Nenhum registro encontrado");
                    break;
                }
                if (logs.getCount() > 0) {

                    logs.moveToFirst();
                    while (logs.moveToNext()) {
                        list.add(logs.getString(1));
                    }
                    final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
                    logList.setAdapter(adapter);
                } else {
                    msgPop("Nenhum registro encontrado");
                }
                */

        }
    }

    // Economizando no Toast
    private void msgPop(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

}