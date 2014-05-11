package edu.buffalo.cse.dic.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


public class SortReducerOutput {
	
	public static class OutputBreaker
			extends Mapper<Object, Text, IntWritable, Text>{

		private IntWritable count = new IntWritable();
		private Text word = new Text();
		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			word.set(value.toString().split("\\t")[0]);
			count.set(Integer.parseInt(value.toString().split("\\t")[1]));
			context.write(count,word);
		}
		
	}
	
	public static class SortByCount
			extends Reducer<IntWritable, Text, IntWritable, Text> {

		@Override
		protected void reduce(IntWritable count, Iterable<Text> words, Context context)
				throws IOException, InterruptedException {
			for (Text word : words) {
				context.write(count,word);
			}
		}

		
	}
	
	public static class ReverseComparator extends WritableComparator {
        private static final IntWritable.Comparator INT_COMPARATOR = new IntWritable.Comparator();

        public ReverseComparator() {
            super(Text.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return (-1)* INT_COMPARATOR
                    .compare(b1, s1, l1, b2, s2, l2);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            if (a instanceof IntWritable && b instanceof IntWritable) {
                return (-1)*(((IntWritable) a)
                        .compareTo((IntWritable) b));
            }
            return super.compare(a, b);
        }
    }
}
