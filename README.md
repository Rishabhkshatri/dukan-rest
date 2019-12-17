A Rest api to update the product price.

>To start server in local environment

`**java -jar build/libs/dukan-rest-0.0.1-SNAPSHOT.jar**`

>To start server in product environment 

`**java -jar build/libs/dukan-rest-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod**`

### Api Detail

Api Endpoint : /update

Request Type : POST

Request Body : {
    "productId": "wwpj-24BVAlmfS-9KlW0",
    "groupName": "Group 2",
    "price": 5555
}

### Files Detail

** Connection.java ** :

Path : [dukan-rest/src/main/java/com/example/dukanrest/connection/](https://github.com/Rishabhkshatri/dukan-rest/tree/master/src/main/java/com/example/dukanrest/connection)

Class : Connection

 - Method : client
 
   - Return : TransportClient

 - Method Description : The method establish a transport client connection with the elastic search use _host_ and _port_ variable whose values depends upon the environment it is running and return and transport client.

**Controller.java**

Path : [dukan-rest/src/main/java/com/example/dukanrest/rest/Controller.java
](https://github.com/Rishabhkshatri/dukan-rest/blob/master/src/main/java/com/example/dukanrest/rest/Controller.java)

Class : Controller

 - Method : updateProduct

   - Argument : Product
   
   - Return : String

     - On Success : Updated Successfully
    
   - Description : This method takes a _Product_  type object as an argument and using transport client and UpdateByQueryRequestBuilder it updates the price of the product in the index_product table and return response as a string.

**Product.java**

Path : [dukan-rest/src/main/java/com/example/dukanrest/rest/Product.java](https://github.com/Rishabhkshatri/dukan-rest/blob/master/src/main/java/com/example/dukanrest/rest/Product.java)

Class : Product

 - Constructor : Product(String productId, String groupName, int price)

 - Methods : 
	- public String getProductId()
	  - Return : product id
	- public String getGroupName()
	  - Return : group name
	- public int getPrice()
	  - Return : price
 - Class Description : A POJO class which returns the product object

**Environment**

   - application.properties : properties for local environment setup.
     1. Elasticsearch host
     2. Elasticsearch port

   - application-prod.properties : properties for prod environment setup.
     1. Elasticsearch host
     2. Elasticsearch port
