package io.demo.service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public enum DTFormatter {
	
	  Full (0, 							"EEE MMM dd HH:mm:ss z uuuu"),
	  Month_Day_Year_hh_mm_ss_zzz(1, 	"MMM d yyyy HH:mm:ss zz"),
	  Month_Day_Year_hh_mm(2, 			"MMM d yyyy h:mm a", "d MMM yyyy h:mm a"),
	  day_of_year(3, 					"MMM d yyyy", "d MMM yyyy"),
	  Month_Day_hr_min(4, ""),
	  Dateformat_short_this_year(5,""),
	  Hour_of_today_format(6,""),
	  Hour_of_day_this_week_format(7,""),
	  Am_pm_format(8,""),
	  Day_Month_Year_hh_mm_ss_zzz(9,""),
	  Day_Month_Year_hh_mm_ss(10, ""),
	  Dow_Month_Day_Year_hh_mm(11,"");
	
	  int code;
	  String pattern_eng;
	  String pattern_rest;
	
	  private DTFormatter( int code, String pattern_all) {
		  this.code=code;
		  this.pattern_eng=pattern_all;
		  this.pattern_rest=pattern_all;
	  }
	  
	  private DTFormatter( int code, String pattern_eng, String pattern) {
		  this.code=code;
		  this.pattern_eng=pattern_eng;
		  this.pattern_rest=pattern;
	  }
	  
	  public int getCode() {
		  return this.code;
	  }
	  
	  public String getPattern( Locale locale) {
		  if (locale.equals(Locale.ENGLISH))
			  return this.pattern_eng;
		  return this.pattern_rest;
	  }

	  public DateTimeFormatter getDateTimeFormatter(Locale locale) { 
		  return DateTimeFormatter.ofPattern( getPattern(locale), locale); 
	  }
 

}
