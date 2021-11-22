package com.emc.documentum.rest.extension.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.emc.documentum.rest.binding.SerializableField;
import com.emc.documentum.rest.binding.SerializableType;
import com.emc.documentum.rest.model.PersistentObject;

@SerializableType(value = "rows", xmlNSPrefix = "dm", xmlNS = "http://identifiers.emc.com/vocab/documentum")
public class RegisteredTableRows  extends PersistentObject{
	
	@SerializableField("rows")
	protected List<RegisteredTableRow> row=new ArrayList<>();

	public Collection<RegisteredTableRow> getTableRow() {
		return new ArrayList<>(this.row);
	}
}
