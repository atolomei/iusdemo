/*
 * Odilon Object Storage
 * (c) kbee 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.demo.util;


import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import io.demo.Logger;

 
 
 


/**
 * 
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
public class NumberFormatter {


	static private Logger logger = Logger.getLogger(NumberFormatter.class.getName());

	static long KB = 1024;
	static long MB = 1000 * KB;
	static long GB = 1000 * MB;
 
	
	static private NumberFormat eng_integer_nf;
	static private NumberFormat eng_float_nf;

	static private NumberFormat es_integer_nf;
	static private NumberFormat es_float_nf;
	
	static {
		
		eng_integer_nf = NumberFormat.getInstance(Locale.ENGLISH);
		eng_integer_nf.setMinimumFractionDigits(0);
		eng_integer_nf.setMaximumFractionDigits(0);
		eng_integer_nf.setRoundingMode(RoundingMode.HALF_UP);
		
		eng_float_nf = NumberFormat.getInstance(Locale.ENGLISH);
		eng_float_nf.setMinimumFractionDigits(2);
		eng_float_nf.setMaximumFractionDigits(2);
		eng_float_nf.setRoundingMode(RoundingMode.HALF_UP);
		
		es_integer_nf = NumberFormat.getInstance(Locale.forLanguageTag("es"));
		es_integer_nf.setMinimumFractionDigits(0);
		es_integer_nf.setMaximumFractionDigits(0);
		es_integer_nf.setRoundingMode(RoundingMode.HALF_UP);
		
		es_float_nf =NumberFormat.getInstance(Locale.forLanguageTag("es"));
		es_float_nf.setMinimumFractionDigits(2);
		es_float_nf.setMaximumFractionDigits(2);
		es_float_nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	static public String formatFileSize(long size) {
		return formatFileSize(size, Locale.getDefault());
	}
	
	static public String formatFileSize(long size, String css) {
		return  formatFileSize(size, Locale.getDefault(), css); 
	}
	
	static public String formatFileSize(long size, Locale locale) {
				return  formatFileSize(size, locale, null); 
	}
	
	static public String formatNumber(long number) {
			return formatNumber( number, Locale.getDefault());
	}
	
	static public String formatNumber(double number) {
		return formatNumber( number, Locale.getDefault());
	}
	

	static public String formatNumber(int number) {
		return formatNumber( number, Locale.getDefault());
	}


	static public String formatNumber(float number) {
		return formatNumber( number, Locale.getDefault());
	}

	
	static public String formatNumber(long number, Locale locale) {
		NumberFormat integer_nf;
		if (locale.equals(Locale.forLanguageTag("es")))
			integer_nf = es_integer_nf;
		else 
			integer_nf = eng_integer_nf;
		return  integer_nf.format(number);
	}
	
	static public String formatNumber(int number,  Locale locale) {
		NumberFormat integer_nf;
		if (locale.equals(Locale.forLanguageTag("es")))
			integer_nf = es_integer_nf;
		else 
			integer_nf = eng_integer_nf;
		return  integer_nf.format(number);

	}
	
	static public String formatNumber(float number,  Locale locale) {
		NumberFormat float_nf;
		if (locale.equals(Locale.forLanguageTag("es")))
			float_nf = es_integer_nf;
		else 
			float_nf = eng_integer_nf;
		return float_nf.format(number);
	}
	
	static public String formatNumber(double number,  Locale locale) {
		NumberFormat float_nf;
		
		if (locale.equals(Locale.forLanguageTag("es"))) {
			float_nf = es_float_nf;
		}
		else {
			float_nf = eng_float_nf;
		}
		return  float_nf.format(number);
	}
	
	
	static final double  d_ONE_SECOND_MS = 1000.0;
	static final double  d_ONE_MINUTE_MS =  d_ONE_SECOND_MS * 60.0;
	static final double  d_ONE_HOUR_MS =  d_ONE_MINUTE_MS * 60.0;

	
	
	static final long ONE_SECOND_MS = 1000;
	static final long ONE_MINUTE_MS =  ONE_SECOND_MS * 60;
	static final long ONE_HOUR_MS =  ONE_MINUTE_MS * 60;
	
	
	static public String formatDuration(long durationMs) {
		return formatDuration( durationMs, Locale.getDefault());
	}
	
	static public String formatDuration(long durationMs, Locale locale) {
		

		// return String.valueOf( Math.round(  Double.valueOf(durationMs) / 1.0)) + " ms";

		
		// return String.valueOf( Math.round(  Double.valueOf(durationMs) / 1000.0)) + " secs";
		
				
		
		if (durationMs<ONE_SECOND_MS) {
			return formatNumber(durationMs, locale)+ " ms";
		}
		
		if (durationMs<ONE_MINUTE_MS) {
			double seconds = Double.valueOf(durationMs) / d_ONE_SECOND_MS;
			return formatNumber(   Math.round(seconds), locale)+ " secs";
		}
	
		if (durationMs<ONE_HOUR_MS) {
			
			double dec_minutes = Double.valueOf(durationMs) / d_ONE_MINUTE_MS;
			
			long minutes = Double.valueOf( Math.floor(dec_minutes)).longValue();
			
			
			long remainderMs = durationMs - minutes * ONE_MINUTE_MS;
			
			double d_remainderSecs =  Double.valueOf(remainderMs) / d_ONE_SECOND_MS;
			long remainderSecs = Math.round(d_remainderSecs);
				
			return formatNumber(minutes, locale ) + " min" + " " + formatNumber(Math.round(remainderSecs), locale )  + " secs";
		}
		
		String ret=formatNumber( Math.round( Double.valueOf(durationMs) / d_ONE_SECOND_MS), locale)+" secs";
		
		logger.debug(ret);
		
		return ret;
 
		
	}
	
	/**
	 * 
	 * @param size
	 * @return
	 */
	static public String formatFileSize(long size, Locale locale, String css) {
	
		NumberFormat integer_nf;
		NumberFormat float_nf;
		
		if (locale.equals(Locale.forLanguageTag("es"))) {
			integer_nf = es_integer_nf;
			float_nf = es_float_nf;
		}
		else {
			integer_nf = eng_integer_nf;
			float_nf = eng_float_nf;
		}

		String css_open =  css!=null? "<span class= \""+css+"\">":"";
		String css_close =   css!=null? "</span>" :"";
		
		if (size==0) return integer_nf.format(size).trim();
		if (size<0) return "n/a";
		if (size<KB) return integer_nf.format(size)+ css_open +" bytes" + css_close;
		if (size<MB) return integer_nf.format((double) size / (double) KB)+ css_open + " KB" + css_close;
		else if (size<GB) {
			if (size<99*MB) return   float_nf.format((double) size / (double) MB) + css_open + " MB" + css_close;   // 		return String.format("%6.2f MB", (double) size / (double) MB).trim();
			else			return integer_nf.format((double) size / (double) MB)+ css_open + " MB" + css_close;    //         return String.format("%6.0f MB", (double) size / (double) MB).trim();
		}
		else 
			return float_nf.format((double) size / (double) GB)+ css_open + " GB" + css_close;	//  return String.format("%6.2f GB", (double) size / (double) GB).trim();	
	}
	


	
}

