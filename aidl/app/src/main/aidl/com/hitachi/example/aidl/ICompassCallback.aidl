// ICompassCallback.aidl
package com.hitachi.example.aidl;

// Declare any non-default types here with import statements

interface ICompassCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onResult(in String result);
}
