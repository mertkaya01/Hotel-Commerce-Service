package com.stajproje.hotel.entity;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Yildiz siralamasi. Bu deger Solr'daki 'ratingValue' alanina yazilir; string
 * 'rating' alani alfabetik siralandiginda FIVE < FOUR < ONE ciktigi icin gerekli.
 */
class HotelRatingTest {

    @Test
    void getStars_shouldReturnCorrectNumbers() {
        assertThat(HotelRating.ONE_STAR.getStars()).isEqualTo(1);
        assertThat(HotelRating.THREE_STAR.getStars()).isEqualTo(3);
        assertThat(HotelRating.FIVE_STAR.getStars()).isEqualTo(5);
        assertThat(HotelRating.UNRATED.getStars()).isZero();
    }

    @Test
    void sortingByStars_shouldBeNumericNotAlphabetical() {
        List<HotelRating> sorted = List.of(
                        HotelRating.THREE_STAR, HotelRating.FIVE_STAR,
                        HotelRating.ONE_STAR, HotelRating.FOUR_STAR)
                .stream()
                .sorted(Comparator.comparingInt(HotelRating::getStars).reversed())
                .toList();

        // alfabetik olsaydi FIVE, FOUR, ONE, THREE gelirdi -> yanlis
        assertThat(sorted).containsExactly(
                HotelRating.FIVE_STAR, HotelRating.FOUR_STAR,
                HotelRating.THREE_STAR, HotelRating.ONE_STAR);
    }
}
