package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RMapReactive;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vadim.redissoncourse.dto.Student;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Lec06MapTest extends BaseTest {

    // Пример как создать и заполнить мапу по ключу в редисе
    @Test
    public void mapTest1() {
        RMapReactive<String, String> map = this.client.getMap("user:1", StringCodec.INSTANCE);
        Mono<String> name = map.put("name", "katya");
        Mono<String> age = map.put("age", "25");
        Mono<String> city = map.put("city", "chelyabinsk");

        StepVerifier.create(name.concatWith(age).concatWith(city).then())
                .verifyComplete();
    }

    // пример как создать мапу на джаве и перенести ее в мапу на редисе
    @Test
    public void mapTest2() {
        RMapReactive<String, String> map = this.client.getMap("user:2", StringCodec.INSTANCE);
        Map<String, String> javaMap = Map.of(
                "name", "vadim",
                "age", "30",
                "city", "san-francisco"
        );

        Mono<Void> mapMono = map.putAll(javaMap);

        StepVerifier.create(mapMono
                        .then())
                .verifyComplete();
    }


    // Пример как заполнять мапу не просто строками, а объектами в значении
    /*
        127.0.0.1:6379> hgetall users
        1) "1"
        2) "{\"age\":30,\"city\":\"chelyabinsk\",\"marks\":[1,2,3],\"name\":\"vadim\"}"
        3) "2"
        4) "{\"age\":24,\"city\":\"batumi\",\"marks\":[10,25,33],\"name\":\"alena\"}"

        127.0.0.1:6379> hget users 1
        "{\"age\":30,\"city\":\"chelyabinsk\",\"marks\":[1,2,3],\"name\":\"vadim\"}"
     */
    @Test
    public void mapTest3() {
        // Map<Integer, Student>
        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec(Integer.class, Student.class);
        RMapReactive<Integer, Student> map = this.client.getMap("users", codec); // указываем ключ во множественном числе

        Student student1 = new Student("vadim", 30, "chelyabinsk", List.of(1, 2, 3));
        Student student2 = new Student("alena", 24, "batumi", List.of(10, 25, 33));

        Mono<Student> mono1 = map.put(1, student1);
        Mono<Student> mono2 = map.put(2, student2);

        StepVerifier.create(mono1.concatWith(mono2).then())
                .verifyComplete();
    }
}
