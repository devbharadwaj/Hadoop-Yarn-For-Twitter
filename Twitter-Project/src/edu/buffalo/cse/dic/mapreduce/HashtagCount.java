package edu.buffalo.cse.dic.mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

public class HashtagCount implements MapReduceJob {
	
	  public static class HashtagMapper 
      	extends Mapper<Object, Text, Text, IntWritable>{
   
		  private final static IntWritable one = new IntWritable(1);
		  private Text word = new Text();
     
		  public void map(Object key, Text value, Context context
				  ) throws IOException, InterruptedException {
	     String tweetText = value.toString().split("\\|")[3];
	     if (!(tweetText.equals(" "))) {
	    	 StringTokenizer itr = new StringTokenizer(tweetText);
	    	 while (itr.hasMoreTokens()) {
	    		 word.set("#"+itr.nextToken());
	    		 context.write(word, one);
	    	 }
	     }
	   }
	 }
	 
	  public static class HashtagSumReducer 
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
	public Map<String, Number> start(String inputFile) {
		  try{
			  	LinkedHashMap<String,Number> topTen = new LinkedHashMap<>();
			    Configuration conf = new Configuration();
			    conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/core-site.xml"));
			    conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/hdfs-site.xml"));

			    FileSystem  fs = FileSystem.get(new URI("hashtagcount"), conf);
			    fs.delete(new Path("hashtagcount"));
			    
			    Job job = new Job(conf, "word count");
			    job.setJarByClass(HashtagCount.class);
			    job.setMapperClass(HashtagMapper.class);
			    job.setCombinerClass(HashtagSumReducer.class);
			    job.setReducerClass(HashtagSumReducer.class);
			    job.setOutputKeyClass(Text.class);
			    job.setOutputValueClass(IntWritable.class);
			    FileInputFormat.addInputPath(job, new Path(inputFile));
			    FileOutputFormat.setOutputPath(job, new Path("hashtagcount"));
			    job.waitForCompletion(true);
			    System.out.println("hashtag count done");
			    
			    FileSystem  fsa = FileSystem.get(new URI("hashtagfinal"), conf);
			    fsa.delete(new Path("hashtagfinal"));
			    
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
			    FileInputFormat.addInputPath(sortJob, new Path("hashtagcount/part-r-00000"));
			    FileOutputFormat.setOutputPath(sortJob, new Path("hashtagfinal"));
			    sortJob.waitForCompletion(true);
			    System.out.println("sort hashtag count");
			    
			    Path output = new Path("hashtagfinal/part-r-00000");
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
