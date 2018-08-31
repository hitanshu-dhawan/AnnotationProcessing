package com.hitanshudhawan.annotationprocessingexample;

import com.hitanshudhawan.singleton_annotations.Singleton;

@Singleton
public class MySingleton {

    private MySingleton() {}

    public static MySingleton getInstance() {
        return null;
    }

}



/*

public final class Singleton {

    private static final Singleton INSTANCE = new Singleton();

    private Singleton() {}

    public static Singleton getInstance() {
        return INSTANCE;
    }
}

*/