package com.cardmanager.card.error;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String resourceName, Object field) {
        super(resourceName + " с полем: " + field + " не найденн(а)");
    }
}
