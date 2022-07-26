package org.moddinginquisition.web.extensions

import com.google.gson.JsonObject
import groovy.transform.CompileStatic

@CompileStatic
class JsonExtensions {
    static boolean getBoolean(JsonObject self, String key) {
        return self.has(key) ? self.get(key).getAsBoolean() : false
    }
}
