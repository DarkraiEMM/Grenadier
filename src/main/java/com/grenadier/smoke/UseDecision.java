package com.grenadier.smoke;

import net.minecraft.network.chat.Component;

import java.util.OptionalInt;

public record UseDecision(boolean allowed, Component denial, OptionalInt effectColor) {
    public static UseDecision allow() {
        return new UseDecision(true, Component.empty(), OptionalInt.empty());
    }

    public static UseDecision allowColor(int color) {
        return new UseDecision(true, Component.empty(), OptionalInt.of(color));
    }

    public static UseDecision deny(Component denial) {
        return new UseDecision(false, denial, OptionalInt.empty());
    }
}
