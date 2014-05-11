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


public class JsonKmeans {
	
	String outputFile;
	Map<String,Number> jsonMap;
	Gson gson;
	
	public JsonKmeans(String outputFile) {
		this.outputFile = outputFile;
		jsonMap = new LinkedHashMap<>();
		gson = new Gson();
	}
	
	public String getJson(){
		try {
			int counter;
			double centroid;
			URL tomcat = new URL("http://localhost:8080/Twitter-Project/"+outputFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(tomcat.openStream()));
			String line;
			String key;
			while ((line = reader.readLine()) != null) {
				key = line.split("\\t")[0];
				centroid = Double.parseDouble(key);
				centroid = Math.round( centroid * 100.0 ) / 100.0;
				key = String.valueOf(centroid);
				if (!jsonMap.containsKey(key)) {
					jsonMap.put(key, 1);
				}
				else {
					counter = (Integer)jsonMap.get(key);
					counter++;
					jsonMap.put(key, counter);
				}
			}
			return gson.toJson(jsonMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getJsonFast(){
		try {
			int counter = 0;
			URL tomcat = new URL("http://localhost:8080/Twitter-Project/Data/computedCentroids.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(tomcat.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				jsonMap.put(line.split("\\t")[0], Integer.parseInt(line.split("\\t")[1]));
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
}
