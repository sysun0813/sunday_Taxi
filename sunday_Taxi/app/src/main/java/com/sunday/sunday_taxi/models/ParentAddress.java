package com.sunday.sunday_taxi.models;

import com.google.api.client.util.Key;

public class ParentAddress {
    @Key("road_address")
    public RoadAddress roadAddress;
    @Key("address")
    public Address address;
}
