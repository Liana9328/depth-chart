package com.fanduel.depthchart.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlayerPayload(@NotNull Integer number, @NotBlank String name) {
}
