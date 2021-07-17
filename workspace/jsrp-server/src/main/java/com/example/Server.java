/*
 * サーバ側のメインクラス
 */
package com.example;

import java.io.IOException;
import java.net.*;

public class Server {
	/** 最大接続数(10クライアント) */
	private static final int MAX_CONNECTION = 10;
	/** サーバソケット */
	private static ServerSocket serverSocket;

	/**
	 * mainメソッド
	 * 
	 * @param args [0]リッスンポート番号
	 */
	public static void main(String[] args) {
		// コマンドラインの引数をチェック
		if (args.length != 1) {
			System.out.println("引数が適切ではありません。");
			System.out.println("usage:BasicTask4Server port");
			System.exit(1);
		}

		// コマンドライン引数からリッスンするポート番号を設定
		int port = Integer.parseInt(args[0]);

		Socket socket = null;
		ServerTask task = null;
		Thread thread = null;

		try {
			// サーバソケットの生成
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			// 指定ポート番号にバインド
			serverSocket.bind(new InetSocketAddress(port), MAX_CONNECTION);
			System.out.println("ポート" + port + "番をリッスン中...");

			while (true) {
				/*
				 * クライアントからの接続要求を待機（クライアント接続するまでブロッキング） ー接続が完了すると，クライアントと通信するためのソケットが生成される
				 */
				socket = serverSocket.accept();
				// 接続したクライアントの情報を表示
				showClientInformation(socket);

				task = new ServerTask(socket);
				thread = new Thread(task);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private static void showClientInformation(Socket socket) throws IOException {
		// クライアントのIPアドレスを取得
		InetAddress address = socket.getInetAddress();
		// クライアントのポート番号を取得
		int port = socket.getPort();

		System.out.println("クライアント[" + address.toString() + ":" + port + "]が接続しました。");
	}
}
