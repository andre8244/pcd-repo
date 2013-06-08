package determinantcalculatorservice;

import akka.actor.ActorRef;

public class RemoteWorker {

	private String remoteAddress;
	private ActorRef actorRef;

	public RemoteWorker(String remoteAddress, ActorRef actorRef) {
		this.remoteAddress = remoteAddress;
		this.actorRef = actorRef;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public ActorRef getActorRef() {
		return actorRef;
	}
}
