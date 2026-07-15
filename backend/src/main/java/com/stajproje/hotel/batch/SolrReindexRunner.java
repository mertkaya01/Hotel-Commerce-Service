package com.stajproje.hotel.batch;

import com.stajproje.hotel.solr.SolrMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Acilista Solr'u H2 ile senkronlar.
 *
 * H2 (kalici kaynak) ile Solr (arama indeksi) uyusmuyorsa — orn. Solr sifirlanmis
 * ya da sema degismis ama H2 verisi duruyorsa — tum otelleri yeniden indexler.
 * H2'ye DOKUNMAZ; kullanici/rezervasyon verisi korunur.
 *
 * CsvImportRunner'dan (@Order 1) sonra calisir: ilk import zaten yeni yapildiysa
 * sayilar esittir ve burada is yapilmaz.
 *
 * NOT: Asil onarim mantigi SolrMaintenanceService'te; ayni mantigi SolrSelfHealTask
 * periyodik olarak da calistirir (Solr acilistan SONRA sifirlanirsa diye).
 */
@Component
@Order(2)
@Profile("!test")
@RequiredArgsConstructor
public class SolrReindexRunner implements CommandLineRunner {

    private final SolrMaintenanceService maintenanceService;

    @Override
    public void run(String... args) {
        maintenanceService.ensureSolrInSync();
    }
}
