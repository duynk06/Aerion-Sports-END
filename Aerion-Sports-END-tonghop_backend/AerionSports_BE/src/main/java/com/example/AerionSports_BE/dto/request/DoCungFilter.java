package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DoCungFilter {
    private String keyword;
    private Integer trangThai;
    private int page = 0;
    private int size = 10;
}
