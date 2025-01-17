package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Lec04BucketAsMapTest extends BaseTest {
    // user:1:name
    // user:2:name
    // user:3:name
    // перед стартом теста добавляем переменные в редис сами. Если укажем в гет ключа которого нет, он просто не заполнит мапу
    @Test
    public void bucketAsMap() {
        Mono<Void> mono = this.client.getBuckets(StringCodec.INSTANCE)
                .get("user:1:name", "user:2:name", "user:3:name")
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }
}
