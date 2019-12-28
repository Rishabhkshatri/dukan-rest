package com.example.dukanrest.connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Connection{
	@Value("${elasticsearch.host:localhost}")
	private String host;

	@Value("${elasticsearch.port:9300}")
	private int port;

	@Bean
	public RestHighLevelClient client(){
		// prepare client connection
		RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(host, port, "http"));
		RestHighLevelClient client = new RestHighLevelClient(clientBuilder);

		return client;
	}
}