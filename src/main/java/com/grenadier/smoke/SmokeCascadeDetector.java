package com.grenadier.smoke;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

final class SmokeCascadeDetector {
    private static final int DIRECTIONS = 8;
    private static final int RADIAL_SAMPLES = 4;
    private static final int LATERAL_SAMPLES = 9;
    private static final double[][] VERTICAL_PROBES = {
            {0.0D, 0.0D},
            {0.75D, 0.0D}, {-0.75D, 0.0D}, {0.0D, 0.75D}, {0.0D, -0.75D},
            {0.75D, 0.75D}, {0.75D, -0.75D}, {-0.75D, 0.75D}, {-0.75D, -0.75D}
    };

    private SmokeCascadeDetector() {
    }

    static SmokeCascade detect(ServerLevel level, Vec3 center, double radius) {
        double verticalDrop = detectVerticalDrop(level, center);
        if (verticalDrop >= 2.5D) {
            double curtainWidth = Math.min(
                    SmokeCascade.MAX_CURTAIN_WIDTH,
                    Math.max(2.0D, radius * 1.98D)
            );
            double poolRadius = poolRadius(radius, curtainWidth, verticalDrop, true);
            return new SmokeCascade(
                    1.0F,
                    0.0F,
                    0.0F,
                    (float) verticalDrop,
                    (float) curtainWidth,
                    (float) poolRadius
            );
        }

        double bestDrop = 0.0D;
        double bestDirectionX = 0.0D;
        double bestDirectionZ = 0.0D;
        double bestEdgeDistance = 0.0D;
        double startDistance = radius * 0.34D;
        double endDistance = radius * 0.86D;

        for (int directionIndex = 0; directionIndex < DIRECTIONS; directionIndex++) {
            double angle = directionIndex * Math.PI * 2.0D / DIRECTIONS;
            double directionX = Math.cos(angle);
            double directionZ = Math.sin(angle);
            for (int sampleIndex = 0; sampleIndex < RADIAL_SAMPLES; sampleIndex++) {
                double progress = sampleIndex / (double) (RADIAL_SAMPLES - 1);
                double distance = startDistance + (endDistance - startDistance) * progress;
                int sampleX = (int) Math.floor(center.x + directionX * distance);
                int sampleZ = (int) Math.floor(center.z + directionZ * distance);
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sampleX, sampleZ);
                double drop = center.y - surfaceY;
                if (drop >= 2.5D && drop > bestDrop) {
                    bestDrop = drop;
                    bestDirectionX = directionX;
                    bestDirectionZ = directionZ;
                    bestEdgeDistance = distance;
                    break;
                }
            }
        }

        if (bestDrop < 2.5D) {
            return SmokeCascade.NONE;
        }
        double tangentX = -bestDirectionZ;
        double tangentZ = bestDirectionX;
        double halfSpan = Math.max(1.5D, radius * 0.72D);
        double spacing = halfSpan * 2.0D / (LATERAL_SAMPLES - 1);
        boolean[] openDrop = new boolean[LATERAL_SAMPLES];
        double[] drops = new double[LATERAL_SAMPLES];
        for (int lateralIndex = 0; lateralIndex < LATERAL_SAMPLES; lateralIndex++) {
            double lateral = (lateralIndex - (LATERAL_SAMPLES - 1) * 0.5D) * spacing;
            int sampleX = (int) Math.floor(center.x + bestDirectionX * bestEdgeDistance + tangentX * lateral);
            int sampleZ = (int) Math.floor(center.z + bestDirectionZ * bestEdgeDistance + tangentZ * lateral);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sampleX, sampleZ);
            drops[lateralIndex] = center.y - surfaceY;
            openDrop[lateralIndex] = drops[lateralIndex] >= 2.5D;
        }

        int centerIndex = LATERAL_SAMPLES / 2;
        int first = centerIndex;
        int last = centerIndex;
        while (first > 0 && openDrop[first - 1]) {
            first--;
        }
        while (last + 1 < LATERAL_SAMPLES && openDrop[last + 1]) {
            last++;
        }
        double dropSum = 0.0D;
        int dropSamples = 0;
        for (int index = first; index <= last; index++) {
            if (openDrop[index]) {
                dropSum += drops[index];
                dropSamples++;
            }
        }
        double representativeDrop = dropSamples == 0 ? bestDrop : Math.max(bestDrop, dropSum / dropSamples);
        double curtainWidth = Math.min(
                SmokeCascade.MAX_CURTAIN_WIDTH,
                Math.max(radius * 0.38D, (last - first + 1) * spacing)
        );
        double poolRadius = poolRadius(radius, curtainWidth, representativeDrop, false);
        return new SmokeCascade(
                (float) bestDirectionX,
                (float) bestDirectionZ,
                (float) bestEdgeDistance,
                (float) Math.min(representativeDrop, SmokeCascade.MAX_DROP),
                (float) curtainWidth,
                (float) poolRadius
        );
    }

    private static double detectVerticalDrop(ServerLevel level, Vec3 center) {
        double bestDrop = 0.0D;
        for (double[] probe : VERTICAL_PROBES) {
            bestDrop = Math.max(bestDrop, detectVerticalDropColumn(
                    level,
                    center,
                    center.x + probe[0],
                    center.z + probe[1]
            ));
        }
        return bestDrop;
    }

    private static double detectVerticalDropColumn(
            ServerLevel level,
            Vec3 center,
            double probeX,
            double probeZ
    ) {
        int x = (int) Math.floor(probeX);
        int z = (int) Math.floor(probeZ);
        int startY = (int) Math.floor(center.y - 0.15D);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
        int scanBlocks = (int) Math.ceil(SmokeCascade.MAX_DROP) + 1;
        for (int step = 0; step <= scanBlocks; step++) {
            int y = startY - step;
            cursor.set(x, y, z);
            if (level.getBlockState(cursor).blocksMotion()) {
                double drop = center.y - (y + 1.0D);
                return drop >= 2.5D ? Math.min(drop, SmokeCascade.MAX_DROP) : 0.0D;
            }
        }
        return SmokeCascade.MAX_DROP;
    }

    private static double poolRadius(double radius, double curtainWidth, double drop, boolean verticalAirburst) {
        double minimumRadius = verticalAirburst ? radius * 0.82D : radius * 0.20D;
        double widthContribution = curtainWidth * (verticalAirburst ? 0.44D : 0.32D);
        return Math.min(
                SmokeCascade.MAX_POOL_RADIUS,
                Math.max(minimumRadius, widthContribution + drop * 0.035D)
        );
    }
}
