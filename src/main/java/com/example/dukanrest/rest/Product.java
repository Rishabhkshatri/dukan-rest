package com.example.dukanrest.rest;

public class Product{
	private String productId;
	private String groupName;
	private int price;

	public Product(String productId, String groupName, int price){
		this.productId = productId;
		this.groupName = groupName;
		this.price = price;
	}

	public String getProductId(){
		return productId;
	}

	public String getGroupName(){
		return groupName;
	}

	public int getPrice(){
		return price;
	}
}
