package edu.buffalo.cse.dic.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import edu.buffalo.cse.dic.mapreduce.Kmeans.Kmapper;
import edu.buffalo.cse.dic.mapreduce.Kmeans.Kreducer;
import edu.buffalo.cse.dic.mapreduce.Kmeans.stop;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class ShortestPath implements MapReduceJob {

	
	public enum Dist{
		dist,
		pre_dist;
		private int value;
		public int getvalue(){
			return value;
		}
		public void setvalue(int val){
			value=val;
		}
	};
	public enum stop{
		SAME
		
	};
	public static class Kmapper
	extends Mapper<Object, Text, Text,Text>{
		
		public void map(Object key, Text value, Context context
				  ) throws IOException, InterruptedException {
			String []text = value.toString().split(" ");
			
			String a;
			//System.out.println(text[0]);
			//System.out.println(text[1]);
			int cost=Integer.parseInt(text[1]);
			
			context.write(new Text(text[0]),new Text(text[1]));
			
			String []node=text[2].split(":");
			for(int i=0;i<node.length;i++){
				context.write(new Text(node[i]),new Text(Integer.toString(cost+1)));
			}
			String []temp = value.toString().split(" ",2);
		//	System.out.println(temp[1]);
		//	System.out.println(text[0]);
			context.write(new Text(text[0]), new Text(temp[1]));
			
		}
	}
	
	public static class Kmapper_I
	extends Mapper<Object, Text, Text,Text>{
		
		public void map(Object key, Text value, Context context
				  ) throws IOException, InterruptedException {
			String []text = value.toString().split("\\s+");
		
			String a;
			int cost=Integer.parseInt(text[1]);
			
			context.write(new Text(text[0]),new Text(text[1]));
			
			String []node=text[2].split(":");
			for(int i=0;i<node.length;i++){
				context.write(new Text(node[i]),new Text(Integer.toString(cost+1)));
			}
			String []temp = value.toString().split("\\t",2);
		//	System.out.println(temp[1]);
			context.write(new Text(text[0]), new Text(temp[1]));
			
		}
	}
	public static class Kreducer 
	  extends Reducer<Text,Text,Text,Text> {
		public void reduce(Text key, Iterable<Text> values, 
                Context context) throws IOException, InterruptedException {
			
			int small_dist=1000000;
			String format = null;
			String []a = null;
			for (Text val : values){
				String temp=val.toString();
				if(temp.contains(" ") ){
					format=temp;
					a=format.split(" ");
					
				}
				else if (temp.contains("\t")){
					format=temp;
					a=format.split("\t");
				}
				else {
					if(small_dist>Integer.parseInt(temp)){
						small_dist=Integer.parseInt(temp);
						
					}
					
				}
			}

			
			
			
			a[0]=Integer.toString(small_dist);
			
			Dist.dist.setvalue(small_dist+Dist.dist.getvalue());
			System.out.println(key+","+small_dist);
			if(Dist.dist.getvalue()==Dist.pre_dist.getvalue()){
				System.out.println("in");
				System.out.println(Dist.pre_dist.getvalue()+","+Dist.dist.getvalue());
				context.getCounter(stop.SAME).increment(1);
			}
			
			format=a[0]+"\t"+a[1];
			
			context.write(key, new Text(format));
			
			
		}
	}
	

	@Override
	public Map<String, Number> start(String inputFile) {
		// TODO Auto-generated method stub
		try{
			LinkedHashMap<String,Integer> topTen = new LinkedHashMap<>();
			Configuration conf = new Configuration();
			conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/core-site.xml"));
			conf.addResource(new Path("/usr/local/hadoop/etc/hadoop/hdfs-site.xml"));
			FileSystem  fs = FileSystem.get(new URI("ShortestPath"), conf);	
			Dist.dist.setvalue(0);
			Dist.pre_dist.setvalue(0);
			Job job = new Job(conf, "ShortestPath");
			int iteration=0;
			for(int i=0;i<=2;i++){
				fs.delete(new Path("ShortestPath"+i));
			}
			
			job.setMapperClass(Kmapper.class);
			job.setReducerClass(Kreducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			FileInputFormat.addInputPath(job, new Path(inputFile));
			FileOutputFormat.setOutputPath(job, new Path("ShortestPath"+iteration));
			job.waitForCompletion(true);
			int pre_dist=Dist.dist.getvalue();
			boolean flag=true;
			fs.delete(new Path("ShortestPath"));
			
			iteration++;
			//System.out.println("out of loop!");
			do{
				
				
				
				Job job1 = new Job(conf, "ShortestPath");			
				Dist.pre_dist.setvalue(pre_dist);
				Dist.dist.setvalue(0);
				//System.out.println("loop!");
				job1.setMapperClass(Kmapper_I.class);
				job1.setReducerClass(Kreducer.class);
				job1.setOutputKeyClass(Text.class);
				job1.setOutputValueClass(Text.class);
				int temp=iteration-1;
				FileInputFormat.addInputPath(job1, new Path("ShortestPath"+temp+"/part-r-00000"));
				
				FileOutputFormat.setOutputPath(job1, new Path("ShortestPath"+iteration));
				job1.waitForCompletion(true);
				pre_dist=Dist.dist.getvalue();
				//System.out.println(job1.getCounters().findCounter(stop.SAME).getValue());
				if(job1.getCounters().findCounter(stop.SAME).getValue()==1){
					flag=false;
				}
				//fs.delete(new Path("ShortestPath"+iteration));
				iteration++;
			}while(flag);
			
			
			
			
			
			
			
			
		}catch (IOException e) {
			  e.printStackTrace();
		} catch (URISyntaxException e) {
			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		return null;
	}

}