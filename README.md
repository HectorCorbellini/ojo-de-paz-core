# Héctor Enrique

> **Cerrando la brecha entre la seguridad operativa y la verificación digital**

---

## Perfil Profesional

Ingeniero de Seguridad con experiencia en protección de infraestructuras críticas (refinerías y puertos), actualmente especializado en desarrollo de software seguro y soluciones criptográficas aplicadas. Mi transición desde la seguridad física hacia la ciberseguridad me proporciona una perspectiva única para diseñar sistemas robustos que protegen tanto activos digitales como operaciones críticas.

---

## Habilidades Técnicas

### 🔐 **Core Competencies**
- **Java (Core)**: Desarrollo de aplicaciones empresariales seguras y backend robusto
- **Seguridad Lógica**: Diseño de arquitecturas resilientes y defensa en profundidad
- **Criptografía Aplicada**: Implementación de sistemas de verificación y autenticación digital
- **Vibe Coding**: Prototipado ágil y desarrollo rápido de soluciones tecnológicas

### 🛠 **Stack Tecnológico**
- **Lenguajes**: Java, Python, JavaScript
- **Seguridad**: Criptografía asimétrica, firmas digitales, PKI
- **Infraestructura**: Docker, Cloud Computing, DevSecOps
- **Metodologías**: Agile, Secure Development Lifecycle (SDL)

---

## Proyecto Destacado: Ojo de Paz

### 🎯 **Misión**
Desarrollar un sistema de verificación criptográfica para barcos pesqueros que previene ataques erróneos de drones contra civiles mediante firmas digitales de hardware.

### 🔍 **Contexto del Problema**
Los pescadores artesanales en Colombia enfrentan riesgos crecientes debido a:
- Identificación incorrecta por sistemas de vigilancia automatizados
- Ataques de drones militares o de seguridad confundiendo barcos civiles
- Falta de mecanismos de verificación confiables en tiempo real

### 💡 **Solución Propuesta**
**Ojo de Paz** implementa:
- **Firmas Digitales de Hardware**: Dispositivos criptográficos embarcados que generan identificaciones únicas
- **Verificación Criptográfica**: Protocolos seguros para autenticación remota de embarcaciones
- **Integración con Sistemas de Vigilancia**: API para interoperabilidad con plataformas existentes
- **Resistencia a Manipulación**: Hardware seguro que previene falsificación de identidades

### 🛡️ **Principios de Diseño**
- **Zero Trust Architecture**: Nunca confía, siempre verifica.
- **Atribución Criptográfica**: Validación mediante atestación de hardware.
- **Integridad Geográfica**: Control de metadatos GPS para prevención de ataques de replay.

### 🏗 **Arquitectura Técnica**
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Barco Pesquero│    │   Infraestructura│    │   Centro de     │
│   + Dispositivo │◄──►│   de Comunicación│◄──►│   Control       │
│   Criptográfico │    │   Satelital      │    │   de Drones     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### ⚙️ **Cómo Funciona**
El sistema procesa objetos `VesselData` realizando una triple validación:

1. **Criptográfica**: Verifica que el token de hardware coincida con el desafío del sistema mediante atestación TPM/TEE.
2. **Integridad**: Asegura que el payload no haya sido alterado usando firmas SHA-256 con prevención de timing attacks.
3. **Operativa**: Cruza los datos GPS con una ventana de tiempo estricta (5 minutos) para invalidar transmisiones grabadas o fuera de zona geográfica autorizada.

### 🚢 **Ojo de Paz - Core**
**Protocolo de Identidad Marítima Inalterable para la Protección de Civiles**

Este repositorio contiene el núcleo lógico del sistema Ojo de Paz, diseñado para cerrar la brecha entre la seguridad operativa y la verificación digital.

---

## Enfoque Metodológico

### 🔄 **De la Seguridad Física a la Digital**
Mi experiencia en protección de infraestructuras críticas me ha enseñado que:
- La seguridad debe ser multicapa y resiliente
- Los sistemas deben funcionar bajo condiciones adversas
- La verificación de identidad es fundamental en operaciones críticas

### ⚡ **Vibe Coding para Prototipado**
Utilizo metodologías ágiles para:
- Desarrollo rápido de prototipos funcionales
- Validación temprana de conceptos con usuarios reales
- Iteración continua basada en feedback del terreno

---

## ⚙️ **Lógica de Validación**

El sistema implementa una arquitectura de confianza cero con tres capas de verificación independientes:

### 🔐 **Validación Criptográfica**
```java
validateHardwareAttestation(hardwareToken)
```
- Protocolo de desafío-respuesta con `MessageDigest.isEqual()`
- Prevención de timing attacks en comparaciones de hash
- Atestación de hardware TPM/TEE simulada

### 🛡️ **Validación de Integridad**
```java
validateDigitalSignature(vesselData)
```
- Verificación SHA-256 del payload completo
- Comparación segura de firmas con `MessageDigest.isEqual()`
- Cadena de confianza criptográfica de extremo a extremo

### 🌍 **Validación Operativa**
```java
validateGPSIntegrity(gpsMetadata)
```
- Ventana de tiempo estricta: 300 segundos de tolerancia
- Validación geográfica: área marítima de Colombia
- Precisión GPS: 6 decimales (≈0.1m)
- Validación matemática independiente de Locale

### 📊 **Flujo de Decisión**
```
VesselData → Hardware Attestation → Digital Signature → GPS Integrity → ValidationResult
     ↓              ↓                      ↓                   ↓              ↓
   Input      ¿Token válido?        ¿Firma válida?     ¿GPS válido?   ✓/✗ + Audit Log
```

📧 **Para consultas técnicas y colaboraciones:**
- Seguridad de infraestructuras críticas
- Implementación de soluciones criptográficas
- Desarrollo de sistemas de verificación digital

🌐 **Áreas de Interés para Colaboración:**
- Ciberseguridad en sectores marítimos
- Sistemas de identificación segura
- Tecnología para protección de civiles

---

> *"La verdadera seguridad no solo protege los sistemas, sino que preserva la vida humana y las operaciones críticas que sostienen nuestra sociedad."*

---

*📍 Basado en Latinoamérica | 🌊 Enfocado en soluciones marítimas | 🔐 Especialista en seguridad digital*
