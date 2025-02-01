package ru.vadim.redissoncourse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeoReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.vadim.redissoncourse.dto.GeoLocation;
import ru.vadim.redissoncourse.dto.Restaurant;
import ru.vadim.redissoncourse.util.RestaurantUtil;

import java.util.function.Function;

public class Lec17GeoSpatialTest extends BaseTest {

    private RGeoReactive<Restaurant> geo;
    private RMapReactive<String, GeoLocation> map;

    @BeforeAll
    public void setGeo() {
        this.geo =  this.client.getGeo("restaurants", new TypedJsonJacksonCodec(Restaurant.class));
        this.map = this.client.getMap("us:texas", new TypedJsonJacksonCodec(String.class, GeoLocation.class));
    }


    @Test
    public void add() {
        Mono<Void> mono = Flux.fromIterable(RestaurantUtil.getRestaurants())
                .flatMap(r -> this.geo.add(r.getLongitude(), r.getLatitude(), r).thenReturn(r)) // добавили в геоструктуру объект и геопозицию
                .flatMap(r -> this.map.fastPut(r.getZip(), GeoLocation.of(r.getLongitude(), r.getLatitude()))) // также положили этти объект под ключами зипКодов против объекта с геоданными
                .then();
        StepVerifier.create(mono)
                .verifyComplete();
    }

    @Test
    public void search() {
        Mono<Void> mono = this.map.get("75224") // из мапы достаем по зипкоду объект с геоданными
                .map(gl -> GeoSearchArgs.from(gl.getLongitude(), gl.getLatitude()).radius(5, GeoUnit.MILES))// формируем гео аргументы
                .flatMap(r -> this.geo.search(r))// передаем геоаргументы к геостурктуру
                .flatMapIterable(Function.identity()) // получаем даннные
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    /*
    // поиск по широте и долготе
    @Test
    public void add() {
        Mono<Void> mono = Flux.fromIterable(RestaurantUtil.getRestaurants())
                .flatMap(r -> this.geo.add(r.getLongitude(), r.getLatitude(), r))
                .then();
        StepVerifier.create(mono)
                .verifyComplete();


           "latitude": 32.809238,
            "longitude": -96.684639,

        OptionalGeoSearch radius = GeoSearchArgs.from(-96.684639, 32.809238).radius(3, GeoUnit.KILOMETERS);
        geo.search(radius)
                .flatMapIterable(Function.identity())
                .doOnNext(System.out::println)
                .subscribe();
    }
    */
}
