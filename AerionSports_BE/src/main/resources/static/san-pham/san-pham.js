function toggleFilterPanel() {
    const panel = document.getElementById('filterPanel');
    if (!panel) return;
    panel.style.display = (panel.style.display === 'none') ? 'block' : 'none';
}

// Định dạng dấu chấm tiền tệ vi-VN động trực tiếp khi kéo thanh slider
function onGiaMaxChange(value) {
    const label = document.getElementById('giaMaxLabel');
    if (label) {
        label.textContent = new Intl.NumberFormat('vi-VN').format(value);
    }
}

// Thay đổi nhanh trạng thái kinh doanh của sản phẩm cha
async function toggleTrangThai(id, trangThaiHienTai, event) {
    if (event) event.stopPropagation();

    const trangThaiMoi = trangThaiHienTai === 1 ? 0 : 1;
    const xacNhan = trangThaiMoi === 1
        ? 'Bạn muốn khôi phục kinh doanh sản phẩm này và toàn bộ biến thể liên quan?'
        : 'Bạn muốn tạm ngừng kinh doanh sản phẩm này? Thao tác này sẽ dừng toàn bộ biến thể con!';

    if (!confirm(xacNhan)) return;

    try {
        const res = await fetch(`/san-pham/api/san-pham/${id}/trang-thai?trangThai=${trangThaiMoi}`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error('Cập nhật trạng thái thất bại!');
        alert('Thay đổi trạng thái sản phẩm và các biến thể thành công!');
        window.location.reload();
    } catch (e) {
        alert('Hệ thống mất kết nối, không thể thay đổi trạng thái kinh doanh!');
    }
}

// Xuất Excel các dòng dữ liệu đang hiển thị trên bảng
function exportToExcel() {
    const table = document.getElementById('tblSanPham');
    if (!table) return;

    const visibleRows = Array.from(table.querySelectorAll('tbody tr')).filter(r => r.style.display !== 'none' && !r.classList.contains('empty-table-row'));

    if (visibleRows.length === 0) {
        alert('Không có dữ liệu hợp lệ để xuất Excel!');
        return;
    }
    if (!confirm('Bạn có chắc chắn muốn xuất danh sách sản phẩm hiện tại ra file Excel không?')) return;

    const data = [];
    data.push(['STT', 'Mã sản phẩm', 'Tên sản phẩm', 'Thương hiệu', 'Xuất xứ', 'Khoảng giá', 'Tổng tồn', 'Trạng thái']);

    visibleRows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length < 8) return;
        data.push([
            cells[0].innerText.trim(),
            cells[1].innerText.trim(),
            cells[2].innerText.trim(),
            cells[3].innerText.trim(),
            cells[4].innerText.trim(),
            cells[5].innerText.trim(),
            cells[6].innerText.trim(),
            cells[7].innerText.trim()
        ]);
    });

    const worksheet = XLSX.utils.aoa_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sản phẩm');
    XLSX.writeFile(workbook, 'Danh_sach_san_pham.xlsx');
}