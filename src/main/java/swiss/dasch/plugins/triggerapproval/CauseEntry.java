package swiss.dasch.plugins.triggerapproval;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import hudson.model.Cause;
import hudson.model.Queue.Task;

public class CauseEntry implements Serializable {

	private static final long serialVersionUID = 5720777744542340813L;

	private String causeClassName = "";

	private @Nullable Cause cause;

	private @Nullable String taskUrlRegex;
	private transient @Nullable Pattern taskUrlPattern;
	private transient boolean isTaskUrlPatternInvalid;

	private int count;

	private @Nullable Date date;

	private CauseEntry(String causeClassName, @Nullable Cause cause, @Nullable String taskUrlRegex, int count) {
		this.causeClassName = causeClassName;
		this.cause = cause;
		this.count = Math.max(0, count);
		this.taskUrlRegex = taskUrlRegex;
	}

	public static CauseEntry limited(Cause cause, boolean exact, @Nullable String taskUrlRegex, int count) {
		Preconditions.checkArgument(count > 0, "Invalid count " + count + ", expected > 0");
		return new CauseEntry(cause.getClass().getName(), exact ? cause : null, taskUrlRegex, count);
	}

	public static CauseEntry limited(String causeClassName, @Nullable String taskUrlRegex, int count) {
		Preconditions.checkArgument(count > 0, "Invalid count " + count + ", expected > 0");
		return new CauseEntry(causeClassName, null, taskUrlRegex, count);
	}

	public static CauseEntry indefinite(Cause cause, boolean exact, @Nullable String taskUrlRegex) {
		return new CauseEntry(cause.getClass().getName(), exact ? cause : null, taskUrlRegex, 0);
	}

	public static CauseEntry indefinite(String causeClassName, @Nullable String taskUrlRegex) {
		return new CauseEntry(causeClassName, null, taskUrlRegex, 0);
	}

	public String getCauseClassName() {
		return this.causeClassName;
	}

	@Nullable
	public Cause getCause() {
		return this.cause;
	}

	@Nullable
	public String getCauseDescription() {
		return this.cause != null ? this.cause.getShortDescription() : null;
	}

	public int getCauseHash() {
		return this.cause != null ? this.cause.hashCode() : 0;
	}

	public boolean isExact() {
		return this.cause != null;
	}

	public boolean isIndefinite() {
		return this.count <= 0;
	}

	public int getCount() {
		return Math.max(0, this.count);
	}

	public synchronized boolean decreaseCount() {
		if (this.count > 1) {
			this.count--;
			return true;
		}
		return false;
	}

	@Nullable
	public String getTaskUrlRegex() {
		return this.taskUrlRegex;
	}

	@Nullable
	public Pattern getTaskUrlPattern() {
		if (this.taskUrlRegex != null) {
			if (this.taskUrlPattern == null && !this.isTaskUrlPatternInvalid) {
				try {
					this.taskUrlPattern = Pattern.compile(this.taskUrlRegex);
				} catch (PatternSyntaxException ex) {
					this.isTaskUrlPatternInvalid = true;
				}
			}
			return this.taskUrlPattern;
		}
		return null;
	}

	public boolean isTaskUrlPatternInvalid() {
		this.getTaskUrlPattern(); // Compile pattern
		return this.isTaskUrlPatternInvalid;
	}

	public boolean matches(Task task, Cause cause) {
		if (cause == null) {
			return false;
		}

		if (!cause.getClass().getName().equals(this.causeClassName)) {
			return false;
		}

		if (this.isTaskUrlPatternInvalid()) {
			return false;
		}

		Pattern pattern = this.getTaskUrlPattern();
		if (pattern != null && !pattern.matcher(task.getUrl()).find()) {
			return false;
		}

		if (this.isExact()) {
			if (this.cause == null) {
				return false;
			}

			if (this.cause.getClass() != cause.getClass()) {
				return false;
			}

			return Objects.equals(this.cause, cause);
		} else {
			return true;
		}
	}

	public int getHash() {
		return this.hashCode();
	}

	public void setDate(@Nullable Date date) {
		this.date = date;
	}

	@Nullable
	public Date getDate() {
		return this.date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getCause(), this.getCauseClassName(), this.getCount(), this.getTaskUrlRegex());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CauseEntry other = (CauseEntry) obj;
		return Objects.equals(this.getCause(), other.getCause())
				&& Objects.equals(this.getCauseClassName(), other.getCauseClassName())
				&& this.getCount() == other.getCount()
				&& Objects.equals(this.getTaskUrlRegex(), other.getTaskUrlRegex());
	}

}
