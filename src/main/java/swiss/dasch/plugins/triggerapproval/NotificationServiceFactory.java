package swiss.dasch.plugins.triggerapproval;

import javax.annotation.Nullable;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.Bound;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.springframework.security.core.Authentication;

import jenkins.model.Jenkins;

public class NotificationServiceFactory {

	private final NotificationServiceState state;

	private NotificationService service;
	private Bound serviceBound;

	public NotificationServiceFactory() {
		Authentication auth = Jenkins.getAuthentication2();

		this.state = new NotificationServiceState(auth != Jenkins.ANONYMOUS2 ? auth.getName() : null);
	}

	@JavaScriptMethod
	@Nullable
	public synchronized String create() {
		if (!TriggerApproval.get().getEnabled()) {
			return null;
		}

		this.removeService();

		Ancestor ancestor = Stapler.getCurrentRequest().findAncestor(BoundObjectTable.class);
		if (ancestor == null) {
			throw new IllegalStateException("No BoundObjectTable ancestor");
		}

		this.service = new NotificationService(this.state);
		this.serviceBound = ((BoundObjectTable) ancestor.getObject()).bind(this.service);

		return this.serviceBound.getProxyScript();
	}

	private synchronized void removeService() {
		if (this.service != null) {
			this.service.cancel();
			this.service = null;
		}

		if (this.serviceBound != null) {
			this.serviceBound.release();
			this.serviceBound = null;
		}
	}

}
