package com.online.voting.gateway.handler;

public class MissingRoleClaimException extends RuntimeException {

    public MissingRoleClaimException(String message) {
        super(message);
    }
}
