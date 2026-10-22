package cl.duoc.pedidos360.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearExchangeRequest(
        @NotBlank(message = "El nombre del exchange no puede estar vacío")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombreExchange,
        @NotBlank(message = "El tipo no puede estar vacío")
        @Pattern(regexp = "(?i)direct|topic|fanout|headers", message = "El tipo debe ser direct, topic, fanout o headers")
        String tipo,
        Boolean durable
) {
}
