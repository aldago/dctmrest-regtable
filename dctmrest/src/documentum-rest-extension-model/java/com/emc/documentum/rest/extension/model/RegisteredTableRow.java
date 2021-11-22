package com.emc.documentum.rest.extension.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.emc.documentum.rest.binding.SerializableField;
import com.emc.documentum.rest.binding.SerializableType;
import com.emc.documentum.rest.model.Attribute;
import com.emc.documentum.rest.model.PersistentObject;

@SerializableType(value = "row", xmlNSPrefix = "dm", xmlNS = "http://identifiers.emc.com/vocab/documentum")
public class RegisteredTableRow extends PersistentObject{

	@SerializableField("row")
	protected List<Attribute<?>> tablerow=new ArrayList<>();

	public Collection<Attribute<?>> getRow() {
		return new ArrayList<>(this.tablerow);
	}
}
