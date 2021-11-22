package com.emc.documentum.rest.extension.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.emc.documentum.rest.binding.SerializableField;
import com.emc.documentum.rest.binding.SerializableType;
import com.emc.documentum.rest.model.PersistentObject;

@SerializableType(value = "predicate", xmlNSPrefix = "dm", xmlNS = "http://identifiers.emc.com/vocab/documentum")
public class RegisteredTableRowSuffix extends PersistentObject{
	
	@SerializableField("predicate")
	protected List<RegisteredTableRowSetWhere> suffix = new ArrayList<>();;

	public  Collection<RegisteredTableRowSetWhere> getSuffix() {
		return this.suffix;
	}
	

}
