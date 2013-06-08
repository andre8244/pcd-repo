package messages;

import determinantcalculatorservice.DeterminantCalculatorManager;
import determinantcalculatorservice.DeterminantCalculatorManager;
import java.util.ArrayList;
import java.io.Serializable;
import java.net.URL;

public class Messages {

	public static class Compute {

		private final int order;
		private final URL fileValues;
		private final String reqId;
		private DeterminantCalculatorManager manager;

		public Compute(int order, URL fileValues, String reqId, DeterminantCalculatorManager manager) {
			this.order = order;
			this.fileValues = fileValues;
			this.reqId = reqId;
			this.manager = manager;
		}

		public int getOrder() {
			return order;
		}

		public URL getFileValues() {
			return fileValues;
		}

		public String getReqId() {
			return reqId;
		}

		public DeterminantCalculatorManager getManager() {
			return manager;
		}
	}

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

	public static class RegisterWorker implements Serializable {

		private final String remoteAddress;

		public RegisterWorker(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}

		public String getRemoteAddress() {
			return remoteAddress;
		}
	}

	public static class RemoveWorker implements Serializable {

		private final String remoteAddress;

		public RemoveWorker(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}

		public String getRemoteAddress() {
			return remoteAddress;
		}
	}
}
