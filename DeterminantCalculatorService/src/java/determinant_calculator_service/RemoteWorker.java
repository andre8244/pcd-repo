package determinant_calculator_service;

import akka.actor.ActorRef;
import java.util.ArrayList;

public class RemoteWorker {

	private String remoteAddress;
	private ActorRef actorRef;
	private ArrayList<Work> works;

	public RemoteWorker(String remoteAddress, ActorRef actorRef) {
		this.remoteAddress = remoteAddress;
		this.actorRef = actorRef;
		works = new ArrayList<Work>();
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public ActorRef getActorRef() {
		return actorRef;
	}
	
	public ArrayList<Work> getWorks() {
		return works;
	}

	public void addWork(String reqId, double[][] rows, int rowNumber) {
		works.add(new Work(reqId,rows,rowNumber));
	}
	
	public void removeWork(Work work) {
		works.remove(work);
	}	
}
