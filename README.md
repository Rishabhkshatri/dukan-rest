A Rest api to update the product price.

>To start server in local environment

` java -jar build/libs/dukan-rest-0.0.1-SNAPSHOT.jar `

>To start server in product environment 

` java -jar build/libs/dukan-rest-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod `

### Api Detail

**Api to** : update the price of a product given with the group and the price.

Api Endpoint : /update

Request Type : POST

Request Header : {Content-Type : “application/json”}

Request Body : {
    "productId": "wwpj-24BVAlmfS-9KlW0",
    "groupName": "Group 2",
    "price": 5555
}

Required Parameter : 
 - productId

 - groupName

Response Body : {
   "apiResponse": "Success",
   "apiData": []
}

Api Working : 
 - Require parameter check.
 
 - Prepares the filter and update inline script using.

 - runs the query and prepare response of ResStructure type.

**Api to** : add a new Product and also Group (if not exist).

Api Endpoint : /insert

Request Type : PUT

Request Header : {Content-Type : “application/json”}

Request Body : {
"groupName" : "Group 33",
"price" : 4000,
"productName" : "pname 1",
"modelName" : "nmodel 1",
"productSerialNo" : "098765432112”
}

Required Parameter : 
 - productName

 - groupName

 - modelName

 - productSerialNo

Response Body : {
   "apiResponse": "Success",
   "apiData": []
}

Api Working : 
 - Require parameter check.
 
 - Prepares and runs search query to get the id of the group if exist.

 - If group does not exist then runs index group query.

 - Prepares and runs query to index the new product.
 
 - prepare response of ResStructure type.

**Api to** : change the group of a product.

Api Endpoint : /updateGroup

Request Type : POST

Request Header : {Content-Type : “application/json”}

Request Body : {
"groupName" : "Group 3",
"productId" : "BcreSm8B9J70b-EzbYwT"
}

Required Parameter : 
 - groupName (new group name)

 - productId

Response Body : {
   "apiResponse": "Success",
   "apiData": []
}

Api Working : 
 - Require parameter check.
 
 - Prepares and runs search query to get the id of the group if exist.

 - Gets the product detail by searching product with the productid.

 - Changes the relation of the product and index it to the new group route.
 
 - Deletes the old product with product id.

 - prepare response of ResStructure type.

**Api to** : API to return the list of groups with total number of products and sum of prices of products.

Api Endpoint : /groupsInfo

Request Type : GET


Response Body : {
   "apiResponse": "Success",
   "apiData": [
		{
			“group” : “Group 1”,
			“count” : “6”,
			“sum”  : “12000”
		}
	]
}

Api Working : 
 - Require parameter check.
 
 - Prepare the query to filter the product.
 
 - Prepare term aggregation to create the bucket of group_name.gtag as group_name.
 
 - Creates sub aggregation to do sum of product price

 -  runs the search query and prepare response of ResStructure type.


### Files Detail

**Connection.java** :

Path : [dukan-rest/src/main/java/com/example/dukanrest/connection/](https://github.com/Rishabhkshatri/dukan-rest/tree/master/src/main/java/com/example/dukanrest/connection)

Class : Connection

 - Method : client
 
   - Return : RestHighLevelClient

 - Method Description : The method establish a http client connection with the elastic search use _host_ and _port_ variable whose values depends on the environment it is running and return and RestHighLevelClient.

**Controller.java**

Path : [dukan-rest/src/main/java/com/example/dukanrest/rest/Controller.java
](https://github.com/Rishabhkshatri/dukan-rest/blob/master/src/main/java/com/example/dukanrest/rest/Controller.java)

Class : Controller

Description : Contains all the api endpoints.

**Product.java**

Path : [dukan-rest/src/main/java/com/example/dukanrest/helper/Product.java](https://github.com/Rishabhkshatri/dukan-rest/blob/master/src/main/java/com/example/dukanrest/helper/Product.java)

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

**ResStructure.java**

Path : [dukan-rest/src/main/java/com/example/dukanrest/helper/ResStructure.java](https://github.com/Rishabhkshatri/dukan-rest/blob/master/src/main/java/com/example/dukanrest/helper/ResStructure.java)

Class : ResStructure

Description : Class to create api response.

**Environment**

   - application.properties : properties for local environment setup.
     1. Elasticsearch host
     2. Elasticsearch port

   - application-prod.properties : properties for prod environment setup.
     1. Elasticsearch host
     2. Elasticsearch port
