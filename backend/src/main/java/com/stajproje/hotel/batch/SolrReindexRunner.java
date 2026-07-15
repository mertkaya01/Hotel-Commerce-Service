package com.stajproje.hotel.batch;

import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.repository.HotelRepository;
import com.stajproje.hotel.solr.SolrHotelIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Kendi kendini onaran Solr yeniden-indexleyici.
 *
 * H2 (kalici kaynak) ile Solr (arama indeksi) sayilari uyusmuyorsa — orn. Solr
 * container'i sifirlanmis ya da sema degismis ama H2 verisi duruyorsa — tum otelleri
 * H2'den okuyup Solr'a yeniden yazar. H2'ye DOKUNMAZ; kullanici/rezervasyon verisi korunur.
 *
 * CsvImportRunner'dan (@Order 1) sonra calisir: eger ilk import zaten yeni yapildiysa
 * sayilar esittir ve burada is yapilmaz.
 */
@Slf4j
@Component
@Order(2)
@Profile("!test")
@RequiredArgsConstructor
public class SolrReindexRunner implements CommandLineRunner {

    /** Kac oteli tek Solr istegiyle gonderecegimiz. */
    private static final int SOLR_BATCH_SIZE = 500;

    private final HotelRepository hotelRepository;
    private final HttpJdkSolrClient solrClient;
    private final SolrHotelIndexer solrIndexer;

    @Override
    public void run(String... args) throws Exception {
        long hotelCount = hotelRepository.count();
        if (hotelCount == 0) {
            return; // H2 bos: reindex edecek veri yok (CsvImportRunner devrede)
        }

        long solrCount = fetchSolrCount();
        // Sema yeni bir alan kazandiginda (orn. siralama alanlari) dokuman SAYISI degismez;
        // bu yuzden eski dokumanlarda alan eksik mi diye ayrica bakariz.
        long missingSortFields = countMissingSortFields();

        if (solrCount == hotelCount && missingSortFields == 0) {
            return; // senkron: yapacak bir sey yok
        }

        log.info("Solr yeniden indexleniyor (H2={} otel, Solr={} dokuman, siralama alani eksik={})...",
                hotelCount, solrCount, missingSortFields);

        int reindexed = 0;
        List<Hotel> batch = new ArrayList<>(SOLR_BATCH_SIZE);
        for (Hotel hotel : hotelRepository.findAll()) {
            batch.add(hotel);
            reindexed++;
            // tek tek degil toplu gonder (bkz. SolrHotelIndexer.indexAll)
            if (batch.size() >= SOLR_BATCH_SIZE) {
                solrIndexer.indexAll(batch);
                batch.clear();
                log.info("{} otel yeniden indexlendi...", reindexed);
            }
        }
        solrIndexer.indexAll(batch);
        solrIndexer.commit();
        log.info("Solr yeniden indexleme tamamlandi. {} otel indexlendi.", reindexed);
    }

    private long fetchSolrCount() throws Exception {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(0);
        return solrClient.query(query).getResults().getNumFound();
    }

    /** Siralama alani (ratingValue) olmayan eski dokumanlarin sayisi. */
    private long countMissingSortFields() throws Exception {
        SolrQuery query = new SolrQuery("-ratingValue:[* TO *]");
        query.setRows(0);
        return solrClient.query(query).getResults().getNumFound();
    }
}
