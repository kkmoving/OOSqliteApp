/** 
 * Filename:    OOSqliteException.java
 * Description:  
 * @author:     kkmoving
 * @version:    1.0
 * Create at:   2014-1-14 下午12:42:16
 * 
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2014-1-14     kkmoving    1.0         1.0 Version
 */
package com.kkmoving.oosqlite;

class OOSqliteException extends RuntimeException {

	public OOSqliteException() {
		super();
	}
	
	public OOSqliteException(String msg) {
		super(msg);
	}
}
