package edu.buffalo.cse.dic.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;


public class JsonGenerator {
	
	String outputFile;
	Map<String,Number> jsonMap;
	Gson gson;
	
	public JsonGenerator(String outputFile) {
		this.outputFile = outputFile;
		jsonMap = new LinkedHashMap<>();
		gson = new Gson();
	}
	
	public String getJson(){
		try {
			int counter = 0;
			URL tomcat = new URL("http://localhost:8080/Twitter-Project/"+outputFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(tomcat.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				jsonMap.put(line.split("\\t")[1], Integer.parseInt(line.split("\\t")[0]));
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
	}

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
	}
	
	public String getShortestPath() {
		try {
			URL tomcat = new URL("http://localhost:8080/Twitter-Project/"+outputFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(tomcat.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				jsonMap.put(line.split("\\t")[0], Integer.parseInt(line.split("\\t")[1]));
			}
			return gson.toJson(jsonMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
