package swiss.dasch.plugins.triggerapproval;

import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;

@Extension
public class TriggerApprovalLink extends ManagementLink {

	@Override
	public String getDisplayName() {
		return Messages.TriggerApprovalLink_DisplayName();
	}

	@Override
	public String getIconFileName() {
		if (!TriggerApproval.get().getEnabled()) {
			return null;
		}
		return "notepad.svg";
	}

	@Override
	public String getUrlName() {
		return TriggerApproval.get().getUrlName();
	}

	@Override
	public Category getCategory() {
		return Category.CONFIGURATION;
	}

	@Override
	public Permission getRequiredPermission() {
		return Permission.CONFIGURE;
	}

	// TODO Upgrade Jenkins and add badge

}
