package swiss.dasch.plugins.triggerapproval;

import java.util.Collection;
import java.util.Collections;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

@Extension
public class TriggerApprovalActionFactory extends TransientActionFactory<Job<?, ?>> {

	private static final TriggerApprovalAction ACTION = new TriggerApprovalAction();

	@Override
	public Collection<? extends Action> createFor(Job<?, ?> job) {
		if (TriggerApproval.get().getEnabled() && Jenkins.get().hasPermission(Permission.CONFIGURE)) {
			return Collections.singleton(ACTION);
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Job<?, ?>> type() {
		return (Class<Job<?, ?>>) (Class<?>) Job.class;
	}

}
