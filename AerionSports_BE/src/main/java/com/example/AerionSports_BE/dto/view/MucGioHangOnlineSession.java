package com.example.AerionSports_BE.dto.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MucGioHangOnlineSession implements Serializable {
    // Du lieu toi thieu luu trong session de xac dinh 1 dong gio hang.
    private Integer productId;
    private Integer variantId;
    private int soLuong;
}
