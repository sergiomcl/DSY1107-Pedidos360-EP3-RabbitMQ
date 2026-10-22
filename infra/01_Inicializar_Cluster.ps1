$ErrorActionPreference = "Stop"

docker compose --env-file .env -f infra/docker-compose.cluster.yml up -d
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo iniciar RabbitMQ con Docker Compose."
}

Write-Host "Esperando que ambos nodos RabbitMQ inicien..."
docker exec pedidos360-rabbit1 rabbitmqctl await_startup
if ($LASTEXITCODE -ne 0) {
    throw "rabbit1 no inició correctamente."
}
docker exec pedidos360-rabbit2 rabbitmqctl await_startup
if ($LASTEXITCODE -ne 0) {
    throw "rabbit2 no inició correctamente."
}

$clusterStatus = docker exec pedidos360-rabbit2 rabbitmqctl cluster_status
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo consultar el estado de rabbit2."
}

if (($clusterStatus -join "`n") -notmatch "rabbit@rabbit1") {
    Write-Host "Uniendo rabbit2 al clúster rabbit1..."
    docker exec pedidos360-rabbit2 rabbitmqctl stop_app
    if ($LASTEXITCODE -ne 0) { throw "No se pudo detener la aplicación RabbitMQ en rabbit2." }
    docker exec pedidos360-rabbit2 rabbitmqctl reset
    if ($LASTEXITCODE -ne 0) { throw "No se pudo reiniciar la configuración de rabbit2." }
    docker exec pedidos360-rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
    if ($LASTEXITCODE -ne 0) { throw "No se pudo unir rabbit2 al clúster." }
    docker exec pedidos360-rabbit2 rabbitmqctl start_app
    if ($LASTEXITCODE -ne 0) { throw "No se pudo iniciar RabbitMQ en rabbit2." }
}

docker exec pedidos360-rabbit1 rabbitmqctl cluster_status
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo verificar el estado final del clúster."
}
Write-Host "Clúster de dos nodos listo. UI 1: http://localhost:15672 | UI 2: http://localhost:15673"
