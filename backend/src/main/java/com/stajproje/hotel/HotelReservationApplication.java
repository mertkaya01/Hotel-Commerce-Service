package com.stajproje.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: SolrSelfHealTask'in periyodik calisabilmesi icin gerekli.
// (Solr indeksi backend AYAKTAYKEN sifirlanabiliyor — bkz. SolrMaintenanceService.)
// @EnableAsync: dogrulama maili gonderimini arka planda calistirmak icin
// (EmailService.sendVerificationEmail @Async — kayit akisini bloklamaz).
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class HotelReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelReservationApplication.class, args);
	}

}
