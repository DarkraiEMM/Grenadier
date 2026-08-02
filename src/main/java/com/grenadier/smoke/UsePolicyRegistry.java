package com.grenadier.smoke;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UsePolicyRegistry {
    private static final List<UseGuard> GUARDS = new CopyOnWriteArrayList<>();
    private static volatile UseCostProvider costProvider;

    private UsePolicyRegistry() {
    }

    public static UseDecision authorize(UseContext context) {
        for (UseGuard guard : GUARDS) {
            UseDecision decision = guard.evaluate(context);
            if (!decision.allowed()) {
                return decision;
            }
        }
        UseCostProvider provider = costProvider;
        return provider == null ? UseDecision.allow() : provider.authorizeAndCharge(context);
    }

    public static void addGuard(UseGuard guard) {
        GUARDS.add(guard);
    }

    public static synchronized void setCostProvider(UseCostProvider provider) {
        if (provider != null && costProvider != null && costProvider != provider) {
            throw new IllegalStateException("A smoke grenade cost provider is already registered");
        }
        costProvider = provider;
    }

    public static synchronized void clearCostProvider(UseCostProvider provider) {
        if (costProvider == provider) {
            costProvider = null;
        }
    }
}
