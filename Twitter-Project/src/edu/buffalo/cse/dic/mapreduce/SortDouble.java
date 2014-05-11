package edu.buffalo.cse.dic.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


public class SortDouble {
	
	public static class DoubleMapper
			extends Mapper<Object, Text, DoubleWritable, Text>{

		private DoubleWritable count = new DoubleWritable();
		private Text word = new Text();
		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			word.set(value.toString().split("\\t")[0]);
			count.set(Double.parseDouble(value.toString().split("\\t")[1]));
			context.write(count,word);
		}
		
	}
	
	public static class DoubleReducer
			extends Reducer<DoubleWritable, Text, DoubleWritable, Text> {

		@Override
		protected void reduce(DoubleWritable count, Iterable<Text> words, Context context)
				throws IOException, InterruptedException {
			for (Text word : words) {
				context.write(count,word);
			}
		}

		
	}
	
	public static class DoubleReverseComparator extends WritableComparator {
        private static final DoubleWritable.Comparator DOUBLE_COMPARATOR = new DoubleWritable.Comparator();

        public DoubleReverseComparator() {
            super(Text.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return (-1)* DOUBLE_COMPARATOR
                    .compare(b1, s1, l1, b2, s2, l2);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            if (a instanceof DoubleWritable && b instanceof DoubleWritable) {
                return (-1)*(((DoubleWritable) a)
                        .compareTo((DoubleWritable) b));
            }
            return super.compare(a, b);
        }
    }
}
