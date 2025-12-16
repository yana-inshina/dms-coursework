package ru.fa.dpi23.dmsisms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dpi23.dmsisms.entity.Client;
import ru.fa.dpi23.dmsisms.entity.CorporateApplication;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.entity.InsuranceApplication;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;
import ru.fa.dpi23.dmsisms.entity.PolicyStatus;
import ru.fa.dpi23.dmsisms.repository.InsurancePolicyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InsurancePolicyService {

    private final InsurancePolicyRepository repo;
    private final ClientService clientService;   // 👈 общий сервис клиентов (физ + корп)

    // ====== СПИСКИ ПОЛИСОВ ======

    @Transactional(readOnly = true)
    public List<InsurancePolicy> list(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return repo.search(keyword, sort);
        }
        return repo.findAll(sort);
    }

    @Transactional(readOnly = true)
    public List<InsurancePolicy> list(String keyword, String sortField, String sortDir) {
        String field = (sortField == null || sortField.isBlank()) ? "id" : sortField;
        String dir = (sortDir == null || sortDir.isBlank()) ? "asc" : sortDir;

        Sort sort = Sort.by(field);
        sort = "desc".equalsIgnoreCase(dir) ? sort.descending() : sort.ascending();

        if (keyword != null && !keyword.isBlank()) {
            return repo.search(keyword, sort);
        }
        return repo.findAll(sort);
    }

    @Transactional(readOnly = true)
    public List<InsurancePolicy> list(String keyword,
                                      String sortField,
                                      String sortDir,
                                      PolicyStatus statusFilter) {

        List<InsurancePolicy> base = list(keyword, sortField, sortDir);

        if (statusFilter == null) {
            return base;
        }

        return base.stream()
                .filter(p -> p.getStatus() == statusFilter)
                .toList();
    }

    // ====== CRUD ======

    @Transactional(readOnly = true)
    public InsurancePolicy get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Полис не найден: " + id));
    }

    public InsurancePolicy save(InsurancePolicy policy) {
        return repo.save(policy);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean isPolicyNumberTaken(String policyNumber, Long excludeId) {
        if (policyNumber == null || policyNumber.isBlank()) return false;

        return repo.findByPolicyNumber(policyNumber)
                .map(p -> excludeId == null || !p.getId().equals(excludeId))
                .orElse(false);
    }

    // ====== ВЫПУСК ПОЛИСА ИЗ INDIVIDUAL-заявки ======

    public InsurancePolicy createFromApplication(InsuranceApplication app) {

        // 1) находим или создаём клиента по данным заявки
        Client client = clientService.findOrCreateFromApplication(app);

        // 2) создаём полис
        InsurancePolicy policy = new InsurancePolicy();

        policy.setProgram(app.getProgram());
        policy.setApplication(app);       // связь "полис ← индивидуальная заявка"
        policy.setClient(client);

        LocalDate start = LocalDate.now();
        policy.setStartDate(start);
        policy.setEndDate(start.plusYears(1));

        policy.setPremium(app.getCalculatedPremium());
        policy.setPolicyNumber(generateIndividualPolicyNumber());
        policy.setStatus(PolicyStatus.ACTIVE);

        return repo.save(policy);
    }

    // ====== ВЫПУСК ПОЛИСА ИЗ CORPORATE-заявки ======

    public InsurancePolicy createCorporatePolicyFromApplication(CorporateApplication app) {

        if (app.getCorporateClient() == null) {
            throw new IllegalArgumentException("У корпоративной заявки не указан клиент");
        }
        if (app.getProgram() == null) {
            throw new IllegalArgumentException("У корпоративной заявки не указана программа");
        }
        // Если премия не рассчитана, назначаем её минимальной (0.01 руб.)
        // иначе используем рассчитанное значение. Корректировка нулевой или отрицательной премии
        // будет выполнена ниже при установке премии полиса.

        CorporateClient corp = app.getCorporateClient();

        // общий клиент в таблице clients (через ClientService)
        Client client = clientService.findOrCreateFromCorporateClient(corp);

        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber(generateCorporatePolicyNumber());
        policy.setClient(client);
        policy.setProgram(app.getProgram());
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));
        // если рассчитанная премия отсутствует или равна/ниже нуля, устанавливаем минимальную сумму 0.01
        BigDecimal premium = app.getCalculatedPremium();
        if (premium == null || premium.compareTo(BigDecimal.ZERO) <= 0) {
            premium = new BigDecimal("0.01");
        }
        policy.setPremium(premium);
        policy.setStatus(PolicyStatus.ACTIVE);

        // если в сущности InsurancePolicy есть поле для связи с корпоративной заявкой – не забудь:
        // policy.setCorporateApplication(app);

        return repo.save(policy);
    }

    // ====== генерация номеров ======

    private String generateIndividualPolicyNumber() {
        return "DMS-" + System.currentTimeMillis();
    }

    private String generateCorporatePolicyNumber() {
        long count = repo.count();
        return String.format("DMS-CORP-%04d", count + 1);
    }
}
