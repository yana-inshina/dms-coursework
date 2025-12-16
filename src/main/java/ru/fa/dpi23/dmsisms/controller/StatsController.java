package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.fa.dpi23.dmsisms.repository.ClientRepository;
import ru.fa.dpi23.dmsisms.repository.InsuranceApplicationRepository;
import ru.fa.dpi23.dmsisms.repository.InsurancePolicyRepository;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;
import ru.fa.dpi23.dmsisms.repository.UserRepository;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final InsuranceProgramRepository programRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceApplicationRepository applicationRepository;

    @GetMapping("/stats")
    public String stats(Model model) {

        // основные счётчики
        model.addAttribute("userCount",        userRepository.count());
        model.addAttribute("clientCount",      clientRepository.count());
        // Считаем только активные программы, чтобы статистика отражала
        // фактическое количество доступных для оформления ДМС продуктов.
        model.addAttribute("programCount",     programRepository.countByActiveTrue());
        model.addAttribute("policyCount",      policyRepository.count());
        model.addAttribute("applicationCount", applicationRepository.count());

        // средняя премия по полисам (BigDecimal, может быть null)
        model.addAttribute("avgPremium", policyRepository.findAveragePremium());

        // среднее время обработки заявок в МИНУТАХ (Long, может быть null)
        model.addAttribute("avgApplicationMinutes", getAvgApplicationMinutes());

        return "stats";   // шаблон stats.html
    }

    /**
     * Берём среднее время обработки в секундах из репозитория
     * и переводим в минуты.
     */
    private Long getAvgApplicationMinutes() {
        Double avgSeconds = applicationRepository.avgProcessingSeconds(); // может быть null
        if (avgSeconds == null) {
            return null; // в шаблоне будет показано "— мин."
        }
        // секунды -> минуты, с округлением
        return Math.round(avgSeconds / 60.0);
    }
}
