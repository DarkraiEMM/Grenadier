#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DepthSampler;

uniform int SmokeCount;
uniform float SmokeTime;
uniform float SmokeInteriorStrength;
uniform vec4 SmokeSpheres[8];
uniform vec4 SmokeColors[8];
uniform vec4 SmokeParams[8];
uniform vec4 SmokeCascades[8];
uniform vec4 SmokeCascadeShapes[8];

in vec2 texCoord;
out vec4 fragColor;

vec2 rotateCloud(vec2 value, vec2 rotation) {
    return vec2(
        value.x * rotation.x - value.y * rotation.y,
        value.x * rotation.y + value.y * rotation.x
    );
}

float lobe(vec3 point, vec3 center, float radius) {
    float distanceToCenter = length(point - center) / radius;
    return 1.0 - smoothstep(0.52, 1.0, distanceToCenter);
}

float sampleDensity(vec3 normalizedPoint, vec2 rotation) {
    normalizedPoint.xz = rotateCloud(normalizedPoint.xz, rotation);
    vec3 warp = vec3(
        sin(normalizedPoint.y * 4.7 + SmokeTime * 0.021),
        sin(normalizedPoint.z * 3.9 - SmokeTime * 0.017),
        sin(normalizedPoint.x * 4.3 + SmokeTime * 0.019)
    ) * 0.075;
    normalizedPoint += warp;
    float body = lobe(normalizedPoint, vec3(0.00, -0.04, 0.00), 0.92);
    body = max(body, lobe(normalizedPoint, vec3(-0.46, -0.12, 0.14), 0.58));
    body = max(body, lobe(normalizedPoint, vec3(0.39, -0.09, -0.27), 0.61));
    body = max(body, lobe(normalizedPoint, vec3(-0.17, 0.30, -0.25), 0.50));
    body = max(body, lobe(normalizedPoint, vec3(0.13, 0.17, 0.41), 0.52));
    float slowPulse = 0.96 + 0.04 * sin(SmokeTime * 0.035 + normalizedPoint.x * 2.1);
    return body * slowPulse;
}

bool ellipsoidSegment(
    vec3 rayDirection,
    float sceneDistance,
    vec3 center,
    vec3 radii,
    out float entry,
    out float segmentLength
) {
    vec3 scaledOrigin = -center / radii;
    vec3 scaledDirection = rayDirection / radii;
    float quadraticA = dot(scaledDirection, scaledDirection);
    float quadraticB = dot(scaledOrigin, scaledDirection);
    float quadraticC = dot(scaledOrigin, scaledOrigin) - 1.0;
    float discriminant = quadraticB * quadraticB - quadraticA * quadraticC;
    if (discriminant <= 0.0) {
        return false;
    }
    float halfChord = sqrt(discriminant);
    entry = max(0.0, (-quadraticB - halfChord) / quadraticA);
    float exitDistance = min(sceneDistance, (-quadraticB + halfChord) / quadraticA);
    segmentLength = exitDistance - entry;
    return segmentLength > 0.0;
}

float integrateBody(
    vec3 rayDirection,
    float sceneDistance,
    vec3 center,
    vec3 radii,
    vec2 rotation
) {
    float entry;
    float segmentLength;
    if (!ellipsoidSegment(rayDirection, sceneDistance, center, radii, entry, segmentLength)) {
        return 0.0;
    }
    float density = 0.0;
    for (int sampleIndex = 0; sampleIndex < 12; sampleIndex++) {
        float t = entry + segmentLength * (float(sampleIndex) + 0.5) / 12.0;
        vec3 normalizedPoint = (rayDirection * t - center) / radii;
        density += sampleDensity(normalizedPoint, rotation);
    }
    return segmentLength * density / 12.0;
}

float softLobeDensity(vec3 point) {
    float distanceToCenter = length(point);
    return 1.0 - smoothstep(0.42, 1.0, distanceToCenter);
}

float integrateSoftLobe(
    vec3 rayDirection,
    float sceneDistance,
    vec3 center,
    vec3 radii
) {
    float entry;
    float segmentLength;
    if (!ellipsoidSegment(rayDirection, sceneDistance, center, radii, entry, segmentLength)) {
        return 0.0;
    }
    float density = 0.0;
    for (int sampleIndex = 0; sampleIndex < 6; sampleIndex++) {
        float t = entry + segmentLength * (float(sampleIndex) + 0.5) / 6.0;
        vec3 normalizedPoint = (rayDirection * t - center) / radii;
        density += softLobeDensity(normalizedPoint);
    }
    return segmentLength * density / 6.0;
}

float cascadePlumesDensity(
    vec3 point,
    vec3 edge,
    vec2 direction,
    float dropDistance,
    float curtainWidth,
    float thickness,
    float seedPhase
) {
    vec2 tangent = vec2(-direction.y, direction.x);
    vec3 offset = point - edge;
    float downward = -offset.y;
    float descent = clamp(downward / max(dropDistance, 0.001), 0.0, 1.0);
    float lateral = dot(offset.xz, tangent);
    float forward = dot(offset.xz, direction);

    float breathing = 0.96
        + 0.10 * sin(downward * 0.82 + seedPhase + SmokeTime * 0.010)
        + 0.045 * sin(downward * 1.91 - seedPhase * 0.73);
    float lowerExpansion = mix(0.94, 1.08, descent);
    float volumeRadius = curtainWidth * 0.40 * breathing * lowerExpansion;
    float lateralDistance = lateral / max(volumeRadius, 0.001);
    float depthDistance = forward / max(volumeRadius, 0.001);
    float crossSection = sqrt(lateralDistance * lateralDistance + depthDistance * depthDistance);
    float roundedVolume = 1.0 - smoothstep(0.46, 1.0, crossSection);

    float verticalEnvelope = smoothstep(-0.65, 0.10, downward);
    verticalEnvelope *= 1.0 - smoothstep(dropDistance - 0.62, dropDistance + 0.52, downward);
    float billow = 0.84
        + 0.10 * sin(downward * 1.29 + lateralDistance * 1.37 + seedPhase)
        + 0.06 * sin(downward * 2.17 - depthDistance * 1.53 - SmokeTime * 0.012);
    return max(0.0, roundedVolume * verticalEnvelope * billow);
}

float integrateCascadePlumes(
    vec3 rayDirection,
    float sceneDistance,
    vec3 edge,
    vec2 direction,
    float dropDistance,
    float curtainWidth,
    float thickness,
    float seedPhase
) {
    vec3 boundsCenter = edge + vec3(
        direction.x * thickness * 0.42,
        -dropDistance * 0.50,
        direction.y * thickness * 0.42
    );
    float horizontalBound = curtainWidth * 0.58 + thickness * 0.72;
    vec3 boundsRadii = vec3(horizontalBound, dropDistance * 0.54 + 0.72, horizontalBound);
    float entry;
    float segmentLength;
    if (!ellipsoidSegment(rayDirection, sceneDistance, boundsCenter, boundsRadii, entry, segmentLength)) {
        return 0.0;
    }
    float density = 0.0;
    for (int sampleIndex = 0; sampleIndex < 24; sampleIndex++) {
        float t = entry + segmentLength * (float(sampleIndex) + 0.5) / 24.0;
        density += cascadePlumesDensity(
            rayDirection * t,
            edge,
            direction,
            dropDistance,
            curtainWidth,
            thickness,
            seedPhase
        );
    }
    return segmentLength * density / 24.0;
}

void main() {
    vec4 scene = texture(DiffuseSampler0, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    vec3 viewPosition = screenToViewSpace(texCoord, depth).xyz;
    float sceneDistance = min(length(viewPosition), VeilCamera.FarPlane);
    vec3 rayDirection = normalize((VeilCamera.IViewMat * vec4(normalize(viewPosition), 0.0)).xyz);

    float transmittance = 1.0;
    vec3 accumulatedSmoke = vec3(0.0);
    for (int i = 0; i < 8; i++) {
        if (i >= SmokeCount) {
            break;
        }
        vec3 anchor = SmokeSpheres[i].xyz;
        float radius = SmokeSpheres[i].w;
        vec4 cascade = SmokeCascades[i];
        bool hasCascade = cascade.w > 0.10;
        float cascadeAmount = step(0.10, cascade.w);
        vec2 cascadeDirection = vec2(0.0);
        if (hasCascade) {
            cascadeDirection = normalize(cascade.xy);
        }
        vec3 center = anchor + vec3(
            0.0,
            radius * mix(0.30, 0.18, cascadeAmount),
            0.0
        );
        vec3 ellipsoidRadii = mix(
            vec3(radius, radius * 0.66, radius),
            vec3(radius * 0.744, radius * 0.504, radius * 0.744),
            cascadeAmount
        );
        float integratedDensity = integrateBody(
            rayDirection, sceneDistance, center, ellipsoidRadii, SmokeParams[i].xy
        );
        if (hasCascade) {
            integratedDensity *= 0.72;
        }

        if (hasCascade) {
            vec2 direction = cascadeDirection;
            vec3 edge = anchor;
            vec4 cascadeShape = SmokeCascadeShapes[i];
            float curtainWidth = max(cascadeShape.x, radius * 0.36) * 1.20;
            float poolRadius = max(cascadeShape.y, radius * 0.18) * 1.08;
            float thickness = clamp(curtainWidth * 0.14, 0.78, radius * 0.32);
            integratedDensity += integrateCascadePlumes(
                rayDirection,
                sceneDistance,
                edge,
                direction,
                cascade.w,
                curtainWidth,
                thickness,
                cascadeShape.z
            ) * 1.14;

            vec3 poolCenter = edge + vec3(
                0.0,
                -cascade.w + max(0.22, poolRadius * 0.12),
                0.0
            );
            vec2 tangent = vec2(-direction.y, direction.x);
            float poolHeight = max(0.28, poolRadius * 0.14);
            vec3 poolRadii = vec3(poolRadius * 0.72, poolHeight, poolRadius * 0.76);
            integratedDensity += integrateSoftLobe(
                rayDirection, sceneDistance, poolCenter, poolRadii
            ) * 0.30;
            integratedDensity += integrateSoftLobe(
                rayDirection,
                sceneDistance,
                poolCenter + vec3(tangent.x * poolRadius * 0.42, 0.04, tangent.y * poolRadius * 0.42),
                vec3(poolRadius * 0.48, poolHeight * 0.86, poolRadius * 0.52)
            ) * 0.20;
            integratedDensity += integrateSoftLobe(
                rayDirection,
                sceneDistance,
                poolCenter - vec3(tangent.x * poolRadius * 0.38, -0.02, tangent.y * poolRadius * 0.38),
                vec3(poolRadius * 0.52, poolHeight * 0.92, poolRadius * 0.46)
            ) * 0.20;
        }

        float opticalDepth = min(integratedDensity * SmokeParams[i].z, 4.2);
        if (opticalDepth <= 0.0001) {
            continue;
        }
        float cloudAlpha = 1.0 - exp(-opticalDepth);
        accumulatedSmoke += transmittance * cloudAlpha * SmokeColors[i].rgb;
        transmittance *= 1.0 - cloudAlpha;
        if (transmittance < 0.015) {
            break;
        }
    }

    vec3 composedColor = scene.rgb * transmittance + accumulatedSmoke;
    float interiorDistanceFog = smoothstep(0.25, 3.50, sceneDistance);
    float interiorFogAmount = SmokeInteriorStrength * mix(0.28, 0.96, interiorDistanceFog);
    composedColor = mix(composedColor, vec3(0.298, 0.314, 0.298), interiorFogAmount);
    fragColor = vec4(composedColor, scene.a);
    gl_FragDepth = depth;
}
