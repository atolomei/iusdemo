package io.demo.results;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Date range options for the search results
 * (based on DateRange from the DellemuseServerApp project).
 */
public enum DateRange {

	ALL("Todos"),
	LAST_3_MONTHS("Últimos 3 meses"),
	LAST_YEAR("Último año"),
	LAST_2_YEARS("Últimos 2 años"),
	LAST_5_YEARS("Últimos 5 años"),
	LAST_10_YEARS("Últimos 10 años"),
	LAST_20_YEARS("Últimos 20 años");

	private final String label;

	DateRange(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/** default selection for the selector */
	public static DateRange getDefault() {
		return LAST_5_YEARS;
	}

	/** Returns the DateRange with the given ordinal, or the default if out of range. */
	public static DateRange fromOrdinal(int ordinal) {
		DateRange[] values = values();
		return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : getDefault();
	}

	/**
	 * Returns the start {@link OffsetDateTime} for this range (inclusive),
	 * or {@code null} if the range is {@link #ALL}.
	 */
	public OffsetDateTime getFrom(ZoneId zoneId) {
		LocalDate today = LocalDate.now(zoneId);
		switch (this) {
		case LAST_3_MONTHS:
			return today.minusMonths(3).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_YEAR:
			return today.minusYears(1).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_2_YEARS:
			return today.minusYears(2).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_5_YEARS:
			return today.minusYears(5).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_10_YEARS:
			return today.minusYears(10).atStartOfDay(zoneId).toOffsetDateTime();
		case LAST_20_YEARS:
			return today.minusYears(20).atStartOfDay(zoneId).toOffsetDateTime();
		case ALL:
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		return label;
	}
}
