/*
 * MIT License
 *
 * Copyright (c) 2022 Modding Inquisition
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.moddinginquisition.web.extensions

import com.google.gson.Gson
import groovy.transform.CompileStatic
import io.javalin.Javalin
import io.javalin.core.security.RouteRole
import org.moddinginquisition.web.dsl.HandlerClos

import java.nio.file.Files
import java.nio.file.Path

import static org.moddinginquisition.web.dsl.DSLs.*

@CompileStatic
@SuppressWarnings('unused')
class InstanceExtensions {
    private static final Gson GSON = new Gson()

    static void post(Javalin self, String path, @HandlerClos Closure clos) {
        self.post(path, handler(clos))
    }
    static void get(Javalin self, String path, @HandlerClos Closure clos) {
        self.get(path, handler(clos))
    }
    static void delete(Javalin self, String path, @HandlerClos Closure clos) {
        self.delete(path, handler(clos))
    }

    static void post(Javalin self, String path, RouteRole role, @HandlerClos Closure clos) {
        self.post(path, handler(clos), role)
    }
    static void get(Javalin self, String path, RouteRole role, @HandlerClos Closure clos) {
        self.get(path, handler(clos), role)
    }
    static void delete(Javalin self, String path, RouteRole role, @HandlerClos Closure clos) {
        self.delete(path, handler(clos), role)
    }

    static String toResponseJson(Object self) {
        return GSON.toJson(self)
    }
    static <T> T fromJson(Class<T> self, String json) {
        return GSON.fromJson(json, self)
    }

    static boolean asBoolean(Path path) {
        Files.exists(path)
    }
    static boolean asBoolean(Optional self) {
        return self.isPresent()
    }
}