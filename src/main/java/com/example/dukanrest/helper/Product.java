package com.example.dukanrest.helper;

public class Product{
	private String productId;
	private String productName;
	private String groupName;
	private int price;
	private String modelName;
	private long productSerialNo;

	public Product(){

	}
	
	public void setProductId(String productId){
		this.productId = productId;
	}
	
	public void setProductName(String productName){
		this.productName = productName;
	}

	public void setGroupName(String groupName){
		this.groupName = groupName;
	}

	public void setPrice(int price){
		this.price = price;
	}

	public void setModelName(String modelName){
		this.modelName = modelName;
	}

	public void setProductSerialNo(long productSerialNo){
		this.productSerialNo = productSerialNo;
	}

	public String getProductId(){
		return productId;
	}
	
	public String getProductName(){
		return productName;
	}

	public String getGroupName(){
		return groupName;
	}

	public int getPrice(){
		return price;
	}

	public String getModelName(){
		return modelName;
	}

	public long getProductSerialNo(){
		return productSerialNo;
	}
}
