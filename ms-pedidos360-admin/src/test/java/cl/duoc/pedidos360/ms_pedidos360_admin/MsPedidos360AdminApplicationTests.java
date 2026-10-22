package cl.duoc.pedidos360.ms_pedidos360_admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import cl.duoc.pedidos360.service.RabbitAdminService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
		"spring.rabbitmq.username=test-user",
		"spring.rabbitmq.password=test-password"
})
class MsPedidos360AdminApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RabbitAdminService adminService;

	@Test
	void contextLoads() {
	}

	@Test
	void crearColaRechazaNombreVacio() throws Exception {
		mockMvc.perform(post("/api/admin/queues")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombreCola\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.nombreCola").exists());
		verifyNoInteractions(adminService);
	}

	@Test
	void crearColaValidaDelegaEnServicio() throws Exception {
		mockMvc.perform(post("/api/admin/queues")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombreCola\":\"q.prueba.evaluacion\"}"))
				.andExpect(status().isOk());
		verify(adminService).crearCola("q.prueba.evaluacion", true);
	}
}
