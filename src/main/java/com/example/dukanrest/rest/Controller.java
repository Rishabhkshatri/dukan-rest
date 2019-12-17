package com.example.dukanrest.rest;

import com.example.dukanrest.connection.Connection;
import com.example.dukanrest.rest.Product;
import java.util.Collections;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.elasticsearch.client.transport.TransportClient;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

@RestController
public class Controller{

    @Autowired
    TransportClient client;
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String updateProduct(@RequestBody Product product){
		String res = "";

        UpdateByQueryRequestBuilder updateByQuery = new UpdateByQueryRequestBuilder(client, UpdateByQueryAction.INSTANCE);
		updateByQuery.source("index_product")
		    .filter(QueryBuilders.termQuery("_id", product.getProductId()))
		    .filter(QueryBuilders.matchPhraseQuery("group_associated", product.getGroupName()))
		    .script(new Script(ScriptType.INLINE,
		        "painless",
		        "ctx._source.product_mrp = "+product.getPrice(),
		        Collections.emptyMap()));
		BulkByScrollResponse response = updateByQuery.get();

		if(response.getUpdated() != 0){
			res = "Updated Successfully";
		}
		if(response.getBulkFailures().size() != 0){
			res = "Bulk Failures";
		}
		if(response.getSearchFailures().size() != 0){
			res = "Search Failures";
		}
		System.out.println(response);
       
		return res;
	}
}
