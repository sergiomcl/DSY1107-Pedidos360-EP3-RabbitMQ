package cl.duoc.pedidos360.dto;

import java.io.Serializable;

public record PedidoEventoDTO(
        String eventoId,
        Long pedidoId,
        String clienteEmail,
        String tipo,
        String fecha,
        boolean falloNotificaciones,
        boolean falloCocina,
        boolean falloDocumentos
) implements Serializable {
}
