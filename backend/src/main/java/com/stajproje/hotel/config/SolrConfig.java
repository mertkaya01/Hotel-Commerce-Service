package com.stajproje.hotel.config;

import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

    @Value("${solr.base-url}")
    private String baseUrl;

    @Value("${solr.collection}")
    private String collection;

    @Bean(destroyMethod = "close")
    public HttpJdkSolrClient solrClient() {
        return new HttpJdkSolrClient.Builder(baseUrl + "/" + collection).build();
    }
}
