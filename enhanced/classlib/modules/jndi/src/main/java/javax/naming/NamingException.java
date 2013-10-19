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


package javax.naming;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A <code>NamingException</code> is the basic exception thrown by the naming
 * classes. There are numerous subclasses of it which are used to further
 * describe the type of error encountered.
 * <p>
 * A <code>NamingException</code> can hold information relating to an error 
 * encountered when trying to resolve a <code>Name</code>. It holds the two 
 * parts of the original name, firstly the part of the name which was 
 * successfully resolved, secondly the part of the name which could not be 
 * resolved.</p>
 * <p>
 * For example:<br />
 * ------------<br />
 * The resolved name could be something like http://www.apache.org where jndi has
 * successfully resolved the DNS name.
 * The part of the name which could not be resolved could be something like
 * java/classes.index.html where jndi could not resolve the file name.</p>
 * <p>
 * It can also refer to the object that is associated with the resolved name.</p>
 * <p>
 * Additionaly it can refer to another exception, which may be the root cause
 * of this exception.</p>
 * <p>
 * Multithreaded access to a <code>NamingException</code> instance is only 
 * safe when client code locks the object first.</p>
 * 
 */
public class NamingException extends Exception {

    /*
     * This constant is used during deserialization to check the J2SE version
     * which created the serialized object.
     */
    private static final long serialVersionUID = -1299181962103167177L;

    /*
     * -------------------------------------------------------------------
     * Instance variables
     * -------------------------------------------------------------------
     */

    /**
     * The resolved name. This may be null.
     */
    protected Name resolvedName = null;

    /**
     * The remaining name. This may be null.
     */
    protected Name remainingName = null;

    /**
     * The resolved object. This may be null.
     */
    protected Object resolvedObj = null;

    /**
     * The exception that caused this NamingException to be raised. This may be null.
     */
    protected Throwable rootException = null;

    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    /**
     * Constructs a <code>NamingException</code> instance 
     * with all data initialized to null.
     */
    public NamingException() {
        super();
    }

    /**
     * Constructs a <code>NamingException</code> instance
     * with the specified message. All other fields are initialized to null.
     * 
     * @param s The detail message for the exception. It may be null. 
     */
    public NamingException(String s) {
        super(s);
    }

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Returns the message passed in as a param to the constructor.
     * This may be null.
     * 
     * @return the message passed in as a param to the constructor.
     */
    public String getExplanation() {
        return super.getMessage();
    }

    /**
     * Appends the supplied string to the <code>Name</code> held as the 
     * remaining name. The string may be null.
     * 
     * @param s the string to append to the remaining Name.
     * @throws  IllegalArgumentException if appending the supplied
     * String s causes the name to become invalid.
     */
    public void appendRemainingComponent(String s) {
        if (null != s) {
            try {
                if (null == remainingName) {
                    remainingName = new CompositeName(""); //$NON-NLS-1$
                }
                remainingName = remainingName.add(s);
            } catch (InvalidNameException e) {
                throw new IllegalArgumentException("Found invalid name, reason: " + e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Returns the remaining name. This may be null.
     * 
     * @return the remaining name. This may be null.
     */
    public Name getRemainingName() {
        return remainingName;
    }

    /**
     * Returns the resolved name. This may be null.
     * 
     * @return the resolved name. This may be null.
     */
    public Name getResolvedName() {
        return resolvedName;
    }

    /**
     * Returns the resolved object. This may be null.
     * 
     * @return the resolved object. This may be null.
     */
    public Object getResolvedObj() {
        return resolvedObj;
    }

    /**
     * Sets the resolved name to the specified name. This may be null. 
     * The resolved name details must not change even if the original 
     * <code>Name</code> itself changes.
     * 
     * @param name the resolved name to set.
     */
    public void setResolvedName(Name name) {
        resolvedName = null == name ? null : (Name) name.clone();
    }

    /**
     * Sets the remaining name to the specified n. This may be null. 
     * The remaining name details must not change even if the original 
     * <code>Name</code> itself changes.
     * 
     * @param name the remaining name to set.
     */
    public void setRemainingName(Name name) {
        remainingName = null == name ? null : (Name) name.clone();
    }

    /**
     * Sets the resolved object to the specified o. This may be null.
     * 
     * @param o the resolved object to set.
     */
    public void setResolvedObj(Object o) {
        resolvedObj = o;
    }

    /**
     * Appends the elements of the supplied <code>Name</code> n to the 
     * <code>Name</code> held as the remaining name. The <code>Name</code> n 
     * may be null or may be empty.
     * 
     * @param n the name to append to the remaining name.
     * @throws IllegalArgumentException if appending the supplied
     * <code>Name</code> n causes the name to become invalid.
     */
    public void appendRemainingName(Name n) {
        if (null != n) {
            try {
                if (null == remainingName) {
                    remainingName = new CompositeName(""); //$NON-NLS-1$
                }
                remainingName = remainingName.addAll(n);
            } catch (InvalidNameException e) {
                throw new IllegalArgumentException("Found invalid name, reason: " + e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Returns the exception which caused this <code>NamingException</code> 
     * which may be null.
     * 
     * @return the exception which caused this <code>NamingException</code> 
     * which may be null.
     */
    public Throwable getRootCause() {
        return rootException;
    }

    /**
     * Sets the exception that caused this <code>NamingException</code>. 
     * It may be null. 
     * Ignore the supplied parameter if it is actually this exception.
     * 
     * @param t the exception that caused this <code>NamingException</code>.
     */
    public void setRootCause(Throwable t) {
        if (t != this) {
            rootException = t;
        }
    }

    /**
     * Returns the same details as the <code>toString()</code> method except 
     * that, if the <code>flag</code> is set to true, then details of the 
     * resolved object are also appended to the string. 
     * The actual format can be decided by the implementor.
     * 
     * @param flag Indicates if the resolved object need to be returned. 
     * 
     * @return the string representation of this <code>NamingException</code>.
     */
    public String toString(boolean flag) {
        return toStringImpl(flag);
    }

    /*
     * -------------------------------------------------------------------
     * Methods override parent class Exception
     * -------------------------------------------------------------------
     */

    /**
     * If there is a root exception associated with this 
     * <code>NamingException</code> then first print the class name and message
     * of this <code>NamingException</code>, followed by the text 
     * ". The stack trace of the root exception is: ", followed by the stack 
     * trace of the exception which caused this exception.
     * <p>
     * If there is no root exception associated with this 
     * <code>NamingException</code> then print the stack trace of this 
     * <code>NamingException</code>.</p>
     * <p>
     * The output from this goes to <code>System.err</code>.</p>
     */
    public void printStackTrace() {
        if (null != rootException) {
            System.err.print(super.toString());
            System.err.print(". The stack trace of the root exception is: "); //$NON-NLS-1$
            rootException.printStackTrace();
        } else {
            super.printStackTrace();
        }
    }

    /**
     * Performs the same as <code>printStackTrace()</code> except the output 
     * goes to the specified <code>PrintStream</code> p.
     * 
     * @param p the <code>PrintStream</code> to which the stack trace is 
     * printed.
     */
    public void printStackTrace(PrintStream p) {
        if (null != rootException) {
            p.print(super.toString());
            p.print(". The stack trace of the root exception is: "); //$NON-NLS-1$
            rootException.printStackTrace(p);
        } else {
            super.printStackTrace(p);
        }
    }

    /**
     * Performs the same as <code>printStackTrace()</code> except the output 
     * goes to the specified <code>PrintWriter</code> p.
     * 
     * @param p the <code>PrintWrite</code> to which the stack trace is 
     * printed.
     */
    public void printStackTrace(PrintWriter p) {
        if (null != rootException) {
            p.print(super.toString());
            p.print(". The stack trace of the root exception is: "); //$NON-NLS-1$
            rootException.printStackTrace(p);
        } else {
            super.printStackTrace(p);
        }
    }
	
    /*
     *  (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
	public Throwable getCause() {
		return super.getCause();
	}

    /*
     *  (non-Javadoc)
     * @see java.lang.Throwable#initCause(Throwable)
     */
	public Throwable initCause(Throwable cause) {
		return super.initCause(cause);
	}

    /*
     * -------------------------------------------------------------------
     * Methods override parent class Object
     * -------------------------------------------------------------------
     */

    /**
     * Returns the string representation of this <code>NamingException</code>.
     * The string contains the string representation of this exception together
     * with details of the exception which caused this and any remaining 
     * portion of the <code>Name</code>. 
     * <p>
     * The actual format can be decided by the implementor.</p>
     * 
     * @return the string
     */
    public String toString() {
        return this.toStringImpl(false);
    }

    private String toStringImpl(boolean flag) {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        if (null != rootException) {
            sb.append(" [Root exception is ").append(rootException.toString()).append( //$NON-NLS-1$
                "]"); //$NON-NLS-1$
        }
        if (null != remainingName) {
            sb.append("; Remaining name: '").append(remainingName.toString()).append("'");  //$NON-NLS-1$//$NON-NLS-2$
        }
        if (flag && null != resolvedObj) {
            sb.append("; Resolved object: '").append(resolvedObj.toString()).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return sb.toString();
    }

}


