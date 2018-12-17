package com.javax0.license3j.three;

import java.util.HashMap;
import java.util.Map;

/**
 * A license describes the rights that a certain user has. The rights are represented by {@link Feature}s.
 * Each feature has a name, type and a value. The license is essentially the set of features.
 * <p>
 * As examples features can be license expiration date and time, number of users allowed to use the software,
 * name of rights and so on.
 */
public class License {
    final private Map<String,Feature> features = new HashMap<>();
    public Feature get(String name){
        return features.get(name);
    }

    public Feature put(String name, Feature feature){
        return features.put(name,feature);
    }


}
