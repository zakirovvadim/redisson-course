package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucketReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

/*
Для реализации нотификации от редиса о том, что какой-либо ключ закончил свой ТТЛ, нужно добавить бакета лисенер с имплементацией интерфейса
 Mono<Void> event = bucket.addListener(new ExpiredObjectListener() {
            @Override
            public void onExpired(String name) {
                System.out.println("Expired: " + name);
            }
        }).then();
НО, по умолчанию лисенер в редисе не включен и нужно добавить необходимую конфигурацию
127.0.0.1:6379> config set notify-keyspace-events AKE

Примеры конфигураций
Configuration
By default keyspace event notifications are disabled because while not very sensible the feature uses some CPU power. Notifications are enabled using the notify-keyspace-events of redis.conf or via the CONFIG SET.

Setting the parameter to the empty string disables notifications. In order to enable the feature a non-empty string is used, composed of multiple characters, where every character has a special meaning according to the following table:

K     Keyspace events, published with __keyspace@<db>__ prefix.
E     Keyevent events, published with __keyevent@<db>__ prefix.
g     Generic commands (non-type specific) like DEL, EXPIRE, RENAME, ...
$     String commands
l     List commands
s     Set commands
h     Hash commands
z     Sorted set commands
t     Stream commands
d     Module key type events
x     Expired events (events generated every time a key expires)
e     Evicted events (events generated when a key is evicted for maxmemory)
m     Key miss events (events generated when a key that doesn't exist is accessed)
n     New key events (Note: not included in the 'A' class)
A     Alias for "g$lshztxed", so that the "AKE" string means all the events except "m" and "n".
At least K or E should be present in the string, otherwise no event will be delivered regardless of the rest of the string.

For instance to enable just Key-space events for lists, the configuration parameter must be set to Kl, and so forth.

You can use the string KEA to enable most types of events.

Несколько приложений джава могут прослушивать ивенты.
 */
public class Lec05EventListenerTest extends BaseTest {

    @Test
    public void expiredEventTest() {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);
        Mono<Void> set = bucket.set("sam", 10, TimeUnit.SECONDS); // установка времени жизнит
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        Mono<Void> event = bucket.addListener(new ExpiredObjectListener() {
            @Override
            public void onExpired(String name) {
                System.out.println("Expired: " + name);
            }
        }).then();

        StepVerifier.create(set.concatWith(get).concatWith(event))
                .verifyComplete();
        sleep(11000);
    }

    /* Если в терминале вызовем 127.0.0.1:6379> del user:1:name, то увидим, как придет сообщение об удалении
    sam
    Deleted: user:1:name
     */
    @Test
    public void deleteEventTest() {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);
        Mono<Void> set = bucket.set("sam");
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        Mono<Void> event = bucket.addListener(new DeletedObjectListener() {
            @Override
            public void onDeleted(String name) {
                System.out.println("Deleted: " + name);
            }
        }).then();

        StepVerifier.create(set.concatWith(get).concatWith(event))
                .verifyComplete();
        sleep(60000);
    }
}
