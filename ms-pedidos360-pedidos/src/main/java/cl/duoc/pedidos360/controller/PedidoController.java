package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.dto.PedidoEventoDTO;
import cl.duoc.pedidos360.service.PedidoProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoProducerService producer;

    public PedidoController(PedidoProducerService producer) {
        this.producer = producer;
    }

    @PostMapping("/aceptar")
    public ResponseEntity<PedidoEventoDTO> aceptar(
            @RequestParam @Positive Long pedidoId,
            @RequestParam @NotBlank @Email String clienteEmail,
            @RequestParam(defaultValue = "false") boolean falloNotificaciones,
            @RequestParam(defaultValue = "false") boolean falloCocina,
            @RequestParam(defaultValue = "false") boolean falloDocumentos
    ) throws JsonProcessingException {
        PedidoEventoDTO evento = producer.publicarPedido(
                pedidoId, clienteEmail, falloNotificaciones, falloCocina, falloDocumentos
        );
        return ResponseEntity.accepted().body(evento);
    }
}
