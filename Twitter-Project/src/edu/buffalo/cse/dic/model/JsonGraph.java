package edu.buffalo.cse.dic.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;


public class JsonGraph {
	
	String outputFile;
	Map<String,List<String>> jsonMap;
	Gson gson;
	
	public JsonGraph(String outputFile) {
		this.outputFile = outputFile;
		jsonMap = new LinkedHashMap<>();
		gson = new Gson();
	}
	
	public String getJson(){
		try {
			URL tomcat = new URL("http://localhost:8080/Twitter-Project/"+outputFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(tomcat.openStream()));
			String line;
			ArrayList<String> nodes;
			while ((line = reader.readLine()) != null) {
				nodes = new ArrayList<>(Arrays.asList(line.split("\\t")[2].split(":")));
				jsonMap.put(line.split("\\t")[0]+" "+line.split("\\t")[1], nodes);
			}
			return gson.toJson(jsonMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
/*
	public String getDoubleJson(){
		try {
			int counter = 0;
			URL tomcat = new URL("http://localhost:8080/Twitter-Project/"+outputFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(tomcat.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				jsonMap.put(line.split("\\t")[1], Double.parseDouble(line.split("\\t")[0]));
				counter++;
				if (counter == 10) break;
			}
			return gson.toJson(jsonMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}*/
}
