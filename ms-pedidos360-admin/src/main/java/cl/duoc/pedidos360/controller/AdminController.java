package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.dto.CrearBindingRequest;
import cl.duoc.pedidos360.dto.CrearColaRequest;
import cl.duoc.pedidos360.dto.CrearExchangeRequest;
import cl.duoc.pedidos360.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RabbitAdminService adminService;

    public AdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/queues")
    public ResponseEntity<String> crearCola(@Valid @RequestBody CrearColaRequest request) {
        adminService.crearCola(request.nombreCola(), !Boolean.FALSE.equals(request.durable()));
        return ResponseEntity.ok("Cola '" + request.nombreCola() + "' creada exitosamente.");
    }

    @DeleteMapping("/queues/{nombre}")
    public ResponseEntity<String> eliminarCola(
            @PathVariable @NotBlank @Size(max = 255) String nombre
    ) {
        boolean eliminada = adminService.eliminarCola(nombre);
        return ResponseEntity.ok("Cola '" + nombre + "' eliminada: " + eliminada);
    }

    @PostMapping("/exchanges")
    public ResponseEntity<String> crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        adminService.crearExchange(request);
        return ResponseEntity.ok("Exchange '" + request.nombreExchange() + "' creado exitosamente.");
    }

    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<String> eliminarExchange(
            @PathVariable @NotBlank @Size(max = 255) String nombre
    ) {
        boolean eliminado = adminService.eliminarExchange(nombre);
        return ResponseEntity.ok("Exchange '" + nombre + "' eliminado: " + eliminado);
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(@Valid @RequestBody CrearBindingRequest request) {
        adminService.crearBinding(request);
        return ResponseEntity.ok("Binding creado exitosamente.");
    }
}
