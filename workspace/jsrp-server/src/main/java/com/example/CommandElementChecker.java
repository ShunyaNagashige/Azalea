package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommandElementChecker {
	private ArrayList<ArrayList<String>> cmdElementList;
	private String preOperation;

	final boolean[][] properOrder = { { false, true, true }, { false, true, true }, { true, false, false } };
	final Map<String, Integer> cmdIdNum = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			int i = 0;
			put("connect", i++);
			put("run", i++);
			put("disconnect", i++);
		}
	};

	public CommandElementChecker(ArrayList<ArrayList<String>> cmdElementList, String preOperation) throws Exception {
		if (cmdElementList == null) {
			throw new IllegalArgumentException("cmdElementListがnullです。");
		}
		if (preOperation == null) {
			throw new IllegalArgumentException("preOperationがnullです。");
		}

		this.cmdElementList = cmdElementList;
		this.preOperation = preOperation;
	}

	public void checkCommandElement() throws Exception {
		ArrayList<String> firstCmdElement = cmdElementList.get(0);
		if (!properOrder[cmdIdNum.get(preOperation)][cmdIdNum.get(firstCmdElement.get(0))]) {
			throw new Exception("直前に行われた操作が" + preOperation + "である場合に，" + firstCmdElement.get(0) + "を行うことはできません。");
		}

		for (int i = 0; i < cmdElementList.size() - 1; i++) {
			int j = cmdIdNum.get(cmdElementList.get(i).get(0));
			int k = cmdIdNum.get(cmdElementList.get(i + 1).get(0));
			if (!properOrder[j][k]) {
				throw new Exception("命令の順序が不適切です。");
			}

			if (cmdElementList.get(i).get(0) == "run" && cmdElementList.get(i).size() < 2) {
				throw new Exception("ボールを移動させる場合は，ホール番号を指定してください。");
			}
		}

		if (cmdElementList.get(cmdElementList.size() - 1).get(0) == "run"
				&& cmdElementList.get(cmdElementList.size() - 1).size() < 2) {
			throw new Exception("ボールを移動させる場合は，ホール番号を指定してください。");
		}
	}
}
