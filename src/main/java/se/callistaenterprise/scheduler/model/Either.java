package se.callistaenterprise.scheduler.model;

import lombok.Getter;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Either<V, E extends Errors> {
    private final V left;  // actual value
    private final E right; // errors if any

    public Either(V target, E errors) {
        this.left = target;
        this.right = errors;
    }

    public static <V, E extends Errors> Either<V, E> left(V target) {
        return new Either<>(target, null);
    }

    public static <V, E extends Errors> Either<V, E> right(E errors) {
        return new Either<>(null, errors);
    }

    public boolean hasErrors() {
        return right != null && right.hasErrors();
    }

    public Map<String, String> getAllErrors() {
        Map<String, String> errorMap = new HashMap<>();
        right.getAllErrors().forEach(error -> {
            Map.Entry<String, String> fieldError = extractFieldError((FieldError) error);
            errorMap.put(fieldError.getKey(), fieldError.getValue());
        });
        return errorMap;
    }

    private Map.Entry<String, String> extractFieldError(FieldError error) {
        return Map.entry(error.getField(), error.getDefaultMessage());
    }
}

