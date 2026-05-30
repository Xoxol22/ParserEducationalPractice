package com.Parser.parser.infrastructure;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cryptocurrencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptocurrencyEntity {

    @Id
    private Long id;
    private String name;
    private String symbol;
    private String slug;

    @Column(name = "circulating_supply", precision = 30, scale = 8)
    private BigDecimal circulatingSupply;

    @Column(name = "usd_price", precision = 30, scale = 8)
    private BigDecimal usdPrice;

    @Column(name = "usd_market_cap", precision = 30, scale = 2)
    private BigDecimal usdMarketCap;
}
