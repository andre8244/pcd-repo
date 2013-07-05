package determinant_calculator_service;

import java.util.ArrayList;

import akka.actor.ActorRef;

/**
 * A data structure that stores the informations about a worker actor.
 * 
 */
public class WorkerInfo {
	
	private String remoteAddress;
	private ActorRef actorRef;
	private ArrayList<GaussJob> jobs;
	
	/**
	 * Constructs the data structure.
	 * 
	 * @param remoteAddress remote path of the worker actor
	 * @param actorRef the <code>ActorRef</code> of the worker actor
	 */
	public WorkerInfo(String remoteAddress, ActorRef actorRef) {
		this.remoteAddress = remoteAddress;
		this.actorRef = actorRef;
		jobs = new ArrayList<GaussJob>();
	}
	
	/**
	 * Returns the remote address of the worker.
	 * 
	 * @return the remote address of the worker
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	/**
	 * Returns the <code>ActorRef</code> of the worker.
	 * 
	 * @return the <code>ActorRef</code> of the worker
	 */
	public ActorRef getActorRef() {
		return actorRef;
	}
	
	/**
	 * Returns the list of jobs the worker actor is working on.
	 * 
	 * @return the list of jobs the worker actor is working on
	 */
	public ArrayList<GaussJob> getJobs() {
		return jobs;
	}
	
	/**
	 * Adds a job to the list of jobs.
	 * 
	 * @param reqId the request the job belongs to
	 * @param rows the set of rows of the <code>GaussJob</code> to add
	 * @param rowNumber the index of the first row of the <code>GaussJob</code> to add
	 */
	public void addJob(String reqId, int nRows, int offset) {
		jobs.add(new GaussJob(reqId, nRows, offset));
	}
	
	/**
	 * Removes a job from the list of jobs.
	 * 
	 * @param job the job to remove
	 */
	public void removeJob(GaussJob job) {
		jobs.remove(job);
	}
}
