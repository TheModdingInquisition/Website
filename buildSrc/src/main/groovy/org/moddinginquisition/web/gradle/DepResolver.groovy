package org.moddinginquisition.web.gradle

import groovy.transform.CompileStatic
import org.gradle.api.artifacts.Configuration

@CompileStatic
class DepResolver {

    static String resolveDeps(Configuration... configurations) {
        final deps = new HashSet()
        configurations.each {
            deps.addAll(resolveDepsOfConf(it))
        }
        return String.join(";", deps)
    }

    private static Set<String> resolveDepsOfConf(Configuration conf) {
        final deps = new HashSet()
        conf.allDependencies.each {
            deps.add("$it.group:$it.name:$it.version")
        }
        conf.getExtendsFrom().stream().map(DepResolver.&resolveDepsOfConf)
                .forEach {deps.addAll(it)}
        return deps
    }
}
