package com.fanduel.depthchart.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AddPlayerRequest(@NotNull @Valid PlayerPayload player, Integer depth) {
}
