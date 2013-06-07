package untyped;

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
	
	public static class Job implements Serializable{
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
	
	public static class JobResult implements Serializable{
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
        
	public static class RegisterWorker implements Serializable{
                private final String name;
                private final String ip;
            	private final int port;
                
                public RegisterWorker(String name, String ip, int port) {
			this.name = name;
                        this.ip = ip;
                        this.port = port;
		}
		
		public String getName() {
			return name;
		}
                
                public String getIp() {
			return ip;
		}
                                
                public int getPort() {
			return port;
		}
	}        
        
	public static class RemoveWorker implements Serializable{
                private final String name;
                private final String ip;
            	private final int port;
                
                public RemoveWorker(String name, String ip, int port) {
			this.name = name;
                        this.ip = ip;
                        this.port = port;
		}
		
		public String getName() {
			return name;
		}
                
                public String getIp() {
			return ip;
		}
                                
                public int getPort() {
			return port;
		}
	}
}
