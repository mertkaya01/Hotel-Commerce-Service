package com.stajproje.hotel.batch;

import com.stajproje.hotel.solr.SolrMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Solr'u periyodik kontrol edip gerekirse onarir.
 *
 * NEDEN acilis kontrolu yetmiyor: ucretsiz deploy'da Solr'un kalici diski yok.
 * Solr uykuya dalip uyandiginda indeksi bosalir. Bu, backend AYAKTAYKEN olursa
 * (ki sik olur) arama kalici olarak bos kalir; cunku acilis kontrolu artik
 * calismayacaktir. Bu gorev farki en gec bir dakika icinde kapatir.
 *
 * Ucuz bir kontrol: Solr senkronsa sadece iki sayim sorgusu atar.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class SolrSelfHealTask {

    private final SolrMaintenanceService maintenanceService;

    @Scheduled(initialDelayString = "60000", fixedDelayString = "60000")
    public void heal() {
        maintenanceService.ensureSolrInSync();
    }
}
