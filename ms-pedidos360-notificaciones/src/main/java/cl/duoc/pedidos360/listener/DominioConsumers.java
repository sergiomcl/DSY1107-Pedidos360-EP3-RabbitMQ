package cl.duoc.pedidos360.listener;

import cl.duoc.pedidos360.service.ProcesadorMensajes;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DominioConsumers {

    private final ProcesadorMensajes processor;

    public DominioConsumers(ProcesadorMensajes processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "pedidos360.notificaciones.queue")
    public void notificaciones(Message message, Channel channel) throws IOException {
        processor.procesar("notificaciones", message, channel);
    }

    @RabbitListener(queues = "pedidos360.cocina.queue")
    public void cocina(Message message, Channel channel) throws IOException {
        processor.procesar("cocina", message, channel);
    }

    @RabbitListener(queues = "pedidos360.documentos.queue")
    public void documentos(Message message, Channel channel) throws IOException {
        processor.procesar("documentos", message, channel);
    }
}
