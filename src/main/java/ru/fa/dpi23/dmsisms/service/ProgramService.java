package ru.fa.dpi23.dmsisms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgramService {

    private final InsuranceProgramRepository repo;

    @Transactional(readOnly = true)
    public List<InsuranceProgram> list(String keyword,
                                       String sortField,
                                       String sortDir,
                                       String activeFilter) {

        String field = (sortField == null || sortField.isBlank()) ? "name" : sortField;
        String dir = (sortDir == null || sortDir.isBlank()) ? "asc" : sortDir;

        Sort sort = Sort.by(field);
        sort = "desc".equalsIgnoreCase(dir) ? sort.descending() : sort.ascending();

        // базовый список (поиск + сортировка уже были)
        List<InsuranceProgram> base;
        if (keyword != null && !keyword.isBlank()) {
            base = repo.findByNameContainingIgnoreCase(keyword, sort);
        } else {
            base = repo.findAll(sort);
        }

        // 🔍 дополнительный фильтр "только активные / только неактивные"
        if ("active".equalsIgnoreCase(activeFilter)) {
            return base.stream()
                    .filter(InsuranceProgram::isActive)
                    .toList();
        } else if ("inactive".equalsIgnoreCase(activeFilter)) {
            return base.stream()
                    .filter(p -> !p.isActive())
                    .toList();
        }

        // "all" или null → без доп. фильтра
        return base;
    }

    @Transactional(readOnly = true)
    public InsuranceProgram get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Программа не найдена: " + id));
    }

    public InsuranceProgram save(InsuranceProgram program) {
        return repo.save(program);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
