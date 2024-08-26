package swiss.dasch.plugins.triggerapproval;

import java.util.List;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.Queue.Task;

public interface TriggerEventListener extends ExtensionPoint {
	public static enum Event {
		APPROVED("approved"), NOT_APPROVED("not_approved"), DENIED("denied");

		public final String name;

		private Event(String name) {
			this.name = name;
		}
	}

	void onTriggerEvent(Event event, Task task, List<Action> actions, List<Cause> causes);

	public static List<TriggerEventListener> all() {
		return ExtensionList.lookup(TriggerEventListener.class);
	}
}
