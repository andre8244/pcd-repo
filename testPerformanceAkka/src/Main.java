import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import com.typesafe.config.ConfigFactory;
import typed.ITypedMaster;
import typed.TypedMaster;
import untyped.Messages.Start;
import untyped.UntypedMaster;

public class Main {
	public static void main(String args[]) {
		ActorSystem system = ActorSystem.create("akkaTestPerformance", ConfigFactory.load().getConfig("testPerformance"));
		boolean typed = false;
		
		if (typed) {
			ITypedMaster master = TypedActor.get(system).typedActorOf(
					new TypedProps<TypedMaster>(ITypedMaster.class, TypedMaster.class),
					"typed-master");
			master.start();
		} else {
			final ActorRef master = system
					.actorOf(new Props(UntypedMaster.class), "untyped-master");
			master.tell(new Start());
		}
		
	}
}
