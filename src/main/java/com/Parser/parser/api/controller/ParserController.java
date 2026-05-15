package com.Parser.parser.api.controller;

import com.Parser.parser.application.service.Parser;
import com.Parser.parser.infrastructure.CryptocurrencyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ParserController {

    private final Parser parser;

    @PostMapping("/parse")
    public List<CryptocurrencyEntity> parseAndSave() {
        return parser.parse();
    }

    @GetMapping("/all")
    public List<CryptocurrencyEntity> findAll() {
        return parser.findAll();
    }

    @GetMapping("/search")
    public List<CryptocurrencyEntity> searchByName(@RequestParam String name) {
        return parser.findByName(name);
    }
}
