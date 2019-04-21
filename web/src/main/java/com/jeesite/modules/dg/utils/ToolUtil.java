package com.jeesite.modules.dg.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

public class ToolUtil {

	public static  String getNum19(){  
		 String numStr = "" ;       
		 String trandStr = String.valueOf((Math.random() * 9 + 1) * 1000000);     
		 String dataStr = new SimpleDateFormat("yyyyMMddHHMMSS").format(new Date());       
		 numStr = trandStr.toString().substring(0, 4);      
		 numStr = numStr + dataStr ;       
		 return numStr ;   
		}
	
	public static void main(String[] args) {
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < 10000; i++) {
			String string = ToolUtil.getNum19();
			if (set.contains(string)) {
				System.out.println(string);
				
			}else {
				set.add(string);
			}
		}
		System.out.println(set.size());
	}


}
