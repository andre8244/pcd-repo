package determinant_calculator_service;

import akka.actor.ActorRef;
import java.util.ArrayList;
import java.util.HashMap;

public class RemoteWorker {

	private String remoteAddress;
	//private String name;
	private ActorRef actorRef;
	private ArrayList<String> reqIds;
	private HashMap<String,double[][]> rows;
	private HashMap<String,Integer> rowNumbers;

	public RemoteWorker(String remoteAddress, ActorRef actorRef) {
		this.remoteAddress = remoteAddress;
		//String[] tokens = remoteAddress.split("/");
		//this.name = tokens[tokens.length-1];
		this.actorRef = actorRef;
		reqIds = new ArrayList<String>();
		rows = new HashMap<String,double[][]>();
		rowNumbers = new HashMap<String,Integer>();
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	/*public String getName() {
		return name;
	}*/	

	public ActorRef getActorRef() {
		return actorRef;
	}
	
	public ArrayList<String> getReqIds() {
		return reqIds;
	}
		
	public double[][] getRows(String reqId) {
		return rows.get(reqId);
	}
	
	public int getRowNumber(String reqId) {
		return rowNumbers.get(reqId);
	}
	
}
