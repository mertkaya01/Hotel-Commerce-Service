package com.stajproje.hotel.dto;

import com.stajproje.hotel.dto.auth.RegisterRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kayit sifre kurali: en az 8 karakter + en az bir harf ve bir rakam.
 * Bean Validation (jakarta) ile DTO seviyesinde dogrulanir.
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private RegisterRequest req(String password) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("a@b.com");
        r.setFirstName("Ad");
        r.setLastName("Soyad");
        r.setPassword(password);
        return r;
    }

    private boolean passwordHasError(String password) {
        return validator.validate(req(password)).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void gecerliSifre_harfVeRakam_hataVermez() {
        assertThat(passwordHasError("parola123")).isFalse();
    }

    @Test
    void sadeceRakam_reddedilir() {
        assertThat(passwordHasError("12345678")).isTrue();
    }

    @Test
    void sadeceHarf_reddedilir() {
        assertThat(passwordHasError("abcdefgh")).isTrue();
    }

    @Test
    void cokKisa_reddedilir() {
        assertThat(passwordHasError("ab12")).isTrue();
    }
}
