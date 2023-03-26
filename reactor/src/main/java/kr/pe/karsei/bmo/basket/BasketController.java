package kr.pe.karsei.bmo.basket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("basket")
public class BasketController {
    private final static List<String> BASKET1 = List.of("kiwi", "orange", "lemon", "orange", "lemon", "kiwi");
    private final static List<String> BASKET2 = List.of("banana", "lemon", "lemon", "kiwi");
    private final static List<String> BASKET3 = List.of("strawberry", "orange", "lemon", "grape", "strawberry");

    /**
     * 바구니 속 과일 종류를 중복없이, 각 종류별로 개수를 나눠보자
     */
    @GetMapping
    public Flux<FruitInfo> basket() {
        // https://tech.kakao.com/2018/05/29/reactor-programming/
        final List<List<String>> baskets = List.of(BASKET1, BASKET2, BASKET3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        // concatMap - 비동기 환경에서 동시성을 지원하면서도 순서를 보장 (인자의 Publisher 의 스트림이 다 끝난 후 다음의 Publisher 스트림을 처리)
        // flatMapSequential - 일단 오는대로 구독하고, 결과는 순서에 맞게 리턴함
        Flux<FruitInfo> map = basketFlux.concatMap(basket -> {
            // 과일이 중복이 없도록 모은다. Flux 에서 Mono 로 바꿈
            final Mono<List<String>> distinctFruits = Flux.fromIterable(basket).distinct().collectList();
            final Mono<Map<String, Long>> countFruitsMono = Flux.fromIterable(basket)
                    // 바구니로 부터 넘어온 과일 기준으로 group 을 묶는다.
                    .groupBy(fruit -> fruit)
                    // 각 과일별로 개수를 Map 으로 리턴, concatMap 으로 순서 보장
                    .concatMap(groupedFlux -> groupedFlux.count()
                            .map(count -> {
                                final Map<String, Long> fruitCount = new LinkedHashMap<>();
                                fruitCount.put(groupedFlux.key(), count);
                                return fruitCount;
                            })
                    )
                    // 그동안 누적된 accumulatedMap 에 현재 넘어오는 currentMap 을 합쳐서 새로운 Map 을 만든다.
                    // map 끼리 putAll 하여 하나의 Map 으로 만든다.
                    .reduce((accumulatedMap, currentMap) -> new LinkedHashMap<String, Long>() {
                        {
                            putAll(accumulatedMap);
                            putAll(currentMap);
                        }
                    });
            return Flux.zip(distinctFruits, countFruitsMono, FruitInfo::new);
        });

        return map;
    }
}
