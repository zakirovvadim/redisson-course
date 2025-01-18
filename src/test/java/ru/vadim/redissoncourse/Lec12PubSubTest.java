package ru.vadim.redissoncourse;

import org.junit.jupiter.api.Test;
import org.redisson.api.RPatternTopicReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.listener.PatternMessageListener;
import org.redisson.client.codec.StringCodec;

/*
при запуске можно мубликовать сообщения прямо из редис-сли
127.0.0.1:6379> publish telegram-room hi
(integer) 2
127.0.0.1:6379> publish telegram-room "how are you guys"
(integer) 2

Работает как горячая подписка
 */

public class Lec12PubSubTest extends BaseTest {

    @Test
    public void subscriber1() {
        RTopicReactive topic = this.client.getTopic("telegram-room", StringCodec.INSTANCE);
        topic.getMessages(String.class)
                .doOnError(System.out::println)
                .doOnNext(System.out::println)
                .subscribe();
        sleep(600000);
    }

    @Test
    public void subscriber2() {
        RTopicReactive topic = this.client.getTopic("telegram-room", StringCodec.INSTANCE);
        topic.getMessages(String.class)
                .doOnError(System.out::println)
                .doOnNext(System.out::println)
                .subscribe();
        sleep(600000);
    }

    /*
    Если надо чтобы консьюмер слушал несколько топиков, можно применять паттерны
     */

    @Test
    public void subscriber3() {
        RTopicReactive topic = this.client.getTopic("telegram-room1", StringCodec.INSTANCE);
        topic.getMessages(String.class)
                .doOnError(System.out::println)
                .doOnNext(System.out::println)
                .subscribe();
        sleep(600000);
    }


    // прмер полученного ответа - telegram-room* telegram-room2 hi
    @Test
    public void subscriber4() {
        RPatternTopicReactive topic = this.client.getPatternTopic("telegram-room*", StringCodec.INSTANCE);
        topic.addListener(String.class, new PatternMessageListener<String>() {
            @Override
            public void onMessage(CharSequence pattern, CharSequence topic, String message) {
                System.out.println(pattern + " " + topic + " " + message);
            }
        }).subscribe();
        sleep(600000);
    }

}
