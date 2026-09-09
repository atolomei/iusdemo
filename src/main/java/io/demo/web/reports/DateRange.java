package io.demo.web.reports;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Date range options for reports.
 */
public enum DateRange {

	TODAY("today"),
	YESTERDAY("yesterday"),
	LAST_7_DAYS("last-7-days"),
	LAST_30_DAYS("last-30-days"),
	LAST_12_MONTHS("last-12-months"),
	ALL("all");

	private final String key;

	DateRange(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	/**
	 * Returns the start {@link OffsetDateTime} for this range (inclusive),
	 * or {@code null} if the range is {@link #ALL}.
	 */
	public OffsetDateTime getFrom(ZoneId zoneId) {
		LocalDate today = LocalDate.now(zoneId);
		switch (this) {
		case TODAY:
			return today.atStartOfDay(zoneId).toOffsetDateTime();
		case YESTERDAY:
			return today.minusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_7_DAYS:
			return today.minusDays(7).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_30_DAYS:
			return today.minusDays(30).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_12_MONTHS:
			return today.minusMonths(12).atStartOfDay(zoneId).toOffsetDateTime();
		case ALL:
		default:
			return null;
		}
	}

	/**
	 * Returns the exclusive upper bound {@link OffsetDateTime} for this range,
	 * or {@code null} if there is no upper bound (i.e. up to now).
	 */
	public OffsetDateTime getTo(ZoneId zoneId) {
		if (this == YESTERDAY) {
			LocalDate today = LocalDate.now(zoneId);
			return today.atStartOfDay(zoneId).toOffsetDateTime();
		}
		return null;
	}
}
