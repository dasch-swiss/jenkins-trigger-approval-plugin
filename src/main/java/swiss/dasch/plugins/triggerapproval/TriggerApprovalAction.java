package swiss.dasch.plugins.triggerapproval;

import hudson.model.Action;
import hudson.security.Permission;
import jenkins.model.Jenkins;

public class TriggerApprovalAction implements Action {

	@Override
	public String getDisplayName() {
		return Messages.TriggerApprovalAction_DisplayName();
	}

	@Override
	public String getIconFileName() {
		if (!TriggerApproval.get().getEnabled() || !Jenkins.get().hasPermission(Permission.CONFIGURE)) {
			return null;
		}
		return "notepad.svg";
	}

	@Override
	public String getUrlName() {
		return Jenkins.get().getRootUrl() + TriggerApproval.get().getUrlName();
	}

}
