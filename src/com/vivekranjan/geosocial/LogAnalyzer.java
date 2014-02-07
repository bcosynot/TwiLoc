package com.vivekranjan.geosocial;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LogAnalyzer {
	
	public static void main(String args[]) {
		String path = "tweets.log";
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			
			while(null!=(line=br.readLine())) {
				int i=0;
				while((i%3!=0 || i==0) && null!=(line=br.readLine())) {
					System.out.println(i+":"+line);
					i++;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
