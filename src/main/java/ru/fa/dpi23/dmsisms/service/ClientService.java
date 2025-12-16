package ru.fa.dpi23.dmsisms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dpi23.dmsisms.entity.Client;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.entity.InsuranceApplication;
import ru.fa.dpi23.dmsisms.repository.ClientRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository repo;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "fullName", "email", "phone", "clientType"
    );

    // ====== список клиентов ======

    @Transactional(readOnly = true)
    public List<Client> list(String keyword, String sortField, String sortDir) {
        Sort sort = buildSort(sortField, sortDir);

        List<Client> clients;
        // если задан ключевой поиск — ищем по имени и сортируем
        if (keyword != null && !keyword.isBlank()) {
            clients = repo.findByFullNameContainingIgnoreCase(keyword, sort);
        } else {
            clients = repo.findAll(sort);
        }

        // фильтруем список, исключая записи с типом "CORPORATE". Корпоративные клиенты
        // управляются через отдельный раздел (corp-clients) и не должны отображаться
        // в общем списке физических лиц. Это устраняет путаницу, когда
        // корпоративные клиенты появлялись в списке клиентов, но отсутствовали
        // в форме создания корпоративной заявки.
        return clients.stream()
                .filter(c -> c.getClientType() == null || !"CORPORATE".equalsIgnoreCase(c.getClientType()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Client get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден: " + id));
    }

    public Client save(Client client) {
        return repo.save(client);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private Sort buildSort(String sortField, String sortDir) {
        String field = (sortField == null || sortField.isBlank()) ? "fullName" : sortField;
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            field = "fullName";
        }

        Sort s = Sort.by(field);
        return "desc".equalsIgnoreCase(sortDir) ? s.descending() : s.ascending();
    }

    // ====== использование при выпуске полиса из индивидуальной заявки ======

    /**
     * Ищем клиента по email заявки.
     * Если не нашли — создаём нового клиента по данным из заявки.
     */
    public Client findOrCreateFromApplication(InsuranceApplication app) {

        // если есть email – сначала пытаемся найти по нему
        if (app.getEmail() != null && !app.getEmail().isBlank()) {
            return repo.findFirstByEmailOrderByIdAsc(app.getEmail())
                    .orElseGet(() -> createClientFromApplication(app));
        }

        // если email не указан – просто создаём нового клиента
        return createClientFromApplication(app);
    }

    private Client createClientFromApplication(InsuranceApplication app) {
        Client client = new Client();
        client.setFullName(app.getFullName());
        client.setPhone(app.getPhone());
        client.setEmail(app.getEmail());

        // тип клиента — физлицо
        client.setClientType("INDIVIDUAL");

        return repo.save(client);
    }

    // ====== использование при выпуске полиса из CORPORATE-заявки ======

    public Client findOrCreateFromCorporateClient(CorporateClient corp) {
        if (corp.getContactEmail() != null && !corp.getContactEmail().isBlank()) {
            return repo.findFirstByEmailOrderByIdAsc(corp.getContactEmail())
                    .orElseGet(() -> repo.save(
                            Client.builder()
                                    .fullName(corp.getName())
                                    .email(corp.getContactEmail())
                                    .phone(corp.getContactPhone())
                                    .clientType("CORPORATE")
                                    .build()
                    ));
        }

        // если email не указан — ищем по названию, иначе создаём
        return repo.findFirstByFullNameOrderByIdAsc(corp.getName())
                .orElseGet(() -> repo.save(
                        Client.builder()
                                .fullName(corp.getName())
                                .email(corp.getContactEmail())
                                .phone(corp.getContactPhone())
                                .clientType("CORPORATE")
                                .build()
                ));
    }
}
