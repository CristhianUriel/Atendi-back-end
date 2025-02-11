package com.mx.atendi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mx.atendi.entity.Hospital;
import com.mx.atendi.repository.HospitalRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class HospitalService implements IHospitalService {
	
    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }
    
    
	@Override
	public Mono<Hospital> crearHospital(Hospital hospital) {
		return hospitalRepository.save(hospital)
                .doOnNext(savedHospital -> log.info("Hospital creado: {}", savedHospital));
	}

	@Override
	public Flux<Hospital> listarHospitales() {
		 return hospitalRepository.findAll();
	}

	@Override
	public Mono<Hospital> buscarHospitalPorId(String id) {
	     return hospitalRepository.findById(id);
	}

    
}

