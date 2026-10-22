package cl.duoc.pedidos360.service;

import cl.duoc.pedidos360.config.RabbitConfig;
import cl.duoc.pedidos360.dto.PedidoEventoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcesadorMensajes {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajes.class);
    private static final int REINTENTOS_MAX = 2;
    private static final String HEADER_INTENTOS = "x-lab-intentos";

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public ProcesadorMensajes(RabbitTemplate rabbit, ObjectMapper mapper) {
        this.rabbit = rabbit;
        this.mapper = mapper;
    }

    public void procesar(String dominio, Message mensaje, Channel channel) throws IOException {
        long deliveryTag = mensaje.getMessageProperties().getDeliveryTag();
        int intentos = leerIntentos(mensaje);
        try {
            PedidoEventoDTO evento = mapper.readValue(mensaje.getBody(), PedidoEventoDTO.class);
            boolean falla = switch (dominio) {
                case "notificaciones" -> evento.falloNotificaciones();
                case "cocina" -> evento.falloCocina();
                case "documentos" -> evento.falloDocumentos();
                default -> throw new IllegalArgumentException("Dominio no reconocido: " + dominio);
            };
            if (falla) {
                throw new IllegalStateException("Fallo de prueba en dominio " + dominio);
            }
            log.info("[{}] Pedido {} procesado. Intento={}", dominio, evento.pedidoId(), intentos + 1);
        } catch (Exception error) {
            log.warn("[{}] Falló el procesamiento del mensaje. Intento={}/{}. Detalle={}",
                    dominio, intentos + 1, REINTENTOS_MAX + 1, error.getMessage());
            reenviarFallido(dominio, mensaje, channel, deliveryTag, intentos);
            return;
        }
        channel.basicAck(deliveryTag, false);
    }

    private void reenviarFallido(
            String dominio,
            Message mensaje,
            Channel channel,
            long deliveryTag,
            int intentos
    ) throws IOException {
        if (intentos >= REINTENTOS_MAX) {
            log.error("[{}] Reintentos agotados. Mensaje enviado a DLQ.", dominio);
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setHeader(HEADER_INTENTOS, intentos + 1);
        properties.setHeader("x-lab-origen", dominio);
        try {
            rabbit.send(RabbitConfig.RETRY, dominio, new Message(mensaje.getBody(), properties));
        } catch (RuntimeException publishError) {
            log.error("[{}] No se pudo publicar el reintento; el mensaje original se reencola.",
                    dominio, publishError);
            channel.basicNack(deliveryTag, false, true);
            return;
        }
        channel.basicAck(deliveryTag, false);
        log.info("[{}] Mensaje enviado a retry con TTL de 5 segundos. Reintento={}",
                dominio, intentos + 1);
    }

    private int leerIntentos(Message mensaje) {
        Object value = mensaje.getMessageProperties().getHeaders().get(HEADER_INTENTOS);
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return 0;
    }
}
