package ru.vadim.redissoncourse.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Restaurant {
    private String id;
    private String city;
    private Double latitude;
    private Double longitude;
    private String name;
    private String zip;
}