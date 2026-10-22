package cl.duoc.pedidos360.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearBindingRequest(
        @NotBlank(message = "El nombre de la cola no puede estar vacío")
        @Size(max = 255, message = "El nombre de la cola no puede superar 255 caracteres")
        String nombreCola,
        @NotBlank(message = "El nombre del exchange no puede estar vacío")
        @Size(max = 255, message = "El nombre del exchange no puede superar 255 caracteres")
        String nombreExchange,
        String routingKey
) {
}
