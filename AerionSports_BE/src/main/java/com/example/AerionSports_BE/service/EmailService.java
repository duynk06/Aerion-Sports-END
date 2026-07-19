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



    // Tá»± Ä‘á»™ng bá»‘c email gá»­i cá»§a báº¡n (vietphan0925@gmail.com) lÃ m email nháº­n bÃ¡o cÃ¡o chÃ­nh luÃ´n
    @Value("${spring.mail.username}")
    private String emailChinhNhanBaoCao;

    /**
     * TÃ­nh nÄƒng 1: Gá»­i email cáº¥p tÃ i khoáº£n vÃ  máº­t kháº©u táº¡m thá»i cho NhÃ¢n viÃªn má»›i
     * @param toEmail Email cá»§a nhÃ¢n viÃªn nháº­n tÃ i khoáº£n
     * @param tenNhanVien TÃªn hiá»ƒn thá»‹ cá»§a nhÃ¢n viÃªn
     * @param matKhauTamThoi Chuá»—i máº­t kháº©u ngáº«u nhiÃªn 8 kÃ½ tá»± sinh tá»« Backend
     */
    @Async // Cháº¡y ngáº§m Ä‘a luá»“ng Ä‘á»ƒ khÃ´ng gÃ¢y ngháº½n/Ä‘Æ¡ giao diá»‡n Front-end
    public void sendAccountCreationEmail(String toEmail, String tenNhanVien, String matKhauTamThoi) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("ðŸŽ‰ Aerion Sports - ThÃ´ng bÃ¡o cáº¥p tÃ i khoáº£n nhÃ¢n viÃªn há»‡ thá»‘ng");

            // Thiáº¿t káº¿ giao diá»‡n HTML pháº³ng vá»›i tÃ´ng mÃ u cam chá»§ Ä‘áº¡o cá»§a thÆ°Æ¡ng hiá»‡u
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; border: 1px solid #f79b66; border-radius: 12px; max-width: 550px; margin: 0 auto;'>"
                    + "<div style='text-align: center; margin-bottom: 20px;'>"
                    + "  <h2 style='color: #f79b66; margin: 0; font-size: 22px; letter-spacing: 1px;'>CHÃ€O Má»ªNG THÃ€NH VIÃŠN Má»šI</h2>"
                    + "  <p style='color: #475569; font-size: 14px;'>TÃ i khoáº£n quáº£n trá»‹ ná»™i bá»™ há»‡ thá»‘ng Aerion Sports cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c khá»Ÿi táº¡o!</p>"
                    + "</div>"
                    + "<p>Xin chÃ o <strong>" + "</strong>,</p>"
                    + "<p>DÆ°á»›i Ä‘Ã¢y lÃ  thÃ´ng tin Ä‘Äƒng nháº­p cÃ¡ nhÃ¢n cá»§a báº¡n trÃªn há»‡ thá»‘ng, vui lÃ²ng báº£o máº­t thÃ´ng tin nÃ y:</p>"
                    + "<div style='background-color: #f8fafc; padding: 18px; border-radius: 8px; border: 1px solid #e2e8f0; margin: 15px 0; line-height: 1.6;'>"
                    + "  <p style='margin: 5px 0; font-size:  tenNhanVien +14px;'>ðŸŒ <strong>Trang quáº£n trá»‹:</strong> <a href='http://localhost:5173/login' style='color: #ea712b; text-decoration: none; font-weight: bold;'>Click Ä‘á»ƒ Ä‘áº¿n trang ÄÄƒng nháº­p</a></p>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>ðŸ“§ <strong>TÃ i khoáº£n (Username):</strong> <span style='font-weight: 600; color: #1e293b;'>" + toEmail + "</span></p>"
                    + "  <p style='margin: 5px 0; font-size: 14px;'>ðŸ”‘ <strong>Máº­t kháº©u táº¡m thá»i:</strong> <span style='font-weight: 700; color: #dc2626; font-family: monospace; background: #fee2e2; padding: 2px 6px; border-radius: 4px;'>" + matKhauTamThoi + "</span></p>"
                    + "</div>"
                    + "<p style='color: #ef4444; font-size: 12.5px; font-style: italic; font-weight: 500;'>âš ï¸ *LÆ°u Ã½ quan trá»ng: VÃ¬ lÃ½ do báº£o máº­t dá»¯ liá»‡u cá»­a hÃ ng, báº¡n báº¯t buá»™c pháº£i thá»±c hiá»‡n thay Ä‘á»•i máº­t kháº©u má»›i ngay trong láº§n Ä‘áº§u tiÃªn Ä‘Äƒng nháº­p há»‡ thá»‘ng thÃ nh cÃ´ng.</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='font-size: 11px; color: #94a3b8; text-align: center; margin: 0;'>Há»‡ thá»‘ng váº­n hÃ nh tá»± Ä‘á»™ng Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true); // ÄÃ¡nh dáº¥u true Ä‘á»ƒ hiá»ƒn thá»‹ Ä‘á»‹nh dáº¡ng HTML thay vÃ¬ text thuáº§n
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] ÄÃ£ gá»­i thÃ´ng tin tÃ i khoáº£n thÃ nh cÃ´ng tá»›i email: " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Tháº¥t báº¡i khi gá»­i tÃ i khoáº£n vá» mail " + toEmail + ". LÃ½ do: " + e.getMessage());
        }
    }

    /**
     * TÃ­nh nÄƒng 2: Gá»­i email chá»©a mÃ£ giáº£m giÃ¡ (Voucher) cÃ¡ nhÃ¢n dÃ nh riÃªng cho KhÃ¡ch hÃ ng Ä‘Æ°á»£c tri Ã¢n
     * @param toEmail Email cá»§a khÃ¡ch hÃ ng nháº­n voucher
     * @param tenKhachHang TÃªn hiá»ƒn thá»‹ cá»§a khÃ¡ch hÃ ng
     * @param maVoucher MÃ£ phiáº¿u giáº£m giÃ¡ (VÃ­ dá»¥: APRILELEMENT, TRIAN2026...)
     * @param giaTriGiam GiÃ¡ trá»‹ pháº§n trÄƒm giáº£m giÃ¡ (VÃ­ dá»¥: 20%)
     * @param ngayKetThuc Háº¡n sá»­ dá»¥ng cá»§a voucher
     */
    @Async
    public void sendVoucherEmail(String toEmail, String tenKhachHang, String maVoucher, String giaTriGiam, String ngayKetThuc) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("ðŸŽ QuÃ  táº·ng Ä‘áº·c biá»‡t tá»« Aerion Sports dÃ nh riÃªng cho báº¡n!");

            // Thiáº¿t káº¿ giao diá»‡n HTML phiáº¿u quÃ  táº·ng báº¯t máº¯t
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 25px; border: 1px solid #f79b66; border-radius: 12px; max-width: 500px; margin: 0 auto;'>"
                    + "<div style='text-align: center; margin-bottom: 15px;'>"
                    + "  <h2 style='color: #ea712b; margin: 0; font-size: 20px;'>MÃ“N QUÃ€ TRI Ã‚N Äáº¶C BIá»†T</h2>"
                    + "</div>"
                    + "<p>ThÃ¢n gá»­i QuÃ½ khÃ¡ch hÃ ng <strong>" + tenKhachHang + "</strong>,</p>"
                    + "<p>Aerion Sports xin gá»­i táº·ng riÃªng báº¡n mÃ£ giáº£m giÃ¡ cÃ¡ nhÃ¢n siÃªu Æ°u Ä‘Ã£i Ã¡p dá»¥ng cho toÃ n bá»™ cÃ¡c sáº£n pháº©m vá»£t vÃ  phá»¥ kiá»‡n cáº§u lÃ´ng táº¡i há»‡ thá»‘ng:</p>"
                    + "<div style='background: #fff7ed; padding: 20px; text-align: center; border: 2px dashed #f79b66; border-radius: 8px; margin: 20px 0;'>"
                    + "  <span style='font-size: 13px; color: #7c2d12; display: block; margin-bottom: 5px; font-weight: bold;'>MÃƒ PHIáº¾U GIáº¢M GIÃ Cá»¦A Báº N:</span>"
                    + "  <span style='font-size: 24px; font-weight: 800; color: #ea712b; font-family: sans-serif; letter-spacing: 1px;'>" + maVoucher + "</span>"
                    + "  <span style='display: block; margin-top: 8px; font-size: 15px; color: #1e293b; font-weight: bold;'>Æ¯u Ä‘Ã£i giáº£m ngay: " + giaTriGiam + "</span>"
                    + "</div>"
                    + "<p style='font-size: 14px;'>â° Thá»i háº¡n Ã¡p dá»¥ng Æ°u Ä‘Ã£i kÃ©o dÃ i Ä‘áº¿n háº¿t ngÃ y: <strong style='color: #ea712b;'>" + ngayKetThuc + "</strong></p>"
                    + "<p style='margin-top: 15px;'>MÃ£ sá»‘ cÃ³ háº¡n vÃ  chá»‰ Ã¡p dá»¥ng duy nháº¥t cho tÃ i khoáº£n cÃ¡ nhÃ¢n cá»§a báº¡n. HÃ£y nhanh chÃ¢n ghÃ© qua cá»­a hÃ ng Aerion Sports Ä‘á»ƒ sÄƒn ngay nhá»¯ng siÃªu pháº©m vá»›i má»©c giÃ¡ tá»‘t nháº¥t nhÃ©!</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='font-size: 11px; color: #94a3b8; text-align: center; margin: 0;'>Cáº£m Æ¡n báº¡n Ä‘Ã£ Ä‘á»“ng hÃ nh cÃ¹ng Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] ÄÃ£ phÃ¡t hÃ nh vÃ  gá»­i voucher thÃ nh cÃ´ng tá»›i khÃ¡ch hÃ ng: " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Lá»—i khi phÃ¡t hÃ nh voucher tá»›i mail " + toEmail + ". Chi tiáº¿t: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Ho_Chi_Minh")
    public void tuDongGuiBaoCaoCuoiNgayScheduled() {
        System.out.println(">>> [CRON JOB] ÄÃ£ Ä‘áº¿n 17h00 chiá»u! Há»‡ thá»‘ng báº¯t Ä‘áº§u káº¿t xuáº¥t bÃ¡o cÃ¡o doanh thu tá»± Ä‘á»™ng...");
        this.executeExportExcelAndSendEmail();
    }

    /**
     * TÃC Vá»¤ 3.2: HÃ m cá»‘t lÃµi xá»­ lÃ½ káº¿t xuáº¥t báº£ng Excel vÃ  gá»­i Ä‘Ã­nh kÃ¨m qua Email
     */
    public void executeExportExcelAndSendEmail() {
        LocalDate homNay = LocalDate.now();
        String chuoiNgay = homNay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // ðŸŒŸ ÄAÌƒ Sá»¬A: Láº¥y má»‘c LocalDateTime tá»« Ä‘áº§u ngÃ y Ä‘áº¿n cuá»‘i ngÃ y hÃ´m nay Ä‘á»ƒ Ä‘á»“ng bá»™ tuyá»‡t Ä‘á»‘i vá»›i Database
        LocalDateTime batDauHomNay = homNay.atStartOfDay();
        LocalDateTime ketThucHomNay = homNay.atTime(LocalTime.MAX);

        // Thu tháº­p sá»‘ liá»‡u tá»« database
        ThongKeCardResponse dataCard = thongKeService.getSingleCardData("today");

        // Äá»“ng bá»™ dá»¯ liá»‡u báº£ng theo má»‘c ngÃ y cá»§a hÃ´m nay
        ThongKeChiTietResponse dataTable = thongKeService.getThongKeChiTietDuLieuDong(homNay, homNay);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thá»‘ng KÃª NgÃ y " + homNay.toString());

            // Thiáº¿t káº¿ style tiÃªu Ä‘á» cá»™t mÃ u cam Aerion Ä‘á»“ng bá»™
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // DÃ²ng tiÃªu Ä‘á» lá»›n trong Excel
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("BÃO CÃO DOANH THU & HIá»†U SUáº¤T KINH DOANH NGÃ€Y " + chuoiNgay);

            // Khá»‘i I: Chá»‰ sá»‘ tá»•ng quan hÃ ng trÃªn
            sheet.createRow(2).createCell(0).setCellValue("I. CHá»ˆ Sá» Tá»”NG QUAN");
            sheet.createRow(3).createCell(0).setCellValue("Tá»•ng Doanh Thu Há»£p Lá»‡:");
            sheet.createRow(3).createCell(1).setCellValue(dataCard.getDoanhThu() != null ? dataCard.getDoanhThu().doubleValue() : 0.0);
            sheet.createRow(4).createCell(0).setCellValue("Tá»•ng ÄÆ¡n HÃ ng PhÃ¡t Sinh:");
            sheet.createRow(4).createCell(1).setCellValue(dataCard.getTongDonHang() != null ? dataCard.getTongDonHang() : 0);
            sheet.createRow(5).createCell(0).setCellValue("Sá»‘ Sáº£n Pháº©m BÃ¡n ÄÆ°á»£c:");
            sheet.createRow(5).createCell(1).setCellValue(dataCard.getSoSanPhamDaBan() != null ? dataCard.getSoSanPhamDaBan() : 0);

            // Khá»‘i II: Tráº¡ng thÃ¡i & Tiáº¿n Ä‘á»™ Ä‘Æ¡n hÃ ng
            int currRow = 7;
            sheet.createRow(currRow++).createCell(0).setCellValue("II. TIáº¾N Äá»˜ ÄÆ N HÃ€NG TRONG NGÃ€Y");
            Row headStatus = sheet.createRow(currRow++);
            headStatus.createCell(0).setCellValue("Tráº¡ng ThÃ¡i ÄÆ¡n");
            headStatus.createCell(1).setCellValue("Sá»‘ LÆ°á»£ng");
            headStatus.getCell(0).setCellStyle(headerStyle);
            headStatus.getCell(1).setCellStyle(headerStyle);

            if (dataTable.getTrangThaiDonHang() != null) {
                for (ThongKeChiTietResponse.TrangThaiDonHang tt : dataTable.getTrangThaiDonHang()) {
                    Row r = sheet.createRow(currRow++);
                    r.createCell(0).setCellValue(tt.getTenTrangThai());
                    r.createCell(1).setCellValue(tt.getSoLuong());
                }
            }

            // Khá»‘i III: Top sáº£n pháº©m bÃ¡n cháº¡y hÃ´m nay
            currRow += 2;
            sheet.createRow(currRow++).createCell(0).setCellValue("III. TOP Sáº¢N PHáº¨M BÃN CHáº Y TRONG NGÃ€Y");
            Row headSp = sheet.createRow(currRow++);
            headSp.createCell(0).setCellValue("TÃªn Sáº£n Pháº©m");
            headSp.createCell(1).setCellValue("ÄÃ£ BÃ¡n");
            headSp.createCell(2).setCellValue("Tá»“n Kho");
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

            // Tá»± Ä‘á»™ng ná»›i rá»™ng Ã´ vá»«a khÃ­t chá»¯
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            // Äáº©y luá»“ng file Excel ra máº£ng Byte
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] excelBytes = bos.toByteArray();

            // TIáº¾N HÃ€NH Gá»¬I MAIL ÄÃNH KÃˆM FILE EXCEL
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailChinhNhanBaoCao);
            helper.setSubject("ðŸ“Š [AERION SPORTS] BÃ¡o cÃ¡o Excel káº¿t quáº£ kinh doanh ngÃ y " + chuoiNgay);

            double doanhThuHienTai = dataCard.getDoanhThu() != null ? dataCard.getDoanhThu().doubleValue() : 0.0;

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; max-width: 500px;'>"
                    + "  <h3 style='color: #f79b66;'>KÃ­nh gá»­i Quáº£n trá»‹ viÃªn,</h3>"
                    + "  <p>Há»‡ thá»‘ng tá»± Ä‘á»™ng xin gá»­i bÃ¡o cÃ¡o káº¿t quáº£ doanh thu tá»•ng há»£p tÃ­nh Ä‘áº¿n thá»i Ä‘iá»ƒm hiá»‡n táº¡i ngÃ y <b>" + chuoiNgay + "</b>:</p>"
                    + "  <table style='width:100%; border-collapse: collapse; font-size:13px; margin: 15px 0;'>"
                    + "    <tr style='background:#f8fafc;'><td style='padding:8px; border:1px solid #e2e8f0;'>ðŸ’° <b>Doanh thu:</b></td><td style='padding:8px; border:1px solid #e2e8f0; font-weight:bold; color:#2563eb;'>" + String.format("%,.0f", doanhThuHienTai) + " Ä‘</td></tr>"
                    + "    <tr><td style='padding:8px; border:1px solid #e2e8f0;'>ðŸ“¦ <b>Tá»•ng sá»‘ Ä‘Æ¡n:</b></td><td style='padding:8px; border:1px solid #e2e8f0; font-weight:bold;'>" + dataCard.getTongDonHang() + " Ä‘Æ¡n</td></tr>"
                    + "    <tr style='background:#f8fafc;'><td style='padding:8px; border:1px solid #e2e8f0;'>âœ… <b>ÄÆ¡n hoÃ n thÃ nh:</b></td><td style='padding:8px; border:1px solid #e2e8f0; font-weight:bold; color:#137333;'>" + dataCard.getDonHoanThanh() + " Ä‘Æ¡n</td></tr>"
                    + "  </table>"
                    + "  <p><i>*Chi tiáº¿t báº£ng biá»ƒu Top máº·t hÃ ng bÃ¡n cháº¡y, danh sÃ¡ch khÃ¡ch hÃ ng tiá»m nÄƒng chi tiÃªu lá»›n vÃ  cáº£nh bÃ¡o tá»“n kho Ä‘Ã£ Ä‘Æ°á»£c Ä‘Ã³ng gÃ³i Ä‘Ã­nh kÃ¨m trong file Excel dÆ°á»›i Ä‘Ã¢y.</i></p>"
                    + "  <hr style='border:none; border-top:1px solid #f1f5f9; margin:15px 0;'/>"
                    + "  <p style='font-size:11px; color:#94a3b8; text-align:center;'>Há»‡ thá»‘ng bÃ¡o cÃ¡o tá»± Ä‘á»™ng Aerion Sports &copy; 2026</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            helper.addAttachment("BaoCao_DoanhThu_Ngay_" + homNay.toString() + ".xlsx", new ByteArrayResource(excelBytes));

            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] ÄÃ£ gá»­i Ä‘Ã­nh kÃ¨m file Excel bÃ¡o cÃ¡o thÃ nh cÃ´ng vá» hÃ²m thÆ° chÃ­nh!");

        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Tháº¥t báº¡i khi xuáº¥t Excel gá»­i mail bÃ¡o cÃ¡o: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            helper.setSubject("Aerion Sports - Cap nhat trang thai don hang #" + maHoaDon);

            // Build HTML cho danh sach san pham trong email.
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
                    // Header (White background for JPG logo)
                    + "<div style='background-color: #ffffff; padding: 32px 24px; text-align: center; border-top: 6px solid #f97316; border-bottom: 1px solid #e5e7eb;'>"
                    + "  <img src='cid:logoImage' alt='Aerion Sports' style='height: 48px; margin-bottom: 8px;' />"
                    + "  <p style='color: #6b7280; margin: 0; font-size: 13px; letter-spacing: 1px; text-transform: uppercase;'>Premium Badminton Equipment</p>"
                    + "</div>"

                    // Status Alert (Softer orange)
                    + "<div style='padding: 24px; text-align: center; background-color: #fffaf0; border-bottom: 1px solid #fed7aa;'>"
                    + "  <p style='color: #9a3412; margin: 0 0 8px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;'>Trạng thái đơn hàng</p>"
                    + "  <h2 style='color: #ea580c; margin: 0; font-size: 24px; font-weight: 800;'>" + trangThaiMoi + "</h2>"
                    + "</div>"

                    + "<div style='padding: 32px 24px;'>"

                    // Customer & Delivery Info
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

                    // Payment & Shipping Methods
                    + "<div style='background-color: #f9fafb; border-radius: 8px; padding: 20px; margin-bottom: 32px; border: 1px solid #f3f4f6;'>"
                    + "  <table style='width: 100%;' cellpadding='0' cellspacing='0'><tr>"
                    + "  <td style='width: 50%; vertical-align: top;'>"
                    + "    <div style='font-size: 12px; color: #6b7280; font-weight: 600; margin-bottom: 4px;'>Phương thức thanh toán</div>"
                    + "    <div style='font-size: 14px; color: #111827; font-weight: 600;'>" + (phuongThucThanhToan != null ? phuongThucThanhToan : "Thanh toán khi nhận hàng (COD)") + "</div>"
                    + "  </td>"
                    + "  <td style='width: 50%; vertical-align: top;'>"
                    + "    <div style='font-size: 12px; color: #6b7280; font-weight: 600; margin-bottom: 4px;'>Vận chuyển</div>"
                    + "    <div style='font-size: 14px; color: #111827; font-weight: 600;'>Giao hàng tiêu chuẩn</div>"
                    + "  </td>"
                    + "  </tr></table>"
                    + "</div>"

                    // Order Items Header (using table instead of flex)
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

                    // Order Summary
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
                org.springframework.core.io.Resource logoRes = new org.springframework.core.io.ClassPathResource("static/images/Logo_Da.jpg");
                if (!logoRes.exists()) {
                    logoRes = new org.springframework.core.io.FileSystemResource(new java.io.File("src/main/resources/static/images/Logo_Da.jpg"));
                }
                if (logoRes.exists()) {
                    helper.addInline("logoImage", logoRes, "image/jpeg");
                } else {
                    System.err.println(">>> Logo không tồn tại ở cả ClassPath và FileSystem!");
                }
            } catch (Exception e) {
                System.err.println("Không thể đính kèm logo: " + e.getMessage());
            }

            mailSender.send(message);
            System.out.println(">>> [MAIL SUCCESS] Gá»­i mail Ä‘Æ¡n hÃ ng #" + maHoaDon + " tá»›i " + toEmail);
        } catch (Exception e) {
            System.err.println(">>> [MAIL ERROR] Lá»—i gá»­i mail Ä‘Æ¡n hÃ ng: " + e.getMessage());
        }
    }
}
