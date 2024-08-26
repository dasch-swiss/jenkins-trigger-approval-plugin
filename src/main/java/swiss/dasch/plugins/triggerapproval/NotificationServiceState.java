package swiss.dasch.plugins.triggerapproval;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

import javax.annotation.Nullable;

public class NotificationServiceState {

	private final LinkedHashSet<NotificationEventHandler.Notification> checkedNotifications = new LinkedHashSet<>();

	@Nullable
	private String userId;

	public NotificationServiceState(@Nullable String userId) {
		this.userId = userId;
	}

	@Nullable
	public String getUserId() {
		return this.userId;
	}

	public void reset() {
		this.checkedNotifications.clear();
	}

	public synchronized boolean checkNewNotification(NotificationEventHandler.Notification notification) {
		if (Objects.equals(this.userId, notification.userId)) {
			boolean added = this.checkedNotifications.add(notification);

			while (this.checkedNotifications.size() > NotificationEventHandler.MAX_RECENT_NOTIFICATIONS) {
				Iterator<NotificationEventHandler.Notification> it = this.checkedNotifications.iterator();
				it.next();
				it.remove();
			}

			return added;
		}

		return false;
	}

}
