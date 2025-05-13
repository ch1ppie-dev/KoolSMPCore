package eu.koolfreedom.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public enum TimeOffset
{
	MILLISECOND(1, "ms"),
	SECOND(1000, "s"),
	MINUTE(60 * SECOND.multiplier, "m"),
	HOUR(60 * MINUTE.multiplier, "h"),
	DAY(24 * HOUR.multiplier, "d"),
	WEEK(7 * DAY.multiplier, "w"),
	MONTH(30 * DAY.multiplier, "d"),
	YEAR(365 * DAY.multiplier, "y"),
	CENTURY(10 * YEAR.multiplier, "c"),
	MILLENNIUM(10 * CENTURY.multiplier, "mil");

	private static final Pattern pattern = Pattern.compile("([0-9]+)([a-z]{1,3})");

	private final int multiplier;
	private final String delimiter;

	TimeOffset(int multiplier, String delimiter)
	{
		this.multiplier = multiplier;
		this.delimiter = delimiter;
	}

	public long multiply(long amount)
	{
		return amount * multiplier;
	}

	public static long getOffset(String str)
	{
		Matcher matcher = pattern.matcher(str);

		AtomicLong amount = new AtomicLong(0);
		matcher.results().forEach(result ->
		{
			int amt = Integer.parseInt(result.group(1));
			String delimiter = result.group(2);

			Optional<TimeOffset> ouch = Arrays.stream(values())
					.filter(lol -> Objects.nonNull(lol.delimiter))
					.filter(lol -> lol.delimiter.equalsIgnoreCase(delimiter)).findAny();

			if (ouch.isEmpty())
			{
				return;
			}

			amount.set(amount.get() + ouch.get().multiply(amt));
		});

		return amount.get();
	}
}
