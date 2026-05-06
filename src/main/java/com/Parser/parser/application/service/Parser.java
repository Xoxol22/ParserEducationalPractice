package com.Parser.parser.application.service;

import com.Parser.parser.application.interfaces.repo.IParserRepository;
import com.Parser.parser.infrastructure.CryptocurrencyEntity;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class Parser {

    private final IParserRepository cryptocurrencyRepository;

    private static final String URL = "https://coinmarketcap.com/";

    private static final String ROW_SELECTOR = "table tbody tr";

    private static final String NAME_SELECTOR = "p.coin-item-name";
    private static final String SYMBOL_SELECTOR = "p.coin-item-symbol";
    private static final String SLUG_SELECTOR = "a[href^=/currencies/]";
    private static final String ID_SELECTOR = "img.coin-logo";
    private static final String SUPPLY_SELECTOR = ".circulating-supply-value span";

    private static final int NAME_COLUMN = 2;
    private static final int PRICE_COLUMN = 3;
    private static final int MARKET_CAP_COLUMN = 7;
    private static final int SUPPLY_COLUMN = 9;

    public List<CryptocurrencyEntity> parse() {
        try {
            Document document = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(15_000)
                    .get();

            Elements rows = document.select(ROW_SELECTOR);

            List<CryptocurrencyEntity> result = new ArrayList<>();

            long fallbackId = 1L;

            for (Element row : rows) {
                Elements columns = row.select("td");

                if (columns.size() <= SUPPLY_COLUMN) {
                    continue;
                }

                Element nameCell = columns.get(NAME_COLUMN);

                String name = getText(nameCell, NAME_SELECTOR);
                String symbol = getText(nameCell, SYMBOL_SELECTOR);
                String slug = getSlug(nameCell);
                Long id = getId(nameCell);

                BigDecimal price = parseNumber(columns.get(PRICE_COLUMN).text());
                BigDecimal marketCap = parseNumber(getMarketCap(columns.get(MARKET_CAP_COLUMN)));
                BigDecimal circulatingSupply = parseNumber(getText(columns.get(SUPPLY_COLUMN), SUPPLY_SELECTOR));

                if (name.isBlank() || symbol.isBlank()) {
                    continue;
                }

                if ("CMC20".equalsIgnoreCase(symbol)) {
                    continue;
                }

                if (id == null || id == 0) {
                    id = fallbackId;
                }

                CryptocurrencyEntity crypto = CryptocurrencyEntity.builder()
                        .id(id)
                        .name(name)
                        .symbol(symbol)
                        .slug(slug)
                        .circulatingSupply(circulatingSupply)
                        .usdPrice(price)
                        .usdMarketCap(marketCap)
                        .build();

                result.add(crypto);
                fallbackId++;

                if (result.size() >= 10) {
                    break;
                }
            }

            return cryptocurrencyRepository.saveAll(result);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки CoinMarketCap", e);
        }
    }

    public List<CryptocurrencyEntity> findByName(String name) {
        if (name == null || name.isBlank()) {
            return cryptocurrencyRepository.findAll();
        }

        return cryptocurrencyRepository.findByNameContainingIgnoreCase(name.trim());
    }

    public List<CryptocurrencyEntity> findAll() {
        return cryptocurrencyRepository.findAll();
    }

    private String getText(Element parent, String selector) {
        Element element = parent.selectFirst(selector);
        return element == null ? "" : element.text().trim();
    }

    private String getSlug(Element nameCell) {
        Element link = nameCell.selectFirst(SLUG_SELECTOR);

        if (link == null) {
            return "";
        }

        return link.attr("href")
                .replace("/currencies/", "")
                .replace("/", "")
                .trim();
    }

    private Long getId(Element nameCell) {
        Element logo = nameCell.selectFirst(ID_SELECTOR);

        if (logo == null) {
            return 0L;
        }

        String src = logo.attr("src");

        Pattern pattern = Pattern.compile("/(\\d+)\\.png");
        Matcher matcher = pattern.matcher(src);

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        return 0L;
    }

    private String getMarketCap(Element marketCapCell) {
        Elements spans = marketCapCell.select("span");

        if (!spans.isEmpty()) {
            return spans.last().text();
        }

        return marketCapCell.text();
    }

    private BigDecimal parseNumber(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String cleaned = value
                .replace("$", "")
                .replace(",", "")
                .replace("%", "")
                .replace(" ", "")
                .trim();

        Pattern pattern = Pattern.compile(
                "([0-9]+(?:\\.[0-9]+)?)([KMBT])?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(cleaned);

        if (!matcher.find()) {
            return BigDecimal.ZERO;
        }

        BigDecimal number = new BigDecimal(matcher.group(1));
        String suffix = matcher.group(2);

        if (suffix == null) {
            return number;
        }

        return switch (suffix.toUpperCase()) {
            case "K" -> number.multiply(BigDecimal.valueOf(1_000));
            case "M" -> number.multiply(BigDecimal.valueOf(1_000_000));
            case "B" -> number.multiply(BigDecimal.valueOf(1_000_000_000));
            case "T" -> number.multiply(BigDecimal.valueOf(1_000_000_000_000L));
            default -> number;
        };
    }
}