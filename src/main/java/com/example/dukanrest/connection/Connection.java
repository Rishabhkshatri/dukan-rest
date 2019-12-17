package com.example.dukanrest.connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.common.settings.Settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Connection{
	@Value("${elasticsearch.host:localhost}")
	String host;

	@Value("${elasticsearch.port:9300}")
	int port;

	@Bean
	public TransportClient client(){
		TransportClient client = null;
	    try{
	    	Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
			client = new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
		} catch (UnknownHostException e) {
		    e.printStackTrace();
		}
		return client;
	}
}