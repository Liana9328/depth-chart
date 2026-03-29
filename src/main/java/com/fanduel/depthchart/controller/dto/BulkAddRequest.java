package com.fanduel.depthchart.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BulkAddRequest(@NotBlank String position, @NotNull @Valid PlayerPayload player, Integer depth) {
}
