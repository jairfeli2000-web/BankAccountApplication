# Proceso de Despliegue — Bank Account Microservice

Este documento describe el proceso de despliegue del microservicio bancario y su frontend, desde desarrollo local hasta un entorno productivo en la nube.

---

## 1. Estrategia general

| Entorno | Base de datos | Backend | Frontend |
|---|---|---|---|
| **Desarrollo** | H2 (in-memory) | `gradlew bootRun` :8080 | `npm run dev` :5173 |
| **Docker local** | PostgreSQL 16 | Contenedor :8080 | Nginx :80 |
| **Producción** | PostgreSQL (RDS/Cloud SQL) | Contenedor en ECS/K8s | CDN + Nginx/S3 |

### Decisiones arquitectónicas

- **Contenedorización**: Docker multi-stage para imágenes ligeras (build + runtime separados).
- **Base de datos**: H2 solo en desarrollo; PostgreSQL en despliegue real por persistencia y escalabilidad.
- **Frontend**: Build estático servido por Nginx con proxy reverso hacia el backend.
- **Perfiles Spring**: `default` (H2) y `prod` (PostgreSQL) activados por variable de entorno.

---

## 2. Despliegue local con Docker (recomendado para demo)

### Requisitos

- Docker Desktop 4.x+
- Docker Compose v2

### Pasos

```bash
# 1. Clonar el repositorio
git clone <url-repositorio>
cd BankAccountApplication

# 2. Construir y levantar todos los servicios
docker compose up --build -d

# 3. Verificar que los contenedores estén activos
docker compose ps

# 4. Probar la API
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"titular": "Juan Perez"}'
```

### URLs disponibles

| Servicio | URL |
|---|---|
| Portal web | http://localhost |
| API REST | http://localhost:8080/accounts |
| Swagger | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 (user: `bank`, pass: `bank`) |

### Detener servicios

```bash
docker compose down          # Detener contenedores
docker compose down -v       # Detener y eliminar volúmenes (borra datos)
```

---

## 3. Pipeline CI/CD propuesto

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Commit  │───►│   Build  │───►│   Test   │───►│  Docker  │───►│  Deploy  │
│  (Git)   │    │  Gradle  │    │  JUnit   │    │  Push    │    │  Cloud   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Etapas del pipeline

1. **Build**: `./gradlew build` — compila y empaqueta el JAR.
2. **Test**: `./gradlew test` — ejecuta pruebas unitarias (JUnit + Mockito).
3. **Docker Build**: construye imágenes `bank-backend` y `bank-frontend`.
4. **Push**: sube imágenes al registry (Docker Hub, ECR, ACR).
5. **Deploy**: despliega en el entorno target (staging → producción).

### Ejemplo GitHub Actions (`.github/workflows/ci-cd.yml`)

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: chmod +x gradlew && ./gradlew build

  docker-deploy:
    needs: build-and-test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: docker compose build
      - run: docker compose push   # Requiere registry configurado
```

---

## 4. Despliegue en la nube (AWS — referencia)

### Arquitectura propuesta

```
Internet
    │
    ▼
┌─────────────┐
│  Route 53   │  DNS
└──────┬──────┘
       │
┌──────▼──────┐
│     ALB     │  Load Balancer
└──┬───────┬──┘
   │       │
   ▼       ▼
┌──────┐ ┌──────┐
│ ECS  │ │ ECS  │  Frontend (Nginx) + Backend (Spring Boot)
│Task 1│ │Task 2│
└──┬───┘ └──┬───┘
   │        │
   └────┬───┘
        ▼
┌──────────────┐
│  RDS         │  PostgreSQL
│  (Multi-AZ)  │
└──────────────┘
```

### Pasos de despliegue en AWS

1. **Crear repositorio ECR** para las imágenes Docker.
2. **Push de imágenes**:
   ```bash
   aws ecr get-login-password | docker login --username AWS --password-stdin <account>.dkr.ecr.<region>.amazonaws.com
   docker tag bank-backend:latest <account>.dkr.ecr.<region>.amazonaws.com/bank-backend:latest
   docker push <account>.dkr.ecr.<region>.amazonaws.com/bank-backend:latest
   ```
3. **Crear RDS PostgreSQL** (db.t3.micro para demo).
4. **Crear ECS Cluster** con Task Definition:
   - Backend: puerto 8080, variables `SPRING_PROFILES_ACTIVE=prod`, `DB_HOST=<rds-endpoint>`.
   - Frontend: puerto 80.
5. **Configurar ALB** con reglas de routing:
   - `/accounts/*` → target group backend
   - `/*` → target group frontend
6. **Variables de entorno** vía AWS Secrets Manager (credenciales DB).

### Alternativas cloud

| Proveedor | Servicio contenedores | Base de datos |
|---|---|---|
| AWS | ECS Fargate / EKS | RDS PostgreSQL |
| Azure | Container Apps / AKS | Azure Database for PostgreSQL |
| GCP | Cloud Run / GKE | Cloud SQL PostgreSQL |

---

## 5. Variables de entorno

| Variable | Descripción | Default (Docker) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil Spring (`default` o `prod`) | `prod` |
| `DB_HOST` | Host de PostgreSQL | `postgres` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `bankdb` |
| `DB_USER` | Usuario de la base de datos | `bank` |
| `DB_PASSWORD` | Contraseña de la base de datos | `bank` |

> En producción, las credenciales deben gestionarse con un secrets manager (AWS Secrets Manager, Azure Key Vault, etc.), nunca en texto plano.

---

## 6. Monitoreo y health checks

### Endpoints de salud

Spring Boot Actuator puede agregarse para endpoints `/actuator/health`. Actualmente se verifica con:

```bash
curl http://localhost:8080/accounts/1/balance
```

### Logs

```bash
# Ver logs del backend en Docker
docker compose logs -f backend

# Ver logs del frontend
docker compose logs -f frontend
```

---

## 7. Rollback

En caso de fallo en producción:

1. Identificar la versión estable anterior en el registry.
2. Redesplegar la imagen anterior:
   ```bash
   docker pull <registry>/bank-backend:<tag-anterior>
   docker compose up -d backend
   ```
3. Verificar health check y logs.
4. En ECS/K8s: revertir al Task Definition / Deployment anterior.

---

## 8. Checklist pre-despliegue

- [ ] Tests unitarios pasan (`./gradlew test`)
- [ ] Build exitoso (`./gradlew build`)
- [ ] Imágenes Docker construidas sin errores
- [ ] Variables de entorno configuradas (DB, perfiles)
- [ ] Credenciales en secrets manager (no en código)
- [ ] CORS configurado para el dominio de producción
- [ ] Swagger deshabilitado o restringido en producción
- [ ] Logs accesibles para diagnóstico
- [ ] Backup de base de datos configurado
