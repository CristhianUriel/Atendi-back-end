//package com.mx.atendi.config;
//
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
//import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import com.mongodb.reactivestreams.client.MongoClient;
//import com.mongodb.reactivestreams.client.MongoClients;
//
//import lombok.extern.slf4j.Slf4j;
//
//@EnableReactiveMongoRepositories(basePackages = "com.mx.atendi.repository")
//@EnableTransactionManagement
//@Configuration
//@Slf4j
//@EnableConfigurationProperties(MongoProperties.class)
//public class MongoDBCConfig extends AbstractReactiveMongoConfiguration {
//	
//	
//    private final MongoProperties mongoProperties;
//
//    public MongoDBCConfig(MongoProperties mongoProperties) {
//        this.mongoProperties = mongoProperties;
//    }
//	
//	@Override
//	protected String getDatabaseName() {
//		log.info("Database name {}",mongoProperties.getDatabase());
//		return mongoProperties.getDatabase();
//	}
//	
//	
//	@Bean
//	@Override
//	public MongoClient reactiveMongoClient() {
//		log.info("Conexion Database {}",mongoProperties.getUri());
//		String uri = "mongodb://"+mongoProperties.getUsername()+":"+mongoProperties.getPassword()+"@"+mongoProperties.getHost()+":"+mongoProperties.getPort()+"/"+mongoProperties.getDatabase()+"?authSource=admin";
//		log.info("Conexion Database [{}]",uri);
//		return MongoClients.create(mongoProperties.getUri());
//	}
//}