package com.voltskiya.structure.lootchest.entity.group;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChestGroupConfigProperty<Obj, D extends Number> {

    private final Function<Obj, D> getter;
    private final BiConsumer<Obj, D> setter;
    private final Supplier<D> getDefault;

    public ChestGroupConfigProperty(Function<Obj, D> getter, BiConsumer<Obj, D> setter, Supplier<D> getDefault) {
        this.getter = getter;
        this.setter = setter;
        this.getDefault = getDefault;
    }

    public void set(Obj obj, D value) {
        setter.accept(obj, value);
    }

    public D get(Obj obj) {
        return getter.apply(obj);
    }

    public D getOrDefault(Obj obj) {
        D val = get(obj);
        if (val != null) return val;
        return this.getDefault();
    }

    private D getDefault() {
        return this.getDefault.get();
    }
}
