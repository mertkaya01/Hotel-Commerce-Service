package com.stajproje.hotel.entity;

public enum HotelRating {
    ONE_STAR(1),
    TWO_STAR(2),
    THREE_STAR(3),
    FOUR_STAR(4),
    FIVE_STAR(5),
    UNRATED(0);

    private final int stars;

    HotelRating(int stars) {
        this.stars = stars;
    }

    /**
     * Yildiz sayisi. Solr'da SAYISAL siralama icin gerekli: 'rating' alani string
     * oldugundan alfabetik siralanir (FIVE, FOUR, ONE...) ve yanlis sonuc verir.
     */
    public int getStars() {
        return stars;
    }
}
