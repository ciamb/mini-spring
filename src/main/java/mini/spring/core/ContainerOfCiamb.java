package mini.spring.core;

import mini.spring.annotations.ComponentByCiamb;
import mini.spring.annotations.InjectByCiamb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ContainerOfCiamb {

    private final Map<Class<?>, Object> singletonsOfCiamb = new HashMap<>();
    private final Set<Class<?>> componentsOfCiamb;

    public ContainerOfCiamb(String scanningBasePackage) {
        this.componentsOfCiamb = ClasspathScannerOfCiamb.findClasses(scanningBasePackage).stream()
                .filter(component -> component.isAnnotationPresent(ComponentByCiamb.class))
                .collect(Collectors.toSet());

        // per ogni component trovato
        componentsOfCiamb.forEach(this::getCiambBean);
    }

    public <T> T getCiambBean(Class<T> classType) {
        Object existing = singletonsOfCiamb.get(classType);

        if (existing != null) {
            return classType.cast(existing); // (T) existing;
        }

        Class<?> implClass = componentsOfCiamb.stream()
                .filter(classType::isAssignableFrom)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No @ComponentByCiamb found for " + classType));

        // a questo punto se trovo una impl la devo costruire
        Object instance = createInstance(implClass);
        injectFields(instance);

        singletonsOfCiamb.put(classType, instance);
        singletonsOfCiamb.put(implClass, instance);
        return classType.cast(instance);
    }

    private Object createInstance(Class<?> implClass) {
        try {
            Optional<Constructor<?>> injectedConstructor = Arrays.stream(implClass.getDeclaredConstructors())
                    .filter(ic -> ic.isAnnotationPresent(InjectByCiamb.class))
                    .findFirst();

            if (injectedConstructor.isPresent()) {
                Constructor<?> constructor = injectedConstructor.get();
                constructor.setAccessible(true);
                Object[] objectArgs = Arrays.stream(constructor.getParameterTypes())
                        .map(this::getCiambBean)
                        .toArray();
                return constructor.newInstance(objectArgs);
            }

            Constructor<?> constructor = Arrays.stream(implClass.getDeclaredConstructors())
                    .min(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new RuntimeException("No constructor found for " + implClass));

            constructor.setAccessible(true);

            Object[] objectArgs = Arrays.stream(constructor.getParameterTypes())
                    .map(this::getCiambBean)
                    .toArray();

            return constructor.newInstance(objectArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectFields(Object instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectByCiamb.class)) {
                field.setAccessible(true);
                Object dep = getCiambBean(field.getType());
                try {
                    field.set(instance, dep);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
