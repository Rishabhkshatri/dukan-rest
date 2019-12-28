package com.example.dukanrest.rest;

import com.example.dukanrest.helper.Product;
import com.example.dukanrest.helper.ResStructure;

import com.example.dukanrest.connection.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;

import org.elasticsearch.index.reindex.DeleteByQueryRequest;

import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;

@RestController
public class Controller{

    @Autowired
    private RestHighLevelClient client;
	
	@Autowired
	ResStructure repobj;
	
	// API to update the price of a product given with the group and the price
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResStructure updateProduct(@RequestBody Product product){
		// require data check
		if(product.getProductId() == null || product.getGroupName() == null){
			repobj.setApiResponse("Error! Insufficient Data");
			return repobj;	
		}

		// preparing a bool query with filter type
		BoolQueryBuilder boolquery = new BoolQueryBuilder();
		boolquery.filter(new TermQueryBuilder("_id",product.getProductId()));
		boolquery.filter(new TermQueryBuilder("group_name.gtag",product.getGroupName()));

		// to access product_data using update_by_query api 
		UpdateByQueryRequest request = new UpdateByQueryRequest("product_data");
		request.setDocTypes("_doc");
		request.setRouting(product.getGroupName());
		request.setQuery(boolquery);
		request.setScript(
				new Script(
					ScriptType.INLINE,
					"painless",
					"ctx._source.product_mrp = "+product.getPrice()+"",
					Collections.emptyMap()
					)
			);

		// run the query
		BulkByScrollResponse response = null;
		try{
			response = client.updateByQuery(request, RequestOptions.DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
		}

		// prepare response
		if(response == null){
			repobj.setApiResponse("Something Went wrong");
		}
		else if(response.getUpdated() != 0){
			repobj.setApiResponse("Success");
		}
		else if(response.getBulkFailures().size() != 0){
			repobj.setApiResponse("Update Failed");
		}
		else if(response.getSearchFailures().size() != 0){
			repobj.setApiResponse("Search Failed");
		}
		else if(response.getUpdated() == 0){
			repobj.setApiResponse("No Match Found");
		}
       
		return repobj;
	}

	// API to add a new Product and also Group (if not exist)
	@RequestMapping(value = "/insert", method = RequestMethod.PUT)
	public ResStructure putProduct(@RequestBody Product product){
		// require data check
		if(product.getProductName() == null || product.getGroupName() == null || product.getModelName() == null || product.getProductSerialNo() == 0){
			repobj.setApiResponse("Error! Insufficient Data");
			return repobj;	
		}

		// preparing a bool query with filter type
		// to get the group id (if exist)
		BoolQueryBuilder boolquery = new BoolQueryBuilder();

		boolquery.filter(new TermQueryBuilder("group_name.gtag",product.getGroupName()));
		boolquery.filter(new TermQueryBuilder("doc_relation","group"));

		// get the search response
		SearchResponse groupresponse = groupSearch(boolquery, product.getGroupName());

		SearchHits hits = groupresponse.getHits();
		long totalHits = hits.getTotalHits();

		// create doc_relation object for product
		Map<String, Object> relationdoc = new HashMap<>();
		relationdoc.put("name", "product");
		String groupid = null;

		// get the group id if exist otherwise create new group
		if(totalHits > 0){
			SearchHit[] grouplist = hits.getHits();
			groupid = grouplist[0].getId();
		}else{
			// create new group
			IndexRequest addgroup = new IndexRequest("product_data", "_doc");
			Map<String, Object> groupdoc = new HashMap<>();
			groupdoc.put("group_name", product.getGroupName());
			groupdoc.put("is_active", true);
			groupdoc.put("doc_relation", "group");
			// set the route as group name
			addgroup.routing(product.getGroupName());
			addgroup.source(groupdoc);
			IndexResponse indexgroup = null;

			try{
				indexgroup = client.index(addgroup, RequestOptions.DEFAULT);
			}catch(Exception e){
				e.printStackTrace();
			}

			groupid = indexgroup.getId();
		}

		relationdoc.put("parent", groupid);

		// create object for new product 
		Map<String, Object> productdoc = new HashMap<>();
		productdoc.put("product_name", product.getProductName());
		productdoc.put("model_name", product.getModelName());
		productdoc.put("product_serial_no", product.getProductSerialNo());
		productdoc.put("product_mrp", product.getPrice());
		productdoc.put("group_name", product.getGroupName());
		productdoc.put("doc_relation", relationdoc);

		// to index new product using index api
		IndexRequest addproduct = new IndexRequest("product_data", "_doc");		
		addproduct.routing(product.getGroupName());
		addproduct.source(productdoc);

		// run the query
		IndexResponse indexproduct = null;
		try{
			indexproduct = client.index(addproduct, RequestOptions.DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
		}

		// prepare response
		if (indexproduct.getResult() == DocWriteResponse.Result.CREATED) {
			repobj.setApiResponse("Success");
		}else{
			repobj.setApiResponse("Failed");
		}

		return repobj;
	}

	// API to change the group of a product
	@RequestMapping(value = "/updateGroup", method = RequestMethod.POST)
	public ResStructure changeGroup(@RequestBody Product product){
		// require data check
		if(product.getProductId() == null || product.getGroupName() == null){
			repobj.setApiResponse("Error! Insufficient Data");
			return repobj;	
		}

		// get new group id
		BoolQueryBuilder boolquery = new BoolQueryBuilder();
		boolquery.filter(new TermQueryBuilder("group_name.gtag",product.getGroupName()));
		boolquery.filter(new TermQueryBuilder("doc_relation","group"));

		// get the search response
		SearchResponse groupresponse = groupSearch(boolquery, product.getGroupName());
		SearchHits hits = groupresponse.getHits();
		long totalHits = hits.getTotalHits();

		// if group not exist return with Error
		if(totalHits == 0){
			repobj.setApiResponse("Error! Unknown Group");
			return repobj;
		}

		SearchHit[] grouplist = hits.getHits();
		String groupid = grouplist[0].getId();

		// create product new relation
		Map<String, Object> relationdoc = new HashMap<>();
		relationdoc.put("name", "product");
		relationdoc.put("parent", groupid);

		// prepare query for product details
		BoolQueryBuilder productboolquery = new BoolQueryBuilder();
		productboolquery.filter(new TermQueryBuilder("_id",product.getProductId()));

		// get product search response
		SearchResponse productdetail = groupSearch(productboolquery, null);
		SearchHits producthits = productdetail.getHits();
		long prototal = producthits.getTotalHits();

		// if product not exist return with Error
		if(prototal == 0){
			repobj.setApiResponse("Error! Unknown Product");
			return repobj;
		}

		// get product deltail
		SearchHit[] productlist = producthits.getHits();
		Map<String, Object> productdoc = productlist[0].getSourceAsMap();
		// change the group_name and doc_relation
		productdoc.put("group_name", product.getGroupName());
		productdoc.put("doc_relation", relationdoc);

		// prepare inde api call
		IndexRequest addproduct = new IndexRequest("product_data", "_doc");		
		addproduct.routing(product.getGroupName());
		addproduct.source(productdoc);

		// run the query
		IndexResponse indexproduct = null;
		try{
			indexproduct = client.index(addproduct, RequestOptions.DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
		}

		// if product created then delete the old product
		if (indexproduct.getResult() == DocWriteResponse.Result.CREATED) {
			// prepare the delete_by_query api call
			DeleteByQueryRequest productdelete = new DeleteByQueryRequest("product_data");
			// query to match id of old product
			productdelete.setQuery(new TermQueryBuilder("_id", product.getProductId()));
			// run the query
			BulkByScrollResponse response = null;
			try{
				response = client.deleteByQuery(productdelete, RequestOptions.DEFAULT);
			}catch(Exception e){
				e.printStackTrace();
			}
			// prepare the response
			if(response == null){
				repobj.setApiResponse("Something Went wrong");
			}
			else if(response.getDeleted() != 0){
				repobj.setApiResponse("Success");
			}
			else if(response.getBulkFailures().size() != 0){
				repobj.setApiResponse("Deletion Failed");
			}
			else if(response.getSearchFailures().size() != 0){
				repobj.setApiResponse("Search Failed");
			}
	      
		}else{
			repobj.setApiResponse("Failed");
		}

		return repobj;
	}

	// API to return the list of groups with total number of products and sum of prices of products
	@RequestMapping(value = "/groupsInfo", method = RequestMethod.GET)
	public ResStructure groupsInfo(){
		// prepare subaggregation query of sum of price
		SumAggregationBuilder​ sumaggs = new SumAggregationBuilder​("total_price");
		sumaggs.field("product_mrp");
		
		// prepare term aggregation query 
		TermsAggregationBuilder aggregation = new TermsAggregationBuilder("group_name",ValueType.STRING);
		aggregation.field("group_name.gtag");
		// set the bucket size to 100 (default 10)
		aggregation.size(100);
		// add the sub aggregation
		aggregation.subAggregation(sumaggs);
		// prepare search body
		SearchSourceBuilder groupbuilder = new SearchSourceBuilder();
		groupbuilder.query(new TermQueryBuilder("doc_relation","product"));
		groupbuilder.aggregation(aggregation);
		// search api request
		SearchRequest grouprequest = new SearchRequest("product_data");
		grouprequest.source(groupbuilder);
		// run the query
		SearchResponse groupresponse = null;
		try{
			groupresponse = client.search(grouprequest, RequestOptions.DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
		}
		// get the bucket
		Aggregations aggregations = groupresponse.getAggregations();
		Terms groupname = aggregations.get("group_name");
		List<?extends Terms.Bucket> groupbucket = groupname.getBuckets();
		List<HashMap> groupinfo = new ArrayList<>();

		// prepare response
		for (Terms.Bucket group : groupbucket){
			HashMap<String, Object> groupobj = new HashMap<>();
			groupobj.put("group",group.getKey());
			groupobj.put("count",group.getDocCount());
			groupobj.put("sum",((Sum)group.getAggregations().get("total_price")).getValue());
			groupinfo.add(groupobj);
		}

		
		repobj.setApiResponse("Success");
		repobj.setApiData(groupinfo);

		return repobj;

	}

	public SearchResponse groupSearch(BoolQueryBuilder boolquery, String groupname){
		// prepare body of search
		SearchSourceBuilder groupbuilder = new SearchSourceBuilder();
		groupbuilder.query(boolquery);
		// preapre search api call
		SearchRequest grouprequest = new SearchRequest("product_data");
		if(groupname != null){
			grouprequest.routing(groupname);
		}
		// set the body
		grouprequest.source(groupbuilder);

		// run the query
		SearchResponse groupresponse = null;
		try{
			groupresponse = client.search(grouprequest, RequestOptions.DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
		}
		// return the response
		return groupresponse;
	}

}
