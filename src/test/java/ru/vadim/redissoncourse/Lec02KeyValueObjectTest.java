package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vadim.redissoncourse.dto.Student;

import java.util.List;

public class Lec02KeyValueObjectTest extends BaseTest {

    @Test
    public void keyValueObjectTest() {
        Student student = new Student("vadim", 10, "chelyabinsk", List.of(1, 2,3));
        RBucketReactive<Object> bucket = this.client.getBucket("student:1", new TypedJsonJacksonCodec(Student.class)); // без кодека сохраняется в двоичном формате (с кодеком джексона будет храниться информация о классе)
        Mono<Void> set = bucket.set(student);
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();
        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }
}
