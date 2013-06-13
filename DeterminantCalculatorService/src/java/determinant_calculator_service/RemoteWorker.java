package determinant_calculator_service;

import akka.actor.ActorRef;

public class RemoteWorker {

	private String remoteAddress;
	private String name;
	private ActorRef actorRef;

	public RemoteWorker(String remoteAddress, ActorRef actorRef) {
		this.remoteAddress = remoteAddress;
		String[] tokens = remoteAddress.split("/");
		this.name = tokens[tokens.length-1];
		this.actorRef = actorRef;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public String getName() {
		return name;
	}	

	public ActorRef getActorRef() {
		return actorRef;
	}
}
