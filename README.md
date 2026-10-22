# Pedidos360 - RabbitMQ

Implementación de EP3 con tres microservicios Spring Boot 3.3.5, Java 21 y un clúster RabbitMQ de dos nodos.

## Requisitos

- JDK 21
- Docker Desktop con Docker Compose
- Python 3 para el monitor opcional

## Configurar credenciales locales

Desde la raíz del repositorio, crea un archivo `.env` a partir de `.env.example` y reemplaza los tres valores de ejemplo por credenciales locales y una cookie aleatoria compartida entre los nodos. El archivo `.env` está excluido de Git.

Para las aplicaciones Spring y el monitor, define estas variables en cada terminal donde los ejecutes, utilizando los mismos valores configurados en `.env`:

```powershell
$env:RABBITMQ_USER = "tu-usuario-local"
$env:RABBITMQ_PASSWORD = "tu-clave-local"
$env:RABBIT_API_USER = "tu-usuario-local"
$env:RABBIT_API_PASSWORD = "tu-clave-local"
```

## Iniciar RabbitMQ

Desde la raíz del repositorio en PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File infra\01_Inicializar_Cluster.ps1
```

La interfaz de administración está disponible en `http://localhost:15672` y `http://localhost:15673`. Usa las credenciales locales configuradas en `.env`.

## Iniciar los microservicios

Abre una terminal por microservicio. Configura las variables anteriores en cada una, y luego ejecuta:

```powershell
cd ms-pedidos360-notificaciones
.\mvnw.cmd spring-boot:run
```

```powershell
cd ms-pedidos360-pedidos
.\mvnw.cmd spring-boot:run
```

```powershell
cd ms-pedidos360-admin
.\mvnw.cmd spring-boot:run
```

El consumidor declara los exchanges y colas durable, las colas de retry con TTL de cinco segundos y las DLQ antes de recibir mensajes.

## Verificar el flujo de pedidos

Pedido exitoso (HTTP 202):

```powershell
curl.exe -X POST "http://localhost:8081/api/pedidos/aceptar?pedidoId=101&clienteEmail=cliente@duoc.cl"
```

Forzar fallos en notificaciones para comprobar dos reintentos y el envío a la DLQ:

```powershell
curl.exe -X POST "http://localhost:8081/api/pedidos/aceptar?pedidoId=102&clienteEmail=error@duoc.cl&falloNotificaciones=true"
```

El evento se publica también en la cola de monitoreo y se procesa de manera independiente en cocina, documentos y notificaciones.

## Administrar RabbitMQ por REST

Crear una cola (HTTP 200) y comprobar validación (HTTP 400):

```powershell
curl.exe -X POST "http://localhost:8083/api/admin/queues" -H "Content-Type: application/json" -d '{\"nombreCola\":\"q.prueba.evaluacion\"}'
curl.exe -X POST "http://localhost:8083/api/admin/queues" -H "Content-Type: application/json" -d '{\"nombreCola\":\"\"}'
curl.exe -X DELETE "http://localhost:8083/api/admin/queues/q.prueba.evaluacion"
```

También están disponibles `POST /api/admin/exchanges`, `DELETE /api/admin/exchanges/{nombre}` y `POST /api/admin/bindings`. Los tipos de exchange válidos son `direct`, `topic`, `fanout` y `headers`.

## Monitorear colas

Con `RABBIT_API_USER` y `RABBIT_API_PASSWORD` definidas en la terminal:

```powershell
python monitor\monitor_queues.py
```

## Ejecutar pruebas

Desde cada proyecto ejecuta `.\mvnw.cmd test`. Las pruebas no requieren un broker activo salvo que explícitamente se inicie el contexto con los listeners conectados.
