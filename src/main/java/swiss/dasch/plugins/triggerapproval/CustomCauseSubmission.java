package swiss.dasch.plugins.triggerapproval;

import javax.annotation.Nullable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.base.Preconditions;

import hudson.model.Cause;

public class CustomCauseSubmission {
	private String causeClassName;
	private boolean selectedExactCause;
	private @Nullable String selectedTaskUrlRegex;
	private int selectedMaxCount;
	private boolean approve;

	@DataBoundConstructor
	public CustomCauseSubmission(String causeClassName) {
		Preconditions.checkArgument(causeClassName != null && causeClassName.length() > 0,
				"Invalid class name " + causeClassName);
		this.causeClassName = causeClassName;
	}

	@DataBoundSetter
	public void setSelectedExactCause(boolean selectedExactCause) {
		this.selectedExactCause = selectedExactCause;
	}

	@DataBoundSetter
	public void setSelectedTaskUrlRegex(@Nullable String selectedTaskUrlRegex) {
		if (selectedTaskUrlRegex != null && selectedTaskUrlRegex.length() > 0) {
			this.selectedTaskUrlRegex = selectedTaskUrlRegex;
		} else {
			this.selectedTaskUrlRegex = null;
		}
	}

	@DataBoundSetter
	public void setSelectedMaxCount(int selectedMaxCount) {
		Preconditions.checkArgument(selectedMaxCount > 0,
				"Invalid selectedMaxCount " + selectedMaxCount + ", expected selectedMaxCount > 0");
		this.selectedMaxCount = selectedMaxCount;
	}

	@DataBoundSetter
	public void setApprove(boolean approve) {
		this.approve = approve;
	}

	public void setIndefinite() {
		this.selectedMaxCount = 0;
	}

	public String getCauseClassName() {
		return this.causeClassName;
	}

	public boolean getSelectedExactCause() {
		return this.selectedExactCause;
	}

	@Nullable
	public String getSelectedTaskUrlRegex() {
		return this.selectedTaskUrlRegex;
	}

	public int getSelectedMaxCount() {
		return this.selectedMaxCount;
	}

	public boolean isApproval() {
		return this.approve;
	}

	public CauseEntry createEntry(@Nullable Cause cause) {
		int count = this.getSelectedMaxCount();
		if (cause != null && this.getCauseClassName().equals(cause.getClass().getName())) {
			if (count > 0) {
				return CauseEntry.limited(cause, this.getSelectedExactCause(), this.getSelectedTaskUrlRegex(), count);
			} else {
				return CauseEntry.indefinite(cause, this.getSelectedExactCause(), this.getSelectedTaskUrlRegex());
			}
		} else {
			if (count > 0) {
				return CauseEntry.limited(this.getCauseClassName(), this.getSelectedTaskUrlRegex(), count);
			} else {
				return CauseEntry.indefinite(this.getCauseClassName(), this.getSelectedTaskUrlRegex());
			}
		}
	}
}
