package edu.buffalo.cse.dic.mapreduce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class RelativeFrequency {
	
	public static class FrequencyMapper 
			extends Mapper<Object,Text,Text,Text>{

		Text word = new Text();
		Text coWordAndCount = new Text();
		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String wordPlusCount = value.toString().split("\\t")[0].split(" ")[1]+" "+value.toString().split("\\t")[1]; 
			word.set(value.toString().split("\\t")[0].split(" ")[0]);
			coWordAndCount.set(wordPlusCount);
			context.write(word,coWordAndCount);
		}

	}
	
	public static class FrequencyReducer
			extends Reducer<Text,Text,Text,Text> {

		double sum = 0;
		double rfreq;
		HashMap<String,String> cache = new HashMap<>();
		@Override
		protected void reduce(Text key, Iterable<Text> value, Context context)
				throws IOException, InterruptedException {
			// Cache iterator as it will be used twice
				String val;
				String coWord;
				String count; 
				while (value.iterator().hasNext()) {
					val = value.iterator().next().toString();
					coWord = val.split("\\t")[0].split(" ")[0];
					count = val.split("\\t")[0].split(" ")[1];
					cache.put(coWord, count);
					sum += Double.parseDouble(val.split("\\t")[0].split(" ")[1]);
				}
				for (String cword : cache.keySet()) {
					rfreq = Double.parseDouble(cache.get(cword))/sum;
					rfreq = Math.round( rfreq * 100.0 ) / 100.0;
					context.write(new Text(key.toString()+" "+cword.toString()), new Text(new DoubleWritable(rfreq).toString()));
				}
				
				sum = 0;
				rfreq = 0;
				cache.clear();
				}
		    }
}