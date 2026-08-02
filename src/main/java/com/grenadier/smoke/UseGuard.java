package com.grenadier.smoke;

@FunctionalInterface
public interface UseGuard {
    UseDecision evaluate(UseContext context);
}
