> **🇬🇧 English Summary** | [Spanish full documentation below](#ojo-de-paz-arquitectura-de-identidad-digital-para-la-seguridad-marítima-civil)
>
> **OjoDePaz** is a Zero Trust identity validation protocol for artisanal fishing vessels operating in maritime exclusion zones.
> It provides vessels with a tamper-proof digital identity using low-cost cryptographic hardware, GPS attestation, and a Java-based real-time integrity engine —
> enabling coast guards and NGOs to distinguish legitimate civilians from threats without relying on expensive military-grade AIS systems.
>
> **Stack**: Java 17 · Maven · JUnit 5 · Mockito · Cryptographic challenge-response · GPS signed payloads

---

# Ojo de Paz: Arquitectura de Identidad Digital para la Seguridad Marítima Civil

> **Cerrando la brecha entre la seguridad operativa y la verificación digital para la protección de la vida humana.**

---

## 1. EL DIAGNÓSTICO (El "Dolor" Real)

En las zonas de exclusión y control de tráfico marítimo, la falta de identificación electrónica en embarcaciones menores genera falsos positivos en los sistemas de vigilancia automatizada (drones/IA). Para un pescador artesanal, no ser identificado correctamente no es un error administrativo, es un riesgo de vida.

## 2. LA SOLUCIÓN (Propuesta Técnica)

**Ojo de Paz** es un protocolo de Confianza Cero (Zero Trust) que dota a la embarcación de una identidad digital inalterable sin depender de equipos militares costosos.

*   **Atestación de Hardware**: Uso de chips criptográficos de bajo costo para firmar la posición GPS.
*   **Validación de Integridad**: Un motor de reglas (implementado en Java) que verifica en tiempo real que los datos no han sido manipulados ni grabados (anti-replay).
*   **Soberanía de Datos**: Sistema diseñado para ser operado por autoridades locales u ONGs, garantizando que el pescador sea visto como un civil legítimo.

## 3. DIFERENCIACIÓN (Por qué esto y no lo de siempre)

A diferencia del AIS convencional —fácilmente clonable y costoso—, **Ojo de Paz** se enfoca en la prueba de humanidad.

*   **Bajo Costo**: Implementable con hardware comercial y software de código abierto.
*   **Rigor Operativo**: Diseñado desde la experiencia en seguridad de infraestructuras críticas, donde el error no es una opción.

## 4. ESCALABILIDAD E INDUSTRIALIZACIÓN

Para su internacionalización, el proyecto sigue lineamientos de calidad industrial:
*   **Diseño Modular**: Separación clara entre el núcleo de validación técnica y las interfaces de visualización.
*   **Estándares de Código**: Adherencia estricta a normas de seguridad y documentación profesional, garantizando su mantenimiento a largo plazo.
*   **Prueba de Concepto (PoC)**: Validado mediante el **Proyecto Anfitrion** para su despliegue en mercados internacionales.

## 5. OBJETIVO DEL LLAMADO

Buscamos establecer un Plan Piloto en comunidades pesqueras del Caribe/Pacífico para validar la reducción de incidentes por identificación errónea y fortalecer la paz territorial mediante tecnología transparente.

---

### 🏗 Architecture Overview

```mermaid
graph TD
    A[Vessel Hardware] -->|Secure Token| B[Identity Validator]
    C[GPS Metadata] -->|Signed Payload| B
    B --> D{Triple Verification}
    D -->|Success| E[ValidationResult: SAFE]
    D -->|Failure| F[Security Audit Alert]
```

---

---

## � Project Structure

This project follows **Maven Standard Directory Layout** with proper package organization:

```
ojo-de-paz-core/
├── pom.xml                          # Maven configuration (JUnit 5, Mockito)
├── src/
│   ├── main/java/com/ojodepaz/core/
│   │   └── MaritimeIdentityValidator.java    # Core validation engine
│   └── test/java/com/ojodepaz/core/
│       └── MaritimeIdentityValidatorTest.java # Unit tests with mocks
├── README.md
├── CHANGELOG.md                     # Version history & security fixes
└── SECURITY.md                      # Security policies & disclosure
```

### Package Organization

**`com.ojodepaz.core`** - Core validation logic containing:

| Component | Purpose |
|-----------|---------|
| `MaritimeIdentityValidator` | Main validation engine with challenge-response attestation |
| `VesselData` | Immutable data class for vessel identification |
| `GPSMetadata` | GPS coordinates with timestamp integrity |
| `ValidationResult` | Immutable validation outcome with audit events |
| `SecurityEvent` | Enumeration of security audit events |

### Design Principles

- **Dependency Injection**: `Clock` and `MessageDigest` injected for testability
- **Package-Private Methods**: `validateGPSIntegrity()` accessible for unit testing
- **Immutable Objects**: All data classes are immutable for thread safety
- **Zero Trust Architecture**: Never trust, always verify

---

## �🚀 Quick Start (Usage Example)

Para integrar la validación en tu flujo de datos, utiliza el `MaritimeIdentityValidator` con el protocolo de atestación:

```java
try {
    MaritimeIdentityValidator validator = new MaritimeIdentityValidator();
    
    // 1. Registro del dispositivo (hecho una vez)
    validator.registerDevice("DEVICE-456", publicKeyBase64);
    
    // 2. Generación de Challenge (por el servidor)
    String nonce = validator.generateChallenge("DEVICE-456");
    
    // 3. Recepción de datos firmados por el hardware
    VesselData vesselData = new VesselData(
        "ID-P-456", "DEVICE-456", nonce, signedNonce, gpsMetadata, digitalSignature
    );
    
    // 4. Validación Final
    ValidationResult result = validator.validateMaritimeIdentity(vesselData);
    
    if (result.isValid()) {
        System.out.println("✅ Embarcación Verificada: SAFE");
    } else {
        System.out.println("🚨 Alerta de Seguridad: " + result.getMessage());
    }
} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
    // Manejo de error de configuración criptográfica
}
```

## 📋 Requisitos & Compilación
- **JDK 11+**
- **Maven 3.6+**

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar auditoría (placeholder para Fase 2)
mvn test
```

---

### 📧 Sobre el Autor

**Héctor Enrique** - Perito de Informática con experiencia en protección de infraestructuras críticas (refinerías y puertos), actualmente especializado en desarrollo de software seguro y soluciones criptográficas aplicadas. Su transición desde la seguridad física hacia la ciberseguridad le proporciona una perspectiva única para diseñar sistemas robustos que protegen tanto activos digitales como operaciones críticas.

Este proyecto sigue los [Estándares de Repositorio Héctor Enrique](https://github.com/HectorCorbellini/hector-repo-standard).
---
*This project follows the [Hector Corbellini Engineering Standards](https://github.com/HectorCorbellini/hector-repo-standard).*
