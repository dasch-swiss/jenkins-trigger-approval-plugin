package swiss.dasch.plugins.triggerapproval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
	private static final Pattern REGEX_SPECIAL_CHARACTERS_PATTERN = Pattern
			.compile(String.format("[%s]", "<>()[]{}\\^$!?|+-=.*".replaceAll(".", "\\\\$0")));

	private static final Pattern JOB_URL_PATTERN = Pattern.compile("(?:^|/)(job/[^/]+/)$");

	public static String escape(String string) {
		return REGEX_SPECIAL_CHARACTERS_PATTERN.matcher(string).replaceAll("\\\\$0");
	}

	public static String getDefaultRegexFromTaskURL(String url) {
		Matcher matcher = JOB_URL_PATTERN.matcher(url);
		if (matcher.find() && matcher.groupCount() == 1) {
			url = matcher.group(0);
		}
		return String.format("(^|/)%s$", RegexUtil.escape(url));
	}
}
