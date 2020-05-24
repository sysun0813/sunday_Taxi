package com.sunday.sunday_taxi.models;

import com.google.api.client.util.Key;

public class RoadAddress {
    @Key("address_name")
    public String addressName;
    @Key("building_name")
    public String buildingName;
}
