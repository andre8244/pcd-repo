/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinantcalculatorservice;

import untyped.Messages.Compute;
import untyped.UntypedMaster;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.typesafe.config.ConfigFactory;
import java.net.URL;
import java.util.HashMap;
import untyped.Messages.PercentageDone;
import untyped.Messages.RegisterWorker;
import untyped.Messages.RemoveWorker;
import untyped.Messages.Result;

/**
 *
 * @author Marco
 */
public class DeterminantCalculatorManager extends UntypedActor{
    
    private static DeterminantCalculatorManager instance;
    private int reqNumber;
    private ActorRef master;
    private HashMap<String,Double> results;
    private HashMap<String,Integer> done;
    
    private DeterminantCalculatorManager(){
        reqNumber=0;
        ActorSystem system = ActorSystem.create("master",ConfigFactory.load().getConfig("master"));
        master = system.actorOf(new Props(UntypedMaster.class), "untyped-master");
        results = new HashMap<String,Double>();
        done = new HashMap<String,Integer>();
    }
    
    public synchronized static DeterminantCalculatorManager getInstance(){
        if(instance==null)
            instance = new DeterminantCalculatorManager();
        return instance;
    }
    
    public synchronized String computeDeterminant(int order, URL fileValues){
        String reqId = "req"+reqNumber;
        reqNumber=reqNumber+1;
        done.put(reqId, 0);
        master.tell(new Compute(order,fileValues,reqId),getSelf());
        return reqId;
    }
    
    public synchronized int getPercentageDone(String reqId){
        return done.get(reqId);
    }
        
    public synchronized double getResult(String reqId){
        return results.get(reqId);
    }

    public synchronized boolean registerWorker(String name, String ip, int port) {
        master.tell(new RegisterWorker(name,ip,port), getSelf());
        return true;
    }
    
    public synchronized boolean removeWorker(String name, String ip, int port) {
        master.tell(new RemoveWorker(name,ip,port), getSelf());
        return true;
    }    

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof PercentageDone) {
            PercentageDone pd = (PercentageDone)msg;
            int percentageDone = pd.getPercentageDone();
            String reqId = pd.getReqId();
            done.put(reqId, percentageDone);
        } else if (msg instanceof Result) {
            Result res = (Result)msg;
            double result = res.getResult();
            String reqId = res.getReqId();
            results.put(reqId, result);
	}
    }
}
