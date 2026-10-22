package cl.duoc.pedidos360.ms_pedidos360_pedidos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.rabbitmq.username=test-user",
		"spring.rabbitmq.password=test-password"
})
class MsPedidos360PedidosApplicationTests {

	@Test
	void contextLoads() {
	}

}
