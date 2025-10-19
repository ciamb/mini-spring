package mini.spring.core;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClasspathScannerOfCiamb {

    public static List<Class<?>> findClasses(String scanningBasePackage) {
        try {
            String packagePath = scanningBasePackage.replace('.','/');
            URL url = Thread.currentThread().getContextClassLoader().getResource(packagePath);

            if (url == null) {
                return Collections.emptyList();
            }

            String directoryPath = URLDecoder.decode(url.getFile(), "UTF-8");
            File directory = new File(directoryPath);
            List<Class<?>> classes = new ArrayList<>();
            scanDirectory(scanningBasePackage, directory, classes);
            return classes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void scanDirectory(String scanningBasePackage, File directory, List<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(scanningBasePackage + "." + file.getName(), file, classes);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = file.getName().replace(".class", "");
                String fqcn = scanningBasePackage + "." + className;
                try {
                    classes.add(Class.forName(fqcn));
                } catch (ClassNotFoundException ignored) {

                }
            }
        }
    }
}
