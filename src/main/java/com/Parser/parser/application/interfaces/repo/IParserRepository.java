package com.Parser.parser.application.interfaces.repo;

import com.Parser.parser.infrastructure.CryptocurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IParserRepository extends JpaRepository<CryptocurrencyEntity, Long> {  
    List<CryptocurrencyEntity> findByNameContainingIgnoreCase(String name);
}
