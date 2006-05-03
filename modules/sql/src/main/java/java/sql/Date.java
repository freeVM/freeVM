/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
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


package java.sql;

import java.text.SimpleDateFormat;
import java.lang.IllegalArgumentException;
import java.text.ParseException;
import java.io.Serializable;

/**
 * A Date class which can consume and produce dates in SQL Date format.
 * <p>
 * The SQL date format represents a date as yyyy-mm-dd.  Note that this date format only deals with year, month
 * and day values.  There are no values for hours, minutes, seconds.
 * <p>
 * This contrasts with regular java.util.Date values, which include time values for hours, minutes, seconds,
 * milliseconds.
 * <p>
 * Time points are handled as millisecond values - milliseconds since the epoch, January 1st 1970, 
 * 00:00:00.000 GMT.  Time values passed to the java.sql.Date class are "normalised" to the time 
 * 00:00:00.000 GMT on the date implied by the time value.
 */
public class Date extends java.util.Date {

	private static final long serialVersionUID = 1511598038487230103L;
	
	/**
	 * @deprecated Please use the constructor Date( long )
	 * Constructs a Date object corresponding to the supplied Year, Month and Day. 
	 * @param theYear the year, specified as the year minus 1900.  Must be in the range 0 to 8099.
	 * @param theMonth the month, specified as a number with 0 = January.  Must be in the range 0 to 11.
	 * @param theDay the day in the month.  Must be in the range 1 to 31.
	 */
	public Date( int theYear, int theMonth, int theDay ) {
		super( theYear, theMonth, theDay );
	} // end method Date(int, int, int )
	
	/**
	 * Creates a Date which corresponds to the day implied by the supplied theDate milliseconds 
	 * time value.
	 * 
	 * @param theDate - a time value in milliseconds since the epoch - January 1 1970 00:00:00 GMT. 
	 * The time value (hours, minutes, seconds, milliseconds) stored in the Date object is adjusted 
	 * to correspond to 00:00:00 GMT on the day implied by the supplied time value. 
	 */
	public Date( long theDate ) {
		super( normalizeTime( theDate ) );
		// System.out.println("Clear version of java.sql.Date");
	} // end method Date( long )
	
	/**
	 * @deprecated
	 * This method is deprecated and must not be used.  SQL Date values do not have an hours component.
	 * @return 0
	 * @throws IllegalArgumentException if this method is called
	 */
	public int getHours() {
		if( true ) {
			throw new IllegalArgumentException();
		} // end if
		return 0;
	} // end method getHours
	
	/**
	 * @deprecated
	 * This method is deprecated and must not be used.  SQL Date values do not have a minutes component.
	 * @return 0
	 * @throws IllegalArgumentException if this method is called
	 */
	public int getMinutes() {
		if( true ) {
			throw new IllegalArgumentException();
		} // end if
		return 0;
	} // end method getMinutes()
	
	/**
	 * @deprecated
	 * This method is deprecated and must not be used.  SQL Date values do not have a seconds component.
	 * @return 0
	 * @throws IllegalArgumentException if this method is called
	 */
	public int getSeconds() {
		if( true ) {
			throw new IllegalArgumentException();
		} // end if
		return 0;
	} // end method getSeconds
	
	/**
	 * @deprecated
	 * This method is deprecated and must not be used.  SQL Date values do not have an hours component.
	 * @param theHours the number of hours to set
	 * @throws IllegalArgumentException if this method is called
	 */
	public void setHours( int theHours ) {
		if( true ) {
			throw new IllegalArgumentException();
		} // end if
	} // end method setHours( int )
	
	/**
	 * @deprecated
	 * This method is deprecated and must not be used.  SQL Date values do not have a minutes component.
	 * @param theMinutes the number of minutes to set
	 * @throws IllegalArgumentException if this method is called
	 */
	public void setMinutes( int theMinutes ) {
		if( true ) {
			throw new IllegalArgumentException();
		} // end if
	} // end method setMinutes( int )
	
	/**
	 * @deprecated
	 * This method is deprecated and must not be used.  SQL Date values do not have a seconds component.
	 * @param theSeconds the number of seconds to set
	 * @throws IllegalArgumentException if this method is called
	 */
	public void setSeconds( int theSeconds ) {
		if( true ) {
			throw new IllegalArgumentException();
		} // end if
	} // end method setHours( int )
	
	/**
	 * Sets this date to a date supplied as a milliseconds value.  The date is set based
	 * on the supplied time value after removing any time elements finer than a day, based 
	 * on zero GMT for that day. 
	 * @param theTime the time in milliseconds since the Epoch
	 */
	public void setTime( long theTime ) {
		// Store the Date based on the supplied time after removing any time elements
		// finer than the day based on zero GMT
		super.setTime( normalizeTime( theTime ) );
	} // end method setTime( int )
	
	/**
	 * Produces a string representation of the Date in SQL format
	 * @return a string representation of the Date in SQL format - "yyyy-mm-dd".
	 */
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" ); //$NON-NLS-1$
		
		return dateFormat.format( this );
	} // end method toString()
	
	/**
	 * Creates a Date from a string representation of a date in SQL format.
	 * @param theString the string representation of a date in SQL format - "yyyy-mm-dd".
	 * @return the Date object 
	 * @throws IllegalArgumentException if the format of the supplied string does not match the
	 * SQL format.
	 */
	public static Date valueOf( String theString ) {
		java.util.Date aDate;
		
		if( theString == null ) throw new IllegalArgumentException();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" ); //$NON-NLS-1$
		try {
			aDate = dateFormat.parse( theString );
		} catch( ParseException pe ) {
			throw new IllegalArgumentException();
		} // end try
		
		return new Date( aDate.getTime() );
	} // end valueOf( String ) 

	/*
	 * Private method which normalizes a Time value, removing all low significance
	 * digits corresponding to milliseconds, seconds, minutes and hours, so that the
	 * returned Time value corresponds to 00:00:00 GMT on a particular day.
	 *
	 */
	private static long normalizeTime( long theTime ) {
		return theTime;
	} // end method normalizeTime( long )
} /* end class Date */


