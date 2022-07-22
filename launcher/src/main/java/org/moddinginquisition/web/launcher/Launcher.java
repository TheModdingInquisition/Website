package org.moddinginquisition.web.launcher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Launcher {
    public static void main(String[] args) throws Exception {
        final var tgtFile = new File(args[0]);
        final var targetLaunch = new JarFile(tgtFile);
        final var jvmArgsFile = args.length < 2 ? null : args[1];

        final var cpPath = Path.of("classpath.txt");
        final String depsIn;
        try (final var is = targetLaunch.getInputStream(targetLaunch.getEntry("dependencies.txt"))) {
            depsIn = new String(is.readAllBytes());
        }

        final var mainClass = targetLaunch.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        Files.writeString(cpPath, "-cp " + tgtFile.toPath().toAbsolutePath() + (WINDOWS ? ";" : ":") + downloadAndResolveCpDeps(depsIn));

        final var command = new ArrayList<String>();
        command.add(findJavaBinary());
        command.add("@classpath.txt");
        if (jvmArgsFile != null) command.add("@" + jvmArgsFile);
        command.add(mainClass);
        new ProcessBuilder(command)
                .directory(new File(System.getProperty("user.dir")))
                .inheritIO()
                .start();
        System.exit(0);
    }

    public static String findJavaBinary() {
        return ProcessHandle.current().info().command().orElse("java");
    }

    private static String downloadAndResolveCpDeps(String depsIn) throws Exception {
        final var deps = Stream.of(depsIn.split(";"))
                .map(DependencyResolver.Dependency::fromString)
                .collect(Collectors.toCollection(HashSet::new));
        final Path libsFolder;
        {
            final var prop = System.getProperty("libsFolder");
            libsFolder = Path.of(Objects.requireNonNullElse(prop, "libs"));
        }

        final List<String> usedLibs = new ArrayList<>();
        DependencyResolver.resolveFullDeps(deps);
        for (final var dep : deps) {
            final var out = dep.resolvePath(libsFolder).toAbsolutePath();
            usedLibs.add(out.toString());
            if (!Files.exists(out)) {
                System.out.println("Downloading '" + dep.group() + ":" + dep.artifact() + ":" + dep.version() + "'");
                if (dep.download(out) == null) {
                    usedLibs.remove(out.toString());
                }
            }
        }
        return String.join(WINDOWS ? ";" : ":", usedLibs);
    }

    public static final boolean WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
}
