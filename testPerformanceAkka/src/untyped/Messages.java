package untyped;

import java.util.ArrayList;

public class Messages {
	public static class Start {
	}
	
	public static class Job {
		private final ArrayList<Double> list;
		
		public Job(ArrayList<Double> list) {
			this.list = list;
		}
		
		public ArrayList<Double> getList() {
			return list;
		}
	}
	
	public static class JobResult {
		private final ArrayList<Double> list;
		
		public JobResult(ArrayList<Double> list) {
			this.list = list;
		}
		
		public ArrayList<Double> getList() {
			return list;
		}
	}
}
