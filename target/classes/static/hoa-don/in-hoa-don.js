/* =========================================================================
   in-hoa-don.js
   Chuyển đổi 1:1 từ printInvoice.js (Vue) sang JS thuần.
   File gốc không có logic nào phụ thuộc Vue — chỉ đổi 2 chỗ:
   - import QRCode from 'qrcode'   ->  dùng thư viện qrcode qua CDN (window.QRCode)
   - export const printInvoice     ->  gán vào window.printInvoice để gọi trực tiếp
     từ onclick trong Thymeleaf (giữ nguyên toàn bộ phần HTML/CSS in hóa đơn bên trong).
   ========================================================================= */

window.printInvoice = async function (hoaDon, danhSachSanPham, lichSuThanhToan) {

    const printWindow = window.open('', '_blank');

    try {
        const formatCurrency = (value) => {
            if (!value) return '0 đ';
            return Number(value).toLocaleString('vi-VN') + ' đ';
        };

        const qrData = `
Mã hóa đơn: ${hoaDon.maHoaDon}
Khách hàng: ${hoaDon.tenNguoiNhan}
Tổng tiền: ${hoaDon.tongTienThanhToan}
`;

        // ✅ Dùng thư viện qrcode-generator tự host (không qua CDN nữa) — hàm đồng bộ,
        // không cần await, và không còn phụ thuộc mạng ngoài nên không thể lỗi vì mất mạng/CDN sập.
        let qrImage = '';
        try {
            if (typeof qrcode === 'undefined') {
                throw new Error('Thư viện qrcode-generator.js chưa được nạp — kiểm tra lại thẻ <script th:src="@{/hoa-don/qrcode-generator.js}"> trong detail.html.');
            }
            const qr = qrcode(0, 'M'); // 0 = tự động chọn version phù hợp độ dài dữ liệu, 'M' = mức sửa lỗi trung bình
            qr.addData(qrData);
            qr.make();
            qrImage = qr.createDataURL(4, 0); // cellSize=4, margin=0 -> trả thẳng base64 PNG data URL
        } catch (err) {
            console.error('[in-hoa-don] Lỗi tạo mã QR, vẫn tiếp tục in hóa đơn không có QR:', err);
        }

        const productRows = danhSachSanPham
            .map((item, index) => `
    <tr>
      <td>${index + 1}</td>
      <td>${item.tenSanPham}</td>
      <td>${item.trongLuong || ''}</td>
      <td>${item.mauSac || ''}</td>
      <td>${item.soLuong}</td>
      <td>${formatCurrency(item.donGia)}</td>
      <td>${formatCurrency(item.thanhTien)}</td>
    </tr>
  `)
            .join('');

        const paymentRows = lichSuThanhToan.map(
            (item, index) => `
          <tr>
            <td>${index + 1}</td>
            <td>${formatCurrency(item.soTien)}</td>
            <td>${new Date(item.ngayThanhToan)
                .toLocaleString('vi-VN')}</td>
            <td>${item.phuongThucThanhToan || ''}</td>
            <td>${item.ghiChu || ''}</td>
          </tr>
        `
        ).join('');

        printWindow.document.write(`
      <html>
        <head>
          <title>${hoaDon.maHoaDon}</title>

<style>
@page{
   size:A4;
   margin:8mm; /* giảm lề giấy */
}

body{
    font-family:Arial,sans-serif;
    padding:10px;
    margin:0;
}

*{
   -webkit-print-color-adjust:exact !important;
   print-color-adjust:exact !important;
   box-sizing:border-box;
}

/* Header */
.header{
    text-align:center;
    background:#f79b66 !important;
    color:white !important;
    padding:18px;
    border-radius:10px;
    margin-bottom:15px;
}

.title{
    font-size:26px;
    font-weight:bold;
    color:white;
}

.sub{
    font-size:14px;
    margin-top:5px;
    color:white;
}

table{
    width:100%;
    border-collapse:collapse;
    margin-top:12px;
}

th,td{
    border:1px solid #ddd;
    padding:8px;
    text-align:center;
}

thead th{
    background:#f79b66 !important;
    color:white;
}

tbody tr:nth-child(even){
    background:#fff7f2;
}

.total{
    margin-top:15px;
}

.total h2{
    color:#f79b66;
}

hr{
    border:none;
    height:2px;
    background:#f79b66;
    margin:12px 0;
}

/* Không tách bảng */
table{
    page-break-inside:avoid;
}

tr{
    page-break-inside:avoid;
}

/* QR */
.qr-section{
   text-align:center;
   margin-top:15px;
}

/* Chỉ giảm QR nhẹ thôi */
.qr-section img{
   width:120px;
   height:120px;
}

.footer{
   text-align:center;
   margin-top:20px;
}
</style>
        </head>

        <body>

          <div class="header">
            <div class="title">
              AERION SPORTS
            </div>

            <div class="sub">
              HỆ THỐNG CẦU LÔNG CHÍNH HÃNG
            </div>
          </div>

          <hr>

          <p>
            <b>Mã hóa đơn:</b>
            ${hoaDon.maHoaDon}
          </p>

          <p>
            <b>Ngày tạo:</b>
            ${new Date(hoaDon.ngayTao)
            .toLocaleString('vi-VN')}
          </p>

          <p>
            <b>Khách hàng:</b>
            ${hoaDon.tenNguoiNhan}
          </p>

          <p>
            <b>SĐT:</b>
            ${hoaDon.sdtNguoiNhan}
          </p>

          <table>
            <thead>
             <tr>
  <th>STT</th>
  <th>Sản phẩm</th>
  <th>Trọng lượng</th>
  <th>Màu sắc</th>
  <th>SL</th>
  <th>Đơn giá</th>
  <th>Thành tiền</th>
</tr>
            </thead>

            <tbody>
              ${productRows}
            </tbody>
          </table>

          <div class="total">

            <p>
              Tổng tiền hàng:
              <b>
                ${formatCurrency(
            hoaDon.tongTienHang
        )}
              </b>
            </p>

            <p>
              Giảm giá:
              <b>
                ${formatCurrency(
            hoaDon.tienGiam
        )}
              </b>
            </p>

            <p>
              Phí vận chuyển:
              <b>
                ${formatCurrency(
            hoaDon.tienVanChuyen
        )}
              </b>
            </p>

            <h2>
              Tổng thanh toán:
              ${formatCurrency(
            hoaDon.tongTienThanhToan
        )}
            </h2>

          </div>
          <h3 style="margin-top:25px">
  Lịch sử thanh toán
</h3>

<table>
  <thead>
    <tr>
      <th>STT</th>
      <th>Số tiền</th>
      <th>Thời gian</th>
      <th>Phương thức</th>
      <th>Ghi chú</th>
    </tr>
  </thead>

  <tbody>
    ${paymentRows}
  </tbody>
</table>

          <hr>

${qrImage ? `
<div class="qr-section">
    <h3>Mã QR hóa đơn</h3>

    <img src="${qrImage}" />

    <p>Quét để xem thông tin hóa đơn</p>
</div>
` : ''}

<hr>

<div class="footer">
    <h3>Cảm ơn quý khách!</h3>
    <p>Hẹn gặp lại tại AERION SPORTS</p>
</div>

        </body>
      </html>
    `);

        printWindow.document.close();

        printWindow.onload = () => {
            setTimeout(() => {
                printWindow.print();
            }, 300);
        };

    } catch (err) {
        // ✅ Nếu có lỗi bất kỳ (không phải riêng lỗi QR), hiện rõ lỗi lên tab mới
        // thay vì để trắng trang không biết nguyên nhân.
        console.error('[in-hoa-don] Lỗi khi in hóa đơn:', err);
        if (printWindow) {
            printWindow.document.write(`
                <html>
                  <body style="font-family:Arial,sans-serif;padding:24px;color:#cf1322;">
                    <h2>Không thể in hóa đơn</h2>
                    <p>${(err && err.message) || err}</p>
                    <p style="color:#888;font-size:13px;">Mở Console (F12) trên tab này để xem chi tiết lỗi.</p>
                  </body>
                </html>
            `);
            printWindow.document.close();
        }
    }
};