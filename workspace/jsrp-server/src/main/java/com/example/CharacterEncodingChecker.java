package com.example;

import java.io.UnsupportedEncodingException;

public class CharacterEncodingChecker {
	public static void checkCharacterEncoding(String ctoken,String str) throws Exception{
		if (str.equals(characterCode(ctoken, "SJIS"))) {
		      System.out.println("ctokenはShift-JISです");
		    } else if (str.equals(characterCode(ctoken, "UTF-16"))) {
		      System.out.println("ctokenはUTF-16です");
		    } else if (str.equals(characterCode(ctoken, "UTF-8"))) {
		      System.out.println("ctokenはUTF-8です");
		    } else {
		      System.out.println("ctokenの文字コードは不明です");
		    }
		if (str.equals(characterCode(str, "SJIS"))) {
		      System.out.println("strはShift-JISです");
		    } else if (str.equals(characterCode(str, "UTF-16"))) {
		      System.out.println("strはUTF-16です");
		    } else if (str.equals(characterCode(str, "UTF-8"))) {
		      System.out.println("strはUTF-8です");
		    } else {
		      System.out.println("strの文字コードは不明です");
		    }
	}
	
	private static String characterCode(String str, String chraCode) throws UnsupportedEncodingException {
	    byte[] tmp = new String(str).getBytes(chraCode);
	    return new String(tmp);
	  }
}
