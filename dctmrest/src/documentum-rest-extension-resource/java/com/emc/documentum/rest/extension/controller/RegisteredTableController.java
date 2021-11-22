package com.emc.documentum.rest.extension.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.documentum.fc.common.DfException;
import com.emc.documentum.rest.config.PagingConfig;
import com.emc.documentum.rest.constant.QueryType;
import com.emc.documentum.rest.controller.AbstractController;
import com.emc.documentum.rest.dfc.ContextSessionManager;
import com.emc.documentum.rest.dfc.query.QueryEngine;
import com.emc.documentum.rest.error.RestErrorBuilder;
import com.emc.documentum.rest.error.RestServiceException;
import com.emc.documentum.rest.extension.model.RegisteredTableRow;
import com.emc.documentum.rest.extension.model.RegisteredTableRowSetWhere;
import com.emc.documentum.rest.extension.model.RegisteredTableRowSuffix;
import com.emc.documentum.rest.extension.model.RegisteredTableRows;
import com.emc.documentum.rest.http.SupportedMediaTypes;
import com.emc.documentum.rest.http.UriInfo;
import com.emc.documentum.rest.http.annotation.RequestUri;
import com.emc.documentum.rest.http.parameter.ContentParameter;
import com.emc.documentum.rest.model.AtomFeed;
import com.emc.documentum.rest.model.Attribute;
import com.emc.documentum.rest.model.QueryResultItem;
import com.emc.documentum.rest.model.RestError;
import com.emc.documentum.rest.model.batch.annotation.BatchProhibition;
import com.emc.documentum.rest.paging.Page;
import com.emc.documentum.rest.utils.DQLTypeExtrator;
import com.emc.documentum.rest.view.annotation.ResourceViewBinding;
import com.emc.documentum.rest.view.impl.DocumentView;
import com.emc.documentum.rest.view.impl.QueryResultFeedView;
import com.emc.documentum.rest.view.impl.QueryResultItemView;

@Controller("registeredtable")
@ResourceViewBinding({QueryResultFeedView.class, QueryResultItemView.class})
@RequestMapping({"/repositories/{repositoryName}/registeredtable/{table_name}/row"})
@BatchProhibition
public class RegisteredTableController extends AbstractController{

	private Set<String> errorsWithoutDetails = (Set<String>)ImmutableSet.of("E_INVALID_QUERY_TABLE_NOT_FOUND");
	
	@Autowired
	private QueryEngine queryEngine;

	@Autowired
	ContextSessionManager contextSessionManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={SupportedMediaTypes.APPLICATION_VND_DCTM_XML_STRING,SupportedMediaTypes.APPLICATION_VND_DCTM_JSON_STRING})
	@ResponseBody
	public AtomFeed getRegisteredTable(@PathVariable("repositoryName") String repositoryName, @PathVariable("table_name") String table_name, @ModelAttribute ContentParameter contentParam, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "items-per-page", required = false) Integer itemsPerPage, @RequestHeader(value="If-None-Match", required=false) String noneMatchStr, @RequestUri UriInfo uriInfo)
			throws DfException{

		String dql="select * from dm_dbo."+table_name;
		itemsPerPage = Integer.valueOf(getItemsPerPage(itemsPerPage));
		int offset = (page - 1) * itemsPerPage.intValue();
		int limit = itemsPerPage.intValue();
		List<QueryResultItem> resultSet = this.queryEngine.execute(QueryResultItem.class, dql, QueryType.READ, offset, limit);
		Page<QueryResultItem> pagedResultSet = new Page(resultSet, null, page, itemsPerPage.intValue());
		Map<String, Object> others = new HashMap<>();
		others.put("is_raw", Boolean.valueOf(false));
		Collection<String> dqlTypes = DQLTypeExtrator.extract(dql);
		others.put("dql_types", dqlTypes);

		return getRenderedPage(repositoryName, pagedResultSet, false, true, uriInfo, others);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method={RequestMethod.POST}, produces={SupportedMediaTypes.APPLICATION_VND_DCTM_XML_STRING,SupportedMediaTypes.APPLICATION_VND_DCTM_JSON_STRING,MediaType.APPLICATION_ATOM_XML_VALUE,MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@ResourceViewBinding({DocumentView.class})
	public AtomFeed insertRow(@PathVariable("repositoryName") String repositoryName, @PathVariable("table_name") String table_name, @RequestBody final RegisteredTableRows rowObject, @RequestUri UriInfo uriInfo)
			throws DfException{
		
		List<QueryResultItem> resultSet = null;
		Map<String, Object> others = new HashMap<>();
		others.put("is_raw", Boolean.valueOf(false));
		
		for (RegisteredTableRow row:rowObject.getTableRow()) {
			String dql=getInsertFormattedDQL("insert into dm_dbo." + table_name + " ", table_name, row.getRow());
			
			resultSet = this.queryEngine.execute(QueryResultItem.class, dql, QueryType.QUERY, 0, 1);
			Collection<String> dqlTypes = DQLTypeExtrator.extract(dql);
			others.put("dql_types", dqlTypes);
		}

		Page<QueryResultItem> pagedResultSet = new Page(resultSet, null, 1, 100);
		return getRenderedPage(repositoryName, pagedResultSet, false, true, uriInfo, others);
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method={RequestMethod.PUT}, produces={SupportedMediaTypes.APPLICATION_VND_DCTM_XML_STRING,SupportedMediaTypes.APPLICATION_VND_DCTM_JSON_STRING,MediaType.APPLICATION_ATOM_XML_VALUE,MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ResourceViewBinding({DocumentView.class})
	public AtomFeed updateRow(@PathVariable("repositoryName") String repositoryName, @PathVariable("table_name") String table_name, @RequestBody final RegisteredTableRowSuffix whereObject, @RequestUri UriInfo uriInfo)
			throws DfException{
		
		List<QueryResultItem> resultSet = null;
		Map<String, Object> others = new HashMap<>();
		others.put("is_raw", Boolean.valueOf(false));
		
		for (RegisteredTableRowSetWhere suffix:whereObject.getSuffix()) {
			String dql="update dm_dbo." + table_name + " set " + suffix.getSet() + " where " + suffix.getWhere();
			
			resultSet = this.queryEngine.execute(QueryResultItem.class, dql+";", QueryType.QUERY, 0, 100);
			Collection<String> dqlTypes = DQLTypeExtrator.extract(dql);
			others.put("dql_types", dqlTypes);
		}
		Page<QueryResultItem> pagedResultSet = new Page(resultSet, null, 1, 100);
		
		return getRenderedPage(repositoryName, pagedResultSet, false, true, uriInfo, others);
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method={RequestMethod.DELETE}, produces={SupportedMediaTypes.APPLICATION_VND_DCTM_XML_STRING,SupportedMediaTypes.APPLICATION_VND_DCTM_JSON_STRING,MediaType.APPLICATION_ATOM_XML_VALUE,MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ResourceViewBinding({DocumentView.class})
	public AtomFeed deleteRow(@PathVariable("repositoryName") String repositoryName, @PathVariable("table_name") String table_name, @RequestBody final RegisteredTableRowSuffix whereObject, @RequestUri UriInfo uriInfo)
			throws DfException{
		
		List<QueryResultItem> resultSet = null;
		Map<String, Object> others = new HashMap<>();
		others.put("is_raw", Boolean.valueOf(false));
		
		for (RegisteredTableRowSetWhere suffix:whereObject.getSuffix()) {
			String dql="delete from dm_dbo." + table_name + " where " + suffix.getWhere();
			
			resultSet = this.queryEngine.execute(QueryResultItem.class, dql+";", QueryType.QUERY, 0, 100);
			Collection<String> dqlTypes = DQLTypeExtrator.extract(dql);
			others.put("dql_types", dqlTypes);
		}

		Page<QueryResultItem> pagedResultSet = new Page(resultSet, null, 1, 100);
		return getRenderedPage(repositoryName, pagedResultSet, false, true, uriInfo, others);
		
	}
	
	@SuppressWarnings("rawtypes")
	private String getInsertFormattedDQL(String basedql, String table_name, Collection<Attribute<?>> attrs) throws DfException {
		String dql="select column_datatype from dm_registered where object_name='%table_name%' and column_name='%column%' enable (ROW_BASED);";
		String fields="(";
		String values=" values (";
		for (Attribute attr:attrs) {
			List<QueryResultItem> resultSet = this.queryEngine.execute(QueryResultItem.class, dql.replace("%table_name%", table_name).replace("%column%", attr.getName()), QueryType.READ, 0, 1);
			
			String column_type=(String)resultSet.get(0).getAttributeByName("column_datatype");
			fields = fields + attr.getName() + ",";
			values = values + getFormattedDataValue(column_type, attr.getValue()) + ",";
		}
		
		fields=fields.substring(0,fields.length()-1) + ")";
		values=values.substring(0,values.length()-1) + ");";
		
		return basedql + fields + values;
	}
	
	private String getFormattedDataValue(String type, Object value) {
		switch (type) {
		case "integer":
			return String.valueOf(value);
		case "small integer":
			return String.valueOf(value);
		case "double":
			return String.valueOf(value);
		case "string":
			return "'"+value+"'";
		case "boolean":
			return "'"+value+"'";
		case "date":
			return "'"+value+"'";
		default:
			return "'"+value+"'";
		}
		
	}
	
	private int getItemsPerPage(Integer itemsPerPage) {
		itemsPerPage = (Integer)MoreObjects.firstNonNull(itemsPerPage, Integer.valueOf(PagingConfig.INSTANCE.defaultItemsPerPage()));
		return (itemsPerPage.intValue() > PagingConfig.INSTANCE.maxAllowedPageSize()) ? PagingConfig.INSTANCE.maxAllowedPageSize() : itemsPerPage.intValue();
	}

	@ResponseBody
	@ExceptionHandler({DfException.class})
	public RestError onDfException(DfException e, HttpServletRequest request, HttpServletResponse response) {
		RestServiceException exception = new RestServiceException(e, RegisteredTableController.class.getSimpleName(), request.getMethod());
		RestError error = exception.toError();
		int status = error.getStatus();
		if (status != HttpStatus.BAD_REQUEST.value() && status != HttpStatus.FORBIDDEN.value()) {
			error = newErrorWithoutDetails(HttpStatus.INTERNAL_SERVER_ERROR.value());
		} else if (this.errorsWithoutDetails.contains(error.getCode())) {
			error.clearDetails();
		} 
		response.setStatus(error.getStatus());
		return error;
	}

	private RestError newErrorWithoutDetails(int status) {
		return (new RestErrorBuilder(null)).status(status).code("E_DQL_QUERY_FAILED").build();
	}
}
