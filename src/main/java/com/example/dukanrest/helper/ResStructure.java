package com.example.dukanrest.helper;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

// structure of response object
@Service
public class ResStructure{
	private String apiResponse;
	private List<HashMap> apiData;
	
	public ResStructure(){}
	
	public void setApiResponse(String apiResponse){
		this.apiResponse = apiResponse;
		this.apiData = new ArrayList<HashMap>();
	}

	public void setApiData(List<HashMap> apiData){
		this.apiData = apiData;
	}

	public String getApiResponse(){
		return apiResponse;
	}

	public List getApiData(){
		return apiData;
	}
}
