package messages;

import java.util.ArrayList;
import java.io.Serializable;

public class Messages {

	public static class Job implements Serializable {

		private final ArrayList<Double> list;
		private final String reqId;

		public Job(ArrayList<Double> list, String reqId) {
			this.list = list;
			this.reqId = reqId;
		}

		public ArrayList<Double> getList() {
			return list;
		}

		public String getReqId() {
			return reqId;
		}
	}

	public static class JobResult implements Serializable {

		private final double result;
		private final String reqId;

		public JobResult(double result, String reqId) {
			this.result = result;
			this.reqId = reqId;
		}

		public double getResult() {
			return result;
		}

		public String getReqId() {
			return reqId;
		}
	}
}
