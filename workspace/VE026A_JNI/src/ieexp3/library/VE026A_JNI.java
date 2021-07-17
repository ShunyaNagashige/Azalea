package ieexp3.library;

import java.io.*;

/**
 * VE026A操作用ネイティブメソッドライブラリ（JNI）<br>
 * 情報工学実験3で使用するロボットVE026Aを制御するために利用するネイティブメソッドライブラリ
 *
 * @author Hidekazu Suzuki
 * @version 1.0 - 2015/02/25
 */
public class VE026A_JNI {

	/**
	 * コンストラクタ（クラスが初期化されたとき、ライブラリを読み込む）
	 */
	public VE026A_JNI() {
		try {
			// VE026A操作用JNI関数ライブラリ（VE026A_JNI.dll）をVE026A_JNI.jarから読み込み
			loadLibraryFromJar("/VE026A_JNI.dll");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * JarファイルからDLLファイルを読み込むメソッド
	 * 
	 * @param path	Jarファイル内のDLLファイルパス（先頭に/を付けること）
	 * @throws IOException	一時ファイルの作成または読み書き処理が失敗した場合
     * @throws IllegalArgumentException	pathで指定したDLLファイルが存在しない場合
     * @throws IllegalArgumentException	pathが絶対パスでない場合、またはDLLファイル名が3文字より短い場合
	 */
	private void loadLibraryFromJar(String path) throws IOException {
		// 接頭辞が"/"で始まっているかチェック
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException(
					"The path has to be absolute (start with '/').");
		}

		// 引数で指定されたpathからファイル名を取得
		String[] parts = path.split("/");
		String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

		// ファイル名をプレフィックス（名前）とサフィックス（拡張子）に分ける
		String prefix = "";
		String suffix = null;
		if (filename != null) {
			parts = filename.split("\\.", 2);
			prefix = parts[0];
			suffix = (parts.length > 1) ? "." + parts[parts.length - 1] : null;
		}

		// ファイル名（プレフィックス）が3文字以上あるかチェック
		if (filename == null || prefix.length() < 3) {
			throw new IllegalArgumentException(
					"The filename has to be at least 3 characters long.");
		}

		// 一時ファイルを作成
		File temp = File.createTempFile(prefix, suffix);
		// 仮想マシンが終了するときに一時ファイルを削除するよう設定
		temp.deleteOnExit();

		if (!temp.exists()) {
			throw new FileNotFoundException("File " + temp.getAbsolutePath()
					+ " does not exist.");
		}

		// DLLデータのコピー用バッファの準備
		byte[] buffer = new byte[1024];
		int readBytes;

		// Jarファイル内の指定されたパスにあるDLLファイルの入力ストリームを開く
		InputStream is = VE026A_JNI.class.getResourceAsStream(path);
		if (is == null) {
			throw new FileNotFoundException("File " + path
					+ " was not found inside JAR.");
		}

		// 出力ストリームを開き、DLLファイルを一時ファイルにコピー
		OutputStream os = new FileOutputStream(temp);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} finally {
			// 読み書きに失敗した場合はストリームを閉じてから例外を投げる
			os.close();
			is.close();
		}

		// 一時ファイルとしてコピーしたDLLファイルを読み込む
		System.load(temp.getAbsolutePath());
	}

	// ============================================================
	// ネイティブメソッドの宣言
	// 以下，VE026A_JNI.cppで定義したネイティブ関数の雛形と一致するように宣言
	// ============================================================

	/**
	 * DCOMを初期化するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_Cao_1InitDCOM関数）
	 * 
	 * @return S_OK, S_FALSE（成功）， E_INVALIDARG, E_OUTOFMEMORY, E_UNEXPECTED（失敗）
	 */
	public native int Cao_InitDCOM();

	/**
	 * DCOMの初期化を解除するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_Cao_1UninitDCOM関数）
	 */
	public native void Cao_UninitDCOM();

	/**
	 * エンジンオブジェクトを作成するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_Cao_1CreateEngine関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int Cao_CreateEngine();

	/**
	 * エンジンオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_Cao_1ClearEngine関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止）
	 */
	public native int Cao_ClearEngine();

	/**
	 * ワークスペースコレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoEngine_1Workspaces関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoEngine_Workspaces();

	/**
	 * ワークスペースオブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoEngine_1AddWorkspace関数）
	 * 
	 * @param strName
	 *            ワークスペース名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoEngine_AddWorkspace(String strName, String strOption);

	/**
	 * ワークスペースコレクション数を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspaces_1Count関数）
	 * 
	 * @return ワークスペースコレクション数（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoWorkspaces_Count();

	/**
	 * ワークスペースオブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspaces_1Add関数）
	 * 
	 * @param strName
	 *            ワークスペース名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoWorkspaces_Add(String strName, String strOption);

	/**
	 * 全てのワークスペースオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspaces_1Clear関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoWorkspaces_Clear();

	/**
	 * ワークスペースオブジェクトを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspaces_1Item関数）
	 * 
	 * @param strName
	 *            ワークスペース名
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoWorkspaces_Item(String strName);

	/**
	 * ワークスペースオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspaces_1Remove関数）
	 * 
	 * @param strName
	 *            ワークスペース名
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoWorkspaces_Remove(String strName);

	/**
	 * コントローラコレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspace_1Controllers関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoWorkspace_Controllers();

	/**
	 * ワークスペース番号を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspace_1Index関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoWorkspace_Index();

	/**
	 * ワークスペース名を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspace_1Name関数）
	 * 
	 * @return ワークスペース名（成功），NULL（失敗）
	 */
	public native String CaoWorkspace_Name();

	/**
	 * コントローラを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoWorkspace_1AddController関数）
	 * 
	 * @param strCtrlName
	 *            コントローラ名（任意の文字列）
	 * @param strProvName
	 *            プロバイダ名
	 * @param strPcName
	 *            プロバイダの実行マシン名（同一マシン上の場合は空文字）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoWorkspace_AddController(String strCtrlName,
			String strProvName, String strPcName, String strOption);

	/**
	 * コントローラコレクション数を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoControllers_1Count関数）
	 * 
	 * @return コントローラコレクション数（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoControllers_Count();

	/**
	 * コントローラオブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoControllers_1Add関数）
	 * 
	 * @param strCtrlName
	 *            コントローラ名（任意の文字列）
	 * @param strProvName
	 *            プロバイダ名
	 * @param strPcName
	 *            プロバイダの実行マシン名（同一マシン上の場合は空文字）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoControllers_Add(String strCtrlName,
			String strProvName, String strPcName, String strOption);

	/**
	 * 全てのコントローラオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoControllers_1Clear関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoControllers_Clear();

	/**
	 * コントローラオブジェクトを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoControllers_1Item関数）
	 * 
	 * @param strName
	 *            コントローラ名
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoControllers_Item(String strName);

	/**
	 * コントローラオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoControllers_1Remove関数）
	 * 
	 * @param strName
	 *            コントローラ名
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoControllers_Remove(String strName);

	/**
	 * コントローラ番号を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1Index関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoController_Index();

	/**
	 * コントローラ名を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1Name関数）
	 * 
	 * @return コントローラ名（成功），NULL（失敗）
	 */
	public native String CaoController_Name();

	/**
	 * ロボットコレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1Robots関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoController_Robots();

	/**
	 * タスクコレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1Tasks関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoController_Tasks();

	/**
	 * コントローラクラスの変数コレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1Variables関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoController_Variables();

	/**
	 * ロボットオブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1AddRobot関数）
	 * 
	 * @param strName
	 *            ロボット名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoController_AddRobot(String strName, String strOption);

	/**
	 * タスクオブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1AddTask関数）
	 * 
	 * @param strName
	 *            タスク名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoController_AddTask(String strName, String strOption);

	/**
	 * 指定したコントローラクラスの変数を読み書きするためにアクセスするネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1AddVariable関数）
	 * 
	 * @param strName
	 *            変数名
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoController_AddVariable(String strName, String strOption);

	/**
	 * コントローラクラスに実装されている拡張コマンドを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoController_1Execute関数）
	 * 
	 * @param strCmd
	 *            コマンド
	 * @param strParam
	 *            パラメータ（文字列で指定）
	 * @return 実行結果の文字列（成功），NULL（失敗）
	 */
	public native String CaoController_Execute(String strCmd, String strParam);

	/**
	 * ロボットコレクション数を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobots_1Count関数）
	 * 
	 * @return ロボットコレクション数（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoRobots_Count();

	/**
	 * ロボットオブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobots_1Add関数）
	 * 
	 * @param strName
	 *            ロボット名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobots_Add(String strName, String strOption);

	/**
	 * 全てのロボットオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobots_1Clear関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobots_Clear();

	/**
	 * ロボットオブジェクトを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobots_1Item関数）
	 * 
	 * @param strName
	 *            ロボット名
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobots_Item(String strName);

	/**
	 * ロボットオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobots_1Remove関数）
	 * 
	 * @param strName
	 *            ロボット名
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobots_Remove(String strName);

	/**
	 * ロボット番号を取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Index関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoRobot_Index();

	/**
	 * ロボット名を取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Name関数）
	 * 
	 * @return ロボット名（成功），NULL（失敗）
	 */
	public native String CaoRobot_Name();

	/**
	 * ロボットクラスの変数コレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Variables関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Variables();

	/**
	 * ロボットの内部加速度と内部減速度を設定するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Accelerate関数）
	 * 
	 * @param lAxis
	 *            軸番号（-1: 手先加速度(ACCEL), 0: 全軸加速度(JACCEL)）
	 * @param fAccel
	 *            加速度（0.0f-100.0f、-1: 変更なし）
	 * @param fDecel
	 *            減速度（0.0f-100.0f、-1: 変更なし）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Accelerate(long lAxis, float fAccel, float fDecel);

	/**
	 * 指定したロボットクラスのシステム変数を読み書きするためにアクセスするネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1AddVariable関数）
	 * 
	 * @param strName
	 *            変数名
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_AddVariable(String strName, String strOption);

	/**
	 * ロボットのツール座標系/ユーザ座標系を変更するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Change関数）
	 * 
	 * @param strName
	 *            ツール座標系/ユーザ座標系の名前（"TOOL*" or "WORK*": *は10進数値）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Change(String strName);

	/**
	 * ロボットクラスに実装されている拡張コマンドMotorを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1Motor関数）
	 * 
	 * @param lState
	 *            モータ状態（0:OFF，1:ON）
	 * @param lNoWait
	 *            完了待ち（0:完了待ちする（デフォルト），1:完了待ちしない）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_Motor(long lState, long lNoWait);

	/**
	 * ロボットクラスに実装されている拡張コマンドExtSpeedを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1ExtSpeed関数）
	 * 
	 * @param fSpeed
	 *            外部速度（0.0f-100.0f）
	 * @param fAccel
	 *            外部加速度（0.0f-100.0f、-1: 変更なし）
	 * @param fDecel
	 *            外部減速度（0.0f-100.0f、-1: 変更なし）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_ExtSpeed(float fSpeed, float fAccel,
			float fDecel);

	/**
	 * ロボットクラスに実装されている拡張コマンドTakeArmを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1TakeArm関数）
	 * 
	 * @param lArmGroup
	 *            アームグループ番号（0-31、省略時:0）
	 * @param lKeep
	 *            初期化設定（0:速度を100、ツール番号とワーク番号を0にする、 1:現在の速度、ツール番号、ワーク番号を保持、
	 *            省略時:0）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_TakeArm(long lArmGroup, long lKeep);

	/**
	 * ロボットクラスに実装されている拡張コマンドGiveArmを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1GiveArm関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_GiveArm();

	/**
	 * ロボットクラスに実装されている拡張コマンドDrawを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1Draw関数）
	 * 
	 * @param lComp
	 *            補間方法（1:PTP，2:CP）
	 * @param strPose
	 *            移動量（C1書式）
	 * @param strOption
	 *            動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_Draw(long lComp, String strPose,
			String strOption);

	/**
	 * ロボットクラスに実装されている拡張コマンドApproachを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1Approach関数）
	 * 
	 * @param lComp
	 *            補間方法（1:PTP，2:CP）
	 * @param strPoseBase
	 *            基準位置（C0書式）
	 * @param strPoseLen
	 *            アプローチ長（C2書式）
	 * @param strOption
	 *            動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_Approach(long lComp, String strPoseBase,
			String strPoseLen, String strOption);

	/**
	 * ロボットクラスに実装されている拡張コマンドDepartを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1Depart関数）
	 * 
	 * @param lComp
	 *            補間方法（1:PTP，2:CP）
	 * @param strPoseLen
	 *            デパート長（C2書式）
	 * @param strOption
	 *            動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_Depart(long lComp, String strPoseLen,
			String strOption);

	/**
	 * ロボットクラスに実装されている拡張コマンドDriveExを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1DriveEx関数）
	 * 
	 * @param strPoses
	 *            軸番号と移動量（C3書式）
	 * @param strOption
	 *            動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_DriveEx(String strPoses, String strOption);

	/**
	 * ロボットクラスに実装されている拡張コマンドDriveAExを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1DriveAEx関数）
	 * 
	 * @param strPoses
	 *            軸番号と移動量（C3書式）
	 * @param strOption
	 *            動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_DriveAEx(String strPoses,
			String strOption);

	/**
	 * ロボットクラスに実装されている拡張コマンドRotateHを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Execute_1RotateH関数）
	 * 
	 * @param strPoseAxis
	 *            アプローチベクトル回りの相対回転角（C2書式）
	 * @param strOption
	 *            動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Execute_RotateH(String strPoseAxis,
			String strOption);

	/**
	 * 動作中のロボットを一時停止させるネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Hold関数）
	 * 
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Hold(String strOption);

	/**
	 * 動作中のロボットを強制停止させるネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Halt関数）<br>
	 * ※TAKEARMしている場合はエラーとなるため，CaoTask_Stopメソッドで停止させてから実行すること．
	 * 
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Halt(String strOption);

	/**
	 * ロボットを動作させるネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoRobot_1Move関数）
	 * 
	 * @param lComp
	 *            補間指定（1L: PTP, 2L: CP_Linear, 3L: CP_Circle, 4L: Spline）
	 * @param strPose
	 *            ポーズ列（文字列で指定）
	 * @param strOption
	 *            動作オプション（文字列で指定）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Move(long lComp, String strPose, String strOption);

	/**
	 * 指定した軸周りの回転動作を行うネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_Rotate関数）
	 * 
	 * @param strRotSuf
	 *            回転面（文字列で指定）
	 * @param fDeg
	 *            角度（deg）
	 * @param strPivot
	 *            回転中心（文字列で指定）
	 * @param strOption
	 *            動作オプション（文字列で指定）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Rotate(String strRotSuf, float fDeg,
			String strPivot, String strOption);

	/**
	 * ロボットの内部移動速度を設定するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_Speed関数）
	 * 
	 * @param lAxis
	 *            軸番号（-1: 手先速度(SPEED), 0: 全軸速度(JSPEED)）
	 * @param fSpeed
	 *            速度（0.0f-100.0f）
	 * @return S_OK（成功），E_ABORT（中止）E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Speed(long lAxis, float fSpeed);

	/**
	 * 一時停止中のロボットの動作を再開させるネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoRobot_Unhold関数）
	 * 
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoRobot_Unhold(String strOption);

	/**
	 * タスクコレクション数を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoTasks_Count関数）
	 * 
	 * @return タスクコレクション数（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoTasks_Count();

	/**
	 * タスクオブジェクトを追加するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTasks_Add関数）
	 * 
	 * @param strName
	 *            タスク名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTasks_Add(String strName, String strOption);

	/**
	 * 全てのタスクオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoTasks_Clear関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTasks_Clear();

	/**
	 * タスクオブジェクトを取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTasks_Item関数）
	 * 
	 * @param strName
	 *            タスク名
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTasks_Item(String strName);

	/**
	 * タスクオブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoTasks_Remove関数）
	 * 
	 * @param strName
	 *            タスク名
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTasks_Remove(String strName);

	/**
	 * タスク番号を取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTask_Index関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoTask_Index();

	/**
	 * タスク名を取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTask_Name関数）
	 * 
	 * @return タスク名（成功），NULL（失敗）
	 */
	public native String CaoTask_Name();

	/**
	 * タスククラスの変数コレクションを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoTask_Variables関数）
	 * 
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTask_Variables();

	/**
	 * 指定したタスククラスのシステム変数を読み書きするためにアクセスするネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoTask_AddVariable関数）
	 * 
	 * @param strName
	 *            変数名
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTask_AddVariable(String strName, String strOption);

	/**
	 * タスクを削除するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTask_Delete関数）
	 * 
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTask_Delete(String strOption);

	/**
	 * タスククラスに実装されている拡張コマンドを実行するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoTask_Execute関数）
	 * 
	 * @param strCmd
	 *            コマンド
	 * @param strParam
	 *            パラメータ（文字列で指定）
	 * @return 実行結果の文字列（成功），NULL（失敗）
	 */
	public native String CaoTask_Execute(String strCmd, String strParam);

	/**
	 * タスクを開始するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTask_Start関数）
	 * 
	 * @param lMode
	 *            モード指定（1L: 1サイクル実行, 2L: 連続実行, 3L: 1ステップ送り, 4L: 1ステップ戻し）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTask_Start(long lMode, String strOption);

	/**
	 * タスクを停止するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoTask_Stop関数）
	 * 
	 * @param lMode
	 *            モード指定（0L: デフォルト停止, 1L: 瞬時停止, 2L: ステップ停止, 3L: サイクル停止, 4L: 初期停止）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoTask_Stop(long lMode, String strOption);

	/**
	 * 変数コレクション数を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariables_Count関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @return 変数コレクション数（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoVariables_Count(int iValType);

	/**
	 * 変数オブジェクトを追加するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariables_Add関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @param strName
	 *            変数名（任意の文字列）
	 * @param strOption
	 *            オプション文字列（指定しない場合は空文字）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoVariables_Add(int iValType, String strName,
			String strOption);

	/**
	 * 全ての変数オブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariables_Clear関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoVariables_Clear(int iValType);

	/**
	 * 変数オブジェクトを取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariables_Item関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @param strName
	 *            変数名
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoVariables_Item(int iValType, String strName);

	/**
	 * 変数オブジェクトを解放するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariables_Remove関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @param strName
	 *            変数名
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoVariables_Remove(int iValType, String strName);

	/**
	 * 変数番号を取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoVariable_Index関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native long CaoVariable_Index(int iValType);

	/**
	 * 変数名を取得するネイティブメソッド （Java_ieexp3_library_VE026A_1JNI_CaoVariable_Name関数）
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（VAL_CTRL:コントローラ，VAL_ROBOT:ロボット，VAL_TASK:タスク）
	 * @return 変数名（成功），NULL（失敗）
	 */
	public native String CaoVariable_Name(int iValType);

	/**
	 * 指定した変数オブジェクトの値を取得するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariable_GetValue関数）<br>
	 * ※事前に各クラスのAddVariable関数で値を取得したい変数にアクセスしておくこと．
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（1: コントローラ，2: ロボット，3: タスク）
	 * @return 変数の値の文字列（成功），NULL（失敗）
	 */
	public native String CaoVariable_GetValue(int iValType);

	/**
	 * 指定した変数オブジェクトの値を設定するネイティブメソッド
	 * （Java_ieexp3_library_VE026A_1JNI_CaoVariable_PutValue関数）<br>
	 * ※事前に各クラスのAddVariable関数で値を設定したい変数にアクセスしておくこと．
	 * 
	 * @param iValType
	 *            アクセスする変数の種類（1: コントローラ，2: ロボット，3: タスク）
	 * @param iDataType
	 *            データ型（VT_I4, VT_R4）
	 * @param strValue
	 *            変数に設定する値の文字列
	 * @return S_OK（成功），E_ABORT（中止），E_FAIL（失敗）
	 */
	public native int CaoVariable_PutValue(int iValType, int iDataType,
			String strValue);

	// ========== ネイティブメソッドの宣言（ここまで） ==========
}
