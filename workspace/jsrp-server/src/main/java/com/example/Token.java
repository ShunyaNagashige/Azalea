package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Token {
	private String lang;
	private ArrayList<String> tokens;

	public Token(String lang, ArrayList<String> tokens) throws Exception {
		if (lang == null || lang == "") {
			throw new Exception("langが指定されていません。");
		}
		if (tokens == null || tokens.size() == 0) {
			throw new Exception("tokensに何も代入されていません。");
		}

		this.lang = lang;
		this.tokens = tokens;
	}

	public ArrayList<ArrayList<String>> toElementList() throws Exception {
		Map<String, Map<String, String>> wordList = new HashMap<>();

		Map<String, String> jaWord = new HashMap<>();
		wordList.put("ja", jaWord);
		jaWord.put("接続", "connect");
		jaWord.put("移動", "run");
		jaWord.put("移す", "run");
		jaWord.put("切断", "disconnect");
		jaWord.put("切る", "disconnect");
		jaWord.put("一", "1");
		jaWord.put("二", "2");
		jaWord.put("三", "3");
		jaWord.put("四", "4");
		jaWord.put("五", "5");
		jaWord.put("六", "6");

		Map<String, String> zhCNWord = new HashMap<>();
		wordList.put("zh-CN", zhCNWord);
		zhCNWord.put("连接", "connect");
		zhCNWord.put("移到", "run");
		zhCNWord.put("断开", "disconnect");
		zhCNWord.put("一", "1");
		zhCNWord.put("二", "2");
		zhCNWord.put("三", "3");
		zhCNWord.put("四", "4");
		zhCNWord.put("五", "5");
		zhCNWord.put("六", "6");

		Map<String, String> enWord = new HashMap<>();
		wordList.put("en", enWord);
		enWord.put("connect", "connect");
		enWord.put("move", "run");
		enWord.put("disconnect", "disconnect");
		enWord.put("first", "1");
		enWord.put("second", "2");
		enWord.put("third", "3");
		enWord.put("fourth", "4");
		enWord.put("fifth", "5");
		enWord.put("sixth", "6");

		ArrayList<ArrayList<String>> cmdElementList = new ArrayList<ArrayList<String>>();

		try {
			int i = 0;
			boolean isLoop = false;
			
			while (tokens.size() > i) {
				ArrayList<String> buf = new ArrayList<String>();
				String token = tokens.get(i);

				System.out.println("Top: " + token);

				isLoop = false;

				// tokenがアラビア数字の場合
				if (Character.isDigit(token.charAt(0))) {
					// tokenが全角文字の場合は，半角に直す
					if (String.valueOf(token).getBytes().length >= 2) {
						int tmp = Integer.parseInt(token);
						tokens.set(i, String.valueOf(tmp));
					}
					isLoop = true;
				}

				// tokenがアラビア数字でない場合
				if (!isLoop) {
					for (Map.Entry<String, String> set : wordList.get(lang).entrySet()) {
						if (token.equals(set.getKey().toString())) {
							if (Character.isDigit(set.getValue().charAt(0))) {
								isLoop = true;
								break;
							}

							if (set.getValue() == "run") {
								isLoop = true;
								break;
							}

							ArrayList<String> cmdElement = new ArrayList<String>();
							cmdElement.add(set.getValue());
							cmdElementList.add(cmdElement);
							break;

						}
					}
				}

				if (!isLoop) {
					isLoop = false;
					i++;
					continue;
				}

				int runIndex=-1;
				int elementCount=0;
				
				// runの処理
				while (isLoop) {					
					System.out.println("isLoop: " + tokens.get(i));

					final String[] punctuations = { ".", "。" };

					// 句点が来たら終わり。
					for (int j = 0; j < punctuations.length; j++) {
						if (tokens.get(i).equals(punctuations[j])) {
							i++;
							elementCount++;
							isLoop = false;
							break;
						}
					}

					if (!isLoop) {
						continue;
					}

					// tokenがアラビア数字の場合
					if (Character.isDigit(tokens.get(i).charAt(0))) {
						// tokenが全角文字の場合は，半角に直す
						if (String.valueOf(tokens.get(i)).getBytes().length >= 2) {
							int tmp = Integer.parseInt(tokens.get(i));
							tokens.set(i, String.valueOf(tmp));
						}
						buf.add(tokens.get(i));
						i++;
						elementCount++;
						continue;
					}

					for (Map.Entry<String, String> set : wordList.get(lang).entrySet()) {
						if (tokens.get(i).equals(set.getKey().toString())) {
							if (Character.isDigit(set.getValue().charAt(0))) {
								buf.add(set.getValue());
								break;
							}

							if (set.getValue() == "run") {
								buf.add(set.getValue());
								runIndex=elementCount;
								break;
							}

							break;
						}
					}
					i++;
				}

				if (buf.size() < 2) {
					throw new Exception("ホール番号を指定してください。");
				}
				
				if(runIndex==-1) {
					throw new Exception("ホール番号は指定されていますが，移動させる旨の記述がありません。");
				}
				
				ArrayList<String> cmdElement = new ArrayList<String>();

				cmdElement.add(buf.remove(runIndex));
				
				for (int j = 0; j < buf.size(); j++) {
					cmdElement.add(buf.get(j));
				}

				cmdElementList.add(cmdElement);

			}
		} catch (IndexOutOfBoundsException e) {
//			e.printStackTrace();
		}

		return cmdElementList;
	}
}
