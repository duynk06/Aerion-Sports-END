package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.HoaDon;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VNPayService {

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.tmn-code:}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.return-url:}")
    private String configuredReturnUrl;

    @Value("${vnpay.bank-code:NCB}")
    private String bankCode;

    public String taoUrlThanhToan(HoaDon hoaDon, HttpServletRequest request) {
        if (!StringUtils.hasText(tmnCode) || !StringUtils.hasText(hashSecret)) {
            throw new RuntimeException("Chưa cấu hình VNPay: vui lòng điền vnpay.tmn-code và vnpay.hash-secret trong application.yml.");
        }
        if (hoaDon == null || hoaDon.getMaHoaDon() == null) {
            throw new RuntimeException("Không tìm thấy đơn hàng để thanh toán VNPay.");
        }

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Date now = Date.from(java.time.LocalDateTime.now().atZone(zoneId).toInstant());
        Date expire = Date.from(java.time.LocalDateTime.now().plusMinutes(15).atZone(zoneId).toInstant());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        long amount = hoaDon.getTongTienThanhToan() == null
                ? 0L
                : hoaDon.getTongTienThanhToan().max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        if (StringUtils.hasText(bankCode)) {
            params.put("vnp_BankCode", bankCode.trim());
        }
        params.put("vnp_TxnRef", hoaDon.getMaHoaDon());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + hoaDon.getMaHoaDon());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", StringUtils.hasText(configuredReturnUrl) ? configuredReturnUrl : taoReturnUrl(request));
        params.put("vnp_IpAddr", layIp(request));
        params.put("vnp_CreateDate", formatter.format(now));
        params.put("vnp_ExpireDate", formatter.format(expire));

        String hashData = taoQuery(params, true);
        String query = taoQuery(params, true) + "&vnp_SecureHash=" + hmacSha512(hashSecret, hashData);
        return payUrl + "?" + query;
    }

    public boolean kiemTraChuKy(Map<String, String> params) {
        if (!StringUtils.hasText(hashSecret) || params == null || !params.containsKey("vnp_SecureHash")) {
            return false;
        }
        String secureHash = params.get("vnp_SecureHash");
        Map<String, String> data = new TreeMap<>();
        params.forEach((key, value) -> {
            if (key != null && key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)
                    && value != null) {
                data.put(key, value);
            }
        });
        String expected = hmacSha512(hashSecret, taoQuery(data, true));
        return expected.equalsIgnoreCase(secureHash);
    }

    private String taoReturnUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String portPart = ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443)
                ? ""
                : ":" + port;
        return scheme + "://" + host + portPart + "/cua-hang/vnpay/return";
    }

    private String layIp(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        String rawIp = StringUtils.hasText(ip) ? ip.split(",")[0].trim() : request.getRemoteAddr();
        if (!StringUtils.hasText(rawIp)
                || "0:0:0:0:0:0:0:1".equals(rawIp)
                || "::1".equals(rawIp)
                || "localhost".equalsIgnoreCase(rawIp)) {
            return "127.0.0.1";
        }
        return rawIp;
    }

    private String taoQuery(Map<String, String> params, boolean encode) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encode(key, encode)).append('=').append(encode(value, encode));
        });
        return builder.toString();
    }

    private String encode(String value, boolean encode) {
        return encode ? URLEncoder.encode(value, StandardCharsets.US_ASCII) : value;
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                hash.append(String.format("%02x", item & 0xff));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Không tạo được chữ ký VNPay.", ex);
        }
    }
}