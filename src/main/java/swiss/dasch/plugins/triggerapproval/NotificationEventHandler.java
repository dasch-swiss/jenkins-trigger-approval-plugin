package swiss.dasch.plugins.triggerapproval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.Queue.Task;

@Extension
public class NotificationEventHandler implements TriggerEventListener {

	public static final int MAX_RECENT_NOTIFICATIONS = 100;

	public static class Notification {
		public final long time;
		public final String userId;
		public final Event event;

		public Notification(long time, String userId, Event event) {
			this.time = time;
			this.userId = userId;
			this.event = event;
		}

		@Override
		public int hashCode() {
			return Objects.hash(event, time, userId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Notification other = (Notification) obj;
			return event == other.event && time == other.time && Objects.equals(userId, other.userId);
		}
	}

	private final LinkedBlockingDeque<Notification> recentNotifications = new LinkedBlockingDeque<>();

	public Collection<Notification> getRecentNotifications() {
		return Collections.unmodifiableCollection(this.recentNotifications);
	}

	@Override
	public void onTriggerEvent(Event event, Task task, List<Action> actions, List<Cause> causes) {
		if (event != Event.APPROVED) {
			for (Cause cause : causes) {
				if (cause instanceof UserIdCause) {
					String causeUserId = ((UserIdCause) cause).getUserId();

					this.recentNotifications.addFirst(new Notification(System.currentTimeMillis(), causeUserId, event));

					while (this.recentNotifications.size() > MAX_RECENT_NOTIFICATIONS) {
						this.recentNotifications.removeLast();
					}
				}
			}
		}
	}

	public static NotificationEventHandler get() {
		return (NotificationEventHandler) TriggerEventListener.all().stream()
				.filter(l -> l instanceof NotificationEventHandler).findAny()
				.orElseThrow(() -> new IllegalStateException("NotificationEventHandler not registered"));
	}

}
