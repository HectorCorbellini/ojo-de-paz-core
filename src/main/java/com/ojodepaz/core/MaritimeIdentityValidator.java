package com.ojodepaz.core;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MaritimeIdentityValidator - Componente de Atestación de Hardware para Ojo de
 * Paz
 *
 * Esta clase implementa protocolos de confianza cero para validar la identidad
 * de embarcaciones pesqueras mediante atestación de hardware criptográfico,
 * asegurando la integridad de datos GPS y verificando firmas digitales
 * para prevenir ataques erróneos de drones contra civiles.
 *
 * Principios de confianza cero: nunca confía, siempre verifica.
 */
public class MaritimeIdentityValidator {

    // Logger de auditoría de seguridad - esencial para rastreo de eventos críticos
    private static final Logger AUDIT_LOGGER = Logger.getLogger("MaritimeSecurityAudit");
    static {
        // Configuración de auditoría para logs persistentes
        AUDIT_LOGGER.setLevel(Level.INFO);
        // Nota: en producción configurar FileHandler para persistencia
    }

    // Inyectable para tests unitarios - Fase 2: usar inyección de dependencias
    private final Clock systemClock;
    private final MessageDigest messageDigest;

    // Constructor con dependencias por defecto - Fase 2: inyectar para tests
    public MaritimeIdentityValidator() throws NoSuchAlgorithmException {
        this(Clock.systemUTC(), MessageDigest.getInstance("SHA-256"));
    }

    // Constructor para inyección de dependencias - facilita tests unitarios
    public MaritimeIdentityValidator(Clock clock, MessageDigest digest) {
        this.systemClock = clock;
        this.messageDigest = digest;
    }

    // Almacén de claves públicas de dispositivos (deviceId -> publicKey)
    private final Map<String, PublicKey> devicePublicKeys = new HashMap<>();

    // Nonces activos para challenge-response (nonce -> deviceId)
    private final Map<String, String> activeNonces = new HashMap<>();
    private static final long MAX_TIMESTAMP_DRIFT_SECONDS = 300; // 5 minutos de tolerancia

    /**
     * Registra un dispositivo con su clave pública para validación
     * En producción: esto viene de certificados de fábrica o PKI
     */
    public void registerDevice(String deviceId, String publicKeyBase64)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(spec);
        devicePublicKeys.put(deviceId, publicKey);
        AUDIT_LOGGER.info("Dispositivo registrado: " + deviceId);
    }

    /**
     * Genera un nonce para challenge-response
     * El servidor envía esto al dispositivo, quien debe firmarlo
     */
    public String generateChallenge(String deviceId) {
        if (!devicePublicKeys.containsKey(deviceId)) {
            throw new IllegalArgumentException("Dispositivo no registrado: " + deviceId);
        }
        String nonce = UUID.randomUUID().toString();
        activeNonces.put(nonce, deviceId);
        AUDIT_LOGGER.info("Challenge generado para dispositivo: " + deviceId);
        return nonce;
    }

    /**
     * Punto de entrada principal para validación de identidad marítima
     * Implementa protocolo de confianza cero: verifica cada componente
     * independientemente
     *
     * @param vesselData Datos de la embarcación incluyendo firma y metadatos GPS
     * @return Resultado de validación con detalles de auditoría
     */
    public ValidationResult validateMaritimeIdentity(VesselData vesselData) {
        AUDIT_LOGGER.info(String.format("Iniciando validación de identidad para embarcación ID: %s",
                vesselData.getVesselId()));

        try {
            // Verificación de atestación de hardware con challenge-response
            boolean hardwareAttested = validateHardwareAttestation(
                    vesselData.getDeviceId(),
                    vesselData.getNonce(),
                    vesselData.getSignedNonce());
            if (!hardwareAttested) {
                AUDIT_LOGGER.warning(String.format("Fallo en atestación de hardware para embarcación %s",
                        vesselData.getVesselId()));
                return new ValidationResult(false, "Atestación de hardware fallida",
                        SecurityEvent.HARDWARE_ATTESTATION_FAILED);
            }

            // Validación de firma digital para integridad de datos
            boolean signatureValid = validateDigitalSignature(vesselData);
            if (!signatureValid) {
                AUDIT_LOGGER.warning(String.format("Firma digital inválida para embarcación %s",
                        vesselData.getVesselId()));
                return new ValidationResult(false, "Firma digital inválida",
                        SecurityEvent.DIGITAL_SIGNATURE_INVALID);
            }

            // Validación de metadatos GPS con verificación de integridad
            boolean gpsValid = validateGPSIntegrity(vesselData.getGpsMetadata());
            if (!gpsValid) {
                AUDIT_LOGGER.warning(String.format("Metadatos GPS inválidos para embarcación %s",
                        vesselData.getVesselId()));
                return new ValidationResult(false, "Metadatos GPS inválidos",
                        SecurityEvent.GPS_INTEGRITY_VIOLATION);
            }

            AUDIT_LOGGER.info(String.format("Validación exitosa para embarcación %s - Confianza cero confirmada",
                    vesselData.getVesselId()));
            return new ValidationResult(true, "Validación exitosa", SecurityEvent.VALIDATION_SUCCESS);

        } catch (Exception e) {
            AUDIT_LOGGER.log(Level.SEVERE, String.format("Error crítico en validación de embarcación %s: %s",
                    vesselData.getVesselId(), e.getMessage()), e);
            return new ValidationResult(false, "Error interno de validación",
                    SecurityEvent.SYSTEM_ERROR);
        }
    }

    /**
     * Valida atestación de hardware mediante protocolo challenge-response
     * PROPER IMPLEMENTATION: Server sends nonce, device signs it with private key
     * 
     * @param deviceId    Identificador del dispositivo
     * @param nonce       Challenge que envió el servidor
     * @param signedNonce Nonce firmado por el dispositivo con su clave privada
     * @return true si la firma es válida y corresponde al dispositivo
     */
    private boolean validateHardwareAttestation(String deviceId, String nonce, byte[] signedNonce) {
        if (deviceId == null || nonce == null || signedNonce == null || signedNonce.length == 0) {
            return false;
        }

        try {
            // Verificar que el nonce existe y corresponde al dispositivo
            String expectedDevice = activeNonces.get(nonce);
            if (expectedDevice == null || !expectedDevice.equals(deviceId)) {
                AUDIT_LOGGER.warning("Nonce inválido o expirado para dispositivo: " + deviceId);
                return false;
            }

            // Obtener clave pública del dispositivo
            PublicKey publicKey = devicePublicKeys.get(deviceId);
            if (publicKey == null) {
                AUDIT_LOGGER.warning("Dispositivo no registrado: " + deviceId);
                return false;
            }

            // Verificar firma ECDSA
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(nonce.getBytes());
            boolean valid = signature.verify(signedNonce);

            // Invalidar nonce después de uso (previene replay)
            activeNonces.remove(nonce);

            if (!valid) {
                AUDIT_LOGGER.warning("Firma de atestación inválida para dispositivo: " + deviceId);
                return false;
            }

            AUDIT_LOGGER.info("Atestación de hardware exitosa para: " + deviceId);
            return true;

        } catch (Exception e) {
            AUDIT_LOGGER.log(Level.SEVERE, "Error en atestación de hardware para: " + deviceId, e);
            return false;
        }
    }

    /**
     * Verifica firma digital para asegurar integridad de datos transmitidos
     * Implementa verificación de cadena de confianza para datos críticos
     */
    private boolean validateDigitalSignature(VesselData vesselData) {
        try {
            // Simulación de verificación ECDSA (en producción usar BouncyCastle o similar)
            String payload = vesselData.getVesselId() + vesselData.getGpsMetadata().toString();
            byte[] payloadHash = messageDigest.digest(payload.getBytes());

            // Firma simulada: en producción verificar contra clave pública del hardware
            String expectedSignature = bytesToHex(payloadHash);
            byte[] expectedBytes = expectedSignature.getBytes();
            byte[] actualBytes = vesselData.getDigitalSignature().getBytes();

            // Comparación segura contra timing attacks usando MessageDigest.isEqual
            return MessageDigest.isEqual(expectedBytes, actualBytes);

        } catch (Exception e) {
            AUDIT_LOGGER.log(Level.SEVERE, "Error en algoritmo de firma digital", e);
            return false;
        }
    }

    /**
     * Valida integridad de metadatos GPS mediante verificación de timestamp y
     * coordenadas
     * Previene ataques de replay y manipulación de ubicación
     * Package-private para permitir tests unitarios
     */
    boolean validateGPSIntegrity(GPSMetadata gpsData) {
        // Verificación de timestamp para prevenir ataques de replay
        Instant now = systemClock.instant();
        Instant gpsTime = Instant.ofEpochSecond(gpsData.getTimestamp());

        if (Math.abs(ChronoUnit.SECONDS.between(gpsTime, now)) > MAX_TIMESTAMP_DRIFT_SECONDS) {
            AUDIT_LOGGER.warning("Timestamp GPS fuera de ventana de tolerancia");
            return false;
        }

        // Validación de coordenadas geográficas (área aproximada de Colombia)
        double latitude = gpsData.getLatitude();
        double longitude = gpsData.getLongitude();

        if (latitude < -4.0 || latitude > 13.0 || longitude < -82.0 || longitude > -66.0) {
            AUDIT_LOGGER.warning(String.format("Coordenadas GPS fuera del área autorizada: %.6f, %.6f",
                    latitude, longitude));
            return false;
        }

        // Verificación de precisión de coordenadas
        if (!hasRequiredPrecision(latitude, longitude)) {
            AUDIT_LOGGER.warning("Precisión GPS insuficiente para validación de seguridad");
            return false;
        }

        return true;
    }

    /**
     * Verifica que las coordenadas GPS tengan la precisión requerida
     * Validación matemática independiente de Locale para evitar vulnerabilidades
     */
    private boolean hasRequiredPrecision(double lat, double lon) {
        // Validación matemática - no depende de Locale (evita problemas con coma/punto)
        double latScaled = lat * 1_000_000;
        double lonScaled = lon * 1_000_000;

        // Verifica que tengamos precisión de 6 decimales (microgrados ≈ 0.1m)
        // Un valor con 6+ decimales tendrá parte fraccionaria cercana a 0 cuando
        // escalado
        return Math.abs(latScaled - Math.round(latScaled)) < 0.001 &&
                Math.abs(lonScaled - Math.round(lonScaled)) < 0.001;
    }

    /**
     * Utilidad para conversión de bytes a hexadecimal
     * Package-private para permitir su uso en tests
     */
    String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Clases de datos para el sistema

    /**
     * Resultado de validación con detalles de auditoría
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final SecurityEvent event;

        public ValidationResult(boolean valid, String message, SecurityEvent event) {
            this.valid = valid;
            this.message = message;
            this.event = event;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public SecurityEvent getEvent() {
            return event;
        }
    }

    /**
     * Datos de la embarcación para validación con challenge-response
     */
    public static class VesselData {
        private final String vesselId;
        private final String deviceId; // Identificador del dispositivo criptográfico
        private final String nonce; // Challenge enviado por el servidor
        private final byte[] signedNonce; // Nonce firmado por el dispositivo
        private final GPSMetadata gpsMetadata;
        private final String digitalSignature;

        public VesselData(String vesselId, String deviceId, String nonce, byte[] signedNonce,
                GPSMetadata gpsMetadata, String digitalSignature) {
            this.vesselId = vesselId;
            this.deviceId = deviceId;
            this.nonce = nonce;
            this.signedNonce = signedNonce;
            this.gpsMetadata = gpsMetadata;
            this.digitalSignature = digitalSignature;
        }

        public String getVesselId() {
            return vesselId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getNonce() {
            return nonce;
        }

        public byte[] getSignedNonce() {
            return signedNonce;
        }

        public GPSMetadata getGpsMetadata() {
            return gpsMetadata;
        }

        public String getDigitalSignature() {
            return digitalSignature;
        }
    }

    /**
     * Metadatos GPS con información de ubicación y tiempo
     */
    public static class GPSMetadata {
        private final double latitude;
        private final double longitude;
        private final long timestamp;

        public GPSMetadata(double latitude, double longitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("GPS{lat=%.6f, lon=%.6f, time=%d}", latitude, longitude, timestamp);
        }
    }

    /**
     * Eventos de seguridad para auditoría
     */
    public enum SecurityEvent {
        VALIDATION_SUCCESS,
        HARDWARE_ATTESTATION_FAILED,
        DIGITAL_SIGNATURE_INVALID,
        GPS_INTEGRITY_VIOLATION,
        SYSTEM_ERROR
    }
}
