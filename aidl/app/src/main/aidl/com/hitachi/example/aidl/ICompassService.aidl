// ICompassService.aidl
package com.hitachi.example.aidl;

// Declare any non-default types here with import statements
import com.hitachi.example.aidl.ICompassCallback;

interface ICompassService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    float getCompass();
    void registerCallback(ICompassCallback cb);
    void unregisterCallback(ICompassCallback cb);
}
