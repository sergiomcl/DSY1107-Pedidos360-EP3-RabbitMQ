package cl.duoc.pedidos360.ms_pedidos360_notificaciones;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.rabbitmq.username=test-user",
		"spring.rabbitmq.password=test-password",
		"spring.rabbitmq.listener.simple.auto-startup=false"
})
class MsPedidos360NotificacionesApplicationTests {

	@Test
	void contextLoads() {
	}

}
