/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ValueComparableMap<K extends Comparable<K>, V>
extends TreeMap<K, V> {
    private final Map<K, V> valueMap;

    public ValueComparableMap(Ordering<? super V> partialValueOrdering) {
        this(partialValueOrdering, new HashMap());
    }

    private ValueComparableMap(Ordering<? super V> partialValueOrdering, HashMap<K, V> valueMap) {
        super(partialValueOrdering.onResultOf(valueMap::get).compound(Comparator.naturalOrder()));
        this.valueMap = valueMap;
    }

    @Override
    public V put(K k, V v) {
        if (this.valueMap.containsKey(k)) {
            this.remove(k);
        }
        this.valueMap.put(k, v);
        return super.put(k, v);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.valueMap.containsKey(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return this.containsKey(key) ? this.get(key) : defaultValue;
    }
}

