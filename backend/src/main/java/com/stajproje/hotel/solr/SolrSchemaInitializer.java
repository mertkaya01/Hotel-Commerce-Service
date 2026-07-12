package com.stajproje.hotel.solr;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class SolrSchemaInitializer {

    private final HttpJdkSolrClient solrClient;

    private static final List<Map<String, Object>> REQUIRED_FIELDS = List.of(
            field("hotelCode", "string", true),
            field("name", "text_general", true),
            field("countryCode", "string", true),
            field("countryName", "string", true),
            field("cityName", "string", true),
            // string alanlar facet/filtre icin (exact match); asagidaki text_general
            // ikizleri ise buyuk/kucuk harf duyarsiz TAM METIN aramasi icin (edismax qf).
            field("cityText", "text_general", true),
            field("countryText", "text_general", true),
            field("rating", "string", true),
            field("address", "text_general", true),
            field("description", "text_general", true),
            field("facilities", "text_general", true),
            field("location", "location", true),
            field("phoneNumber", "string", false),
            field("websiteUrl", "string", false)
    );

    @PostConstruct
    public void ensureSchema() throws Exception {
        Set<String> existingFields = fetchExistingFieldNames();

        for (Map<String, Object> fieldDef : REQUIRED_FIELDS) {
            String fieldName = (String) fieldDef.get("name");
            if (existingFields.contains(fieldName)) {
                continue;
            }
            new SchemaRequest.AddField(fieldDef).process(solrClient);
            log.info("Solr sema alani eklendi: {}", fieldName);
        }
    }

    private Set<String> fetchExistingFieldNames() throws Exception {
        SchemaResponse.FieldsResponse response = new SchemaRequest.Fields().process(solrClient);
        return response.getFields().stream()
                .map(f -> (String) f.get("name"))
                .collect(Collectors.toSet());
    }

    private static Map<String, Object> field(String name, String type, boolean indexed) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("name", name);
        attributes.put("type", type);
        attributes.put("stored", true);
        attributes.put("indexed", indexed);
        attributes.put("multiValued", false);
        return attributes;
    }
}
