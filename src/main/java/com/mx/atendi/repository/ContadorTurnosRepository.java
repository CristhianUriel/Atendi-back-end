package com.mx.atendi.repository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.mx.atendi.entity.ContadorTurnos;
import reactor.core.publisher.Mono;
import java.time.LocalDate;

@Repository
public interface ContadorTurnosRepository extends ReactiveMongoRepository<ContadorTurnos, String> {
    Mono<ContadorTurnos> findByHospitalIdAndDepartamentoIdAndFecha(String hospitalId, String departamentoId, LocalDate fecha);
    Mono<ContadorTurnos> findByHospitalIdAndFecha(String hospitalId, LocalDate fecha);
}