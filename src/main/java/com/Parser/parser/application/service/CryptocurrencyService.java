package com.Parser.parser.application.service;

import com.Parser.parser.application.interfaces.repo.IParserRepository;
import com.Parser.parser.infrastructure.CryptocurrencyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptocurrencyService {

    private final IParserRepository repository;

    public List<CryptocurrencyEntity> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }
}
