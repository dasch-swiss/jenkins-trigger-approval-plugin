package swiss.dasch.plugins.triggerapproval;

import hudson.Extension;
import hudson.model.PageDecorator;

@Extension
public class NotificationPageDecorator extends PageDecorator {
	public boolean isEnabled() {
		return TriggerApproval.get().getEnabled();
	}
}
