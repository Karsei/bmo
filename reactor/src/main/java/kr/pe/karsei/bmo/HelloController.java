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
    // https://devsh.tistory.com/entry/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EB%A6%AC%EC%95%A1%ED%84%B0-%EA%B8%B0%EC%B4%88-1-%EB%AA%A8%EB%85%B8
    /*
     * Mono.just 는 객체를 인자로 받은 뒤 Mono 로 래핑하는 팩토리 함수이다.
     * Flux 와 Mono 는 Lazy 로 동작하기 때문에 subscribe 를 호출하지 않으면 작성한 로직이 동작하지 않는다.
     *
     * map 은 전달받은 요소를 새로운 모노로 변환할 때 사용하는 연산자다.
     */
    @GetMapping("mono")
    public Mono<String> mono() {
        return Mono.just("Hello, World!")
                .map(s -> { System.out.println(s); return s; });
    }

    /*
     * Mono.justOrEmpty 는 값이 null 이 될 수 있는 데이터를 받아서 처리한다.
     * Mono.just 는 값이 null 이면 NPE 를 발생시키므로 주의해야 한다.
     */
    @GetMapping("mono-empty")
    public Mono<String> monoEmpty() {
        String greeting = null;
        return Mono.justOrEmpty(greeting)
                .map(s -> { System.out.println(s); return s; });
    }

    /*
     * null 인 데이터를 제공받으면 switchIfEmpty 를 사용해 처리할 수 있다.
     * 이때 switchIfEmpty 는 값이 null 이면 새로운 데이터로 변환해주는데 Mono.defer 연산을 사용하면 내부 코드의 호출이 지연되어 실제 사용되는 시점에 호출된다.
     */
    @GetMapping("mono-switch")
    public Mono<String> monoSwitch() {
        String greeting = null;
        return Mono.justOrEmpty(greeting)
                // null 일 경우에만 동작한다.
                .switchIfEmpty(Mono.defer(() -> Mono.just("yeah, there is no one")))
                // 위와 아래는 다른데, 아래의 경우 값의 존재 유무에 상관없이 내부 코드가 동작된다.
                //.switchIfEmpty(Mono.just("yeah, there is no one"))
                .defaultIfEmpty("yeah, full")
                .map(s -> { System.out.println(s); return s; });
    }

    /*
     * 내부에서 로직에 대한 결과로 Mono 객체를 생성할 경우 Mono.fromSupplier 를 사용할 수 있다.
     * Supplier 는 Java 8 에서 추가된 함수형 인터페이스로, 별도의 인자없이 내부의 값을 반환하는 T get() 함수를 구현하도록 되어 있다.
     * 특정한 구현 로직이 필요한 경우나 늦은 초기화가 필요한 경우 사용된다.
     */
    @GetMapping("mono-supplier")
    public Mono<String> monoSupplier() {
        return Mono.fromSupplier(() -> {
            String greeting = "hello, world";
            greeting += "~!";
            return greeting;
        })
                // 예외가 발생한 경우 별도의 핸들링이 가능하다.
                .onErrorResume(Mono::error)
                .map(s -> { System.out.println(s); return s; });
    }

    /*
     * flatMap 은 주로 단일 요소를 복수개의 요소로 변환할 때 사용된다. map 과는 다르게 마지막에서 반환할 요소를 Mono.just 와 같은 팩토리 함수로 감싸서 반환해야 한다.
     * 특정 조건에 따라 에러를 발생시키거나 비어 있는 값을 리턴할 수 있다.
     */
    @GetMapping("mono-flatmap")
    public Mono<String> monoFlatMap() {
        return Mono.just("this is a text for test")
                .flatMap(it -> {
                    if (it.contains("test")) {
                        return Mono.just("nooooooo test");
                    }
                    else {
                        return Mono.empty();
                    }
                });
    }

    /*
     * filter 연산자는 특정 조건이 true 인지 판단하여 true 인 경우에만 데이터를 통지하는 연산자이다.
     */
    @GetMapping("mono-filter")
    public Mono<String> monoFilter() {
        return Mono.just("this is a text for test")
                .filter(it -> it.startsWith("this"));
    }

    /*
     * zip 은 여러 개의 Mono 객체를 하나의 Mono 로 결합할 수 있다.
     * zip 함수의 처리가 완료되어 새로운 Mono 를 생성한 뒤 다음 연산자에 통지를 보내는 시점은 인자로 제공된 Mono 중 가장 오래 수행된 Mono 의 시간을 기준으로 결합한다.
     * 보통 각 Mono 의 로직을 비동기 처리한 뒤 결과에 대한 결합을 위해 zip 을 사용한다.
     * 최대 8개의 Mono 를 인자로 받을 수 있지만 그 이상의 경우 컴비네이터(Combinator) 함수를 사용하여 합칠 수 있다.
     */
    @GetMapping("mono-zip")
    public Mono<String> monoZip() {
        Mono<String> m1 = Mono.just("This");
        Mono<String> m2 = Mono.just("is");
        Mono<String> m3 = Mono.just("a test String");
        return Mono.zip(m1, m2, m3)
                .map(tuple -> String.format("%s %s %s", tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

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
