package typed;

import java.util.ArrayList;

public interface ITypedWorker {
	void submitJob(ArrayList<Double> job, ITypedMaster master);
}
