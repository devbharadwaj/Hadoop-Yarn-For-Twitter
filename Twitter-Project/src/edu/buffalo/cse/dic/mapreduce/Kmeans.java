package edu.buffalo.cse.dic.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

//import edu.buffalo.cse.dic.mapreduce.WordCount.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class Kmeans implements MapReduceJob{

//private static Log log=LogFactory.getLog(Kmeans.class);
	public enum centroids{
		c1,
		c2,
		c3;
		private double value;
		public double getvalue(){
			return value;
		}
		public void setvalue(double val){
			value=val;
		}

	};
	public enum stop{
		CONVERGE
		
	};
  public static class Kmapper
    extends Mapper<Object, Text, Text, IntWritable>{
 
  
  
 // private Text word = new Text();
   
  public void map(Object key, Text value, Context context
  ) throws IOException, InterruptedException {
     String tweetText_followers = value.toString().split("\\|")[1];
     int number=Integer.parseInt(tweetText_followers);
     
//     String a1=context.getCounter(centroids.c1).getDisplayName();
//     String a2=context.getCounter(centroids.c2).getDisplayName();
//     String a3=context.getCounter(centroids.c3).getDisplayName();
     
     int index=0;
//     System.out.println(a1);
//     System.out.println(a2);
//     System.out.println(a3);
//     if(isDouble(a1)){
//    	 System.out.println("In");
//    	 Double c11=Double.parseDouble(a1);
//    	 Double c22=Double.parseDouble(a2);
//    	 Double c33=Double.parseDouble(a3);
//         double d1=getEnumDistance(number,c11);
//         double d2=getEnumDistance(number,c22);
//         double d3=getEnumDistance(number,c33);
//         
//         if(d1>=d2 && d1>=d3){
//      	   index=1;
//         }
//         else if(d2>=d1 && d2>=d3){
//      	   index=2;
//         }
//         else{
//      	   index=3;
//         }     
//     }
//     else{
    	 //System.out.println("Out");
    	 double d1=getEnumDistance(number,centroids.c1.getvalue());       
    	 double d2=getEnumDistance(number,centroids.c2.getvalue());
    	 double d3=getEnumDistance(number,centroids.c3.getvalue());
    	// System.out.println("this is map"+d1+","+d2+","+d3);
	     if(d1<=d2 && d1<=d3){
	    	   
	    	   context.write(new Text(Double.toString(centroids.c1.getvalue())), new IntWritable(number));
	       }
	     else if(d2<=d1 && d2<=d3){
	    	 context.write(new Text(Double.toString(centroids.c2.getvalue())), new IntWritable(number));
	       }
	     else{
	    	 context.write(new Text(Double.toString(centroids.c3.getvalue())), new IntWritable(number)); 
	       } 
        
        
//         }   	 
     
     
     
     
     
     

     

    
   }
  public static double getEnumDistance(double point,double center){  //  get the distance 
      double distance=0.0;  
      distance=(point-center)*(point-center);
      distance=Math.sqrt(distance);
      return distance;  
  }
  
//  public static boolean isDouble(String value) {
//	  try {
//	   Double.parseDouble(value);
//	   if (value.contains("."))
//	    return true;
//	   return false;
//	  } catch (NumberFormatException e) {
//	   return false;
//	  }
//	 }
}

 
  
  
  
  
  
  
  
   
public static class Kreducer 
  extends Reducer<Text,IntWritable,Text,IntWritable> {
  // private IntWritable result = new IntWritable();
   
   public void reduce(Text key, Iterable<IntWritable> values, 
                      Context context) throws IOException, InterruptedException {
	   Double d=0.00; 
     Double sum = 0.0;
     Double counter=0.0;
     List<IntWritable> cache = new ArrayList<IntWritable>();
     for (IntWritable val : values) {
    sum += val.get();
    counter++;
    cache.add(new IntWritable(val.get()));
     }
	     if(counter!=0){
	    d=sum/counter;
	     }
	if(Double.parseDouble(key.toString())==centroids.c1.getvalue()){
		System.out.println("this is reduce key 1"+d);
		if(Math.abs(centroids.c1.getvalue()-d)<0.1){
			context.getCounter(stop.CONVERGE).increment(1);
			
		}
		centroids.c1.setvalue(d);
	}
	else if(Double.parseDouble(key.toString())==centroids.c2.getvalue()){
		System.out.println("this is reduce key 2"+d);
		if(Math.abs(centroids.c2.getvalue()-d)<0.1){
			context.getCounter(stop.CONVERGE).increment(1);
			
		}
		centroids.c2.setvalue(d);
	}
	else{
		System.out.println("this is reduce key 3"+d);
		if(Math.abs(centroids.c3.getvalue()-d)<0.1){
			context.getCounter(stop.CONVERGE).increment(1);
			
		}
		centroids.c3.setvalue(d);
	}
    for (IntWritable value:cache){
    
    context.write(key,value);
    
    }
     
     
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
FileSystem  fs = FileSystem.get(new URI("Kmeans"), conf);

int i=0;
double a=1000.00;
double b=5000.00;
double c=10000.00;
	
	Job job = new Job(conf, "Kmeans");
	
	fs.delete(new Path("Kmeans"));
	centroids.c1.setvalue(a);
	centroids.c2.setvalue(b);
	centroids.c3.setvalue(c);
	    
	 job.setMapperClass(Kmapper.class);
	 job.setReducerClass(Kreducer.class);
	 job.setOutputKeyClass(Text.class);
	 job.setOutputValueClass(IntWritable.class);
	 FileInputFormat.addInputPath(job, new Path(inputFile));
	 FileOutputFormat.setOutputPath(job, new Path("Kmeans"));
	 job.waitForCompletion(true);
	 System.out.println("Kmeans done");
	a=centroids.c1.getvalue();
	b=centroids.c2.getvalue();
	c=centroids.c3.getvalue();
	 double pre_a=a;
	 double pre_b=b;
	 double pre_c=c;
	 boolean flag=true;
	 do{
		 
		 	pre_a=a;
		 	pre_b=b;
		 	pre_c=c;
			Job job1 = new Job(conf, "Kmeans");
			
			fs.delete(new Path("Kmeans"));
			centroids.c1.setvalue(a);
			centroids.c2.setvalue(b);
			centroids.c3.setvalue(c);
			 job1.setMapperClass(Kmapper.class);
			 job1.setReducerClass(Kreducer.class);
			 job1.setOutputKeyClass(Text.class);
			 job1.setOutputValueClass(IntWritable.class);
			 FileInputFormat.addInputPath(job1, new Path(inputFile));
			 FileOutputFormat.setOutputPath(job1, new Path("Kmeans"));
			 job1.waitForCompletion(true);
			 System.out.println("Kmeans done");
			a=centroids.c1.getvalue();
			b=centroids.c2.getvalue();
			c=centroids.c3.getvalue();
			System.out.println("this is current reduce key "+a+","+b+","+c);
			System.out.println("this is previous reduce key "+pre_a+","+pre_b+","+pre_c);
			if(job1.getCounters().findCounter(stop.CONVERGE).getValue()==3){
				flag=false;
			}
	 }while(flag);

    


}catch (IOException e) {
  e.printStackTrace();
}catch (ClassNotFoundException e) {
e.printStackTrace();
}catch (InterruptedException e) {
e.printStackTrace();
} catch (URISyntaxException e) {
	
	e.printStackTrace();
}

return null;
}



}