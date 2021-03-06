/*This code is copyrighted by Teknowledge (c) 2003.
It is released underthe GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
Users ofthis code also consent, by use of this code, to credit Teknowledge in any
writings, briefings,publications, presentations, or other representations of any
software which incorporates, builds on, or uses this code.*/ 

 
package jpl;



//----------------------------------------------------------------------/
// JPLException
/**
 * This base class for JPL Exceptions is thrown from various methods
 * in JPL.  Consult the documentation for situations in which this
 * method has occasion to be thrown.
 * <hr><i>
 * Copyright (C) 1998  Fred Dushin<p>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.<p>
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library Public License for more details.<p>
 * </i><hr>
 * @author  Fred Dushin <fadushin@syr.edu>
 * @version $Revision: 1.1.1.1 $
 */
// Implementation notes:  
// 
//----------------------------------------------------------------------/
public class
JPLException extends RuntimeException
{
	public
	JPLException()
	{
		super();
	}
	
	public
	JPLException( java.lang.String s )
	{
		super( s );
	}
}
