package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RMapCacheReactive;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vadim.redissoncourse.dto.Student;

import java.util.List;
import java.util.concurrent.TimeUnit;
/*
Реализация кешМапы, она похожа на обычную мапу, только в каждый элемент в ней можно установить время жизни
 */
public class Lec07MapCacheTest extends BaseTest {

    @Test
    public void mapCacheTest() {
        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec(Integer.class, Student.class);

        RMapCacheReactive<Integer, Student> mapCache = this.client.getMapCache("users:cache", codec);

        Student student1 = new Student("vadim", 30, "chelyabinsk", List.of(1, 2, 3));
        Student student2 = new Student("alena", 24, "batumi", List.of(10, 25, 33));

        Mono<Student> st1 = mapCache.put(1, student1, 5, TimeUnit.SECONDS);
        Mono<Student> st2 = mapCache.put(2, student2, 10, TimeUnit.SECONDS);

        StepVerifier.create(st1.concatWith(st2).then())
                .verifyComplete();

        sleep(3000);

        //access students
        mapCache.get(1).doOnNext(System.out::println).subscribe();
        mapCache.get(2).doOnNext(System.out::println).subscribe();

        sleep(3000);
        //access student
        mapCache.get(1).doOnNext(System.out::println).subscribe();
        mapCache.get(2).doOnNext(System.out::println).subscribe();
    }
}
