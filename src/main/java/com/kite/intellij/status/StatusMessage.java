package com.kite.intellij.status;

import java.util.Optional;

/**
 * Defines a status message which contains either a success or an error message.
 * If both are undefined or defined then the constructor will throw an {@link IllegalStateException}.
 *
  */
class StatusMessage {
    private final Optional<String> success;
    private final Optional<String> error;

    public StatusMessage(Optional<String> success, Optional<String> error) {
        this.success = success;
        this.error = error;

        if (success.isPresent() == error.isPresent()) {
            throw new IllegalStateException("One message must be defined, the other one undefined");
        }
    }

    public static StatusMessage success(String message) {
        return new StatusMessage(Optional.of(message), Optional.empty());
    }

    public static StatusMessage error(String message) {
        return new StatusMessage(Optional.empty(), Optional.of(message));
    }

    public boolean isSuccess() {
        return success.isPresent();
    }

    public boolean isError() {
        return error.isPresent();
    }

    public String getMessage() {
        if (success.isPresent()) {
            return success.get();
        } else if (error.isPresent()) {
            return error.get();
        }

        throw new IllegalStateException("Invalid State, neither success nor error is defined.");
    }
}
