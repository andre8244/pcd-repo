/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinantcalculatoruser;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import untyped.UntypedWorker;

/**
 *
 * @author Marco
 */
public class DeterminantCalculatorUser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int nWorkers = 5;
        for (int i=0; i<nWorkers; i++){
            ActorSystem system = ActorSystem.create("worker"+i,ConfigFactory.load().getConfig("worker"+i));
            ActorRef worker = system.actorOf(new Props(UntypedWorker.class), "worker"+i);
            register("worker"+i,"127.0.0.1",(2553+i));
        }
    }

    private static boolean register(String name, String ip, int port) {
        determinantcalculatorservice.DeterminantCalculatorService_Service service = new determinantcalculatorservice.DeterminantCalculatorService_Service();
        determinantcalculatorservice.DeterminantCalculatorService servicePort = service.getDeterminantCalculatorServicePort();
        return servicePort.registerWorker(name,ip,port);
    }
}