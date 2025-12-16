package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.Client;
import ru.fa.dpi23.dmsisms.repository.ClientRepository;

@Component
@RequiredArgsConstructor
public class StringToClientConverter implements Converter<String, Client> {

    private final ClientRepository clientRepository;

    @Override
    public Client convert(String source) {
        if (source == null || source.isBlank()) return null;

        Long id = Long.valueOf(source);
        return clientRepository.findById(id).orElse(null);
    }
}

