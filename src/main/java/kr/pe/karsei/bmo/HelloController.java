package kr.pe.karsei.bmo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@RestController
public class HelloController {
    // https://www.devkuma.com/docs/spring-webflux/
    /*
     * 단순히 요청을 날리면 text/plain 으로 받는다.
     * 요청을 보낼 때 Accept 를 text/event-stream 로 보내면 데이터가 여러 개로 넘어온다.
     * application/stream+json 으로 보내면 text/plain 으로 보내는 것과 다름없어 보인다.
     */
    @GetMapping("/v1")
    Flux<String> hello() {
        return Flux.just("Hello", "World");
    }

    /*
     * Accept 를 application/stream+json 으로 보내면 JSON 응답이 배열이 아닌 개별로 각각 넘어온다.
     * 참고로 application/json 으로 보내면 배열로 넘어온다(무한으로 하면 대신 응답 자체가 반환이 안 된다).
     */
    @GetMapping("/v2")
    Flux<Map<String, Integer>> helloStream() {
        Stream<Integer> stream = Stream.iterate(0, i -> i + 1);
        return Flux
                .fromStream(stream)
                .map(i -> Collections.singletonMap("value", i));
    }

    /*
     * 요청 본문을 Mono 로 감싸서 받도록 하면 비동기적으로 chain/compose 할 수 있다(그냥 String 으로 받으면 non-blocking 으로 동기화된다).
     */
    @PostMapping("/v3")
    Mono<String> echo(@RequestBody Mono<String> body) {
        return body.map(String::toUpperCase);
    }
}
