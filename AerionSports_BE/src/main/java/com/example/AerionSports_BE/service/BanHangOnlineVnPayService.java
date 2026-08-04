package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.ChiTietHoaDon;
import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.HoaDon;
import com.example.AerionSports_BE.entity.LichSuHoaDon;
import com.example.AerionSports_BE.entity.LichSuThanhToan;
import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineChiTietSanPhamRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuThanhToanRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineNhanVienRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BanHangOnlineVnPayService {

    public static final int TRANG_THAI_CHO_THANH_TOAN = 8;
    public static final int TRANG_THAI_CAN_XU_LY = 9;
    private static final int TRANG_THAI_DA_XAC_NHAN = 1;
    private static final int TRANG_THAI_DA_HUY = 6;
    private static final String TT_CHO_THANH_TOAN = "Chờ thanh toán";
    private static final String TT_DA_THANH_TOAN = "Đã thanh toán";
    private static final String TT_THAT_BAI = "Thanh toán thất bại";
    private static final String TT_HET_HAN = "Hết hạn thanh toán";

    private final BanHangOnlineHoaDonRepository hoaDonRepository;
    private final BanHangOnlineLichSuThanhToanRepository lichSuThanhToanRepository;
    private final BanHangOnlineLichSuHoaDonRepository lichSuHoaDonRepository;
    private final BanHangOnlineNhanVienRepository nhanVienRepository;
    private final BanHangOnlineChiTietSanPhamRepository chiTietSanPhamRepository;
    private final BanHangOnlinePhieuGiamGiaRepository phieuGiamGiaRepository;
    private final BanHangOnlinePhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final VNPayService vnPayService;

    @Transactional(readOnly = true)
    public String taoUrlThanhToan(String maHoaDon, HttpServletRequest request) {
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDonWithChiTiet(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng VNPay."));
        if (!Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_CHO_THANH_TOAN)) {
            throw new RuntimeException("Đơn hàng không còn ở trạng thái chờ thanh toán.");
        }
        if (daQuaHanThanhToan(hoaDon)) {
            throw new RuntimeException("Đơn hàng đã quá hạn thanh toán VNPay.");
        }
        return vnPayService.taoUrlThanhToan(hoaDon, request);
    }

    @Transactional
    public String xuLyKetQuaThanhToan(Map<String, String> params) {
        String maHoaDon = params == null ? null : params.get("vnp_TxnRef");
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDonWithChiTiet(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng VNPay."));
        LichSuThanhToan thanhToan = layDongThanhToanGanNhat(hoaDon);

        if (!vnPayService.kiemTraChuKy(params)) {
            capNhatThanhToan(thanhToan, TT_THAT_BAI, "VNPay trả về sai chữ ký.");
            return hoaDon.getMaHoaDon();
        }

        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        String transactionNo = params.getOrDefault("vnp_TransactionNo", "");
        if (!"00".equals(responseCode)) {
            capNhatThanhToan(thanhToan, TT_THAT_BAI, "VNPay thanh toán thất bại. Mã phản hồi: " + responseCode);
            return hoaDon.getMaHoaDon();
        }

        capNhatThanhToan(thanhToan, TT_DA_THANH_TOAN, "VNPay giao dịch " + transactionNo);
        if (!Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_CHO_THANH_TOAN)) {
            return hoaDon.getMaHoaDon();
        }

        if (duTonKho(hoaDon)) {
            truTonKho(hoaDon);
            capNhatLuotSuDungPhieuGiamGia(hoaDon);
            doiTrangThaiHoaDon(hoaDon, TRANG_THAI_DA_XAC_NHAN, "VNPay thanh toán thành công", "Đã xác nhận đơn sau khi VNPay thanh toán thành công.");
        } else {
            doiTrangThaiHoaDon(hoaDon, TRANG_THAI_CAN_XU_LY, "VNPay đã thanh toán nhưng thiếu kho", "Thanh toán đã thành công nhưng tồn kho không đủ, cần shop xử lý hoàn tiền hoặc đổi sản phẩm.");
        }
        return hoaDon.getMaHoaDon();
    }

    @Transactional
    public void lamMoiThanhToanChoDon(String maHoaDon, Integer idKhachHang) {
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDonWithChiTiet(maHoaDon)
                .filter(item -> item.getKhachHang() != null && Objects.equals(item.getKhachHang().getId(), idKhachHang))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng của bạn."));
        if (!Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_CHO_THANH_TOAN)) {
            throw new RuntimeException("Chỉ đơn chờ thanh toán VNPay mới được thanh toán lại.");
        }
        if (daQuaHanThanhToan(hoaDon)) {
            huyDonQuaHan(hoaDon);
            throw new RuntimeException("Đơn hàng đã quá hạn thanh toán VNPay.");
        }
        taoLichSuThanhToan(hoaDon, TT_CHO_THANH_TOAN, "Khách hàng tạo lại phiên thanh toán VNPay.");
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void huyDonVnPayQuaHan() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<HoaDon> danhSachQuaHan = hoaDonRepository.findByLoaiHoaDonAndTrangThaiAndNgayTaoBefore(1, TRANG_THAI_CHO_THANH_TOAN, cutoff);
        danhSachQuaHan.forEach(this::huyDonQuaHan);
    }

    private boolean daQuaHanThanhToan(HoaDon hoaDon) {
        return hoaDon.getNgayTao() != null && hoaDon.getNgayTao().isBefore(LocalDateTime.now().minusMinutes(15));
    }

    private void huyDonQuaHan(HoaDon hoaDon) {
        if (!Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_CHO_THANH_TOAN)) {
            return;
        }
        capNhatThanhToan(layDongThanhToanGanNhat(hoaDon), TT_HET_HAN, "Đơn VNPay quá hạn 15 phút chưa thanh toán thành công.");
        doiTrangThaiHoaDon(hoaDon, TRANG_THAI_DA_HUY, "VNPay quá hạn", "Tự hủy đơn VNPay quá hạn thanh toán.");
    }

    private boolean duTonKho(HoaDon hoaDon) {
        if (hoaDon.getChiTietHoaDons() == null) {
            return false;
        }
        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            ChiTietSanPham spct = ct.getChiTietSanPham();
            int tonKho = spct == null || spct.getSoLuong() == null ? 0 : spct.getSoLuong();
            int soLuongDat = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            if (soLuongDat <= 0 || tonKho < soLuongDat) {
                return false;
            }
        }
        return true;
    }

    private void truTonKho(HoaDon hoaDon) {
        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            ChiTietSanPham spct = ct.getChiTietSanPham();
            spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) - ct.getSoLuong());
            chiTietSanPhamRepository.save(spct);
        }
    }

    private void capNhatLuotSuDungPhieuGiamGia(HoaDon hoaDon) {
        if (hoaDon.getPhieuGiamGia() == null || hoaDon.getPhieuGiamGia().getId() == null) {
            return;
        }
        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(hoaDon.getPhieuGiamGia().getId()).orElse(null);
        if (phieu == null) {
            return;
        }
        int daSuDung = phieu.getSoLuongDaSuDung() == null ? 0 : phieu.getSoLuongDaSuDung();
        phieu.setSoLuongDaSuDung(daSuDung + 1);
        if (phieu.getSoLuong() != null && phieu.getSoLuongDaSuDung() >= phieu.getSoLuong()) {
            phieu.setTrangThai(0);
        }
        phieu.setNgayCapNhat(LocalDateTime.now());
        phieuGiamGiaRepository.save(phieu);

        if (hoaDon.getKhachHang() != null) {
            phieuGiamGiaKhachHangRepository.findChuaSuDung(phieu.getId(), hoaDon.getKhachHang().getId())
                    .ifPresent(phieuKhachHang -> {
                        LocalDateTime now = LocalDateTime.now();
                        phieuKhachHang.setDaSuDung(true);
                        phieuKhachHang.setDaSuDungNgay(now);
                        phieuKhachHang.setNgaySuDung(now);
                        phieuGiamGiaKhachHangRepository.save(phieuKhachHang);
                    });
        }
    }

    private LichSuThanhToan layDongThanhToanGanNhat(HoaDon hoaDon) {
        return lichSuThanhToanRepository.findFirstByHoaDon_IdOrderByNgayThanhToanDesc(hoaDon.getId())
                .orElseGet(() -> taoLichSuThanhToan(hoaDon, TT_CHO_THANH_TOAN, "Tạo phiên thanh toán VNPay."));
    }

    private LichSuThanhToan taoLichSuThanhToan(HoaDon hoaDon, String trangThai, String ghiChu) {
        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon);
        lichSu.setSoTien(hoaDon.getTongTienThanhToan());
        lichSu.setPhuongThucThanhToan("VNPay");
        lichSu.setTrangThaiThanhToan(trangThai);
        lichSu.setNgayThanhToan(LocalDateTime.now());
        lichSu.setGhiChu(ghiChu);
        return lichSuThanhToanRepository.save(lichSu);
    }

    private void capNhatThanhToan(LichSuThanhToan thanhToan, String trangThai, String ghiChu) {
        thanhToan.setTrangThaiThanhToan(trangThai);
        thanhToan.setNgayThanhToan(LocalDateTime.now());
        thanhToan.setGhiChu(ghiChu);
        lichSuThanhToanRepository.save(thanhToan);
    }

    private void doiTrangThaiHoaDon(HoaDon hoaDon, Integer trangThaiMoi, String hanhDong, String ghiChu) {
        Integer trangThaiCu = hoaDon.getTrangThai();
        hoaDon.setTrangThai(trangThaiMoi);
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat("VNPay");
        hoaDonRepository.save(hoaDon);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setNhanVien(nhanVienMacDinh());
        lichSu.setTrangThaiCu(trangThaiCu);
        lichSu.setTrangThaiMoi(trangThaiMoi);
        lichSu.setHanhDong(hanhDong);
        lichSu.setGhiChu(ghiChu);
        lichSu.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSu);
    }

    private NhanVien nhanVienMacDinh() {
        return nhanVienRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên mặc định."));
    }
}