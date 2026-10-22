package cl.duoc.pedidos360.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearColaRequest(
        @NotBlank(message = "El nombre de la cola no puede estar vacío")
        @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
        String nombreCola,
        Boolean durable
) {
}
