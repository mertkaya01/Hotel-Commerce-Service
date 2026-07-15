package com.stajproje.hotel.solr;

import com.stajproje.hotel.entity.Hotel;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

/**
 * Bir Hotel'i Solr dokumanina cevirip indexler. Hem ilk CSV import'unda
 * (CsvImportRunner) hem de yeniden indexlemede (SolrReindexRunner) kullanilir;
 * boylece indexleme mantigi tek yerde durur (tekrar yok).
 */
@Component
@RequiredArgsConstructor
public class SolrHotelIndexer {

    private final HttpJdkSolrClient solrClient;

    public void index(Hotel hotel) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", hotel.getHotelCode());
        doc.addField("hotelCode", hotel.getHotelCode());
        doc.addField("name", hotel.getName());
        doc.addField("countryCode", hotel.getCountryCode());
        doc.addField("countryName", hotel.getCountryName());
        doc.addField("cityName", hotel.getCityName());
        // string alanlarin text ikizleri -> buyuk/kucuk harf duyarsiz arama (edismax qf)
        doc.addField("cityText", hotel.getCityName());
        doc.addField("countryText", hotel.getCountryName());
        doc.addField("rating", hotel.getRating().name());
        // siralamaya ozel alanlar (bkz. SolrSchemaInitializer)
        doc.addField("nameSort", hotel.getName());
        doc.addField("ratingValue", hotel.getRating().getStars());
        doc.addField("address", hotel.getAddress());
        doc.addField("description", hotel.getDescription());
        doc.addField("facilities", hotel.getFacilities());
        doc.addField("phoneNumber", hotel.getPhoneNumber());
        doc.addField("websiteUrl", hotel.getWebsiteUrl());

        if (hotel.getLatitude() != null && hotel.getLongitude() != null) {
            doc.addField("location", hotel.getLatitude() + "," + hotel.getLongitude());
        }

        solrClient.add(doc);
    }

    public void commit() throws Exception {
        solrClient.commit();
    }
}
