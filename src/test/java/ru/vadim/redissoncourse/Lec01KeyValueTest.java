package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Lec01KeyValueTest extends BaseTest {

    @Test
    public void keyValueAccessTest() {
        // bucket нужны для хранения простых объектов
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE); // кодек нужен для правильной сериализации, остальные кодеки смотри на https://redisson.org/docs/data-and-services/data-serialization/
        Mono<Void> set = bucket.set("sam"); // сохраняем значение в бакет с вышеуказанным ключом и делаем по сути паблишер
        Mono<Void> get = bucket.get()  // и делаем паблишер для получения
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get)) // ну конкат ты уже знаешь, сначала делаем сет, потом след паблишер гет
                .verifyComplete(); // так как моно у нас ничего не возвращает - войд, тогда просто проверяем сигнал комплит
    }

    @Test
    public void keyValueExpiryTest() {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);
        Mono<Void> set = bucket.set("sam", 10, TimeUnit.SECONDS); // установка времени жизнит
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }

    @Test
    public void keyValueExtendExpiryTest() {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);
        Mono<Void> set = bucket.set("sam", 10, TimeUnit.SECONDS); // установка времени жизнит
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();
        StepVerifier.create(set.concatWith(get))
                .verifyComplete();

        //extend
        sleep(5000);
        Mono<Boolean> mono = bucket.expire(Duration.ofSeconds(60));
        StepVerifier.create(mono)
                .expectNext(true)
                .verifyComplete();

        // access expiration time (если нам нужно посмотреть сколько осталось времени до истечения времени жизни)
        Mono<Void> ttl = bucket.remainTimeToLive()
                .doOnNext(System.out::println)
                .then();
        StepVerifier.create(ttl)
                .verifyComplete();
    }
}
