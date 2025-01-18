package ru.vadim.redissoncourse;

import org.junit.jupiter.api.BeforeAll;


import org.junit.jupiter.api.Test;
import org.redisson.api.LocalCachedMapCacheOptions;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;
import ru.vadim.redissoncourse.config.RedissonConfig;
import ru.vadim.redissoncourse.dto.Student;

import java.time.Duration;
import java.util.List;

/* Стратегии
Sync Strategy NONE - если где-то локальный кеш будет обновлен, он обновиться и в редисе, НО локальный кеш остальных юзеров не будет проинформирован на обновление
Sync Strategy INVALIDATE - если какое то поле обновили в одном кеше, обновленная запись актуализируется в редисе, НО инвалидируется - удалиться в локальном кеше других.
Sync Strategy UPDATE - обновиться инфа во всех кешах локальных
Reconnect Strategy - CLEAR - если локальный кеш будет отсоединен от сети к редису, при восстановлении он удалит свой кеш и загрузит его заново. Мне кажется это пример для использования деградации фичи, т.е. когда нам лучше выдавать хоть какие то данные чем вообще никакие.
Reconnect Strategy - NONE означает, что клиент не будет пытаться автоматически переподключаться к серверу Redis в случае потери соединения. Если соединение будет потеряно, клиент просто завершит работу с ошибкой, и разработчик должен самостоятельно обработать это состояние и решить, как действовать дальше.

 */
public class Lec08LocalCachedMapTest extends BaseTest {


    private RLocalCachedMap<Integer, Student> studentsMap;
    @BeforeAll
    public void setupClient() {
        RedissonConfig config = new RedissonConfig();
        RedissonClient redissonClient = config.getClient();

        LocalCachedMapOptions<Integer, Student> mapOptions = LocalCachedMapOptions.<Integer, Student>defaults()
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.studentsMap = redissonClient.getLocalCachedMap(
                "students",
                new TypedJsonJacksonCodec(Integer.class, Student.class),
                mapOptions
        );
    }

    @Test
    public void appServer1() {
        Student student1 = new Student("vadim", 30, "chelyabinsk", List.of(1, 2, 3));
        Student student2 = new Student("alena", 24, "batumi", List.of(10, 25, 33));

        this.studentsMap.put(1, student1);
        this.studentsMap.put(2, student2);

        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(i -> System.out.println(i + " ==> " + studentsMap.get(1)))
                .subscribe();

        sleep(600000);
    }

    @Test
    public void appServer2() {
        Student student1 = new Student("vadim-updated", 30, "chelyabinsk43", List.of(1, 2, 3));
        this.studentsMap.put(1, student1);
    }
}
