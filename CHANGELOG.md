# Changelog - Ojo de Paz

Todos los cambios notables en este proyecto se documentarán en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-03-12

### Added
- 🚀 **Proyecto Ojo de Paz** - Sistema de verificación criptográfica para barcos pesqueros
- 🔐 **MaritimeIdentityValidator.java** - Componente core de validación de identidad marítima

### Changed
- 📝 **Documentation Refactor**: Migración de plantillas e identidad de marca al repositorio central de estándares para mejorar la mantenibilidad y el enfoque en el core.

### Security Features
- **Atestación de Hardware**: Protocolo de desafío-respuesta para verificación de dispositivos criptográficos
- **Firma Digital**: Verificación de integridad de datos mediante SHA-256
- **Validación GPS**: Verificación de timestamp y coordenadas geográficas
- **Sistema de Auditoría**: Logs estructurados para eventos de seguridad

### Technical Implementation
- **Zero Trust Architecture**: Principios de confianza cero en cada componente
- **Timing Attack Prevention**: Uso de `MessageDigest.isEqual()` en comparaciones críticas
- **Dependency Injection**: Inyección de `Clock` y `MessageDigest` para tests unitarios
- **Locale Independence**: Validación matemática de coordenadas GPS independiente de configuración regional

### Security Events
- `VALIDATION_SUCCESS` - Validación exitosa
- `HARDWARE_ATTESTATION_FAILED` - Fallo en atestación de hardware
- `DIGITAL_SIGNATURE_INVALID` - Firma digital inválida
- `GPS_INTEGRITY_VIOLATION` - Violación de integridad GPS
- `SYSTEM_ERROR` - Error interno del sistema

### Data Models
- `VesselData` - Datos de embarcación para validación
- `GPSMetadata` - Metadatos GPS con timestamp y coordenadas
- `ValidationResult` - Resultado de validación con detalles de auditoría

### Configuration
- **GPS Coordinate Precision**: 6 decimales (≈0.1m de precisión)
- **Timestamp Drift Tolerance**: 300 segundos (5 minutos)
- **Geographic Bounds**: Área marítima de Colombia (-4.0° a 13.0° lat, -82.0° a -66.0° lon)

---

## [Planeado] - Próximas Versiones

### [0.2.0] - Tests Unitarios
- 🧪 **JUnit Tests** - Suite completa de tests unitarios
- 📊 **Mock Objects** - Tests con inyección de dependencias
- 🔍 **Edge Cases** - Validación de casos límite y errores

### [0.3.0] - Integración Criptográfica
- 🔑 **BouncyCastle** - Integración con biblioteca criptográfica profesional
- 📜 **PKI Integration** - Certificados digitales para hardware
- 🔐 **ECDSA** - Firma digital con curvas elípticas

### [0.4.0] - API REST
- 🌐 **Spring Boot** - API REST para integración con sistemas de drones
- 📡 **WebSocket** - Comunicación en tiempo real
- 🛡️ **Rate Limiting** - Protección contra abusos

### [1.0.0] - Producción
- 🚀 **Docker** - Contenedores para despliegue
- 📊 **Monitoring** - Métricas y alertas
- 📋 **Documentation** - API docs y guía de despliegue

---

## Notas de Seguridad

### Version 0.1.0
- **Estado**: Prototipo funcional con simulación criptográfica
- **Producción**: No recomendado - usar solo para desarrollo y pruebas
- **Mejoras Críticas**: Implementar criptografía real (BouncyCastle) antes de producción

### Threat Model
- **Timing Attacks**: Mitigado con `MessageDigest.isEqual()`
- **Replay Attacks**: Mitigado con validación de timestamp
- **GPS Spoofing**: Mitigado con validación geográfica y precisión
- **Hardware Tampering**: Mitigado con atestación de hardware

---

## Colaboradores

- **Héctor Enrique** - Arquitecto de Seguridad y Desarrollador Principal

---

## Licencia

Este proyecto es parte de la iniciativa **Ojo de Paz** para la protección de pescadores artesanales en Colombia.

---

*Para más detalles técnicos, revisar el código fuente y la documentación del proyecto.*
