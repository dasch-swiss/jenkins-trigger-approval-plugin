package swiss.dasch.plugins.triggerapproval;

import org.kohsuke.stapler.bind.JavaScriptMethod;

import jenkins.util.ProgressiveRendering;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class NotificationService extends ProgressiveRendering {

	private static final JSONObject EMPTY_JSON = new JSONObject();

	private static final long MAX_TIME_IN_THE_PAST = 3000;

	private final NotificationServiceState state;

	private final long listenStartTime;

	private float interval = 0.5f;
	private int duration = 10;

	private volatile boolean canceled = false;

	private JSON result;

	public NotificationService(NotificationServiceState state) {
		this.state = state;
		this.listenStartTime = System.currentTimeMillis();
	}

	public void setDuration(int seconds) {
		this.duration = seconds;
	}

	@JavaScriptMethod
	public void reset() {
		this.state.reset();
	}

	@JavaScriptMethod
	public void cancel() {
		this.canceled = true;
	}

	@Override
	protected void compute() throws Exception {
		int duration = this.duration;
		float interval = this.interval;

		int iterations = (int) Math.ceil(duration / interval);

		long startTime = System.currentTimeMillis();

		outer: while ((System.currentTimeMillis() - startTime) / 1000 < duration && --iterations >= 0 && !this.canceled
				&& !this.canceled()) {

			for (NotificationEventHandler.Notification notification : NotificationEventHandler.get()
					.getRecentNotifications()) {
				if (this.checkNotification(notification)) {
					break outer;
				}
			}

			Thread.sleep((int) Math.ceil(interval * 1000));
		}
	}

	private boolean checkNotification(NotificationEventHandler.Notification notification) {
		if ((notification.time - this.listenStartTime) >= -MAX_TIME_IN_THE_PAST
				&& this.state.checkNewNotification(notification)) {
			JSONObject obj = new JSONObject();
			obj.put("notification", notification.event.name);

			this.setResult(obj);

			return true;
		}

		return false;
	}

	private synchronized void setResult(JSONObject data) {
		this.result = data;
	}

	@Override
	protected synchronized JSON data() {
		return this.result != null ? this.result : EMPTY_JSON;
	}

}
