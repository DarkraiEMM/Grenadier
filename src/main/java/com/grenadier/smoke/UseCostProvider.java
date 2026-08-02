package com.grenadier.smoke;

@FunctionalInterface
public interface UseCostProvider {
    UseDecision authorizeAndCharge(UseContext context);
}
