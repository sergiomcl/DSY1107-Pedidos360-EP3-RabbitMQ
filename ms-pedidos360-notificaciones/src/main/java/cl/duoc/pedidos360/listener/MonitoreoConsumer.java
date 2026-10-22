package cl.duoc.pedidos360.listener;

import cl.duoc.pedidos360.dto.PedidoEventoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MonitoreoConsumer {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoConsumer.class);
    private final ObjectMapper mapper;

    public MonitoreoConsumer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @RabbitListener(queues = "pedidos360.monitoreo.queue")
    public void monitorear(String body) throws Exception {
        PedidoEventoDTO evento = mapper.readValue(body, PedidoEventoDTO.class);
        log.info("[MONITOREO] evento={} pedido={} tipo={}",
                evento.eventoId(), evento.pedidoId(), evento.tipo());
    }
}
