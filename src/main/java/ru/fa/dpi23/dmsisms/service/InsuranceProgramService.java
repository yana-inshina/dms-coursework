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
public class InsuranceProgramService {

    private final InsuranceProgramRepository repo;

    @Transactional(readOnly = true)
    public List<InsuranceProgram> list(String keyword, String sortField, String sortDir) {
        Sort sort = buildSort(sortField, sortDir);

        if (keyword != null && !keyword.isBlank()) {
            return repo.findByNameContainingIgnoreCase(keyword, sort);
        }
        return repo.findAll(sort);
    }

    @Transactional(readOnly = true)
    public InsuranceProgram get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Программа не найдена: " + id));
    }

    public InsuranceProgram save(InsuranceProgram p) {
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private Sort buildSort(String field, String dir) {
        String f = (field == null || field.isBlank()) ? "name" : field;
        String d = (dir == null || dir.isBlank()) ? "asc" : dir;

        Sort s = Sort.by(f);
        return "desc".equalsIgnoreCase(d) ? s.descending() : s.ascending();
    }
}
