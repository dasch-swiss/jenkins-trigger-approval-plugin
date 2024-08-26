package swiss.dasch.plugins.triggerapproval;

import java.util.Objects;

import javax.annotation.Nullable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ExistingCauseSubmission {
	private String causeClassName;
	private boolean exact;
	private String taskUrlRegex;
	private int count;
	private int hash;

	@DataBoundConstructor
	public ExistingCauseSubmission(String causeClassName, boolean exact, int count, int hash) {
		this.causeClassName = causeClassName;
		this.exact = exact;
		this.count = count;
		this.hash = hash;
	}

	@DataBoundSetter
	public void setTaskUrlRegex(@Nullable String taskUrlRegex) {
		if (taskUrlRegex != null && taskUrlRegex.length() > 0) {
			this.taskUrlRegex = taskUrlRegex;
		} else {
			this.taskUrlRegex = null;
		}
	}

	public boolean matches(CauseEntry entry) {
		return entry.getCauseClassName().equals(this.causeClassName) && entry.isExact() == this.exact
				&& Objects.equals(entry.getTaskUrlRegex(), this.taskUrlRegex) && entry.getCount() == this.count
				&& entry.getHash() == this.hash;
	}
}
