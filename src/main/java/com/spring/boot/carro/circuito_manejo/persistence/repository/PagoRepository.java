package com.spring.boot.carro.circuito_manejo.persistence.repository;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {

}
