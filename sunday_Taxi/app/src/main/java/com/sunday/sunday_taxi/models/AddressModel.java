package com.sunday.sunday_taxi.models;

import com.google.api.client.util.Key;

import java.util.ArrayList;

public class AddressModel {
    @Key("documents")
    public ArrayList<ParentAddress> documents;
}
