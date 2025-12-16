package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    boolean existsByPolicyNumber(String policyNumber);

    Optional<InsurancePolicy> findByPolicyNumber(String policyNumber);

    @Query("select avg(p.premium) from InsurancePolicy p")
    BigDecimal findAveragePremium();

    @Query("""
        select p from InsurancePolicy p
        join p.client c
        join p.program pr
        where lower(p.policyNumber) like lower(concat('%', :kw, '%'))
           or lower(c.fullName)     like lower(concat('%', :kw, '%'))
           or lower(pr.name)        like lower(concat('%', :kw, '%'))
    """)
    List<InsurancePolicy> search(@Param("kw") String keyword, Sort sort);
}
