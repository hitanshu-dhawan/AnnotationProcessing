package com.hitanshudhawan.annotationprocessingexample;

import com.hitanshudhawan.ksingleton_annotations.KSingleton;
import com.hitanshudhawan.singleton_annotations.Singleton;

@Singleton
public class MySingleton {

    private MySingleton() {}

    public static MySingleton getInstance() {
        return new MySingleton();
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