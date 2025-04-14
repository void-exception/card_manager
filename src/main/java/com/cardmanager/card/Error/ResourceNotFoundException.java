package com.cardmanager.card.Error;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String resourceName, Object field) {
        super(resourceName + " с полем: " + field + " не найденн(а)");
    }
}
