package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.response.ThongKeCardResponse;
import com.example.AerionSports_BE.dto.response.ThongKeChiTietResponse;
import com.example.AerionSports_BE.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ThongKeService {

    @Autowired
    private HoaDonRepository hoaDonRepo;

    public ThongKeCardResponse getSingleCardData(String loaiThoiGian) {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        switch (loaiThoiGian.toLowerCase()) {
            case "today":
                startDateTime = today.atStartOfDay();
                endDateTime = today.atTime(LocalTime.MAX);
                break;
            case "week":
                startDateTime = today.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                endDateTime = today.with(java.time.DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
                break;
            case "month":
                startDateTime = today.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                endDateTime = today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
                break;
            case "year":
                startDateTime = today.with(java.time.temporal.TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                endDateTime = today.with(java.time.temporal.TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
                break;
            default:
                throw new IllegalArgumentException("Mốc thời gian không hợp lệ!");
        }

        ThongKeCardResponse card = new ThongKeCardResponse();
        card.setDoanhThu(hoaDonRepo.sumDoanhThuThucTe(startDateTime, endDateTime));
        card.setDoanhThuTienMat(hoaDonRepo.sumDoanhThuTienMat(startDateTime, endDateTime));
        card.setDoanhThuChuyenKhoan(hoaDonRepo.sumDoanhThuChuyenKhoan(startDateTime, endDateTime));
        card.setSoSanPhamDaBan(hoaDonRepo.countSanPhamDaBanThucTe(startDateTime, endDateTime));
        card.setTongDonHang(hoaDonRepo.countTongDonHangPhatSinh(startDateTime, endDateTime));
        card.setDonHoanThanh(hoaDonRepo.countDonHangTheoTrangThaiCucBo(5, startDateTime, endDateTime));
        card.setDonHuy(hoaDonRepo.countDonHangTheoTrangThaiCucBo(0, startDateTime, endDateTime));
        card.setDonDangXuLy(hoaDonRepo.countDonHangTheoTrangThaiCucBo(1, startDateTime, endDateTime));
        return card;
    }

    public ThongKeChiTietResponse getThongKeChiTietDuLieuDong(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null) tuNgay = LocalDate.now().minusDays(30);
        if (denNgay == null) denNgay = LocalDate.now();

        ThongKeChiTietResponse dto = new ThongKeChiTietResponse();

        dto.setTopBanChay(hoaDonRepo.queryTopBanChay(tuNgay, denNgay).stream().map(obj -> {
            ThongKeChiTietResponse.TopBanChay item = new ThongKeChiTietResponse.TopBanChay();
            item.setTenSanPham((String) obj[0]);
            item.setDaBan(((Number) obj[1]).longValue());
            item.setTonKho(((Number) obj[2]).intValue());
            return item;
        }).toList());

        dto.setTrangThaiDonHang(hoaDonRepo.queryTrangThaiDonHang(tuNgay, denNgay).stream().map(obj -> {
            ThongKeChiTietResponse.TrangThaiDonHang item = new ThongKeChiTietResponse.TrangThaiDonHang();
            item.setTenTrangThai((String) obj[0]);
            item.setSoLuong(((Number) obj[1]).longValue());
            return item;
        }).toList());

        dto.setKhachHangTiemNang(hoaDonRepo.queryKhachHangTiemNang(tuNgay, denNgay).stream().map(obj -> {
            ThongKeChiTietResponse.KhachHangTiemNang item = new ThongKeChiTietResponse.KhachHangTiemNang();
            item.setHoTen((String) obj[0]);
            item.setSdt((String) obj[1]);
            item.setSoDon(((Number) obj[2]).longValue());
            item.setTongChiTieu(BigDecimal.valueOf(((Number) obj[3]).doubleValue()));
            return item;
        }).toList());

        dto.setSanPhamTonKho(hoaDonRepo.querySanPhamBanChamTonKho(denNgay).stream().map(obj -> {
            ThongKeChiTietResponse.SanPhamTonKho item = new ThongKeChiTietResponse.SanPhamTonKho();
            item.setTenSanPham((String) obj[0]);
            item.setDaBan(((Number) obj[1]).longValue());
            item.setTonKho(((Number) obj[2]).intValue());
            return item;
        }).toList());

        return dto;
    }

    // 🌟 ĐÃ SỬA: Bảo vệ ép kiểu an toàn cho biểu đồ Theo Ngày
    public Map<Integer, BigDecimal> getDoanhThuDoThiBieuDo(int thang, int nam) {
        List<Object[]> rawList = hoaDonRepo.queryDoanhThuTheoTungNgayTrongThang(thang, nam);
        Map<Integer, BigDecimal> mapData = new HashMap<>();
        for (Object[] obj : rawList) {
            if (obj[0] != null && obj[1] != null) {
                Integer ngayKey = ((Number) obj[0]).intValue();
                BigDecimal doanhThuVal = BigDecimal.valueOf(((Number) obj[1]).doubleValue());
                mapData.put(ngayKey, doanhThuVal);
            }
        }
        return mapData;
    }

    public Map<String, Object> getDoanhThuDoThiBieuDoTachPhuongThuc(int thang, int nam) {
        Map<String, Object> result = new HashMap<>();
        result.put("tienMat", toMap(hoaDonRepo.queryDoanhThuTheoNgayTienMat(thang, nam)));
        result.put("chuyenKhoan", toMap(hoaDonRepo.queryDoanhThuTheoNgayChuyenKhoan(thang, nam)));
        return result;
    }

    private Map<Integer, BigDecimal> toMap(List<Object[]> rawList) {
        Map<Integer, BigDecimal> mapData = new HashMap<>();
        for (Object[] obj : rawList) {
            if (obj[0] != null && obj[1] != null) {
                mapData.put(((Number) obj[0]).intValue(), BigDecimal.valueOf(((Number) obj[1]).doubleValue()));
            }
        }
        return mapData;
    }

    // 🌟 ĐÃ SỬA: Bảo vệ ép kiểu an toàn cho biểu đồ Theo Tháng
    public Map<Integer, BigDecimal> getDoanhThuTheoNam(int nam) {
        List<Object[]> rawList = hoaDonRepo.queryDoanhThu12ThangTheoNam(nam);
        Map<Integer, BigDecimal> mapData = new HashMap<>();
        for (Object[] obj : rawList) {
            if (obj[0] != null && obj[1] != null) {
                Integer thangKey = ((Number) obj[0]).intValue();
                BigDecimal doanhThuVal = BigDecimal.valueOf(((Number) obj[1]).doubleValue());
                mapData.put(thangKey, doanhThuVal);
            }
        }
        return mapData;
    }

    // 🌟 ĐÃ SỬA: Bảo vệ ép kiểu an toàn cho biểu đồ Theo Quý
    public Map<Integer, BigDecimal> getDoanhThuTheoQuy(int nam) {
        List<Object[]> rawList = hoaDonRepo.queryDoanhThu4QuyTheoNam(nam);
        Map<Integer, BigDecimal> mapData = new HashMap<>();
        for (Object[] obj : rawList) {
            if (obj[0] != null && obj[1] != null) {
                Integer quyKey = ((Number) obj[0]).intValue();
                BigDecimal doanhThuVal = BigDecimal.valueOf(((Number) obj[1]).doubleValue());
                mapData.put(quyKey, doanhThuVal);
            }
        }
        return mapData;
    }
}