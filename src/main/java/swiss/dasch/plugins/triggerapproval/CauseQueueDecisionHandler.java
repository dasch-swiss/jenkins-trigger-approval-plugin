package swiss.dasch.plugins.triggerapproval;

import java.util.List;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Queue.QueueDecisionHandler;
import hudson.model.Queue.Task;

@Extension
public class CauseQueueDecisionHandler extends QueueDecisionHandler {

	@Override
	public boolean shouldSchedule(Task p, List<Action> actions) {
		TriggerApproval manager = TriggerApproval.get();

		if (manager.getEnabled()) {
			return manager.tryApproveTask(p, actions);
		}

		return true;
	}

}
