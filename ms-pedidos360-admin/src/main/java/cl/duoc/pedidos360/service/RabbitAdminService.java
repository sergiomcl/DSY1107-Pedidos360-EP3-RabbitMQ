package cl.duoc.pedidos360.service;

import cl.duoc.pedidos360.dto.CrearBindingRequest;
import cl.duoc.pedidos360.dto.CrearExchangeRequest;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Locale;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void crearCola(String nombre, boolean durable) {
        Queue queue = durable
                ? QueueBuilder.durable(nombre).build()
                : QueueBuilder.nonDurable(nombre).build();
        amqpAdmin.declareQueue(queue);
    }

    public void crearExchange(CrearExchangeRequest request) {
        boolean durable = !Boolean.FALSE.equals(request.durable());
        Exchange exchange = switch (request.tipo().toLowerCase(Locale.ROOT)) {
            case "direct" -> new DirectExchange(request.nombreExchange(), durable, false);
            case "topic" -> new TopicExchange(request.nombreExchange(), durable, false);
            case "fanout" -> new FanoutExchange(request.nombreExchange(), durable, false);
            case "headers" -> new HeadersExchange(request.nombreExchange(), durable, false);
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + request.tipo());
        };
        amqpAdmin.declareExchange(exchange);
    }

    public void crearBinding(CrearBindingRequest request) {
        Binding binding = new Binding(
                request.nombreCola(),
                Binding.DestinationType.QUEUE,
                request.nombreExchange(),
                request.routingKey() == null ? "" : request.routingKey(),
                Map.of()
        );
        amqpAdmin.declareBinding(binding);
    }

    public boolean eliminarCola(String nombre) {
        return amqpAdmin.deleteQueue(nombre);
    }

    public boolean eliminarExchange(String nombre) {
        return amqpAdmin.deleteExchange(nombre);
    }
}
