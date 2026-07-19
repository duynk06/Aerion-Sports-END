package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.response.ThongKeCardResponse;
import com.example.AerionSports_BE.dto.response.ThongKeChiTietResponse;
import jakarta.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ThongKeService thongKeService;

    // Tự động bốc email gửi của bạn (vietphan0925@gmail.com) làm email nhận báo cáo chính luôn
    @Value("${spring.mail.username}")
    private String emailChinhNhanBaoCao;

    /**
     * Tính năng 1: Gửi email cấp tài khoản và mật khẩu tạm thời cho Nhân viên mới
     * @param toEmail Email của nhân viên nhận tài khoản
     * @param tenNhanVien Tên hiển thị của nhân viên
     * @param matKhauTamThoi Chuỗi mật khẩu ngẫu nhiên 8 ký tự sinh từ Backend
     */
    @Async
    public void sendAccountCreationEmail(String toEmail, String tenNhanVien, String matKhauTamThoi) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎉 Aerion Sports - Thông báo cấp tài khoản nhân viên hệ thống");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; border: 1px solid #f79b66; border-radius: 12px; max-width: 550px; margin: 0 auto;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "  <h2 style='color: #f79b66; margin: 0; font-size: 22px; letter-spacing: 1px;'>CHÀO MỪNG THÀNH VIÊN MỚI</h2>"
                    + "  <p style='color: #475569; font-size: 14px;'>Tài khoản quản trị nội bộ hệ thống Aerion Sports của bạn đã được khởi tạo!</p>"
                    + "</div>"
                    + "<p>Xin chào <strong>" + tenNhanVien + "</strong>,</p>"
                    + "<p>Dưới đây là thông tin đăng nhập cá nhân của bạn trên hệ thống, vui lòng bảo mật thông tin này:</p>"
                    + "<div style='background-color: #f8fafc; padding: 18px; border-radius: 8px; border: 1px solid #e2e8f0; margin: 15px 0; line-height: 1.6;'>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>🌐 <strong>Trang quản trị:</strong> <a href='http://localhost:5173/login' style='color: #ea712b; text-decoration: none; font-weight: bold;'>Click để đến trang Đăng nhập</a></p>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>📧 <strong>Tài khoản (Username):</strong> <span style='font-weight: 600; color: #1e293b;'>" + toEmail + "</span></p>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>🔑 <strong>Mật khẩu tạm thời:</strong> <span style='font-weight: 700; color: #dc2626; font-family: monospace; background: #fee2e2; padding: 2px 6px; border-radius: 4px;'>" + matKhauTamThoi + "</span></p>"
                    + "</div>"
                    + "<p style='color: #ef4444; font-size: 12.5px; font-style: italic; font-weight: 500;'>⚠️ *Lưu ý quan trọng: Vì lý do bảo mật dữ liệu cửa hàng, bạn bắt buộc phải thực hiện thay đổi mật khẩu mới ngay trong lần đầu tiên đăng nhập hệ thống thành công.</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='font-size: 11px; color: #94a3b8; text-align: center; margin: 0;'>Hệ thống vận hành tự động Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Đã gửi thông tin tài khoản thành công tới email: " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Thất bại khi gửi tài khoản về mail " + toEmail + ". Lý do: " + e.getMessage());
        }
    }

    @Async
    public void sendKhachHangAccountEmail(String toEmail, String tenKhachHang, String matKhauTamThoi) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎉 Aerion Sports - Đăng ký tài khoản thành công");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; border: 1px solid #f79b66; border-radius: 12px; max-width: 550px; margin: 0 auto;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "  <h2 style='color: #f79b66; margin: 0; font-size: 22px; letter-spacing: 1px;'>CẢM ƠN BẠN ĐÃ ĐĂNG KÝ</h2>"
                    + "  <p style='color: #475569; font-size: 14px;'>Tài khoản mua sắm của bạn tại Aerion Sports đã sẵn sàng!</p>"
                    + "</div>"
                    + "<p>Xin chào <strong>" + tenKhachHang + "</strong>,</p>"
                    + "<p>Dưới đây là thông tin đăng nhập của bạn:</p>"
                    + "<div style='background-color: #f8fafc; padding: 18px; border-radius: 8px; border: 1px solid #e2e8f0; margin: 15px 0; line-height: 1.6;'>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>📧 <strong>Tài khoản (Username):</strong> <span style='font-weight: 600; color: #1e293b;'>" + toEmail + "</span></p>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>🔑 <strong>Mật khẩu:</strong> <span style='font-weight: 700; color: #dc2626; font-family: monospace; background: #fee2e2; padding: 2px 6px; border-radius: 4px;'>" + matKhauTamThoi + "</span></p>"
                    + "</div>"
                    + "<p style='color: #ef4444; font-size: 12.5px; font-style: italic; font-weight: 500;'>⚠️ Để bảo mật, bạn nên đổi mật khẩu ngay sau lần đăng nhập đầu tiên.</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='font-size: 11px; color: #94a3b8; text-align: center; margin: 0;'>Hệ thống vận hành tự động Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Đã gửi tài khoản khách hàng tới: " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Thất bại khi gửi tài khoản khách hàng tới " + toEmail + ". Lý do: " + e.getMessage());
        }
    }

    /**
     * Tính năng: Gửi email thông báo mật khẩu mới sau khi người dùng yêu cầu đặt lại mật khẩu (quên mật khẩu)
     * @param toEmail Email của người dùng nhận mật khẩu mới
     * @param tenNguoiDung Tên hiển thị của người dùng
     * @param matKhauMoi Mật khẩu mới được hệ thống sinh ra
     */
    @Async
    public void sendResetPasswordEmail(String toEmail, String tenNguoiDung, String matKhauMoi) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔐 Aerion Sports - Yêu cầu đặt lại mật khẩu");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; border: 1px solid #f79b66; border-radius: 12px; max-width: 550px; margin: 0 auto;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "  <h2 style='color: #f79b66; margin: 0; font-size: 22px; letter-spacing: 1px;'>YÊU CẦU ĐẶT LẠI MẬT KHẨU</h2>"
                    + "  <p style='color: #475569; font-size: 14px;'>Hệ thống Aerion Sports đã ghi nhận yêu cầu đặt lại mật khẩu của bạn.</p>"
                    + "</div>"
                    + "<p>Xin chào <strong>" + tenNguoiDung + "</strong>,</p>"
                    + "<p>Mật khẩu mới của bạn đã được khởi tạo, vui lòng sử dụng thông tin dưới đây để đăng nhập:</p>"
                    + "<div style='background-color: #f8fafc; padding: 18px; border-radius: 8px; border: 1px solid #e2e8f0; margin: 15px 0; line-height: 1.6;'>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>📧 <strong>Tài khoản (Username):</strong> <span style='font-weight: 600; color: #1e293b;'>" + toEmail + "</span></p>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>🔑 <strong>Mật khẩu mới:</strong> <span style='font-weight: 700; color: #dc2626; font-family: monospace; background: #fee2e2; padding: 2px 6px; border-radius: 4px;'>" + matKhauMoi + "</span></p>"
                    + "</div>"
                    + "<p style='color: #ef4444; font-size: 12.5px; font-style: italic; font-weight: 500;'>⚠️ *Nếu bạn không phải là người thực hiện yêu cầu này, vui lòng đổi mật khẩu ngay hoặc liên hệ với chúng tôi để được hỗ trợ.</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='font-size: 11px; color: #94a3b8; text-align: center; margin: 0;'>Hệ thống vận hành tự động Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Đã gửi mật khẩu mới thành công tới email: " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Thất bại khi gửi mật khẩu mới về mail " + toEmail + ". Lý do: " + e.getMessage());
        }
    }

    /**
     * Tính năng 2: Gửi email chứa mã giảm giá (Voucher) cá nhân dành riêng cho Khách hàng được tri ân
     * @param toEmail Email của khách hàng nhận voucher
     * @param tenKhachHang Tên hiển thị của khách hàng
     * @param maVoucher Mã phiếu giảm giá (Ví dụ: APRILELEMENT, TRIAN2026...)
     * @param giaTriGiam Giá trị phần trăm giảm giá (Ví dụ: 20%)
     * @param ngayKetThuc Hạn sử dụng của voucher
     */
    @Async
    public void sendVoucherEmail(String toEmail, String tenKhachHang, String maVoucher, String giaTriGiam, String ngayKetThuc) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎁 Quà tặng đặc biệt từ Aerion Sports dành riêng cho bạn!");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; border: 1px solid #f79b66; border-radius: 12px; max-width: 500px; margin: 0 auto;'>"
                    + "<div style='text-align: center; margin-bottom: 15px;'>"
                    + "  <h2 style='color: #ea712b; margin: 0; font-size: 20px;'>MÓN QUÀ TRI ÂN ĐẶC BIỆT</h2>"
                    + "</div>"
                    + "<p>Thân gửi Quý khách hàng <strong>" + tenKhachHang + "</strong>,</p>"
                    + "<p>Aerion Sports xin gửi tặng riêng bạn mã giảm giá cá nhân siêu ưu đãi áp dụng cho toàn bộ các sản phẩm vợt và phụ kiện cầu lông tại hệ thống:</p>"
                    + "<div style='background: #fff7ed; padding: 20px; text-align: center; border: 2px dashed #f79b66; border-radius: 8px; margin: 20px 0;'>"
                    + "  <span style='font-size: 13px; color: #7c2d12; display: block; margin-bottom: 5px; font-weight: bold;'>MÃ PHIẾU GIẢM GIÁ CỦA BẠN:</span>"
                    + "  <span style='font-size: 24px; font-weight: 800; color: #ea712b; font-family: sans-serif; letter-spacing: 1px;'>" + maVoucher + "</span>"
                    + "  <span style='display: block; margin-top: 8px; font-size: 15px; color: #1e293b; font-weight: bold;'>Ưu đãi giảm ngay: " + giaTriGiam + "</span>"
                    + "</div>"
                    + "<p style='font-size: 14px;'>⏰ Thời hạn áp dụng ưu đãi kéo dài đến hết ngày: <strong style='color: #ea712b;'>" + ngayKetThuc + "</strong></p>"
                    + "<p style='margin-top: 15px;'>Mã số có hạn và chỉ áp dụng duy nhất cho tài khoản cá nhân của bạn. Hãy nhanh chân ghé qua cửa hàng Aerion Sports để săn ngay những siêu phẩm với mức giá tốt nhất nhé!</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='font-size: 11px; color: #94a3b8; text-align: center; margin: 0;'>Cảm ơn bạn đã đồng hành cùng Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Đã phát hành và gửi voucher thành công tới khách hàng: " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Lỗi khi phát hành voucher tới mail " + toEmail + ". Chi tiết: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Ho_Chi_Minh")
    public void tuDongGuiBaoCaoCuoiNgayScheduled() {
        System.out.println(">>> [CRON JOB] Đã đến 17h00 chiều! Hệ thống bắt đầu kết xuất báo cáo doanh thu tự động...");
        this.executeExportExcelAndSendEmail();
    }

    /**
     * TÁC VỤ 3.2: Hàm cốt lõi xử lý kết xuất bảng Excel và gửi đính kèm qua Email
     */
    public void executeExportExcelAndSendEmail() {
        LocalDate homNay = LocalDate.now();
        String chuoiNgay = homNay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        LocalDateTime batDauHomNay = homNay.atStartOfDay();
        LocalDateTime ketThucHomNay = homNay.atTime(LocalTime.MAX);

        ThongKeCardResponse dataCard = thongKeService.getSingleCardData("today");
        ThongKeChiTietResponse dataTable = thongKeService.getThongKeChiTietDuLieuDong(homNay, homNay);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thống Kê Ngày " + homNay.toString());

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("BÁO CÁO DOANH THU & HIỆU SUẤT KINH DOANH NGÀY " + chuoiNgay);

            sheet.createRow(2).createCell(0).setCellValue("I. CHỈ SỐ TỔNG QUAN");
            sheet.createRow(3).createCell(0).setCellValue("Tổng Doanh Thu Hợp Lệ:");
            sheet.createRow(3).createCell(1).setCellValue(dataCard.getDoanhThu() != null ? dataCard.getDoanhThu().doubleValue() : 0.0);
            sheet.createRow(4).createCell(0).setCellValue("Tổng Đơn Hàng Phát Sinh:");
            sheet.createRow(4).createCell(1).setCellValue(dataCard.getTongDonHang() != null ? dataCard.getTongDonHang() : 0);
            sheet.createRow(5).createCell(0).setCellValue("Số Sản Phẩm Bán Được:");
            sheet.createRow(5).createCell(1).setCellValue(dataCard.getSoSanPhamDaBan() != null ? dataCard.getSoSanPhamDaBan() : 0);

            int currRow = 7;
            sheet.createRow(currRow++).createCell(0).setCellValue("II. TIẾN ĐỘ ĐƠN HÀNG TRONG NGÀY");
            Row headStatus = sheet.createRow(currRow++);
            headStatus.createCell(0).setCellValue("Trạng Thái Đơn");
            headStatus.createCell(1).setCellValue("Số Lượng");
            headStatus.getCell(0).setCellStyle(headerStyle);
            headStatus.getCell(1).setCellStyle(headerStyle);

            if (dataTable.getTrangThaiDonHang() != null) {
                for (ThongKeChiTietResponse.TrangThaiDonHang tt : dataTable.getTrangThaiDonHang()) {
                    Row r = sheet.createRow(currRow++);
                    r.createCell(0).setCellValue(tt.getTenTrangThai());
                    r.createCell(1).setCellValue(tt.getSoLuong());
                }
            }

            currRow += 2;
            sheet.createRow(currRow++).createCell(0).setCellValue("III. TOP SẢN PHẨM BÁN CHẠY TRONG NGÀY");
            Row headSp = sheet.createRow(currRow++);
            headSp.createCell(0).setCellValue("Tên Sản Phẩm");
            headSp.createCell(1).setCellValue("Đã Bán");
            headSp.createCell(2).setCellValue("Tồn Kho");
            headSp.getCell(0).setCellStyle(headerStyle);
            headSp.getCell(1).setCellStyle(headerStyle);
            headSp.getCell(2).setCellStyle(headerStyle);

            if (dataTable.getTopBanChay() != null) {
                for (ThongKeChiTietResponse.TopBanChay sp : dataTable.getTopBanChay()) {
                    Row r = sheet.createRow(currRow++);
                    r.createCell(0).setCellValue(sp.getTenSanPham());
                    r.createCell(1).setCellValue(sp.getDaBan());
                    r.createCell(2).setCellValue(sp.getTonKho());
                }
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] excelBytes = bos.toByteArray();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailChinhNhanBaoCao);
            helper.setSubject("📊 [AERION SPORTS] Báo cáo Excel kết quả kinh doanh ngày " + chuoiNgay);

            double doanhThuHienTai = dataCard.getDoanhThu() != null ? dataCard.getDoanhThu().doubleValue() : 0.0;

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; max-width: 500px;'>"
                    + "  <h3 style='color: #f79b66;'>Kính gửi Quản trị viên,</h3>"
                    + "  <p>Hệ thống tự động xin gửi báo cáo kết quả doanh thu tổng hợp tính đến thời điểm hiện tại ngày <b>" + chuoiNgay + "</b>:</p>"
                    + "  <table style='width:100%; border-collapse: collapse; font-size:13px; margin: 15px 0;'>"
                    + "    <tr style='background:#f8fafc;'><td style='padding:8px; border:1px solid #e2e8f0;'>💰 <b>Doanh thu:</b></td><td style='padding:8px; border:1px solid #e2e8f0; font-weight:bold; color:#2563eb;'>" + String.format("%,.0f", doanhThuHienTai) + " đ</td></tr>"
                    + "    <tr><td style='padding:8px; border:1px solid #e2e8f0;'>📦 <b>Tổng số đơn:</b></td><td style='padding:8px; border:1px solid #e2e8f0; font-weight:bold;'>" + dataCard.getTongDonHang() + " đơn</td></tr>"
                    + "    <tr style='background:#f8fafc;'><td style='padding:8px; border:1px solid #e2e8f0;'>✅ <b>Đơn hoàn thành:</b></td><td style='padding:8px; border:1px solid #e2e8f0; font-weight:bold; color:#137333;'>" + dataCard.getDonHoanThanh() + " đơn</td></tr>"
                    + "  </table>"
                    + "  <p><i>*Chi tiết bảng biểu Top mặt hàng bán chạy, danh sách khách hàng tiềm năng chi tiêu lớn và cảnh báo tồn kho đã được đóng gói đính kèm trong file Excel dưới đây.</i></p>"
                    + "  <hr style='border:none; border-top:1px solid #f1f5f9; margin:15px 0;'/>"
                    + "  <p style='font-size:11px; color:#94a3b8; text-align:center;'>Hệ thống báo cáo tự động Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            helper.addAttachment("BaoCao_DoanhThu_Ngay_" + homNay.toString() + ".xlsx", new ByteArrayResource(excelBytes));

            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Đã gửi đính kèm file Excel báo cáo thành công về hòm thư chính!");

        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Thất bại khi xuất Excel gửi mail báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi email cập nhật trạng thái đơn hàng cho khách hàng (bản nâng cấp: có logo đính kèm dạng inline)
     */
    @Async
    public void sendOrderStatusEmail(
            String toEmail,
            String tenKhachHang,
            String maHoaDon,
            String trangThaiMoi,
            String diaChiNhan,
            String sdtNhan,
            BigDecimal tongTienHang,
            BigDecimal tienGiam,
            BigDecimal phiVanChuyen,
            BigDecimal tongThanhToan,
            List<ChiTietEmailDTO> sanPhamList,
            String phuongThucThanhToan
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_RELATED, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("📦 Aerion Sports - Cập nhật trạng thái đơn hàng #" + maHoaDon);

            // Build HTML cho danh sách sản phẩm trong email
            StringBuilder spHtml = new StringBuilder();
            if (sanPhamList != null) {
                for (ChiTietEmailDTO sp : sanPhamList) {
                    spHtml.append("<tr>")
                            .append("<td style='padding: 16px 0; border-bottom: 1px solid #f3f4f6;'>")
                            .append("<div style='font-size: 14px; font-weight: 600; color: #111827; margin-bottom: 4px;'>").append(sp.getTenSanPham()).append("</div>")
                            .append("<div style='font-size: 13px; color: #6b7280;'>").append(sp.getMauSac()).append(" / ").append(sp.getTrongLuong()).append("</div>")
                            .append("<div style='font-size: 13px; color: #6b7280; margin-top: 4px;'>")
                            .append(String.format("%,.0f", sp.getDonGia())).append(" VND x ").append(sp.getSoLuong())
                            .append("</div>")
                            .append("</td>")
                            .append("<td style='padding: 16px 0; border-bottom: 1px solid #f3f4f6; text-align: right; font-weight: 600; color: #111827; font-size: 14px; vertical-align: middle;'>")
                            .append(String.format("%,.0f", sp.getThanhTien())).append(" VND")
                            .append("</td>")
                            .append("</tr>");
                }
            }

            String htmlContent = "<div style='font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'>"
                    // Header (nền trắng để hiển thị logo JPG)
                    + "<div style='background-color: #ffffff; padding: 32px 24px; text-align: center; border-top: 6px solid #f97316; border-bottom: 1px solid #e5e7eb;'>"
                    + "  <img src='cid:logoImage' alt='Aerion Sports' width='180' style='height: 48px; margin-bottom: 8px;' />"
                    + "  <p style='color: #6b7280; margin: 0; font-size: 13px; letter-spacing: 1px; text-transform: uppercase;'>Premium Badminton Equipment</p>"
                    + "</div>"

                    // Trạng thái đơn hàng
                    + "<div style='padding: 24px; text-align: center; background-color: #fffaf0; border-bottom: 1px solid #fed7aa;'>"
                    + "  <p style='color: #9a3412; margin: 0 0 8px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;'>Trạng thái đơn hàng</p>"
                    + "  <h2 style='color: #ea580c; margin: 0; font-size: 24px; font-weight: 800;'>" + trangThaiMoi + "</h2>"
                    + "</div>"

                    + "<div style='padding: 32px 24px;'>"

                    // Thông tin người nhận + Địa chỉ giao hàng
                    + "<table style='width: 100%; margin-bottom: 32px;' cellpadding='0' cellspacing='0'><tr>"
                    + "<td style='width: 50%; vertical-align: top; padding-right: 16px;'>"
                    + "  <div style='font-size: 12px; text-transform: uppercase; color: #6b7280; font-weight: 700; letter-spacing: 1px; margin-bottom: 8px;'>Thông tin người nhận</div>"
                    + "  <div style='font-size: 14px; color: #111827; font-weight: 600; margin-bottom: 4px;'>" + tenKhachHang + "</div>"
                    + "  <div style='font-size: 14px; color: #4b5563; margin-bottom: 4px;'>" + (sdtNhan != null ? sdtNhan : "") + "</div>"
                    + "  <div style='font-size: 14px; color: #f97316; text-decoration: none;'>" + toEmail + "</div>"
                    + "</td>"
                    + "<td style='width: 50%; vertical-align: top; padding-left: 16px;'>"
                    + "  <div style='font-size: 12px; text-transform: uppercase; color: #6b7280; font-weight: 700; letter-spacing: 1px; margin-bottom: 8px;'>Địa chỉ giao hàng</div>"
                    + "  <div style='font-size: 14px; color: #4b5563; line-height: 1.5;'>"
                    + (diaChiNhan != null && !diaChiNhan.isBlank() ? diaChiNhan : "Chưa có địa chỉ")
                    + "  </div>"
                    + "</td>"
                    + "</tr></table>"

                    // Phương thức thanh toán + vận chuyển
                    + "<div style='background-color: #f9fafb; border-radius: 8px; padding: 20px; margin-bottom: 32px; border: 1px solid #f3f4f6;'>"
                    + "  <table style='width: 100%;' cellpadding='0' cellspacing='0'><tr>"
                    + "  <td style='width: 50%; vertical-align: top;'>"
                    + "    <div style='font-size: 12px; color: #6b7280; font-weight: 600; margin-bottom: 4px;'>Phương thức thanh toán</div>"
                    + "    <div style='font-size: 14px; color: #111827; font-weight: 600;'>" + (phuongThucThanhToan != null ? phuongThucThanhToan : "Thanh toán khi nhận hàng (COD)") + "</div>"
                    + "  </td>"
                    + "  <td style='width: 50%; vertical-align: top;'>"
                    + "    <div style='font-size: 12px; color: #6b7280; font-weight: 600; margin-bottom: 4px;'>Đơn vị vận chuyển</div>"
                    + "    <div style='font-size: 14px; color: #111827; font-weight: 600;'>Giao hàng tiết kiệm</div>"
                    + "  </td>"
                    + "  </tr></table>"
                    + "</div>"

                    // Chi tiết đơn hàng
                    + "<div style='margin-bottom: 24px;'>"
                    + "  <table style='width: 100%; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px; margin-bottom: 8px;' cellpadding='0' cellspacing='0'>"
                    + "    <tr>"
                    + "      <td style='vertical-align: bottom;'>"
                    + "        <div style='font-size: 18px; font-weight: 800; color: #111827; margin-bottom: 4px;'>Chi tiết đơn hàng</div>"
                    + "        <div style='font-size: 13px; color: #6b7280;'>Mã ĐH: <span style='color: #f97316; font-weight: 700;'>#" + maHoaDon + "</span></div>"
                    + "      </td>"
                    + "      <td style='vertical-align: bottom; text-align: right; font-size: 13px; color: #6b7280;'>"
                    + "        " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + "      </td>"
                    + "    </tr>"
                    + "  </table>"
                    + "  <table style='width: 100%; border-collapse: collapse;'>"
                    + "    <tbody>" + spHtml + "</tbody>"
                    + "  </table>"
                    + "</div>"

                    // Tổng kết đơn hàng
                    + "<div style='width: 100%;'>"
                    + "  <table style='width: 100%; max-width: 320px; margin-left: auto; font-size: 14px;' cellpadding='0' cellspacing='0'>"
                    + "    <tr>"
                    + "      <td style='padding: 8px 0; color: #6b7280;'>Tạm tính</td>"
                    + "      <td style='padding: 8px 0; text-align: right; color: #111827; font-weight: 500;'>" + String.format("%,.0f", tongTienHang) + " đ</td>"
                    + "    </tr>"
                    + "    <tr>"
                    + "      <td style='padding: 8px 0; color: #6b7280;'>Giảm giá</td>"
                    + "      <td style='padding: 8px 0; text-align: right; color: #10b981; font-weight: 500;'>-" + String.format("%,.0f", tienGiam != null ? tienGiam : java.math.BigDecimal.ZERO) + " đ</td>"
                    + "    </tr>"
                    + "    <tr>"
                    + "      <td style='padding: 8px 0; color: #6b7280;'>Phí vận chuyển</td>"
                    + "      <td style='padding: 8px 0; text-align: right; color: #111827; font-weight: 500;'>" + String.format("%,.0f", phiVanChuyen != null ? phiVanChuyen : java.math.BigDecimal.ZERO) + " đ</td>"
                    + "    </tr>"
                    + "    <tr>"
                    + "      <td colspan='2' style='padding: 16px 0 0; border-top: 2px solid #e5e7eb;'>"
                    + "        <table style='width: 100%;' cellpadding='0' cellspacing='0'>"
                    + "          <tr>"
                    + "            <td style='font-size: 16px; font-weight: 700; color: #111827; vertical-align: middle;'>Tổng thanh toán</td>"
                    + "            <td style='font-size: 24px; font-weight: 800; color: #f97316; text-align: right; vertical-align: middle;'>" + String.format("%,.0f", tongThanhToan) + " đ</td>"
                    + "          </tr>"
                    + "        </table>"
                    + "      </td>"
                    + "    </tr>"
                    + "  </table>"
                    + "</div>"

                    + "</div>"

                    // Footer
                    + "<div style='background-color: #f9fafb; padding: 32px 24px; text-align: center; border-top: 1px solid #e5e7eb;'>"
                    + "  <p style='font-size: 13px; color: #6b7280; margin: 0 0 8px;'>Cảm ơn bạn đã đồng hành cùng <strong>Aerion Sports</strong>!</p>"
                    + "  <p style='font-size: 12px; color: #9ca3af; margin: 0;'>Email này được gửi tự động, vui lòng không trả lời.</p>"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);

            try {
                ClassPathResource logoRes = new ClassPathResource("static/images/Logo_Da.jpg");
                if (logoRes.exists()) {
                    helper.addInline("logoImage", logoRes);
                } else {
                    System.err.println(">>> Logo không tồn tại tại đường dẫn static/images/Logo_Da.jpg!");
                }
            } catch (Exception e) {
                System.err.println(">>> Không thể đính kèm logo: " + e.getMessage());
            }

            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Gửi mail đơn hàng #" + maHoaDon + " tới " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Lỗi gửi mail đơn hàng: " + e.getMessage());
        }
    }
}