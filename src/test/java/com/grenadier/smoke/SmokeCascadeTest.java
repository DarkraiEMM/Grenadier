package com.grenadier.smoke;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeCascadeTest {
    @Test
    void onlyZeroEdgeDropIsTreatedAsVerticalAirburst() {
        assertTrue(new SmokeCascade(1.0F, 0.0F, 0.0F, 8.0F, 12.0F, 7.0F).verticalAirburst());
        assertFalse(new SmokeCascade(1.0F, 0.0F, 3.0F, 8.0F, 12.0F, 7.0F).verticalAirburst());
        assertFalse(SmokeCascade.NONE.verticalAirburst());
    }
}
