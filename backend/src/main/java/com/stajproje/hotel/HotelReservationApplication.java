package com.stajproje.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: SolrSelfHealTask'in periyodik calisabilmesi icin gerekli.
// (Solr indeksi backend AYAKTAYKEN sifirlanabiliyor — bkz. SolrMaintenanceService.)
@SpringBootApplication
@EnableScheduling
public class HotelReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelReservationApplication.class, args);
	}

}
