package com.stajproje.hotel.solr;

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
            // Siralama icin: 'name' text_general oldugundan siralanamaz; 'rating' ise
            // string oldugundan alfabetik (yanlis) siralanir. Bu ikisi siralamaya ozel.
            field("nameSort", "string", true),
            field("ratingValue", "pint", true),
            // en ucuz oda fiyati — range sorgusu (fiyat filtresi) icin sayisal
            field("minPrice", "pdouble", true),
            field("address", "text_general", true),
            field("description", "text_general", true),
            field("facilities", "text_general", true),
            field("location", "location", true),
            field("phoneNumber", "string", false),
            field("websiteUrl", "string", false)
    );

    /**
     * Eksik sema alanlarini TEK istekte ekler.
     *
     * NEDEN toplu: her AddField istegi Solr'da core'un yeniden yuklenmesini
     * tetikler (searcher kapanir/acilir). Alanlari tek tek eklemek ilk acilista
     * 14 ard arda reload demekti; bu, kucuk bellekli bir Solr'u zorluyor.
     * MultiUpdate ile tek reload yeter.
     *
     * NOT: Bu metot artik @PostConstruct DEGIL. Onceden acilista calisiyordu ve
     * Solr uykudaysa/erisilemezse istisna firlatip UYGULAMANIN ACILMASINI
     * ENGELLIYORDU (deploy'da backend, Solr uyanana kadar cokup duruyordu).
     * Artik SolrMaintenanceService cagiriyor: hem acilista hem periyodik olarak,
     * hata durumunda uygulamayi cokertmeden.
     */
    public void ensureSchema() throws Exception {
        Set<String> existingFields = fetchExistingFieldNames();

        List<SchemaRequest.Update> missing = REQUIRED_FIELDS.stream()
                .filter(f -> !existingFields.contains((String) f.get("name")))
                .map(f -> (SchemaRequest.Update) new SchemaRequest.AddField(f))
                .toList();

        if (missing.isEmpty()) {
            return;
        }

        new SchemaRequest.MultiUpdate(missing).process(solrClient);
        log.info("Solr semasina {} alan tek istekte eklendi.", missing.size());
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
