package com.spring.boot.carro.circuito_manejo.service.implementation;

import com.spring.boot.carro.circuito_manejo.persistence.entity.*;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.MetodoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoPagoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.*;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.*;
import com.spring.boot.carro.circuito_manejo.presentation.dto.pago.detalle.PagoDetalleResponseDTO;
import com.spring.boot.carro.circuito_manejo.service.exception.BusinessException;
import com.spring.boot.carro.circuito_manejo.service.exception.NotFoundException;
import com.spring.boot.carro.circuito_manejo.service.interfaces.IPagoService;
import com.spring.boot.carro.circuito_manejo.util.mapper.PagoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PagoService implements IPagoService {

    private final String NOT_FOUND_MSG = "Pago no encontrado con el id: ";

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PagoMapper pagoMapper;

    @Transactional(readOnly = true)
    @Override
    public List<PagoListadoResponseDTO> listarPagos() {
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toListadoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PagoDetalleResponseDTO obtenerPagoConCuotas(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

        return pagoMapper.toDetalleResponse(pago);
    }

    @Transactional
    @Override
    public PagoListadoResponseDTO crearPagoContado(PagoContadoRequestDTO dto) {

        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Paquete paquete = paqueteRepository.findById(dto.paqueteId())
                .orElseThrow(() -> new NotFoundException("Paquete no encontrado"));

        Pago pago = pagoMapper.toPagoContado(dto, usuario, paquete);

        pago.setTipoPago(TipoPagoEnum.CONTADO);
        pago.setEstado(EstadoPagoEnum.PAGADO);
        pago.setFechaPago(LocalDateTime.now());
        String numeroBoleta = UUID.randomUUID().toString().substring(0, 8);
        pago.setNumeroBoleta(numeroBoleta);

        return pagoMapper.toListadoResponse(pagoRepository.save(pago));
    }

    @Transactional
    @Override
    public PagoDetalleResponseDTO crearPagoCuotas(PagoCuotasRequestDTO dto) {

        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Paquete paquete = paqueteRepository.findById(dto.paqueteId())
                .orElseThrow(() -> new NotFoundException("Paquete no encontrado"));

        validarPrimerPago(dto, paquete);

        Pago pago = pagoMapper.toPagoCuotas(dto, usuario, paquete);

        pago.setTipoPago(TipoPagoEnum.CUOTAS);
        pago.setEstado(EstadoPagoEnum.PENDIENTE);
        pago.setMetodoPago(dto.metodoPago());
        pago.setFechaPago(LocalDateTime.now());
        String numeroBoleta = UUID.randomUUID().toString().substring(0, 8);
        pago.setNumeroBoleta(numeroBoleta);

        List<DetallePago> detalles = generarCuotas(dto, paquete, pago);
        pago.setDetalles(detalles);

        return pagoMapper.toDetalleResponse(pagoRepository.save(pago));
    }

    private void validarPrimerPago(PagoCuotasRequestDTO dto, Paquete paquete) {
        if (dto.montoPrimerPago().compareTo(paquete.getPrecioTotal()) >= 0)
            throw new BusinessException("El primer pago no puede ser mayor o igual al total.");
    }

    private List<DetallePago> generarCuotas(PagoCuotasRequestDTO dto, Paquete paquete, Pago pago) {

        List<DetallePago> detalles = new ArrayList<>();

        BigDecimal total = paquete.getPrecioTotal();
        BigDecimal primera = dto.montoPrimerPago();
        BigDecimal restante = total.subtract(primera);
        int cuotasRestantes = dto.cuotas() - 1;

        // Primera cuota pagada
        detalles.add(DetallePago.builder()
                .pago(pago)
                .numeroCuota(1)
                .montoCuota(primera)
                        .metodoPago(dto.metodoPago())
                .fechaVencimiento(LocalDate.now())
                .estadoCuota(EstadoPagoEnum.PAGADO)
                .build()
        );

        // Cuotas restantes
        BigDecimal montoCuotaRestante = restante.divide(
                BigDecimal.valueOf(cuotasRestantes),
                2, RoundingMode.HALF_UP
        );

        for (int i = 2; i <= dto.cuotas(); i++) {
            detalles.add(DetallePago.builder()
                    .pago(pago)
                    .numeroCuota(i)
                    .montoCuota(montoCuotaRestante)
                            .metodoPago(MetodoPagoEnum.PENDIENTE)
                    .fechaVencimiento(LocalDate.now().plusMonths(i - 1))
                    .estadoCuota(EstadoPagoEnum.PENDIENTE)
                    .build()
            );
        }

        return detalles;
    }


    @Transactional
    @Override
    public void pagarCuota(Long idPago, Integer numeroCuota, MetodoPagoEnum metodoPago) {

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

        if (pago.getTipoPago() != TipoPagoEnum.CUOTAS) {
            throw new BusinessException("Solo se pueden cancelar cuotas de pagos en modalidad CUOTAS.");
        }

        DetallePago cuota = pago.getDetalles().stream()
                .filter(d -> d.getNumeroCuota().equals(numeroCuota))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe la cuota número " + numeroCuota));

        if (cuota.getEstadoCuota() != EstadoPagoEnum.PENDIENTE) {
            throw new BusinessException("Solo se pueden cancelar cuotas pendientes.");
        }

        // Método de pago queda registrado tanto en PAGO como en la CUOTA
        pago.setMetodoPago(metodoPago);
        cuota.setMetodoPago(metodoPago);

        // Se marca la cuota como pagada
        cuota.setEstadoCuota(EstadoPagoEnum.PAGADO);

        // Reevaluamos el estado general del pago
        boolean todasPagadas = pago.getDetalles().stream()
                .allMatch(c -> c.getEstadoCuota() == EstadoPagoEnum.PAGADO);

        if (todasPagadas) {
            pago.setEstado(EstadoPagoEnum.PAGADO);
        } else {
            pago.setEstado(EstadoPagoEnum.PENDIENTE);
        }

        pagoRepository.save(pago);
    }

    @Transactional
    @Override
    public void suspenderPago(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MSG + id));

        if (pago.getTipoPago() != TipoPagoEnum.CUOTAS) {
            throw new BusinessException("Solo se pueden cancelar pagos que fueron realizados en cuotas.");
        }

        if (pago.getEstado() != EstadoPagoEnum.PENDIENTE) {
            throw new BusinessException("Solo se pueden cancelar pagos pendientes.");
        }

        pago.getDetalles().forEach(detalle -> {
            if (detalle.getEstadoCuota() == EstadoPagoEnum.PENDIENTE) {
                detalle.setEstadoCuota(EstadoPagoEnum.CANCELADO);
            }
        });

        pago.setEstado(EstadoPagoEnum.CANCELADO);
        pagoRepository.save(pago);
    }
}
