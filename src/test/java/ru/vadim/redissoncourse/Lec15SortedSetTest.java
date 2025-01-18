package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

public class Lec15SortedSetTest extends BaseTest {

    @Test
    public void sortedSetTest() {
        RScoredSortedSetReactive<String> sortedSet = this.client.getScoredSortedSet("student:score", StringCodec.INSTANCE);

        Mono<Void> mono = sortedSet.addScore("sam", 12.245)
                .then(sortedSet.add(23.25, "mike"))
                .then(sortedSet.addScore("jake", 7))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();

        sortedSet.entryRange(0, 1)
                .flatMapIterable(Function.identity()) //так как возвращается колекшн и нам нужджно просто все извлеч, выбираем флакс фром итерабл
                .map(se -> se.getScore() +" : " + se.getValue())
                .doOnNext(System.out::println)
                .subscribe();

        sleep(1000);

//        sortedSet.entryRange(0, 1) дай ранжировку по рагну от до
//        sortedSet.entryRangeКумукыув(0, 1) с реверсом
    }
}
