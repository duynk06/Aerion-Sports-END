// =========================================================================
// 1. CÁC HÀM XỬ LÝ GIAO DIỆN (UI)
// =========================================================================
function toggleFilterPanel() {
    const panel = document.getElementById('filterPanel');
    if(panel) panel.style.display = (panel.style.display === 'none') ? '' : 'none';
}

function closeFullEditModal() { document.getElementById('fullEditModal').style.display = 'none'; }
function closeQrModal() { document.getElementById('qrModal').style.display = 'none'; }

// =========================================================================
// 2. CÁC HÀM XỬ LÝ BẢNG & EXCEL
// =========================================================================
async function toggleTrangThai(id, trangThaiHienTai) {
    if (!confirm('Xác nhận thay đổi trạng thái hoạt động?')) return;
    try {
        const res = await fetch(`/api/chi-tiet-san-pham/${id}/trang-thai?trangThai=${trangThaiHienTai === 1 ? 0 : 1}`, { method: 'PUT' });
        if (res.ok) window.location.reload();
        else throw new Error();
    } catch (e) { alert('Lỗi kết nối máy chủ!'); }
}

function exportToExcel() {
    const table = document.getElementById('tblTatCaBienThe');
    if (!table) return;
    const wb = XLSX.utils.table_to_book(table, { sheet: "BienThe" });
    XLSX.writeFile(wb, 'Danh_Sach_Bien_The.xlsx');
}

// =========================================================================
// 3. QUẢN LÝ QR CODE (ĐÃ TỐI ƯU)
// =========================================================================
let currentQrInfo = { ma: '', mau: '', trongLuong: '', anh: '' };

function moModalXemQr(id) {
    const holder = document.querySelector(`.qr-data-holder[data-id="${id}"]`);
    if (!holder) return;

    currentQrInfo = {
        ma: holder.getAttribute('data-ma'),
        mau: holder.getAttribute('data-mau'),
        trongLuong: holder.getAttribute('data-trong-luong'),
        anh: holder.getAttribute('data-anh')
    };

    document.getElementById('qrMaCtsp').innerText = currentQrInfo.ma;
    document.getElementById('qrMauSac').innerText = currentQrInfo.mau;
    document.getElementById('qrTrongLuong').innerText = currentQrInfo.trongLuong;
    document.getElementById('qrAnhSanPham').src = currentQrInfo.anh;

    const canvasHolder = document.getElementById('qrCanvasHolder');
    canvasHolder.innerHTML = '';
    new QRCode(canvasHolder, { text: currentQrInfo.ma, width: 160, height: 160 });

    document.getElementById('qrModal').style.display = 'flex';
}

function inMaQRCode() {
    const img = document.querySelector('#qrCanvasHolder img');
    if (!img) return;
    const win = window.open('', '_blank');
    win.document.write(`<html><body onload="window.print(); window.close();" style="text-align:center;">
        <div style="border:1px dashed #000; display:inline-block; padding:15px;">
            <div style="font-weight:bold; margin-bottom:5px;">AERION SPORTS</div>
            <img src="${currentQrInfo.anh}" style="width:80px;height:80px;"/>
            <img src="${img.src}" style="width:80px;height:80px;"/>
            <div style="font-family:monospace; font-weight:bold;">${currentQrInfo.ma}</div>
            <div style="font-size:11px;">${currentQrInfo.mau} | ${currentQrInfo.trongLuong}</div>
        </div></body></html>`);
    win.document.close();
}

// =========================================================================
// 4. CẬP NHẬT BIẾN THỂ (Modal)
// =========================================================================
function openFullEditModal(id, sku, giaBan, soLuong, idMauSac, idTrongLuong) {
    document.getElementById('modalVariantId').value = id;
    document.getElementById('modalSkuLabel').innerText = sku;
    document.getElementById('modalGiaBan').value = Math.round(giaBan);
    document.getElementById('modalSoLuong').value = soLuong;
    document.getElementById('modalMauSac').value = idMauSac;
    document.getElementById('modalTrongLuong').value = idTrongLuong;
    document.getElementById('fullEditModal').style.display = 'flex';
}

async function submitFullEdit(event) {
    event.preventDefault();
    const id = document.getElementById('modalVariantId').value;
    const formData = new FormData();
    formData.append("idMauSac", document.getElementById('modalMauSac').value);
    formData.append("idTrongLuong", document.getElementById('modalTrongLuong').value);
    formData.append("giaBan", document.getElementById('modalGiaBan').value);
    formData.append("soLuong", document.getElementById('modalSoLuong').value);
    const file = document.getElementById('modalFileAnh').files[0];
    if (file) formData.append("fileAnh", file);

    try {
        const res = await fetch(`/api/chi-tiet-san-pham/${id}/cap-nhat-day-du`, {
            method: 'POST',
            body: formData
        });
        const data = await res.json();
        if (res.ok && data.success) {
            alert(data.message || 'Cập nhật thành công!');
            window.location.reload();
        } else {
            alert('Lỗi cập nhật: ' + (data.message || 'Không xác định'));
        }
    } catch (e) {
        alert('Lỗi kết nối máy chủ!');
    }
}