package com.akaxedx.newqqbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ding {
    private Long id;
    private Double length;
    private Integer has;
    private Long time;
}
