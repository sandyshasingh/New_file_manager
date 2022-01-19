package com.simplemobiletools.commons;
import java.io.Serializable;

/**
 * Created by ashishsaini on 3/8/17.
 */

public class KeyValueModel implements Serializable{

    public KeyValueModel(String key , String value){
        this.key  =key;
        this.value = value;
    }

    public String key;

    public String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
