package cl.duoc.pedidos360.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitConfig {

    public static final String DIRECT = "pedidos360.direct.exchange";
    public static final String TOPIC = "pedidos360.topic.exchange";
    public static final String RETRY = "pedidos360.retry.exchange";
    public static final String DLX = "pedidos360.dlx";
    public static final String MONITOREO = "pedidos360.monitoreo.queue";
    public static final String[] DOMINIOS = {"notificaciones", "cocina", "documentos"};

    public static String principal(String dominio) {
        return "pedidos360." + dominio + ".queue";
    }

    public static String reintento(String dominio) {
        return "pedidos360." + dominio + ".retry";
    }

    public static String dlq(String dominio) {
        return "pedidos360." + dominio + ".dlq";
    }

    @Bean
    public Declarables mensajeriaDeclarables() {
        DirectExchange mainExchange = new DirectExchange(DIRECT, true, false);
        TopicExchange topicExchange = new TopicExchange(TOPIC, true, false);
        DirectExchange retryExchange = new DirectExchange(RETRY, true, false);
        DirectExchange deadExchange = new DirectExchange(DLX, true, false);

        List<Declarable> elements = new ArrayList<>(List.of(
                mainExchange, topicExchange, retryExchange, deadExchange
        ));

        Queue monitorQueue = QueueBuilder.durable(MONITOREO).build();
        elements.add(monitorQueue);
        elements.add(BindingBuilder.bind(monitorQueue).to(topicExchange).with("pedido.*"));

        for (String domain : DOMINIOS) {
            Queue mainQueue = QueueBuilder.durable(principal(domain))
                    .deadLetterExchange(DLX)
                    .deadLetterRoutingKey(domain + ".failed")
                    .build();
            Queue retryQueue = QueueBuilder.durable(reintento(domain))
                    .ttl(5000)
                    .deadLetterExchange(DIRECT)
                    .deadLetterRoutingKey(domain)
                    .build();
            Queue deadQueue = QueueBuilder.durable(dlq(domain)).build();

            elements.add(mainQueue);
            elements.add(BindingBuilder.bind(mainQueue).to(mainExchange).with(domain));
            elements.add(retryQueue);
            elements.add(BindingBuilder.bind(retryQueue).to(retryExchange).with(domain));
            elements.add(deadQueue);
            elements.add(BindingBuilder.bind(deadQueue).to(deadExchange).with(domain + ".failed"));
        }
        return new Declarables(elements);
    }
}
