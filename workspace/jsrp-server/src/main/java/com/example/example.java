package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.Gson;

public class Example {
	public static void main(String[] args) {
//        Tokenizer tokenizer = new Tokenizer() ;
//        List<Token> tokens = tokenizer.tokenize("１番のホールにあるボールを、２番、３番のホールへ順に移動させる。");
//        for (Token token : tokens) {
//            System.out.println(token.getSurface() + "\t" + token.getAllFeatures());
//        }
		try {
//			String regex  = " |,";
//			String str = "First, connect. move second to third, fifth, first holes.";
//
//			String[] result = str.split(regex,10);
//			
//			for(int i=0;i<result.length;i++) {
//				System.out.println(result[i]);
//			}
			
			Model cModel=new Model();
//			cModel.lang="ja";
//			cModel.message="ロボットに接続し，4番のホールにあるボールを、3番、2番のホールへ順に移動させる。";
			cModel.lang="en";
			cModel.message="First, connect to the robot. Then, move the ball in the second hole to the third, fifth, and first holes in order.";
//			cModel.message="First, connect. move second to third, fifth, first holes.";			
			Gson gson=new Gson();
			String json=gson.toJson(cModel);
			
			// jsonをModelオブジェクトに直す
			gson = new Gson();
			Model model = gson.fromJson(json, Model.class);

			// トークンの生成
			Analysis a = new Analysis(model.lang, model.message);
			ArrayList<String> tokens = a.analyze();

			// トークンの中からコマンドの構成要素を抽出する
			Token t = new Token(model.lang, tokens);
			ArrayList<ArrayList<String>> cmdElementList = t.toElementList();

			String preOperation="disconnect";
			
			// cmdElementListが適切かどうかチェック
			CommandElementChecker cec=new CommandElementChecker(cmdElementList,preOperation);
			cec.checkCommandElement();
			
			// コマンドを生成する
			CommandElement ce = new CommandElement(cmdElementList);
			ArrayList<String> cmdList = ce.generateCmd();
			
			System.out.println(cmdList);
			
//			StringTokenizer st = new StringTokenizer("run 4 3 2", " ");
//			
//			ArrayList<String> holes=new ArrayList<String>();
//			
//			System.out.println(st.nextToken());
//			int size=st.countTokens();
//			for(int i=0;i<size;i++) {
//				holes.add(st.nextToken());
//				System.out.println(st.countTokens());
//			}
//			
//			System.out.println(holes);
			
//			Analysis ma = new Analysis("ja", "接続。4,3,2,移動。");
//			MorphologicalAnalysis ma = new MorphologicalAnalysis("zh-CN", "第四洞的球依次移到第二洞和第三洞。");
//			ArrayList<String> cmd = ma.createCmd();
//			System.out.println(cmd);
			
//			Model model=new Model();
//			model.lang="ja";
//			model.message="4番のホールにあるボールを、3番、2番のホールへ順に移動させる。";
//			
//			Gson gson=new Gson();
//			String json=gson.toJson(model);
//			
//			System.out.println(json);
//			
////			String clientMessage=
////					"{\r\n"
////					+ "  \"lang\":\"ja\",\r\n"
////					+ "  \"message\":\"4番のホールにあるボールを、3番、2番のホールへ順に移動させる。\"\r\n"
////					+ "}";
//	        model = gson.fromJson(json, Model.class);
//
//	        System.out.println("lang:" + model.lang);
//	        System.out.println("message:" + model.message);
//			String[] tmp = clientMessage.split(" ");
//			if(tmp.length<2) {
//				System.out.println("不適切な長さ");
//			}
//			System.out.println(tmp[0]);
//			CNFactory factory = null;
//			// モデルファイルのパスを指定し、形態素解析器を呼び出す
//			try {
//			    factory = CNFactory.getInstance("./dic");
//			} catch (LoadModelException lme) {
//			    lme.printStackTrace();
//			}
//			String message = "今天天气真好啊！";
////			>> [今天, 天气, 真, 好, 啊, ！]
//			try {
//			    factory = CNFactory.getInstance("./dic");
//			} catch (LoadModelException lme) {
//			    lme.printStackTrace();
//			}
//			String[][] ctokens = factory.tag(message);
//			List<String> list =Arrays.asList(ctokens[0]);
//			ArrayList<String> newList=new ArrayList<>(list);
//			System.out.println(newList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}