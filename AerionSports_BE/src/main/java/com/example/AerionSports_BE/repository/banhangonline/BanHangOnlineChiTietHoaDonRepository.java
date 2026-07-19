package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import com.example.AerionSports_BE.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BanHangOnlineChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {

    @Query("""
SELECT new com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse(
    cthd.id,
    sp.maSanPham,
    sp.tenSanPham,
    ms.tenMauSac,
    tl.tenTrongLuong,
    cthd.soLuong,
    cthd.donGia,
    cthd.thanhTien,
    ha.duongDanAnh,
    ctsp.giaBan
)
FROM ChiTietHoaDon cthd
JOIN cthd.chiTietSanPham ctsp
JOIN ctsp.idSanPham sp
LEFT JOIN ctsp.idMauSac ms
LEFT JOIN ctsp.idTrongLuong tl
LEFT JOIN ctsp.hinhAnhs ha
WHERE cthd.hoaDon.id = :idHoaDon
AND (ha.laAnhChinh = true OR ha.id IS NULL)
""")
    List<ChiTietHoaDonResponse> getChiTietHoaDon(@Param("idHoaDon") Integer idHoaDon);

}
