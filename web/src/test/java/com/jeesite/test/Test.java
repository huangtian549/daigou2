package com.jeesite.test;

public class Test {

	public static void main(String[] args) {
		String fileUrl = "http://ppndeld0s.bkt.clouddn.com/1120534887630880770?e=1555994933&token=5we4SuM8VVUz1nesGzySNPQYnwVEsMnoBGNOELXE:syTUnMQz2Cb48GvHlmW9iaGXlJ0=";
		
		String qiniuId = fileUrl.split("\\?")[0].replace("http://ppndeld0s.bkt.clouddn.com/", "");
		System.out.println(qiniuId);
	}
	
}
