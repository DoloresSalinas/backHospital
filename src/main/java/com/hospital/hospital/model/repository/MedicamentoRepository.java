package com.hospital.hospital.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hospital.hospital.model.entity.Medicamento;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {

    List<Medicamento> findByRecetaIdReceta(Integer idReceta);

}