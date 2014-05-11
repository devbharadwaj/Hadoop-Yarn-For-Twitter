package edu.buffalo.cse.dic.mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;

import edu.buffalo.cse.dic.mapreduce.RelativeFrequency.FrequencyMapper;
import edu.buffalo.cse.dic.mapreduce.RelativeFrequency.FrequencyReducer;
import edu.buffalo.cse.dic.mapreduce.SortDouble.DoubleMapper;
import edu.buffalo.cse.dic.mapreduce.SortDouble.DoubleReducer;
import edu.buffalo.cse.dic.mapreduce.SortDouble.DoubleReverseComparator;
import edu.buffalo.cse.dic.mapreduce.SortReducerOutput.OutputBreaker;
import edu.buffalo.cse.dic.mapreduce.SortReducerOutput.ReverseComparator;
import edu.buffalo.cse.dic.mapreduce.SortReducerOutput.SortByCount;

public class StripeOccurrance implements MapReduceJob {
	  
	public static class StripeMapper 
    	extends Mapper<Object, Text, Text, MapWritable>{
 
		  private final static IntWritable one = new IntWritable(1);
		  private Text word = new Text();
		  private MapWritable assocArray= new MapWritable();
		  private HashMap<String, Integer> associativeArray = new HashMap<>();
		  
		  public void map(Object key, Text value, Context context
				  ) throws IOException, InterruptedException {
		 ArrayList<String> stopwords = new ArrayList<>(Arrays.asList("RT","a","an","and","are","as","at","be","but","by","for","if","in","into","is","it","no","not","of","on","or","such","that","the","their","then","there","these","they","this","to","was","will","with","I","you","-","&amp;","The","your","me"));
	     String tweetText = value.toString().split("\\|")[2];
	     List<String> words = Arrays.asList(tweetText.split("\\s+"));
	     int numOfWords = words.size();
	     int increment;
	     Collections.sort(words);
	     for (int i = 0; i < numOfWords-1; i++) {
	    	for (int j = i+1; j <= numOfWords-1; j++) {
	    		if (!stopwords.contains(words.get(i).toString()) && !stopwords.contains(words.get(j).toString())) {
	    			if (!words.get(i).toString().matches("http.*") && !words.get(j).toString().matches("http.*")) {
	    	    		// If the same coword occurs more once
			    		if (associativeArray.containsKey(words.get(j))) {
			    			increment = associativeArray.get(words.get(j));
			    			increment++;
			    			associativeArray.put(words.get(j), increment);
			    		} 
			    		// First time coword found
			    		else {
			    			word.set(words.get(i));
			    			associativeArray.put(words.get(j),1);
			    		}
	    			}
	    		}
	    	}
	    	// Dump to MapWritable Array
	    	for (String coword : associativeArray.keySet()){
	    		assocArray.put(new Text(coword), new IntWritable(associativeArray.get(coword)));
	    	}
	    	context.write(word,assocArray);
	    	assocArray.clear();
	    	associativeArray.clear();
	    }
	  }
	}

	  public static class StripeSumReducer 
	  		extends Reducer<Text,MapWritable,Text,Text> {
	   private IntWritable result = new IntWritable();
	   
	   public void reduce(Text key, Iterable<MapWritable> values, 
	                      Context context) throws IOException, InterruptedException {
	     int increment;
	     HashMap<String,Integer> cache = new HashMap<>();
	     for (MapWritable map : values) {
	    	 for (Writable coword : map.keySet()) {
	    		 if (cache.containsKey(coword.toString())) {
	    			 increment = Integer.parseInt(map.get(coword).toString());
	    			 increment += cache.get(coword.toString());
	    			 cache.put(coword.toString(), increment);
	    		 }
	    		 else {
	    			 cache.put(coword.toString(), Integer.parseInt(map.get(coword).toString()));
	    		 }
	    	 }
	     }
	     //Dump Hashmap out
	     for (String allcowords : cache.keySet()) {
	    	 context.write(new Text(key+" "+allcowords), new Text(cache.get(allcowords).toString()));
	     }
	   }
	
	 }
	@Override  
	public Map<String, Number> start(String inputFile) {
		  try{
			  	LinkedHashMap<String,Number> topTen = new LinkedHashMap<>();
			    Configuration conf = new Configuration();
			    conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/core-site.xml"));
			    conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/hdfs-site.xml"));

			    FileSystem  fs = FileSystem.get(new URI("stripecount"), conf);
			    fs.delete(new Path("stripecount"));
			    
			    Job job = new Job(conf, "pair count");
			    job.setJarByClass(StripeOccurrance.class);
			    job.setMapperClass(StripeMapper.class);
			    job.setReducerClass(StripeSumReducer.class);
			    job.setMapOutputKeyClass(Text.class);
			    job.setMapOutputValueClass(MapWritable.class);
			    job.setOutputKeyClass(Text.class);
			    job.setOutputValueClass(Text.class);
			    FileInputFormat.addInputPath(job, new Path(inputFile));
			    FileOutputFormat.setOutputPath(job, new Path("stripecount"));
			    job.waitForCompletion(true);
			    System.out.println("stripe count done");
			    
			    fs.delete(new Path("rfreqstripe"), true);
			    
			    Job freqJob = new Job(conf, "stripe count");
			    freqJob.setJarByClass(RelativeFrequency.class);
			    freqJob.setMapperClass(FrequencyMapper.class);
			    freqJob.setReducerClass(FrequencyReducer.class);
			    freqJob.setOutputKeyClass(Text.class);
			    freqJob.setOutputValueClass(Text.class);
			    FileInputFormat.addInputPath(freqJob, new Path("stripecount"));
			    FileOutputFormat.setOutputPath(freqJob, new Path("rfreqstripe"));
			    freqJob.waitForCompletion(true);
			    System.out.println("relative frequency done");

			    
			    FileSystem  fsa = FileSystem.get(new URI("stripecount"), conf);
			    fsa.delete(new Path("stripecountfinal"));
			    
			    Job sortJob = new Job(conf, "sort reducer");
			    sortJob.setJarByClass(SortDouble.class);
			    sortJob.setMapperClass(DoubleMapper.class);
			    sortJob.setSortComparatorClass(DoubleReverseComparator.class);
			    sortJob.setReducerClass(DoubleReducer.class);
			    sortJob.setOutputKeyClass(DoubleWritable.class);
			    sortJob.setOutputValueClass(Text.class);
			    sortJob.setPartitionerClass(TotalOrderPartitioner.class);
			    Path partitionFile = new Path("rfreqstripe", "_sortPartitioning");
			    TotalOrderPartitioner.setPartitionFile(sortJob.getConfiguration(), partitionFile);
			    FileInputFormat.addInputPath(sortJob, new Path("rfreqstripe/part-r-00000"));
			    FileOutputFormat.setOutputPath(sortJob, new Path("stripecountfinal"));
			    sortJob.waitForCompletion(true);
			    sortJob.waitForCompletion(true);
			    System.out.println("sort stripe count");
			    
			    Path output = new Path("stripecountfinal/part-r-00000");
			    FileSystem fileSystem = FileSystem.get(output.toUri(), conf);
			    FileStatus[] items = fileSystem.listStatus(output);
			    for(FileStatus item: items) {
			    	InputStream stream = null;
			        // ignoring files like _SUCCESS
			        if(item.getPath().getName().startsWith("_")) {
			          continue;
			        }
			        else {
			            stream = fileSystem.open(item.getPath());
			        }
			        Scanner scan = new Scanner(stream).useDelimiter("\\n");
			        for (int i = 0; i < 10; i++) {
			        	if (scan.hasNext()) {
			        		String data = scan.next();
			        		topTen.put(data.split("\\t")[1], Double.parseDouble(data.split("\\t")[0]));
			        	}
			        }
			    }
			    return topTen;
			  } catch (IOException e) {
				  e.printStackTrace();
			  } catch (ClassNotFoundException e) {
				e.printStackTrace();
			  } catch (InterruptedException e) {
				e.printStackTrace();
			  } catch (URISyntaxException e) {
				e.printStackTrace();
			  }
		return null;  
	}

}


