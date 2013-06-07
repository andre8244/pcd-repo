package untyped;

import java.util.ArrayList;
import java.io.Serializable;
import java.net.URL;

public class Messages {
	public static class Compute implements Serializable{
                private final int order;
                private final URL fileValues;
            	private final String reqId;
                
                public Compute(int order, URL fileValues, String reqId) {
			this.order = order;
                        this.fileValues = fileValues;
                        this.reqId = reqId;
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

	public static class PercentageDone implements Serializable{
		private final int percentageDone;
                private final String reqId;
                
                public PercentageDone(String reqId, int percentageDone) {
                        this.reqId = reqId;
			this.percentageDone = percentageDone;
		}
		
		public int getPercentageDone() {
			return percentageDone;
		}   
                
                public String getReqId() {
			return reqId;
		}                 
	}
      
	public static class Result implements Serializable{
		private final int result;
                private final String reqId;
                
                public Result(String reqId, int result) {
                        this.reqId = reqId;
			this.result = result;
		}
		
		public int getResult() {
			return result;
		}
                
                public String getReqId() {
			return reqId;
		}                 
	}
}
