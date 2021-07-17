package com.example;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

public class Analysis {
	private String lang;
	private String message;

	public Analysis(String lang, String message) throws Exception {
		if (message == null || message == "") {
			throw new IllegalArgumentException("messageに何も代入されていません。");
		}
		if (lang == null || lang == "") {
			throw new IllegalArgumentException("言語コードが指定されていません。");
		}

		this.lang = lang;
		this.message = message;
	}

	public ArrayList<String> analyze() throws Exception {
		ArrayList<String> tokens;

		// 形態素解析を行う
		switch (this.lang) {
		// 日本語
		case "ja":
			tokens = analyzeJA(message);
			break;
//		// 中国語（簡体字）
//		case "zh-CN":
//			tokens = analyzeZHCN(message);
//			break;
		// 英語
		case "en":
			tokens = analyzeSpacedWordingLang(message);
			break;
		default:
			throw new IllegalArgumentException("サポートされていない言語コード : " + lang);
		}

		return tokens;
	}

	private ArrayList<String> analyzeJA(String message) throws Exception {
		Tokenizer tokenizer = new Tokenizer();
		List<Token> aTokens = tokenizer.tokenize(message);
		ArrayList<String> tokens = new ArrayList<String>();

		for (Token kToken : aTokens) {
			tokens.add(kToken.getSurface());
		}

		return tokens;
	}

//	private ArrayList<String> analyzeZHCN(String message) throws Exception {
//		CNFactory factory = null;
//
//		// モデルファイルのパスを指定し、形態素解析器を呼び出す
//		factory = CNFactory.getInstance("./lib");
//		String[][] atokens = factory.tag(message);
//
//		// .asListは固定長のListを返すため，一度Listに代入してから，ArrayListに変換する
//		List<String> list = Arrays.asList(atokens[0]);
//		ArrayList<String> tokens = new ArrayList<>(list);
//
//		return tokens;
//	}

	private ArrayList<String> analyzeSpacedWordingLang(String message) throws Exception {
		// .の前にスペースを入れる
		message = message.replace(".", " .");

		StringTokenizer st = new StringTokenizer(message, " ,");
		ArrayList<String> tokens = new ArrayList<String>();

		try {
			while (true) {
				tokens.add(st.nextToken());
			}
		} catch (NoSuchElementException e) {
		}

		return tokens;
	}
}