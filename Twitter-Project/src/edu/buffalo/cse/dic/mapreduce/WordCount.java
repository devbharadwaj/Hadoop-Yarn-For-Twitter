/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.buffalo.cse.dic.mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;

import edu.buffalo.cse.dic.mapreduce.SortReducerOutput.OutputBreaker;
import edu.buffalo.cse.dic.mapreduce.SortReducerOutput.ReverseComparator;
import edu.buffalo.cse.dic.mapreduce.SortReducerOutput.SortByCount;


public class WordCount implements MapReduceJob{

  public static class TokenizerMapper 
       extends Mapper<Object, Text, Text, IntWritable>{
    
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      ArrayList<String> stopwords = new ArrayList<>(Arrays.asList("RT","a","an","and","are","as","at","be","but","by","for","if","in","into","is","it","no","not","of","on","or","such","that","the","their","then","there","these","they","this","to","was","will","with","I","you","-","&amp;","The","your","me"));
      String tweetText = value.toString().split("\\|")[2];
      StringTokenizer itr = new StringTokenizer(tweetText);
      String token;
      while (itr.hasMoreTokens()) {
  		  token = itr.nextToken();
    	  if (!stopwords.contains(token) && !stopwords.contains(token)) {
			if (!token.matches("http.*") && !token.matches("http.*")) {
		        word.set(token);
		        context.write(word, one);
			}
    	  }
      }
    }
  }
  
  public static class IntSumReducer 
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();
    
    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }

  }
  @Override
  public Map<String,Number> start(String inputFile){
	  try{
	  	LinkedHashMap<String,Number> topTen = new LinkedHashMap<>();
	    Configuration conf = new Configuration();
	    conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/core-site.xml"));
	    conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/hdfs-site.xml"));

	    FileSystem  fs = FileSystem.get(new URI("wordcount"), conf);
	    fs.delete(new Path("wordcount"));
	    
	    Job job = new Job(conf, "word count");
	    job.setJarByClass(WordCount.class);
	    job.setMapperClass(TokenizerMapper.class);
	    job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(inputFile));
	    FileOutputFormat.setOutputPath(job, new Path("wordcount"));
	    job.waitForCompletion(true);
	    System.out.println("word count done");
	    
	    FileSystem  fsa = FileSystem.get(new URI("wordcount"), conf);
	    fsa.delete(new Path("wordcountfinal"));
	    
	    Job sortJob = new Job(conf, "sort reducer");
	    sortJob.setJarByClass(SortReducerOutput.class);
	    sortJob.setMapperClass(OutputBreaker.class);
	    sortJob.setSortComparatorClass(ReverseComparator.class);
	    sortJob.setReducerClass(SortByCount.class);
	    sortJob.setOutputKeyClass(IntWritable.class);
	    sortJob.setOutputValueClass(Text.class);
	    sortJob.setPartitionerClass(TotalOrderPartitioner.class);
	    Path partitionFile = new Path("trendcount", "_sortPartitioning");
	    TotalOrderPartitioner.setPartitionFile(sortJob.getConfiguration(), partitionFile);
	    FileInputFormat.addInputPath(sortJob, new Path("wordcount/part-r-00000"));
	    FileOutputFormat.setOutputPath(sortJob, new Path("wordcountfinal"));
	    sortJob.waitForCompletion(true);
	    System.out.println("sort word count");
	    
	    Path output = new Path("wordcountfinal/part-r-00000");
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
	        		topTen.put(data.split("\\t")[1], Integer.parseInt(data.split("\\t")[0]));
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