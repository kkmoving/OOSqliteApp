/** 
 * Filename:    OOTableListener.java
 * Description:
 * @author:     kkmoving
 * @version:    1.0
 * Create at:   2014-1-23 上午10:25:00
 * 
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2014-1-23     kkmoving    1.0         1.0 Version
 */
package com.kkmoving.oosqlite;

public interface OOTableListener {
	void onReady();
    void onCreate();
	void onUpgrade(final int oldVersion, final int newVersion);
	void onDowngrade(final int oldVersion, final int newVersion);
}
