# Shopping Cart PWA

Referencia full-stack con backend JHipster/Spring Boot separado, frontend Ionic Angular PWA, PostgreSQL, Cypress, Docker, ELK y Jenkins.

## Arquitectura

```text
Ionic PWA :8088/8100 ── /api ──> JHipster :8080 ──> PostgreSQL :5432
          IndexedDB                 stdout ── GELF ──> Logstash ──> Elasticsearch ──> Kibana :5601
```

El repositorio contiene:

- `backend/`: JHipster 9.2.0, Java 21, Maven, JWT y PostgreSQL.
- `frontend/`: Ionic Angular, Service Worker, IndexedDB y Cypress.
- `compose.yml`: aplicación, base de datos y perfil opcional ELK.
- `infra/jenkins/`: Jenkins LTS con Java 21, Node 24 y Docker CLI.
- `shopping-cart.jdl`: modelo reproducible del dominio.

## Fase 1: preparación de Ubuntu 24.04

Instala Java, Git, curl y las bibliotecas usadas por Cypress:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk git curl ca-certificates build-essential \
  libgtk2.0-0t64 libgtk-3-0t64 libgbm-dev libnotify-dev libnss3 \
  libxss1 libasound2t64 libxtst6 xauth xvfb
java -version
git --version
```

Instala Node.js 24 LTS con NVM y las CLI fijadas por el proyecto:

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source "$HOME/.nvm/nvm.sh"
nvm install 24
nvm alias default 24
npm install -g @ionic/cli@7.2.1 generator-jhipster@9.2.0
node --version
npm --version
ionic --version
jhipster --version
```

Cypress se instala localmente con el frontend:

```bash
cd frontend
npm ci
npx cypress install
npx cypress verify
cd ..
```

Docker Desktop debe mostrar cliente y servidor:

```bash
docker version
docker compose version
```

### Jenkins en Docker

En WSL con Docker Desktop normalmente se usa `/var/run/docker.sock`. En Docker Desktop nativo para Linux, usa `/home/TU_USUARIO/.docker/desktop/docker.sock`.

```bash
export DOCKER_DESKTOP_SOCKET=/var/run/docker.sock
export DOCKER_SOCKET_GID="$(stat -c '%g' "$DOCKER_DESKTOP_SOCKET")"
docker compose -f infra/jenkins/compose.yml up -d --build
docker compose -f infra/jenkins/compose.yml exec jenkins \
  cat /var/jenkins_home/secrets/initialAdminPassword
```

Abre `http://localhost:8081` e instala los plugins sugeridos más **Pipeline**, **Git** y **Credentials Binding**.

## Fase 2: backend y modelo JDL

La base existente se reprodujo con estos comandos:

```bash
mkdir backend
cd backend
jhipster jdl ../shopping-cart.jdl --skip-install --skip-git
cd ..
```

El JDL corrige la relación original agregando `Customer.email`. JHipster genera las entidades, migraciones Liquibase, DTO MapStruct, servicios y pruebas CRUD.

La API de negocio adicional es:

- `GET /api/products`: catálogo público paginado.
- `GET /api/cart`: obtiene o crea el carrito `PENDING` del usuario JWT.
- `PUT /api/cart/items/{productId}` con `{ "quantity": 2 }`: fija una cantidad absoluta e idempotente.

El servicio valida el stock y calcula `CartItem.totalPrice` y `ShoppingCart.totalPrice`. Los CRUD directos de clientes, carritos e ítems quedan reservados a `ROLE_ADMIN`.

Para desarrollar con PostgreSQL dockerizado:

Creá un `.env` local (está ignorado por Git) y reemplazá los valores antes de usar un entorno compartido:

```dotenv
POSTGRES_DB=shoppingCart
POSTGRES_USER=shoppingCart
POSTGRES_PASSWORD=change-me-local
JWT_BASE64_SECRET=<resultado-de-openssl-rand-base64-64>
ELASTIC_VERSION=9.5.2
ELASTIC_JAVA_OPTS=-Xms512m -Xmx512m
```

```bash
openssl rand -base64 64
docker compose up -d postgresql
cd backend
SPRING_DOCKER_COMPOSE_ENABLED=false ./mvnw
```

Pruebas backend:

```bash
cd backend
./mvnw test
./mvnw verify
./mvnw -Dtest=CurrentCartServiceTest test
./mvnw -Dit.test=ProductResourceIT,CustomerResourceIT,ShoppingCartResourceIT,CartItemResourceIT verify
```

`test` ejecuta las pruebas unitarias rápidas; `verify` agrega las suites `*IT` contra PostgreSQL mediante Testcontainers y es el comando usado por Jenkins.

## Fase 3: frontend Ionic PWA

La aplicación original se creó así:

```bash
ionic start frontend blank --type=angular-standalone --capacitor --no-git
cd frontend
ng add @angular/pwa
npm install idb
```

Para desarrollo, el proxy de Angular dirige `/api` a `http://localhost:8080`:

```bash
cd frontend
npm ci
npm start
```

Abre `http://localhost:8100` e ingresa con `user / user`. El Service Worker se activa solamente en compilaciones de producción; almacena el app shell y el catálogo. Las modificaciones offline se guardan por usuario en IndexedDB y se sincronizan al regresar la conexión. Un 401 conserva la cola hasta un nuevo login y un 409 marca el conflicto de stock.

Para comprobar el modo offline usa el frontend Dockerizado en `http://localhost:8088`, carga el catálogo una vez y cambia DevTools → Network → Offline. Los Service Workers requieren HTTPS fuera de `localhost`.

## Fase 4: Cypress E2E

Con backend y frontend de desarrollo en ejecución:

```bash
cd frontend
npm run e2e:open
# o en modo headless
npm run e2e
```

El comando personalizado `loginByApi` llama directamente a `POST /api/authenticate`, guarda `id_token` con las mismas claves que Ionic y reutiliza la sesión con `cy.session()`. El escenario valida el producto fijo `Producto E2E`, agrega una unidad por UI y confirma el carrito por API.

Las credenciales pueden reemplazarse sin editar el test:

```bash
CYPRESS_username=user CYPRESS_password=user npm run e2e
```

## Fase 5: Docker, PostgreSQL y ELK

Arranca la aplicación con PostgreSQL persistente:

```bash
docker compose up -d --build postgresql backend frontend
docker compose ps
curl http://localhost:8080/management/health
```

- PWA: `http://localhost:8088`
- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`, volumen `postgres-data`

Arranca también Elasticsearch, Logstash y Kibana:

```bash
docker compose --profile observability up -d --build
docker compose restart backend frontend
curl http://localhost:9200/_cluster/health
```

Abre `http://localhost:5601`, crea un Data View `shopping-cart-*` y selecciona `@timestamp`. Backend y Nginx envían stdout/stderr a Logstash mediante GELF UDP 12201.

La seguridad de Elastic está desactivada solo para este laboratorio. Antes de producción se deben habilitar TLS y autenticación, cambiar todas las claves de `.env`, eliminar el contexto Liquibase `faker` y servir la PWA detrás de HTTPS.

Para detener sin borrar datos:

```bash
docker compose --profile observability down
```

Para borrar también los volúmenes locales, ejecuta explícitamente `docker compose --profile observability down --volumes`.

## Fase 6: Jenkins CI

En Jenkins crea una credencial global de tipo **Username with password**:

- ID: `dockerhub-creds`
- Username: usuario/organización de Docker Hub.
- Password: access token de Docker Hub, no la contraseña de la cuenta.

Para un repositorio privado configura además la credencial Git/SSH en el job. Crea un Pipeline desde SCM apuntando a este repositorio. El `Jenkinsfile` ejecuta checkout, pruebas backend, lint/tests/build frontend, construye ambas imágenes, ejecuta Cypress contra contenedores y publica:

```text
DOCKERHUB_USER/shopping-cart-backend:<BUILD_NUMBER>
DOCKERHUB_USER/shopping-cart-backend:latest
DOCKERHUB_USER/shopping-cart-frontend:<BUILD_NUMBER>
DOCKERHUB_USER/shopping-cart-frontend:latest
```

El pipeline limpia únicamente el proyecto Compose efímero de ese build en el bloque `post`.
