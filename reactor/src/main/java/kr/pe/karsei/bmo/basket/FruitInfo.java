package kr.pe.karsei.bmo.basket;

import lombok.Builder;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
public class FruitInfo {
    private final List<String> distinctFruits;
    private final Map<String, Long> countFruits;

    @Builder
    public FruitInfo(List<String> distinctFruits, Map<String, Long> countFruits) {
        this.distinctFruits = distinctFruits;
        this.countFruits = countFruits;
    }
}
