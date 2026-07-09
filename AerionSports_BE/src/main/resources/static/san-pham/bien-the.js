function toggleFilterPanel() {
    const panel = document.getElementById('filterPanel');
    panel.style.display = (panel.style.display === 'none') ? '' : 'none';
}

// Bổ sung thuộc tính 'anh' vào đối tượng quản lý toàn cục
let currentQrInfo = { ma: '', mau: '', trongLuong: '', anh: '' };
let qrCodeInstance = null;

function moModalXemQr(id) {
    const holder = document.querySelector(`.qr-data-holder[data-id="${id}"]`);
    if (!holder) return;

    // 1. Thu thập toàn bộ thông tin bao gồm cả đường dẫn ảnh sản phẩm
    currentQrInfo = {
        ma: holder.getAttribute('data-ma') || '',
        mau: holder.getAttribute('data-mau') || 'Mặc định',
        trongLuong: holder.getAttribute('data-trong-luong') || 'Mặc định',
        anh: holder.getAttribute('data-anh') || 'https://placehold.co/120x120?text=No+Image'
    };

    // 2. Hiển thị thông tin chữ và ảnh lên giao diện Modal
    document.getElementById('qrMaCtsp').innerText = currentQrInfo.ma;
    document.getElementById('qrMauSac').innerText = currentQrInfo.mau;
    document.getElementById('qrTrongLuong').innerText = currentQrInfo.trongLuong;

    const imgProductModal = document.getElementById('qrAnhSanPham');
    if (imgProductModal) {
        imgProductModal.src = currentQrInfo.anh;
    }

    // 3. Khởi tạo và vẽ mã QR Code động
    const canvasHolder = document.getElementById('qrCanvasHolder');
    canvasHolder.innerHTML = '';
    qrCodeInstance = new QRCode(canvasHolder, {
        text: currentQrInfo.ma || 'CTSP',
        width: 165,
        height: 165,
        colorDark: '#1e293b',
        colorLight: '#ffffff'
    });

    document.getElementById('qrModal').style.display = 'flex';
}

function closeQrModal() {
    document.getElementById('qrModal').style.display = 'none';
}

// Hàm in tem nhãn cao cấp: Chứa cả ảnh sản phẩm, ảnh QR và thông tin chi tiết
function inMaQRCode() {
    const canvasHolder = document.getElementById('qrCanvasHolder');
    const img = canvasHolder.querySelector('img');
    const canvas = canvasHolder.querySelector('canvas');
    const urlAnhQr = img ? img.src : (canvas ? canvas.toDataURL('image/png') : '');

    if (!urlAnhQr) {
        alert("Không tìm thấy mã QR để thực hiện in!");
        return;
    }

    const cuaSoIn = window.open('', '_blank');
    cuaSoIn.document.write(`
        <html>
          <head>
            <title>In mã QR - ${currentQrInfo.ma}</title>
            <style>
              body { font-family: 'Arial', sans-serif; text-align: center; padding: 10px; margin: 0; }
              .stamp-box { border: 1px dashed #334155; padding: 12px; display: inline-block; border-radius: 6px; background: #fff; }
              .title-brand { font-size: 12px; font-weight: bold; color: #f97316; letter-spacing: 1px; margin-bottom: 8px; }
              .media-group { display: flex; align-items: center; justify-content: center; gap: 15px; margin-bottom: 8px; }
              .product-img { width: 75px; height: 75px; object-fit: cover; border-radius: 4px; border: 1px solid #e2e8f0; }
              .qr-img { width: 75px; height: 75px; }
              .sku-title { font-size: 13px; font-weight: bold; margin-top: 4px; color: #0f172a; font-family: monospace; }
              .spec-detail { font-size: 11px; color: #475569; margin-top: 2px; }
            </style>
          </head>
          <body>
            <div class="stamp-box">
                <div class="title-brand">AERION SPORTS</div>
                <div class="media-group">
                    <img class="product-img" src="${currentQrInfo.anh}"/>
                    <img class="qr-img" src="${urlAnhQr}"/>
                </div>
                <div class="sku-title">${currentQrInfo.ma}</div>
                <div class="spec-detail">Màu: ${currentQrInfo.mau} | TL: ${currentQrInfo.trongLuong}</div>
            </div>
            
            <script>
                // Đợi toàn bộ ảnh (Ảnh sản phẩm + Ảnh QR) tải xong hoàn toàn rồi mới kích hoạt hộp thoại in
                window.onload = function() {
                    setTimeout(function() {
                        window.print();
                        window.close();
                    }, 250); 
                };
            <\/script>
          </body>
        </html>
    `);
    cuaSoIn.document.close();
}

// Keep nguyên bản logic của toggleTrangThai và exportToExcel của bạn...
async function toggleTrangThai(id, trangThaiHienTai, event) {
    if (event) event.stopPropagation();
    const trangThaiMoi = trangThaiHienTai === 1 ? 0 : 1;
    if (!confirm('Bạn có chắc chắn muốn thay đổi trạng thái hoạt động của biến thể này?')) return;

    try {
        const res = await fetch(`/api/chi-tiet-san-pham/${id}/trang-thai?trangThai=${trangThaiMoi}`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error('Request failed');
        window.location.reload();
    } catch (e) {
        alert('Hệ thống mất kết nối, không thể cập nhật trạng thái biến thể!');
    }
}

function exportToExcel() {
    const table = document.getElementById('tblChiTietBienThe');
    const rows = Array.from(table.querySelectorAll('tbody tr')).filter(r => !r.classList.contains('empty-row'));

    if (rows.length === 0) {
        alert('Không có dữ liệu biến thể để xuất Excel!');
        return;
    }
    if (!confirm('Bạn có chắc chắn muốn xuất toàn bộ dữ liệu biến thể của sản phẩm này ra file Excel không?')) return;

    const data = [];
    data.push(['STT', 'Mã SKU', 'Màu sắc', 'Trọng lượng', 'Danh mục', 'Chu vi cán', 'Độ cứng', 'Điểm cân bằng',
        'Chất liệu thân', 'Chất liệu khung', 'Tồn kho', '% Giảm', 'Giá bán lẻ', 'Trạng thái']);

    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length < 15) return;
        data.push([
            cells[0].innerText.trim(),
            cells[2].innerText.trim(),
            cells[3].innerText.trim(),
            cells[4].innerText.trim(),
            cells[5].innerText.trim(),
            cells[6].innerText.trim(),
            cells[7].innerText.trim(),
            cells[8].innerText.trim(),
            cells[9].innerText.trim(),
            cells[10].innerText.trim(),
            cells[11].innerText.trim(),
            cells[12].innerText.trim(),
            cells[13].innerText.trim(),
            cells[14].querySelector('.badge-status-text')?.innerText.trim() || cells[14].innerText.trim()
        ]);
    });

    const worksheet = XLSX.utils.aoa_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Danh sách biến thể');
    XLSX.writeFile(workbook, 'BienThe.xlsx');
}