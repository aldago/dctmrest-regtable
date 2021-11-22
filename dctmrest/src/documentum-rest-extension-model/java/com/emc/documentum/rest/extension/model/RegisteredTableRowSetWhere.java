package com.emc.documentum.rest.extension.model;

import com.emc.documentum.rest.binding.SerializableField;
import com.emc.documentum.rest.binding.SerializableType;
import com.emc.documentum.rest.model.PersistentObject;

@SerializableType(value = "setwhere", xmlNSPrefix = "dm", xmlNS = "http://identifiers.emc.com/vocab/documentum")
public class RegisteredTableRowSetWhere extends PersistentObject{
	
	@SerializableField("set")
	protected String set;

	@SerializableField("where")
	protected String where;

	public String getSet() {
		return this.set;
	}
	public String getWhere() {
		return this.where;
	}
	

}
