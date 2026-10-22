import base64
import json
import os
import urllib.error
import urllib.request


def consultar() -> None:
    url = os.getenv("RABBIT_API_URL", "http://localhost:15672/api/queues/%2F")
    user = os.environ["RABBIT_API_USER"]
    password = os.environ["RABBIT_API_PASSWORD"]
    token = base64.b64encode(f"{user}:{password}".encode()).decode()
    request = urllib.request.Request(url, headers={"Authorization": f"Basic {token}"})

    try:
        with urllib.request.urlopen(request, timeout=8) as response:
            queues = json.load(response)
    except (urllib.error.URLError, TimeoutError) as error:
        raise SystemExit(f"No se pudo consultar la API de RabbitMQ: {error}") from error

    print("\n--- ESTADO DE COLAS RABBITMQ ---")
    for queue in sorted(queues, key=lambda item: item.get("name", "")):
        name = queue.get("name", "")
        if not name.startswith("pedidos360."):
            continue
        ready = int(queue.get("messages_ready", 0) or 0)
        unacked = int(queue.get("messages_unacknowledged", 0) or 0)
        consumers = int(queue.get("consumers", 0) or 0)
        print(f"{name:42} Ready={ready:<4} Unacked={unacked:<4} Consumidores={consumers}")
        if name.endswith(".dlq") and ready > 0:
            print("  ALERTA: Hay mensajes fallidos en la DLQ.")


if __name__ == "__main__":
    consultar()
