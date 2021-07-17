package ieexp3.library;

/**
 * VE026A操作用CAO API（情報工学実験3 ライブラリ）
 * 
 * @author Hidekazu Suzuki
 * @version 1.1 - 2017/04/07
 */
public class CaoAPI {
	/** JNIネイティブメソッドが成功した場合に返ってくる値 */
	private static final int S_OK = 0;
	/** JNIネイティブメソッドが失敗した場合に返ってくるエラーコード */
	private static final int E_FAIL = 0x80004005;
	
	/** プロバイダ名 */
	private static final String PROVIDER_NAME = "CaoProv.DENSO.RC8";
	/** プロバイダの実行マシン名（同一マシン上の場合は空文字） */
	private static final String PC_NAME = "";
	
	/** コントローラの手動モード値 */
	public static final int CONTROLLER_MODE_MANUAL = 1;
	/** コントローラのティーチチェックモード値 */
	public static final int CONTROLLER_MODE_CHECK = 2;
	/** コントローラの自動モード値 */
	public static final int CONTROLLER_MODE_AUTO = 3;
	
	/** CAOエンジンのコントローラ用変数を示す配列番号 */
	private static final int VAL_CTRL = 0;
	/** CAOエンジンのロボット用変数を示す配列番号 */
	private static final int VAL_ROBOT = 1;
	/** CAOエンジンのタスク用変数を示す配列番号 */
	private static final int VAL_TASK = 2;
	/** CAOエンジンの変数を示す配列番号の最大値 */
	private static final int VAL_MAX = 3;
	
	/** データ型VT_I2に対応する値 */
	private static final int VT_I2 = 2;
	/** データ型VT_I4に対応する値 */
	private static final int VT_I4 = 3;
	/** データ型VT_R4に対応する値 */
	private static final int VT_R4 = 4;
	/** データ型VT_R8に対応する値 */
	private static final int VT_R8 = 5;
	/** データ型VT_CYに対応する値 */
	private static final int VT_CY = 6;
	/** データ型VT_DATEに対応する値 */
	private static final int VT_DATE = 7;
	/** データ型VT_BSTRに対応する値 */
	private static final int VT_BSTR = 8;
	/** データ型VT_BOOLに対応する値 */
	private static final int VT_BOOL = 11;
	/** データ型VT_VARIANTに対応する値 */
	private static final int VT_VARIANT = 17;
	/** データ型VT_ARRAYに対応する値 */
	private static final int VT_ARRAY = 8192;
	
	/** VE026A制御用JNIオブジェクト */
	private static VE026A_JNI jni;
	/** ロボットの接続状態を示すフラグ（接続中:true、未接続:false） */
	private static boolean isConnected;
	/** ロボットアーム制御権の取得状態を示すフラグ（取得:true、解放:false） */
	private static boolean hasArm;
	/** モータのON/OFF状態を示すフラグ（ON:true、OFF:false） */
	private static boolean isTurnedOn;
	
	/**
	 * CAO APIロード時に最初に行う初期化処理（static initializer）
	 */
	static {
		// VE026A制御用JNIオブジェクトを生成
		jni = new VE026A_JNI();
		// フラグの設定
		isConnected = false;	// ロボット未接続
		hasArm = false;			// アーム制御権なし
		isTurnedOn = false;		// モータOFF
	}
	
	/**
	 * ロボットの接続状態を取得するクラスメソッド
	 * 
	 * @return	ロボットの接続状態
	 */
	public static boolean isConnected() {
		return isConnected;
	}

	/**
	 * アーム制御権の取得状態を取得するクラスメソッド
	 * 
	 * @return	アーム制御権の取得状況
	 */
	public static boolean hasArm() {
		return hasArm;
	}

	/**
	 * モータのON/OFF状態を取得するクラスメソッド
	 * 
	 * @return	モータのON/OFF状況
	 */
	public static boolean isTurnedOn() {
		return isTurnedOn;
	}
	
	/**
	 * CAO APIの初期化処理を行うクラスメソッド
	 * 
	 * @param workspaceName	作成するワークスペース名（任意）
	 * @throws IllegalArgumentException	引数がNULLの場合
	 * @throws Exception				初期化処理に失敗した場合
	 */
	public static void init(String workspaceName) throws Exception {
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (workspaceName == null)
			throw new IllegalArgumentException("Argument 'workspaceName' is null.");
				
		// DCOMを初期化
		int result = jni.Cao_InitDCOM();
		if (result < S_OK)
			throw new Exception(createErrrorMessage("Cao_InitDCOM() failed.", result));

		// CAOエンジンを作成
		result = jni.Cao_CreateEngine();
		if (result < S_OK)
			throw new Exception(createErrrorMessage("Cao_CreateCaoEngine() failed.", result));

		// ワークスペースコレクションを取得
		result = jni.CaoEngine_Workspaces();
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoEngine_GetCaoWorkspaces() failed.", result));

		// ワークスペースオブジェクトを追加
		result = jni.CaoEngine_AddWorkspace(workspaceName, "");
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoEngine_AddWorkspace() failed.", result));

	}
	
	/**
	 * ロボットに接続するクラスメソッド
	 * 
	 * @param ctrlName		コントローラ名（任意）
	 * @param ctrlOption	コントローラ接続時オプション（指定しない場合は空文字）
	 * @param robotName		ロボット名（任意）
	 * @param robotOption	ロボット接続時オプション（指定しない場合は空文字）
	 * @throws IllegalArgumentException	引数がNULLの場合
	 * @throws Exception				ロボットの接続処理に失敗した場合
	 */
	public static void connect(String ctrlName, String ctrlOption, String robotName, String robotOption) throws Exception {
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (ctrlName == null)
			throw new IllegalArgumentException("Argument 'ctrlName' is null.");
		if (ctrlOption == null)
			throw new IllegalArgumentException("Argument 'ctrlOption' is null.");
		if (robotName == null)
			throw new IllegalArgumentException("Argument 'robotName' is null.");
		if (robotOption == null)
			throw new IllegalArgumentException("Argument 'robotOption' is null.");
		
		// コントローラコレクションを取得
		int result = jni.CaoWorkspace_Controllers();
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoWorkspace_Controllers() failed.", result));

		// コントローラオブジェクトを生成（接続）
		result = jni.CaoWorkspace_AddController(ctrlName, PROVIDER_NAME, PC_NAME, ctrlOption);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoWorkspace_AddController() failed.", result));

		// ロボットコレクションを取得
		result = jni.CaoController_Robots();
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoController_Robots() failed.", result));

		// ロボットオブジェクトを生成（接続）
		result = jni.CaoController_AddRobot(robotName, robotOption);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoController_AddRobot() failed.", result));
		
		// ロボット接続状態を示すフラグをtrueに変更
		isConnected = true;
	}
	
	/**
	 * ロボットに接続するクラスメソッド（オプションを指定しない場合に利用）
	 * 
	 * @param ctrlName		コントローラ名（任意）
	 * @param robotName		ロボット名（任意）
	 * @throws IllegalArgumentException	引数がNULLの場合
	 * @throws Exception				ロボットの接続処理に失敗した場合
	 */
	public static void connect(String ctrlName, String robotName) throws Exception {
		connect(ctrlName, "wpj=*", robotName, "");
	}
	
	/**
	 * ロボットから切断するクラスメソッド
	 * 
	 * @throws Exception	ロボットの切断処理に失敗した場合
	 */
	public static void disconnect() throws Exception {
		/*
		 * CAOオブジェクトの解放
		 */
		// 変数オブジェクト
		int result;
		for (int i = 1; i < VAL_MAX; i++) {
			result = jni.CaoVariables_Clear(i);
			if (result == E_FAIL)
				throw new Exception(createErrrorMessage("CaoVariables_Clear() failed.", result));
		}
		// タスクオブジェクト
		result = jni.CaoTasks_Clear();
		if (result == E_FAIL)
			throw new Exception(createErrrorMessage("CaoTasks_Clear() failed.", result));
		// ロボットオブジェクト
		result = jni.CaoRobots_Clear();
		if (result == E_FAIL)
			throw new Exception(createErrrorMessage("CaoRobots_Clear() failed.", result));
		// コントローラオブジェクト
		result = jni.CaoControllers_Clear();
		if (result == E_FAIL)
			throw new Exception(createErrrorMessage("CaoControllers_Clear() failed.", result));
		// ワークスペースオブジェクト
		result = jni.CaoWorkspaces_Clear();
		if (result == E_FAIL)
			throw new Exception(createErrrorMessage("CaoWorkspaces_Clear() failed.", result));
		// エンジンオブジェクト
		result = jni.Cao_ClearEngine();
		if (result == E_FAIL)
			throw new Exception(createErrrorMessage("Cao_ClearEngine() failed.", result));

		// DCOMの初期化を解除
		jni.Cao_UninitDCOM();

		// ロボット接続状態を示すフラグをfalseに変更
		isConnected = false;
	}
	
	/**
	 * ロボットの外部速度、外部加速度、外部減速度を設定するクラスメソッド
	 * 
	 * @param speed	外部速度（0.0f-100.0f）
	 * @param accel	外部加速度（0.0f-100.0f、-1: 変更なし）
	 * @param decel	外部減速度（0.0f-100.0f、-1: 変更なし）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				設定に失敗した場合
	 */
	public static void setExtSpeed(float speed, float accel, float decel) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(0.0f <= speed && speed <= 100.0f))
			throw new IllegalArgumentException("Invalid speed specified.");
		if (!(0.0f <= accel && accel <= 100.0f))
			throw new IllegalArgumentException("Invalid accel specified.");
		if (!(0.0f <= decel && decel <= 100.0f))
			throw new IllegalArgumentException("Invalid decel specified.");
		
		// 外部速度を最大に設定
		int result = jni.CaoRobot_Execute_ExtSpeed(speed, accel, decel);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_ExtSpeed() failed.", result));
	}
	
	/**
	 * コントローラの動作モードを設定するクラスメソッド
	 * 
	 * @param mode	動作モード（1:手動、2:ティーチチェック、3:自動）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				設定に失敗した場合
	 */
	public static void setControllerMode(long mode) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(1L <= mode && mode <= 3L))
			throw new IllegalArgumentException("Invalid mode specified.");
				
		// コントローラの動作モードをコントローラ用変数オブジェクトに設定
		int result = jni.CaoController_AddVariable("@MODE", "");
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoController_AddVariable(\"@MODE\") failed.", result));

		// 指定された動作モードに設定
		result = jni.CaoVariable_PutValue(VAL_CTRL, VT_I4, String.valueOf(mode));
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoVariable_PutValue(\"@MODE\") failed.", result));
	}

	/**
	 * アーム制御権を取得するクラスメソッド
	 *
	 * @param armGroup	アームグループ番号（0-31、省略時:0）
	 * @param keep	初期化設定（0:速度を100、ツール番号とワーク番号を0にする、 1:現在の速度、ツール番号、ワーク番号を保持、
	 *            省略時:0）
	 * @throws Exception	取得処理に失敗した場合
	 */
	public static void takeArm(long armGroup, long keep) throws Exception {
		int result = jni.CaoRobot_Execute_TakeArm(armGroup, keep);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_TakeArm(0, 0) failed.", result));
	
		// アーム制御権取得フラグをtrueに設定
		hasArm = true;
	}

	/**
	 * アーム制御権の解放するクラスメソッド
	 * 
	 * @throws Exception	解放処理に失敗した場合
	 */
	public static void giveArm() throws Exception {
		int result = jni.CaoRobot_Execute_GiveArm();
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_GiveArm() failed.", result));

		// アーム制御権取得フラグをfalseに設定
		hasArm = false;
	}
	
	/**
	 * モータをONにするクラスメソッド
	 * 
	 * @throws Exception	設定に失敗した場合
	 */
	public static void turnOnMotor() throws Exception {
		int result = jni.CaoRobot_Execute_Motor(1L, 0L);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_Motor(1, 0) failed.", result));
		
		// モータのON/OFFフラグをtrueに設定
		isTurnedOn = true;
	}
	
	/**
	 * モータをOFFにするクラスメソッド
	 * 
	 * @throws Exception	設定に失敗した場合
	 */
	public static void turnOffMotor() throws Exception {
		int result = jni.CaoRobot_Execute_Motor(0L, 0L);
		if (result == E_FAIL)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_Motor(0, 0) failed.", result));
	}
	
	
	/**
	 * ロボットを停止させるクラスメソッド
	 * 
	 * @throws Exception	停止処理に失敗した場合
	 */
	public static void halt() throws Exception {
		int result = jni.CaoRobot_Halt("");
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Halt() failed.", result));
	}

	/**
	 * Moveコマンドを実行するクラスメソッド
	 * 
	 * @param complement	補間指定（1L: PTP, 2L: CP_Linear, 3L: CP_Circle, 4L: Spline）
 	 * @param pose			ポーズ列（文字列で指定）
 	 * @param option		動作オプション（文字列で指定）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void move(long complement, String pose, String option) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(1L <= complement && complement <= 3L))
			throw new IllegalArgumentException("Invalid complement specified.");
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (pose == null)
			throw new IllegalArgumentException("Argument 'pose' is null.");
		if (option == null)
			throw new IllegalArgumentException("Argument 'option' is null.");
		
		// Moveコマンドを実行
		int result = jni.CaoRobot_Move(complement, pose, option);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Move() failed.", result));
	}
	
	/**
	 * Approachコマンドを実行するクラスメソッド
	 * 
	 * @param complement	補間方法（1:PTP，2:CP）
	 * @param poseBase		基準位置（C0書式）
	 * @param poseLength	アプローチ長（C2書式）
	 * @param option		動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void approach(long complement, String poseBase, String poseLength, String option) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(1L <= complement && complement <= 2L))
			throw new IllegalArgumentException("Invalid complement specified.");
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (poseBase == null)
			throw new IllegalArgumentException("Argument 'poseBase' is null.");
		if (poseLength == null)
			throw new IllegalArgumentException("Argument 'poseLength' is null.");
		if (option == null)
			throw new IllegalArgumentException("Argument 'option' is null.");

		// Approachコマンドを実行
		int result = jni.CaoRobot_Execute_Approach(complement, poseBase, poseLength, option);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_Approach() failed.", result));
	}
	
	/**
	 * Departコマンドを実行するクラスメソッド
	 * 
	 * @param complement	補間方法（1:PTP，2:CP）
	 * @param poseLength	デパート長（C2書式）
	 * @param option		動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void depart(long complement, String poseLength, String option) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(1L <= complement && complement <= 2L))
			throw new IllegalArgumentException("Invalid complement specified.");
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (poseLength == null)
			throw new IllegalArgumentException("Argument 'poseLength' is null.");
		if (option == null)
			throw new IllegalArgumentException("Argument 'option' is null.");
		
		// Departコマンドを実行
		int result = jni.CaoRobot_Execute_Depart(complement, poseLength, option);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_Depart() failed.", result));
	}
	
	/**
	 * Drawコマンドを実行するクラスメソッド
	 * 
	 * @param complement	補間方法（1:PTP，2:CP）
	 * @param pose			移動量（C1書式）
	 * @param option		動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void draw(long complement, String pose, String option) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(1L <= complement && complement <= 2L))
			throw new IllegalArgumentException("Invalid complement specified.");
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (pose == null)
			throw new IllegalArgumentException("Argument 'pose' is null.");
		if (option == null)
			throw new IllegalArgumentException("Argument 'option' is null.");
				
		// Drawコマンドを実行
		int result = jni.CaoRobot_Execute_Draw(complement, pose, option);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_Draw() failed.", result));
	}
	
	/**
	 * DriveAExコマンドを実行するクラスメソッド
	 * 
	 * @param poses		軸番号と移動量（C3書式）
	 * @param option	動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void driveAEx(String poses, String option) throws Exception {
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (poses == null)
			throw new IllegalArgumentException("Argument 'poses' is null.");
		if (option == null)
			throw new IllegalArgumentException("Argument 'option' is null.");
		
		// DriveAExコマンドを実行
		int result = jni.CaoRobot_Execute_DriveAEx(poses, option);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_DriveAEx() failed.", result));
	}
	
	/**
	 * DriveExコマンドを実行するクラスメソッド
	 * 
	 * @param poses		軸番号と移動量（C3書式）
	 * @param option	動作オプション（[SPEED=n][,ACCEL=n][,DECEL=n][,NEXT]）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void driveEx(String poses, String option) throws Exception {
		// 引数にNULLが指定されている場合はIllegalArgumentExceptionを発生
		if (poses == null)
			throw new IllegalArgumentException("Argument 'poses' is null.");
		if (option == null)
			throw new IllegalArgumentException("Argument 'option' is null.");
		
		// DriveExコマンドを実行
		int result = jni.CaoRobot_Execute_DriveEx(poses, option);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Execute_DriveEx() failed.", result));
	}
	
	/**
	 * ロボットの内部移動速度を設定するクラスメソッド
	 * 
	 * @param axis	軸番号（-1: 手先速度(SPEED), 0: 全軸速度(JSPEED)）
 	 * @param speed	速度（0.0f-100.0f）
	 * @throws IllegalArgumentException	引数で指定された値が不適切な場合
	 * @throws Exception				コマンド実行に失敗した場合
	 */
	public static void speed(long axis, float speed) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(-1L <= axis && axis <= 0L))
			throw new IllegalArgumentException("Invalid axis specified.");
		if (!(0.0f <= speed && speed <= 100.0f))
			throw new IllegalArgumentException("Invalid speed specified.");
		
		// Speedコマンドを実行
		int result = jni.CaoRobot_Speed(axis, speed);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Speed() failed.", result));
	}

	/**
	 * ロボットのツール座標系を変更するクラスメソッド
	 * 
	 * @param number	変更する番号
	 * @throws Exception	設定に失敗した場合
	 */
	public static void changeTool(long number) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(0L <= number))
			throw new IllegalArgumentException("Invalid number specified.");
		
		int result = jni.CaoRobot_Change("TOOL" + number);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Change() failed.", result));
	}
	
	/**
	 * ロボットのユーザ座標系を変更するクラスメソッド
	 * 
	 * @param number	変更する番号
	 * @throws Exception	設定に失敗した場合
	 */
	public static void changeWork(long number) throws Exception {
		// 引数の値が不適切な場合はIllegalArgumentExceptionを発生
		if (!(0L <= number))
			throw new IllegalArgumentException("Invalid number specified.");
		
		int result = jni.CaoRobot_Change("WORK" + number);
		if (result < S_OK)
			throw new Exception(createErrrorMessage("CaoRobot_Change() failed.", result));
	}

	/**
	 * エラーコード付メッセージを生成するクラスメソッド
	 * 
	 * @param message	エラーメッセージ
	 * @param code		エラーコード
	 * @return コード付エラーメッセージ（文字列）
	 */
	private static String createErrrorMessage(String message, int code) {
		return String.format("%s (Error code = %#x)", message, code);
	}
}
