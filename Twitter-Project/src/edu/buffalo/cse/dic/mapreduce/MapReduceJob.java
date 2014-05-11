package edu.buffalo.cse.dic.mapreduce;

import java.util.Map;


public interface MapReduceJob {
	
	public Map<String, Number> start(String inputFile);
	
	
}
