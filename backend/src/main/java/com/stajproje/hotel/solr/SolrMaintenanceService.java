package com.stajproje.hotel.solr;

import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Solr'u H2 ile senkron TUTAR (kendi kendini onarma).
 *
 * NEDEN GEREKLI: Solr'un arama indeksi TUREVDIR — tek dogru kaynak H2'dir.
 * Ucretsiz deploy ortaminda Solr'un kalici diski yoktur: servis uykuya dalip
 * uyaninca indeks BOSALIR, sema da silinir. Uygulama yalnizca acilista kontrol
 * etseydi, Solr backend ayaktayken yeniden baslarsa arama kalici olarak bos
 * kalirdi. Bu servis hem acilista hem periyodik olarak calisir ve farki kapatir.
 */
@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class SolrMaintenanceService {

    /** Kac oteli tek Solr istegiyle gonderecegimiz. */
    private static final int SOLR_BATCH_SIZE = 500;

    private final HotelRepository hotelRepository;
    private final HttpJdkSolrClient solrClient;
    private final SolrHotelIndexer solrIndexer;
    private final SolrSchemaInitializer schemaInitializer;

    /**
     * Semayi ve indeksi olmasi gereken hale getirir. Solr'a ulasilamiyorsa
     * (orn. uykuda) sessizce vazgecer — bir sonraki tur tekrar dener.
     *
     * @return onarim yapildiysa true
     */
    public boolean ensureSolrInSync() {
        long hotelCount = hotelRepository.count();
        if (hotelCount == 0) {
            return false; // H2 bos: yazacak veri yok (CsvImportRunner devrede)
        }

        try {
            // Solr yeniden basladiysa semasi da gitmis olabilir — once onu kur.
            schemaInitializer.ensureSchema();

            long solrCount = fetchSolrCount();
            long missingFields = countMissingIndexedFields();

            if (solrCount == hotelCount && missingFields == 0) {
                return false; // senkron
            }

            log.warn("Solr senkron degil (H2={}, Solr={}, yeni alani eksik dokuman={}). Yeniden indexleniyor...",
                    hotelCount, solrCount, missingFields);
            reindexAll();
            return true;

        } catch (Exception e) {
            // Solr uykuda/erisilemez olabilir. Uygulamayi COKERTME — sonraki tur dener.
            log.warn("Solr'a ulasilamadi, onarim atlandi ({}). Sonraki kontrolde tekrar denenecek.",
                    e.getMessage());
            return false;
        }
    }

    private void reindexAll() throws Exception {
        int reindexed = 0;
        List<Hotel> batch = new ArrayList<>(SOLR_BATCH_SIZE);

        for (Hotel hotel : hotelRepository.findAll()) {
            batch.add(hotel);
            reindexed++;
            if (batch.size() >= SOLR_BATCH_SIZE) {
                solrIndexer.indexAll(batch);
                batch.clear();
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

    /**
     * Sonradan eklenen indexli alanlardan (ratingValue, minPrice) BIRI eksik olan
     * dokuman sayisi. Sema yeni alan kazandiginda dokuman SAYISI degismedigi icin
     * bu kontrol, eski dokumanlarin yeniden indexlenmesi gerektigini yakalar.
     * (minPrice teorik olarak odasiz otelde bos olabilir ama pratikte her otelin
     * odasi var; yine de eksik cikan varsa reindex zararsiz.)
     */
    private long countMissingIndexedFields() throws Exception {
        SolrQuery query = new SolrQuery("-ratingValue:[* TO *] OR -minPrice:[* TO *]");
        query.setRows(0);
        return solrClient.query(query).getResults().getNumFound();
    }
}
