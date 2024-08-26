package swiss.dasch.plugins.triggerapproval;

import java.util.regex.Pattern;

public class RegexUtil {
	private static final Pattern REGEX_SPECIAL_CHARACTERS_PATTERN = Pattern
			.compile(String.format("[%s]", "<>()[]{}\\^$!?|+-=.*".replaceAll(".", "\\\\$0")));

	public static String escape(String string) {
		return REGEX_SPECIAL_CHARACTERS_PATTERN.matcher(string).replaceAll("\\\\$0");
	}
}
