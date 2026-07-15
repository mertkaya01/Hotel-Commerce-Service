package com.stajproje.hotel.solr;

import com.stajproje.hotel.entity.Hotel;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Bir Hotel'i Solr dokumanina cevirip indexler. Hem ilk CSV import'unda
 * (CsvImportRunner) hem de yeniden indexlemede (SolrReindexRunner) kullanilir;
 * boylece indexleme mantigi tek yerde durur (tekrar yok).
 */
@Component
@RequiredArgsConstructor
public class SolrHotelIndexer {

    private final HttpJdkSolrClient solrClient;

    /** Tek otel indexler (orn. ev sahibinin oteli onaylandiginda). */
    public void index(Hotel hotel) throws Exception {
        solrClient.add(toDocument(hotel));
    }

    /**
     * Otelleri TEK HTTP istegiyle indexler.
     *
     * NEDEN: index() her cagrida Solr'a ayri bir HTTP istegi atar. Yerelde Solr
     * localhost'ta oldugu icin bu farkedilmez; ancak deploy'da backend ve Solr
     * AYRI SERVISLER olduğundan her istek ag uzerinden gider. 5000 otel = 5000
     * istek = dakikalarca sure + Solr'da gereksiz yuk. Toplu gonderim bunu
     * ~10 istege dusurur.
     */
    public void indexAll(Collection<Hotel> hotels) throws Exception {
        if (hotels.isEmpty()) {
            return;
        }
        List<SolrInputDocument> docs = hotels.stream().map(this::toDocument).toList();
        solrClient.add(docs);
    }

    private SolrInputDocument toDocument(Hotel hotel) {
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

        return doc;
    }

    public void commit() throws Exception {
        solrClient.commit();
    }
}
