package untyped;

import akka.actor.ActorRef;

/**
 *
 * @author Marco
 */
public class Worker {

	private String name;
	private String ip;
	private int port;
	private ActorRef actorRef;

	public Worker(String name, String ip, int port, ActorRef actorRef) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.actorRef = actorRef;
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

	public ActorRef getActorRef() {
		return actorRef;
	}
}
