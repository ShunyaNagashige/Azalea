package com.example;

import java.util.ArrayList;

public class CommandElement {
	private ArrayList<ArrayList<String>> cmdElementList;
	
	public CommandElement(ArrayList<ArrayList<String>> cmdElementList) throws Exception{
		if(cmdElementList==null||cmdElementList.size()==0) {
			throw new Exception("cmdElementListに何も代入されていません。");
		}
		
		this.cmdElementList=cmdElementList;
	}
	
	public ArrayList<String> generateCmd(){
		ArrayList<String> cmdList = new ArrayList<String>();

		for (int i = 0; i < cmdElementList.size(); i++) {
			String cmd = "";

			for (int j = 0; j < cmdElementList.get(i).size() - 1; j++) {
				cmd = cmd + cmdElementList.get(i).get(j) + " ";
			}

			cmd += cmdElementList.get(i).get(cmdElementList.get(i).size() - 1);

			cmdList.add(cmd);
		}

		return cmdList;
	}
}
