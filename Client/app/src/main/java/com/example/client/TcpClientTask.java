package com.example.client;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClientTask extends AsyncTask<Void, String, Void> {
    /** サーバのIPアドレス */
    private String address;
    /** サーバのポート番号 */
    private int port;
    /** ソケット */
    private Socket socket;
    /** データ入力用ストリーム */
    private DataInputStream dis;
    /** データ出力用ストリーム */
    private DataOutputStream dos;
    /** ソケットの受信ループフラグ */
    private boolean isLoop;

    private TcpClientCallback callback;

    public TcpClientTask(String address, int port, TcpClientCallback callback) {
        this.address = address;
        this.port = port;
        this.callback = callback;
        this.isLoop = true;
    }

    /**
     * バックグラウンド処理
     * （アクティビティのUI操作は不可能）
     * @param params    未使用（Void型の配列）
     * @return  null
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            // サーバへ接続（3秒でタイムアウト）
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 3000);

            try {
                // コネクションが確立したらソケットの入出力ストリームにバッファ付ストリームと
                // データ入出力ストリームを連結
                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                // UIを操作したい場合はpublishProgress()を実行してonProgressUpdate()をコールバック
                publishProgress("established a connection with "
                        + socket.getInetAddress().toString() + ":" + socket.getPort() + "!");

                // サーバからの応答受信ループ（ノンブロッキング）
                while (isLoop) {
                    // データ入力ストリームに読み込み可能なデータがあるか確認
                    if (dis.available() > 0) {
                        // データを受信
                        String log = dis.readUTF();

                        if (log == "disconnect succeeded."){
                            publishProgress(log);
                            this.stop();
                            continue;
                        }

                        if(log=="connect succeeded"||log=="run succeeded"){
                            publishProgress(log);
                            continue;
                        }

                        throw new Exception(log);

                    } else {
                        // 100ms待機（過負荷対策）
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Log.e("Sample5", "error", ie);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
                publishProgress("[ERROR] " + e.getMessage());
            } finally {
                // ソケットを閉じる
                close();
            }
        } catch (Exception e) {
            Log.e("Sample5", "error", e);
            publishProgress("[ERROR] " + e.getMessage());
        }

        return null;
    }

    /**
     * バックグラウンド処理を行う前の事前処理
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (callback != null)
            callback.onPreExecute();
    }

    /**
     * doInBackground()の処理が終了したときに呼び出されるメソッド
     * @param aVoid doInBackground()の戻り値
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callback != null)
            callback.onPostExecute(aVoid);
    }

    /**
     * doInBackground()内でpublishProgress()が呼ばれたときに呼び出されるメソッド
     * @param values   Logに出力するメッセージ
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (callback != null)
            callback.onProgressUpdate(values);
    }

    /**
     * メッセージをサーバへ送信するメソッド
     * @param message   メッセージ
     */
    public void sendJson(String lang,String message) {
        if (message != null && lang!=null) {
            try {
                Model model=new Model();

                model.lang=lang;
                model.message=message;

                Gson gson=new Gson();
                String json=gson.toJson(model);

                // メッセージをサーバへ送信
                dos.writeUTF(json);
                dos.flush();

                publishProgress("send the message '" + message + "' to the server.");
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }
        }
    }

    /**
     * 受信処理ループを停止するメソッド
     */
    public void stop() {
        this.isLoop = false;
    }

    /**
     * ソケット及びストリームを閉じるメソッド
     */
    private void close() {
        // ソケットを閉じる
        if (socket != null && socket.isConnected())
            try {
                socket.close();
                publishProgress("socket is closed.");
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }

        // データ入力ストリームを閉じる
        if (dis != null)
            try {
                dis.close();
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }

        // データ出力ストリームを閉じる
        if (dos != null)
            try {
                dos.flush();
                dos.close();
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }
    }

}
