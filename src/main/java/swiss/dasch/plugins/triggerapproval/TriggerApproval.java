package swiss.dasch.plugins.triggerapproval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import hudson.BulkChange;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.model.Queue.Task;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Symbol("triggerApproval")
@Extension
public class TriggerApproval extends GlobalConfiguration implements RootAction {

	private static final Logger LOGGER = Logger.getLogger(TriggerApproval.class.getName());

	// User configurable properties
	private boolean enabled;
	private boolean allowJobsWithoutCauses;
	private boolean strictCheckingEnabled;
	private int maxPendingCauses;
	private boolean loggingEnabled;

	// Internal persistent values
	private Map<CauseEntry, CauseEntry> pendingCauses = new LinkedHashMap<>();
	private Set<CauseEntry> ignoredCauses = new LinkedHashSet<>();
	private List<CauseEntry> approvedCauses = new ArrayList<>();
	private List<CauseEntry> deniedCauses = new ArrayList<>();

	public TriggerApproval() {
		this.resetProperties();
		this.load();
	}

	private synchronized void resetProperties() {
		this.enabled = false;
		this.allowJobsWithoutCauses = true;
		this.strictCheckingEnabled = false;
		this.maxPendingCauses = 10;
		this.loggingEnabled = true;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	@DataBoundSetter
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getAllowJobsWithoutCauses() {
		return this.allowJobsWithoutCauses;
	}

	@DataBoundSetter
	public void setAllowJobsWithoutCauses(boolean allowJobsWithoutCauses) {
		this.allowJobsWithoutCauses = allowJobsWithoutCauses;
	}

	public boolean getStrictCheckingEnabled() {
		return this.strictCheckingEnabled;
	}

	@DataBoundSetter
	public void setStrictCheckingEnabled(boolean strictCheckingEnabled) {
		this.strictCheckingEnabled = strictCheckingEnabled;
	}

	public int getMaxPendingCauses() {
		return this.maxPendingCauses;
	}

	@DataBoundSetter
	public void setMaxPendingCauses(int maxPendingCauses) {
		this.maxPendingCauses = maxPendingCauses;
	}

	public boolean getLoggingEnabled() {
		return this.loggingEnabled;
	}

	@DataBoundSetter
	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json) {
		this.resetProperties();

		try (BulkChange bc = new BulkChange(this)) {
			req.bindJSON(this, json);
			bc.commit();
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Exception during BulkChange.", ex);
			return false;
		}

		return true;
	}

	public synchronized void addApprovedCauseEntry(CauseEntry entry) {
		this.approvedCauses.add(entry);
		this.save();
	}

	public synchronized boolean removeApprovedCauseEntry(CauseEntry entry) {
		if (this.approvedCauses.remove(entry)) {
			this.save();
			return true;
		}
		return false;
	}

	public synchronized void clearApprovedCauseEntries(CauseEntry entry) {
		this.approvedCauses.clear();
		this.save();
	}

	public synchronized List<CauseEntry> getApprovedCauseEntries() {
		return new ArrayList<>(this.approvedCauses);
	}

	public synchronized void addDeniedCauseEntry(CauseEntry entry) {
		this.deniedCauses.add(entry);
		this.save();
	}

	public synchronized boolean removeDeniedCauseEntry(CauseEntry entry) {
		if (this.deniedCauses.remove(entry)) {
			this.save();
			return true;
		}
		return false;
	}

	public synchronized List<CauseEntry> getDeniedCauseEntries() {
		return new ArrayList<>(this.deniedCauses);
	}

	public synchronized void clearDeniedCauseEntries() {
		this.deniedCauses.clear();
		this.save();
	}

	private synchronized boolean addPendingCauseEntry(CauseEntry entry) {
		CauseEntry existing;
		if ((existing = this.pendingCauses.get(entry)) == null) {
			entry.setDate(new Date());

			this.pendingCauses.put(entry, entry);

			while (this.pendingCauses.size() > this.maxPendingCauses) {
				Iterator<CauseEntry> it = this.pendingCauses.values().iterator();
				it.next();
				it.remove();
			}

			this.save();

			return true;
		} else {
			existing.setDate(new Date());

			// Move to back
			this.pendingCauses.remove(existing);
			this.pendingCauses.put(existing, existing);

			this.save();
		}

		return false;
	}

	public synchronized boolean removePendingCauseEntry(CauseEntry entry) {
		if (this.pendingCauses.remove(entry) != null) {
			this.save();
			return true;
		}
		return false;
	}

	public synchronized List<CauseEntry> getPendingCauseEntries() {
		List<CauseEntry> list = new ArrayList<>(this.pendingCauses.values());
		Collections.reverse(list);
		return list;
	}

	public synchronized void clearPendingCauseEntries() {
		this.pendingCauses.clear();
		this.save();
	}

	public synchronized boolean hasPendingCauseEntries() {
		return !this.pendingCauses.isEmpty();
	}

	public synchronized boolean addIgnoredCauseEntry(CauseEntry entry) {
		if (this.ignoredCauses.add(entry)) {
			this.save();
			return true;
		}
		return false;
	}

	public synchronized boolean removeIgnoredCauseEntry(CauseEntry entry) {
		if (this.ignoredCauses.remove(entry)) {
			this.save();
			return true;
		}
		return false;
	}

	public synchronized List<CauseEntry> getIgnoredCauseEntries() {
		return new ArrayList<>(this.ignoredCauses);
	}

	public boolean tryApproveTask(Task task, List<Action> actions) {
		boolean isApproved = false;
		TriggerEventListener.Event event = TriggerEventListener.Event.NOT_APPROVED;

		List<Cause> causes = new ArrayList<>();

		synchronized (this) {
			if (this.allowJobsWithoutCauses && actions.isEmpty()) {
				return true;
			}

			for (Action action : actions) {
				if (action instanceof CauseAction) {
					causes.addAll(((CauseAction) action).getCauses());
				}
			}

			Set<Cause> matchedCauses = new HashSet<>();

			Set<CauseEntry> denyingEntries = new HashSet<>();

			for (Cause cause : causes) {
				for (CauseEntry entry : this.deniedCauses) {
					if (entry.matches(task, cause)) {
						matchedCauses.add(cause);
						denyingEntries.add(entry);
					}
				}
			}

			Set<CauseEntry> approvingEntries = new HashSet<>();

			boolean allCausesApproved = true;
			boolean anyCausesApproved = false;

			for (Cause cause : causes) {
				boolean causeApproved = false;

				for (CauseEntry entry : this.approvedCauses) {
					if (entry.matches(task, cause)) {
						matchedCauses.add(cause);
						approvingEntries.add(entry);
						causeApproved = true;
					}
				}

				if (causeApproved) {
					anyCausesApproved = true;
				} else {
					allCausesApproved = false;
				}
			}

			outer: for (Cause cause : causes) {
				if (!matchedCauses.contains(cause)) {
					CauseEntry entry = CauseEntry.indefinite(cause, true,
							RegexUtil.getDefaultRegexFromTaskURL(task.getUrl()));

					if (this.ignoredCauses.contains(entry)) {
						continue outer;
					}

					for (CauseEntry ignoredCause : this.ignoredCauses) {
						if (ignoredCause.matches(task, cause)) {
							continue outer;
						}
					}

					if (this.addPendingCauseEntry(entry) && this.loggingEnabled) {
						LOGGER.info("Added new pending cause " + entry.getCauseClassName() + " for task "
								+ task.getFullDisplayName());
					}
				}
			}

			boolean couldBeApproved;
			if (this.strictCheckingEnabled) {
				couldBeApproved = allCausesApproved;
			} else {
				couldBeApproved = anyCausesApproved;
			}

			if (couldBeApproved) {
				boolean changed = false;

				if (!denyingEntries.isEmpty()) {
					for (CauseEntry entry : denyingEntries) {
						if (entry.getCount() == 1) {
							this.removeDeniedCauseEntry(entry);
						} else {
							changed |= entry.decreaseCount();
						}
					}

					isApproved = false;

					event = TriggerEventListener.Event.DENIED;

					if (this.loggingEnabled) {
						LOGGER.info("Denied task " + task.getFullDisplayName() + " due to its causes being denied: "
								+ String.join(", ", denyingEntries.stream().map(entry -> entry.getCauseClassName())
										.toArray(i -> new String[i])));
					}
				} else {
					for (CauseEntry entry : approvingEntries) {
						if (entry.getCount() == 1) {
							this.removeApprovedCauseEntry(entry);
						} else {
							changed |= entry.decreaseCount();
						}
					}

					isApproved = true;

					event = TriggerEventListener.Event.APPROVED;
				}

				if (changed) {
					this.save();
				}
			} else if (this.loggingEnabled) {
				LOGGER.info("Denied task " + task.getFullDisplayName() + " due to its causes not being approved: "
						+ String.join(", ",
								causes.stream().map(cause -> cause.getClass().getName()).toArray(i -> new String[i])));
			}
		}

		dispatchEvent(event, task, actions, causes);

		return isApproved;
	}

	private static void dispatchEvent(TriggerEventListener.Event event, Task task, List<Action> actions,
			List<Cause> causes) {
		TriggerEventListener.all().forEach(l -> l.onTriggerEvent(event, task, actions, causes));
	}

	public static TriggerApproval get() {
		return (TriggerApproval) Jenkins.get().getDescriptorOrDie(TriggerApproval.class);
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return "triggerApproval";
	}

	@Override
	public String getDisplayName() {
		return Messages.TriggerApproval_DisplayName();
	}

	public ListBoxModel doFillSelectedTaskUrlRegexItems() {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		ListBoxModel list = new ListBoxModel();

		Collection<String> jobNames = Jenkins.get().getJobNames();

		for (String jobName : jobNames) {
			TopLevelItem item = Jenkins.get().getItem(jobName);

			if (item != null && item.hasPermission(Item.DISCOVER)) {
				list.add(item.getFullDisplayName(), RegexUtil.getDefaultRegexFromTaskURL(item.getUrl()));
			}
		}

		return list;
	}

	private static CustomCauseSubmission parseCustomCauseSubmission(StaplerRequest req) throws ServletException {
		JSONObject json = req.getSubmittedForm();

		CustomCauseSubmission submission = req.bindJSON(CustomCauseSubmission.class, json);

		submission.setApprove(req.getParameter("approve") != null);

		if (!"on".equals(req.getParameter("hasSelectedMaxCount"))) {
			submission.setIndefinite();
		}

		if (!"on".equals(req.getParameter("hasSelectedTaskUrlRegex"))) {
			submission.setSelectedTaskUrlRegex(null);
		}

		if (!"on".equals(req.getParameter("hasSelectedExactCause"))) {
			submission.setSelectedExactCause(false);
		}

		return submission;
	}

	@POST
	public synchronized void doSubmitCustomCause(StaplerRequest req, StaplerResponse resp)
			throws IOException, ServletException {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		CustomCauseSubmission submission = parseCustomCauseSubmission(req);

		CauseEntry entry = submission.createEntry(null);

		if (submission.isApproval()) {
			this.addApprovedCauseEntry(entry);
		} else {
			this.addDeniedCauseEntry(entry);
		}

		resp.forwardToPreviousPage(req);
	}

	@POST
	public void doRemoveApprovedCause(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		JSONObject json = req.getSubmittedForm();

		ExistingCauseSubmission submission = req.bindJSON(ExistingCauseSubmission.class, json);

		synchronized (this) {
			for (CauseEntry entry : this.approvedCauses) {
				if (submission.matches(entry)) {
					this.removeApprovedCauseEntry(entry);
					break;
				}
			}
		}

		resp.forwardToPreviousPage(req);
	}

	@POST
	public void doRemoveDeniedCause(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		JSONObject json = req.getSubmittedForm();

		ExistingCauseSubmission submission = req.bindJSON(ExistingCauseSubmission.class, json);

		synchronized (this) {
			for (CauseEntry entry : this.deniedCauses) {
				if (submission.matches(entry)) {
					this.removeDeniedCauseEntry(entry);
					break;
				}
			}
		}

		resp.forwardToPreviousPage(req);
	}

	@POST
	public void doSubmitExistingCause(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		JSONObject json = req.getSubmittedForm();

		ExistingCauseSubmission existingSubmission = req.bindJSON(ExistingCauseSubmission.class, json);

		CustomCauseSubmission customSubmission = parseCustomCauseSubmission(req);

		synchronized (this) {
			CauseEntry newEntry = null;

			for (CauseEntry pendingEntry : this.pendingCauses.values()) {
				if (existingSubmission.matches(pendingEntry)) {
					newEntry = customSubmission.createEntry(pendingEntry.getCause());

					this.removePendingCauseEntry(pendingEntry);

					if (req.getParameter("ignore") != null) {
						this.addIgnoredCauseEntry(newEntry);
					}

					break;
				}
			}

			if (newEntry != null && (req.getParameter("approve") != null || req.getParameter("deny") != null)) {
				if (customSubmission.isApproval()) {
					this.addApprovedCauseEntry(newEntry);
				} else {
					this.addDeniedCauseEntry(newEntry);
				}
			}
		}

		resp.forwardToPreviousPage(req);
	}

	@POST
	public void doSubmitIgnoredCause(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		JSONObject json = req.getSubmittedForm();

		ExistingCauseSubmission existingSubmission = req.bindJSON(ExistingCauseSubmission.class, json);

		CustomCauseSubmission customSubmission = parseCustomCauseSubmission(req);

		synchronized (this) {
			CauseEntry newEntry = null;

			for (CauseEntry ignoredEntry : this.ignoredCauses) {
				if (existingSubmission.matches(ignoredEntry)) {
					newEntry = customSubmission.createEntry(ignoredEntry.getCause());

					if (req.getParameter("remove") != null) {
						this.removeIgnoredCauseEntry(ignoredEntry);
					} else if (req.getParameter("ignore") != null) {
						this.addIgnoredCauseEntry(newEntry);
					}

					break;
				}
			}

			if (newEntry != null && (req.getParameter("approve") != null || req.getParameter("deny") != null)) {
				if (customSubmission.isApproval()) {
					this.addApprovedCauseEntry(newEntry);
				} else {
					this.addDeniedCauseEntry(newEntry);
				}
			}
		}

		resp.forwardToPreviousPage(req);
	}

	@POST
	public void doClearPendingCauses(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
		Jenkins.get().checkPermission(Permission.CONFIGURE);

		this.clearPendingCauseEntries();

		resp.forwardToPreviousPage(req);
	}

}
