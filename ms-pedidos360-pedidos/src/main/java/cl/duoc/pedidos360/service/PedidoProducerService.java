package cl.duoc.pedidos360.service;

import cl.duoc.pedidos360.dto.PedidoEventoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PedidoProducerService {

    private static final String DIRECT_EXCHANGE = "pedidos360.direct.exchange";
    private static final String TOPIC_EXCHANGE = "pedidos360.topic.exchange";
    private static final String[] DOMINIOS = {"notificaciones", "cocina", "documentos"};

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public PedidoProducerService(RabbitTemplate rabbit, ObjectMapper mapper) {
        this.rabbit = rabbit;
        this.mapper = mapper;
    }

    public PedidoEventoDTO publicarPedido(
            Long pedidoId,
            String clienteEmail,
            boolean falloNotificaciones,
            boolean falloCocina,
            boolean falloDocumentos
    ) throws JsonProcessingException {
        PedidoEventoDTO evento = new PedidoEventoDTO(
                UUID.randomUUID().toString(),
                pedidoId,
                clienteEmail,
                "PEDIDO_ACEPTADO",
                OffsetDateTime.now().toString(),
                falloNotificaciones,
                falloCocina,
                falloDocumentos
        );
        String json = mapper.writeValueAsString(evento);

        for (String dominio : DOMINIOS) {
            enviar(DIRECT_EXCHANGE, dominio, json);
        }
        enviar(TOPIC_EXCHANGE, "pedido.aceptado", json);
        return evento;
    }

    private void enviar(String exchange, String routingKey, String body) {
        rabbit.convertAndSend(exchange, routingKey, body, message -> {
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }
}
