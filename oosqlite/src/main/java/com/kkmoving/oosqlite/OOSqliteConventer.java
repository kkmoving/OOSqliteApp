/** 
 * Filename:    LeSqliteConventer.java
 * Description:  
 * @author:     kkmoving
 * @version:    1.0
 * Create at:   2014-1-14 下午3:26:12
 * 
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2014-1-14     kkmoving    1.0         1.0 Version
 */
package com.kkmoving.oosqlite;

import java.util.Map;

interface OOSqliteConventer {
	
	Object convertToDb(OOColumn columnDef, OOSqliteEntity entity);
	
	OOSqliteEntity convertFromDb(Class cls, Map<OOColumn, Object> colValueMap);

}
