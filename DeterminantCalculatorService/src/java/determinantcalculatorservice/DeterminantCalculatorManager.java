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
import com.typesafe.config.ConfigFactory;
import java.net.URL;
import java.util.HashMap;
import untyped.Messages.RegisterWorker;
import untyped.Messages.RemoveWorker;
import untyped.Request;

/**
 *
 * @author Marco
 */
public class DeterminantCalculatorManager {
    
    private static DeterminantCalculatorManager instance;
    private int reqNumber;
    private ActorRef master;
    private HashMap<String,Request> requests;
    
    private DeterminantCalculatorManager(){
        reqNumber=0;
        ActorSystem system = ActorSystem.create("master",ConfigFactory.load().getConfig("master"));
        master = system.actorOf(new Props(UntypedMaster.class), "untyped-master");
        requests = new HashMap<String,Request>();
    }
    
    public synchronized static DeterminantCalculatorManager getInstance(){
        if(instance==null)
            instance = new DeterminantCalculatorManager();
        return instance;
    }
    
    public synchronized String computeDeterminant(int order, URL fileValues){
        String reqId = "req"+reqNumber;
        reqNumber=reqNumber+1;
        requests.put(reqId, new Request());
        master.tell(new Compute(order,fileValues,reqId));
        return reqId;
    }
    
    public synchronized int getPercentageDone(String reqId){
        return requests.get(reqId).getPercentageDone();
    }
        
    public synchronized double getResult(String reqId){
        return requests.get(reqId).getResult();
    }

    public synchronized boolean registerWorker(String name, String ip, int port) {
        master.tell(new RegisterWorker(name,ip,port));
        return true;
    }
    
    public synchronized boolean removeWorker(String name, String ip, int port) {
        master.tell(new RemoveWorker(name,ip,port));
        return true;
    }    
}
