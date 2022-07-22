package org.moddinginquisition.web.launcher;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

// TODO we need to figure out stuff from x-x.module
public class DependencyResolver {
    // group, artifact, artifact-version
    private static final String TARGET_URL = "https://repo1.maven.org/maven2/%s/%s/%s/%s";
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    public static void resolveFullDeps(Set<Dependency> dependencies) throws Exception {
        final var copy = Set.copyOf(dependencies);
        for (final var dep : copy) {
            resolveChilds(dep, dependencies);
        }
    }

    private static void resolveChilds(Dependency dep, Set<Dependency> target) throws Exception {
        final var builder = FACTORY.newDocumentBuilder();
        try {
            if (dep.group == null || dep.artifact == null || dep.version == null) return;
            final var uri =  URI.create(TARGET_URL.formatted(
                    dep.group.replace('.', '/'), dep.artifact, dep.version, dep.artifact + "-" + dep.version
            ) + ".pom");
            try (final var is = uri.toURL().openStream()) {
                final var doc = builder.parse(is);
                final var deps = doc.getElementsByTagName("dependency");
                for (int i = 0; i < deps.getLength(); i++) {
                    final var subDep = (Element) deps.item(i);
                    if (subDep == null) continue;
                    final var scopeElement = subDep.getElementsByTagName("scope").item(0);
                    if (scopeElement == null || scopeElement.getTextContent().equals("runtime")) {
                        try {
                            final var asDep = new Dependency(
                                    subDep.getElementsByTagName("groupId").item(0).getTextContent(),
                                    subDep.getElementsByTagName("artifactId").item(0).getTextContent(),
                                    subDep.getElementsByTagName("version").item(0).getTextContent()
                            );
                            final var actualDep = resolveVersion(target, asDep);
                            if (!target.contains(asDep)) {
                                target.add(actualDep);
                                resolveChilds(actualDep, target);
                            }
                        } catch (NullPointerException ignored) {}
                    }
                }
            }
        } catch (FileNotFoundException | IllegalArgumentException ignored) {}
    }

    private static Dependency resolveVersion(Set<Dependency> existing, Dependency target) {
        return existing.stream()
                .filter(d -> d.group.equals(target.group) && d.artifact.equals(target.artifact))
                .findFirst()
                .map(existingDep -> {
                    if (existingDep.equals(target)) return target;
                    try {
                        final int targetMajor;
                        final int targetMinor;
                        final int targetPatch;
                        {
                            final var split = target.version().split("\\.");
                            targetMajor = Integer.parseInt(split[0]);
                            targetMinor = Integer.parseInt(split[1]);
                            targetPatch = Integer.parseInt(split[2]);
                        }

                        final var splitString = existingDep.version().split("\\.");
                        final int major = Integer.parseInt(splitString[0]);
                        final int minor = Integer.parseInt(splitString[1]);
                        final int patch = Integer.parseInt(splitString[2]);
                        if (major < targetMajor) return target;
                        else if (minor < targetMinor) return target;
                        else if (patch < targetPatch) return target;
                        return existingDep;
                    } catch (Exception ignored) {
                        return target;
                    }
                })
                .orElse(target);
    }

    public record Dependency(String group, String artifact, String version) {
        public static Dependency fromString(String full) {
            final var split = full.split(":");
            return new Dependency(split[0], split[1], split[2]);
        }

        public Path resolvePath(Path basePath) {
            return basePath.resolve(group).resolve(artifact).resolve(version + ".jar");
        }

        public Path download(Path targetPath) throws IOException {
            try {
                final var downloadUrl = URI.create(TARGET_URL.formatted(
                        group.replace('.', '/'), artifact, version, artifact + "-" + version
                ) + ".jar").toURL();
                Files.createDirectories(targetPath.getParent());
                Files.createFile(targetPath);
                try (final var readableByteChannel = Channels.newChannel(downloadUrl.openStream());
                     final var fileOS = new FileOutputStream(targetPath.toFile());
                     final var channel = fileOS.getChannel()) {
                    channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                } catch (FileNotFoundException ex) {
                    return null;
                }
                return targetPath;
            } catch (FileNotFoundException | IllegalArgumentException e) {
                return targetPath;
            }
        }
    }
}
