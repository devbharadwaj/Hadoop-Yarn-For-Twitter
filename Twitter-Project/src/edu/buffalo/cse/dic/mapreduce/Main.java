package edu.buffalo.cse.dic.mapreduce;

import java.util.Map;

import com.google.gson.Gson;

public class Main {

	/**
	 * Initiate the MapReduce Job from here. Not part of the Servlet framework
	 */
	public static void main(String[] args) {
		Gson gson = new Gson();
		String inputFile = "HadoopDataFinal.txt";
		Map<String,Number> topTen;
		
		WordCount topWords = new WordCount();
		topTen = topWords.start(inputFile);
		System.out.println(gson.toJson(topTen));

		HashtagCount hashCount = new HashtagCount();
		topTen = hashCount.start(inputFile);
		System.out.println(gson.toJson(topTen));
		
		ReplyToCount replyCount = new ReplyToCount();
		topTen = replyCount.start(inputFile);
		System.out.println(gson.toJson(topTen));

		TrendCount trendCount = new TrendCount();
		topTen = trendCount.start(inputFile);
		System.out.println(gson.toJson(topTen));

		CoHashtagCount coHashCount = new CoHashtagCount();
		topTen = coHashCount.start(inputFile);
		System.out.println(gson.toJson(topTen));

		Kmeans kmeans = new Kmeans();
		kmeans.start(inputFile);

		PairOccurrance pairs = new PairOccurrance();
		topTen = pairs.start(inputFile);
		System.out.println(gson.toJson(topTen));

		StripeOccurrance stripe = new StripeOccurrance();
		topTen = stripe.start(inputFile);
		System.out.println(gson.toJson(topTen));
		
		inputFile = "input-graph-large";
		
		ShortestPath graph = new ShortestPath();
		graph.start(inputFile);
	}

}
