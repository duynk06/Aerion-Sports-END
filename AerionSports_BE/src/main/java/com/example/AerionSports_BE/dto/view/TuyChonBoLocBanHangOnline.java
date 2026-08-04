package com.example.AerionSports_BE.dto.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TuyChonBoLocBanHangOnline {
    // Ten hien thi cua 1 muc bo loc, vi du: Yonex, Lining...
    private String label;

    // So san pham dang thuoc vao muc bo loc nay.
    private long count;
}
