// =========================================================================
// 1. Hàm lọc nội bộ trực quan dựa trên các dòng tr đang hiển thị ở trang hiện tại
// =========================================================================
function locBangGiaoDien() {
    const keyword = document.getElementById("ttSearchInput").value.trim().toLowerCase();
    const status = document.getElementById("ttStatusFilter").value;
    const rows = document.querySelectorAll("#ttTableBody .data-row");

    rows.forEach(row => {
        const codeText = row.querySelector(".search-target-code").textContent.toLowerCase();
        const nameText = row.querySelector(".search-target-name").textContent.toLowerCase();
        const rowStatus = row.getAttribute("data-status") || row.dataset.status;

        const matchKeyword = !keyword || codeText.includes(keyword) || nameText.includes(keyword);
        const matchStatus = status === "" || String(rowStatus) === String(status);

        if (matchKeyword && matchStatus) {
            row.style.display = "";
        } else {
            row.style.display = "none";
        }
    });
}

// Xóa nhanh bộ lọc tìm kiếm
function xoaBoLocGiaoDien() {
    document.getElementById("ttSearchInput").value = "";
    document.getElementById("ttStatusFilter").value = "";
    locBangGiaoDien();
    // Tải lại trang để trả về tổng số lượng chuẩn từ server
    window.location.reload();
}

// =========================================================================
// 2. Hàm điều khiển đóng/mở BẢNG NHỎ thêm/sửa nhanh
// =========================================================================
function toggleQuickAddPanel() {
    const panel = document.getElementById("ttQuickAddPanel");
    const dangMo = panel.style.display === "block";

    if (dangMo) {
        dongPanelThemNhanh();
        return;
    }

    moPanelThemMoi();
}

// Mở bảng nhỏ ở chế độ THÊM MỚI
function moPanelThemMoi() {
    document.getElementById("itemId").value = "";
    document.getElementById("ttPanelTitle").innerHTML =
        '<i class="fa-solid fa-tag"></i> Thêm mới ' + (window.TT_CONFIG ? window.TT_CONFIG.title.toLowerCase() : "thuộc tính");
    document.getElementById("ttFormValue").value = "";
    clearLoiValidatePanel();

    const codeLabel = document.getElementById("ttFormCodeLabel");
    if (codeLabel) {
        codeLabel.innerText = "Hệ thống tự tăng";
        codeLabel.style.color = "#64748b";
    }

    const panel = document.getElementById("ttQuickAddPanel");
    panel.style.display = "block";
    document.getElementById("ttFormValue").focus();
}

// Mở bảng nhỏ ở chế độ CHỈNH SỬA thuộc tính đã có
function moPanelChinhSua(id, name, code) {
    document.getElementById("itemId").value = id;
    document.getElementById("ttPanelTitle").innerHTML =
        '<i class="fa-solid fa-pen-to-square"></i> Cập nhật ' + (window.TT_CONFIG ? window.TT_CONFIG.title.toLowerCase() : "thuộc tính");
    document.getElementById("ttFormValue").value = name; // Điền tên cũ vào input để sửa
    clearLoiValidatePanel();

    const codeLabel = document.getElementById("ttFormCodeLabel");
    if (codeLabel) {
        codeLabel.innerText = code;
        codeLabel.style.color = "#1e293b";
    }

    const panel = document.getElementById("ttQuickAddPanel");
    panel.style.display = "block";
    document.getElementById("ttFormValue").focus();

    panel.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

// Đóng bảng nhỏ thêm/sửa
function dongPanelThemNhanh() {
    document.getElementById("ttQuickAddPanel").style.display = "none";
    document.getElementById("ttFormValue").value = "";
    document.getElementById("itemId").value = "";

    const codeLabel = document.getElementById("ttFormCodeLabel");
    if (codeLabel) {
        codeLabel.innerText = "Hệ thống tự tăng";
        codeLabel.style.color = "#475569";
    }

    clearLoiValidatePanel();
}

function clearLoiValidatePanel() {
    const err = document.getElementById("ttFormError");
    if (err) err.textContent = "";
    const input = document.getElementById("ttFormValue");
    if (input) {
        input.style.borderColor = "";
        input.classList.remove("input-error");
    }
}

// =========================================================================
// 3. HÀM XUẤT EXCEL CHUYÊN NGHIỆP TRỰC TIẾP TỪ BẢNG HTML
// =========================================================================
function xuatExcelLocal() {
    const table = document.getElementById("ttMainTable");
    if (!table) {
        alert("Không tìm thấy dữ liệu bảng để xuất!");
        return;
    }

    const cloneTable = table.cloneNode(true);
    const rows = cloneTable.querySelectorAll("tr");
    rows.forEach(row => {
        if (row.lastElementChild) {
            row.lastElementChild.remove();
        }
    });

    const titlePage = window.TT_CONFIG ? window.TT_CONFIG.title : "ThuocTinh";
    const apiPath = window.TT_CONFIG ? window.TT_CONFIG.apiPath : "data";

    const workbook = XLSX.utils.table_to_book(cloneTable, { sheet: `Danh sách ${titlePage}` });
    XLSX.writeFile(workbook, `Danh_Sach_${apiPath}_AerionSports.xlsx`);
}


document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("ttFormMonolith");
    const inputTen = document.getElementById("ttFormValue");
    const errorSpan = document.getElementById("ttFormError");
    const idInput = document.getElementById("itemId");

    if (form) {
        form.addEventListener("submit", function (event) {
            const apiPath = window.TT_CONFIG ? window.TT_CONFIG.apiPath : "";
            const titleName = window.TT_CONFIG ? window.TT_CONFIG.title.toLowerCase() : "thuộc tính";

            // 1. Chuẩn hóa khoảng trắng (Trim)
            let trimmedValue = inputTen.value.trim().replace(/\s+/g, ' ');
            inputTen.value = trimmedValue;

            // 2. Kiểm tra trống dữ liệu
            if (trimmedValue === "") {
                hienThiLoi(`Tên ${titleName} không được để trống!`);
                event.preventDefault(); return false;
            }

            // 3. 🛑 CHẶN ĐỊNH DẠNG SAI THEO NGHIỆP VỤ TỪNG LOẠI THUỘC TÍNH

            // Nhóm 1: MÀU SẮC, XUẤT XỨ, CHẤT LIỆU (Chỉ được phép nhập chữ và khoảng trắng, TUYỆT ĐỐI KHÔNG CÓ SỐ)
            if (["mau-sac", "xuat-xu", "chat-lieu-than", "chat-lieu-khung", "danh-muc"].includes(apiPath)) {
                const regexThuanchu = /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠưăâêôơ\s]+$/;
                if (!regexThuanchu.test(trimmedValue)) {
                    hienThiLoi(`Tên ${titleName} bắt buộc phải là chữ tiếng Việt, không chứa số hay ký tự lạ!`);
                    event.preventDefault(); return false;
                }
            }

            // Nhóm 2: TRỌNG LƯỢNG (Bắt buộc phải có số + chữ U, ví dụ: 3U, 4U, 4U-G5)
            else if (apiPath === "trong-luong") {
                const regexTrongLuong = /^[0-9][uU]$|^[0-9][uU]\s*-\s*[gG][0-9]$/;
                if (!regexTrongLuong.test(trimmedValue)) {
                    hienThiLoi(`Trọng lượng vợt phải đúng quy ước quốc tế (Ví dụ: 3U, 4U, 5U...)`);
                    event.preventDefault(); return false;
                }
            }

            // Nhóm 3: CHU VI CÁN VỢT (Bắt buộc phải tuân theo chuẩn G, ví dụ: G4, G5, G6)
            else if (apiPath === "chu-vi-can") {
                const regexChuVi = /^[gG][0-9]$/;
                if (!regexChuVi.test(trimmedValue)) {
                    hienThiLoi(`Chu vi cán vợt phải theo quy chuẩn (Ví dụ: G4, G5, G6)`);
                    event.preventDefault(); return false;
                }
            }

            // Nhóm 4: ĐIỂM CÂN BẰNG (Phải nhập số mm, ví dụ: 295, 300, hoặc kèm chữ như 295mm, 300 +- 3mm)
            else if (apiPath === "diem-can-bang") {
                const regexCânBang = /^[0-9a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠưăâêôơ\s\+\-]+$/;
                if (!regexCânBang.test(trimmedValue)) {
                    hienThiLoi(`Điểm cân bằng không hợp lệ!`);
                    event.preventDefault(); return false;
                }
            }

            // Nhóm 5: CÁC THUỘC TÍNH CÒN LẠI (Thương hiệu, Độ cứng - Cho phép chữ và số như "Yonex", "Độ cứng 8.5")
            else {
                const regexChung = /^[a-zA-Z0-9ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠưăâêôơ\s\-\.\/]+$/;
                if (!regexChung.test(trimmedValue)) {
                    hienThiLoi(`Tên ${titleName} không được chứa ký tự đặc biệt!`);
                    event.preventDefault(); return false;
                }
            }

            // 4. Kiểm tra giới hạn độ dài
            if (trimmedValue.length < 1 || trimmedValue.length > 50) {
                hienThiLoi(`Tên ${titleName} phải từ 1 đến 50 ký tự!`);
                event.preventDefault(); return false;
            }

            // 5. Kiểm tra trùng lặp trên hệ thống
            const allItems = (window.TT_CONFIG && window.TT_CONFIG.allItems) ? window.TT_CONFIG.allItems : [];
            const currentId = idInput.value;
            const isDuplicate = allItems.some(item => {
                if (currentId && String(item.id) === String(currentId)) return false;
                return item.ten.trim().toLowerCase() === trimmedValue.toLowerCase();
            });

            if (isDuplicate) {
                hienThiLoi(`Tên ${titleName} này đã tồn tại trên hệ thống Aerion Sports!`);
                event.preventDefault(); return false;
            }

            clearLoiValidatePanel();
        });
    }

    function hienThiLoi(message) {
        if (errorSpan) {
            errorSpan.textContent = message;
            errorSpan.style.display = "block";
        }
        if (inputTen) {
            inputTen.style.borderColor = "#ef4444";
            inputTen.focus();
        }
    }

    if (inputTen) {
        inputTen.addEventListener("input", function() {
            if (errorSpan && errorSpan.textContent !== "") {
                clearLoiValidatePanel();
            }
        });
    }
});
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".status-toggle-container").forEach(function (link) {
        link.addEventListener("click", function (e) {
            const track = this.querySelector(".toggle-track");
            const isActive = track && track.classList.contains("track-active");
            const message = isActive
                ? "Bạn có chắc chắn muốn NGỪNG HOẠT ĐỘNG thuộc tính này?"
                : "Bạn có chắc chắn muốn KÍCH HOẠT thuộc tính này?";
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });
});