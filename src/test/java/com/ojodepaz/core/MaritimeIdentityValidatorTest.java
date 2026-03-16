package com.ojodepaz.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class MaritimeIdentityValidatorTest {

    private MaritimeIdentityValidator validator;
    private Clock mockClock;
    private MessageDigest mockDigest;
    private static final Instant TEST_TIME = Instant.parse("2025-03-15T12:00:00Z");

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        mockClock = Clock.fixed(TEST_TIME, ZoneOffset.UTC);
        mockDigest = mock(MessageDigest.class);
        when(mockDigest.digest(any(byte[].class))).thenReturn("mock_hash".getBytes());
        validator = new MaritimeIdentityValidator(mockClock, mockDigest);
    }

    @Test
    @DisplayName("Debe rechazar validación cuando el deviceId es null")
    void testNullDeviceId() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.VesselData data = new MaritimeIdentityValidator.VesselData(
                "VESSEL-001", null, "nonce123", new byte[]{1, 2, 3}, gps, "signature");
        MaritimeIdentityValidator.ValidationResult result = validator.validateMaritimeIdentity(data);
        assertFalse(result.isValid());
        assertEquals(MaritimeIdentityValidator.SecurityEvent.HARDWARE_ATTESTATION_FAILED, result.getEvent());
    }

    @Test
    @DisplayName("Debe rechazar validación cuando el nonce es null")
    void testNullNonce() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.VesselData data = new MaritimeIdentityValidator.VesselData(
                "VESSEL-001", "DEVICE-001", null, new byte[]{1, 2, 3}, gps, "signature");
        MaritimeIdentityValidator.ValidationResult result = validator.validateMaritimeIdentity(data);
        assertFalse(result.isValid());
        assertEquals(MaritimeIdentityValidator.SecurityEvent.HARDWARE_ATTESTATION_FAILED, result.getEvent());
    }

    @Test
    @DisplayName("Debe rechazar validación cuando el signedNonce está vacío")
    void testEmptySignedNonce() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.VesselData data = new MaritimeIdentityValidator.VesselData(
                "VESSEL-001", "DEVICE-001", "nonce123", new byte[0], gps, "signature");
        MaritimeIdentityValidator.ValidationResult result = validator.validateMaritimeIdentity(data);
        assertFalse(result.isValid());
        assertEquals(MaritimeIdentityValidator.SecurityEvent.HARDWARE_ATTESTATION_FAILED, result.getEvent());
    }

    @Test
    @DisplayName("Debe aceptar coordenadas GPS válidas dentro del área de Colombia")
    void testValidGPSCoordinates() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.423456, -75.523456, TEST_TIME.getEpochSecond());
        assertTrue(validator.validateGPSIntegrity(gps));
    }

    @Test
    @DisplayName("Debe rechazar latitud fuera de los límites de Colombia")
    void testInvalidLatitude() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(20.0, -75.0, TEST_TIME.getEpochSecond());
        assertFalse(validator.validateGPSIntegrity(gps));
    }

    @Test
    @DisplayName("Debe rechazar longitud fuera de los límites de Colombia")
    void testInvalidLongitude() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -50.0, TEST_TIME.getEpochSecond());
        assertFalse(validator.validateGPSIntegrity(gps));
    }

    @Test
    @DisplayName("Debe aceptar timestamp dentro de la ventana de 5 minutos")
    void testValidTimestamp() {
        long validTimestamp = TEST_TIME.minusSeconds(60).getEpochSecond();
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -75.0, validTimestamp);
        assertTrue(validator.validateGPSIntegrity(gps));
    }

    @Test
    @DisplayName("Debe rechazar timestamp mayor a 5 minutos de diferencia")
    void testInvalidTimestamp() {
        long invalidTimestamp = TEST_TIME.minusSeconds(400).getEpochSecond();
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -75.0, invalidTimestamp);
        assertFalse(validator.validateGPSIntegrity(gps));
    }

    @Test
    @DisplayName("Debe rechazar timestamp en el futuro")
    void testFutureTimestamp() {
        long futureTimestamp = TEST_TIME.plusSeconds(400).getEpochSecond();
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.0, -75.0, futureTimestamp);
        assertFalse(validator.validateGPSIntegrity(gps));
    }

    @Test
    @DisplayName("Debe manejar gracefully GPSMetadata con valores null")
    void testNullGPSMetadata() {
        assertThrows(NullPointerException.class, () -> {
            validator.validateGPSIntegrity(null);
        });
    }

    @Test
    @DisplayName("Debe aceptar coordenadas en los límites exactos del área")
    void testBoundaryCoordinates() {
        MaritimeIdentityValidator.GPSMetadata gpsNorth = new MaritimeIdentityValidator.GPSMetadata(13.0, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.GPSMetadata gpsSouth = new MaritimeIdentityValidator.GPSMetadata(-4.0, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.GPSMetadata gpsWest = new MaritimeIdentityValidator.GPSMetadata(10.0, -82.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.GPSMetadata gpsEast = new MaritimeIdentityValidator.GPSMetadata(10.0, -66.0, TEST_TIME.getEpochSecond());
        assertTrue(validator.validateGPSIntegrity(gpsNorth), "Latitud norte límite");
        assertTrue(validator.validateGPSIntegrity(gpsSouth), "Latitud sur límite");
        assertTrue(validator.validateGPSIntegrity(gpsWest), "Longitud oeste límite");
        assertTrue(validator.validateGPSIntegrity(gpsEast), "Longitud este límite");
    }

    @Test
    @DisplayName("Debe rechazar coordenadas justo fuera de los límites")
    void testJustOutsideBoundary() {
        MaritimeIdentityValidator.GPSMetadata gpsNorth = new MaritimeIdentityValidator.GPSMetadata(13.01, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.GPSMetadata gpsSouth = new MaritimeIdentityValidator.GPSMetadata(-4.01, -75.0, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.GPSMetadata gpsWest = new MaritimeIdentityValidator.GPSMetadata(10.0, -82.01, TEST_TIME.getEpochSecond());
        MaritimeIdentityValidator.GPSMetadata gpsEast = new MaritimeIdentityValidator.GPSMetadata(10.0, -65.99, TEST_TIME.getEpochSecond());
        assertFalse(validator.validateGPSIntegrity(gpsNorth), "Justo fuera límite norte");
        assertFalse(validator.validateGPSIntegrity(gpsSouth), "Justo fuera límite sur");
        assertFalse(validator.validateGPSIntegrity(gpsWest), "Justo fuera límite oeste");
        assertFalse(validator.validateGPSIntegrity(gpsEast), "Justo fuera límite este");
    }

    @Test
    @DisplayName("ValidationResult debe mantener inmutabilidad")
    void testValidationResultImmutability() {
        MaritimeIdentityValidator.ValidationResult result = new MaritimeIdentityValidator.ValidationResult(
                true, "Test message", MaritimeIdentityValidator.SecurityEvent.VALIDATION_SUCCESS);
        assertTrue(result.isValid());
        assertEquals("Test message", result.getMessage());
        assertEquals(MaritimeIdentityValidator.SecurityEvent.VALIDATION_SUCCESS, result.getEvent());
    }

    @Test
    @DisplayName("GPSMetadata debe formatear correctamente en toString")
    void testGPSMetadataToString() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.423456, -75.523456, 1742040000L);
        String expected = "GPS{lat=10.423456, lon=-75.523456, time=1742040000}";
        assertEquals(expected, gps.toString());
    }

    @Test
    @DisplayName("VesselData debe almacenar todos los campos correctamente")
    void testVesselDataFields() {
        MaritimeIdentityValidator.GPSMetadata gps = new MaritimeIdentityValidator.GPSMetadata(10.423456, -75.523456, TEST_TIME.getEpochSecond());
        byte[] signedNonce = new byte[]{0x01, 0x02, 0x03};
        MaritimeIdentityValidator.VesselData data = new MaritimeIdentityValidator.VesselData(
                "VESSEL-001", "DEVICE-001", "nonce123", signedNonce, gps, "signature123");
        assertEquals("VESSEL-001", data.getVesselId());
        assertEquals("DEVICE-001", data.getDeviceId());
        assertEquals("nonce123", data.getNonce());
        assertArrayEquals(signedNonce, data.getSignedNonce());
        assertEquals(gps, data.getGpsMetadata());
        assertEquals("signature123", data.getDigitalSignature());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
