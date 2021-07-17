package com.example.client;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TcpClientCallback {
    protected EditText editInputbox;
    protected EditText editAddress;
    protected EditText editPort;
    protected ImageView imageView;
    protected Spinner spinner;
    protected Button buttonExecute;
    protected Button buttonClear;
    protected TextView viewLog;
    private TcpClientTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editInputbox=findViewById(R.id.editInputBox);
        spinner = findViewById(R.id.spinner);
        buttonExecute=findViewById(R.id.buttonExecute);
        buttonClear=findViewById(R.id.buttonClear);
        editAddress=findViewById(R.id.editAddress);
        editPort=findViewById(R.id.editPort);
        viewLog=findViewById(R.id.viewLog);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.list, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // 次の行は消さないこと
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
    }

    /**
     * Connectボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleExecute(View view) {
        // サーバのIPアドレスとポート番号を取得
        String address = editAddress.getText().toString();
        int port = Integer.parseInt(editPort.getText().toString());

        // TCPクライアントタスクを生成してバックグラウンドで実行（非同期処理）
        task = new TcpClientTask(address, port, this);
        task.execute();

        // 入力したメッセージを取得
        String message = editInputbox.getText().toString();

        Map<String,String> langCode=new HashMap<>();
        langCode.put("Japanese","ja");
        langCode.put("Simplified Chinese","zh-CN");
        langCode.put("English","en");

        // 言語コードを取得
        String lang=langCode.get((String)spinner.getSelectedItem());
        task.sendJson(lang,message);
    }

    /**
     * Clearボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleButtonClear(View view) {
        editInputbox.setText("");
    }

    @Override
    public void onPreExecute() {
        Toast.makeText(this, "ClientTask is started!.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressUpdate(String... values) {
        // メインアクティビティのLogにメッセージを設定または追記
        if (viewLog.length() == 0)
            viewLog.setText(values[0]);
        else
            viewLog.append("\n" + values[0]);
    }

    /**
     * TcpClientTask側のonPostExecute()からコールバックされるメソッド
     * @param aVoid doInBackground()の戻り値
     */
    @Override
    public void onPostExecute(Void aVoid) {
        // トーストの表示
        Toast.makeText(this, "ClientTask is finished.", Toast.LENGTH_SHORT).show();
    }
}
