package com.mx.atendi.service;

import com.mx.atendi.entity.Hospital;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IHospitalService {
	
	 public Mono<Hospital> crearHospital(Hospital hospital) ;
	 public Flux<Hospital> listarHospitales();
	 public Mono<Hospital> buscarHospitalPorId(String id);
}
