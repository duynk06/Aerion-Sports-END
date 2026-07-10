/* =========================================================================
   ban-hang.js
   Chuyển đổi toàn bộ logic của BanHangView.vue sang JavaScript thuần.
   Vì Thymeleaf chỉ render HTML một lần ở server, tất cả phần "reactive"
   (đổi tab hóa đơn, thêm/xóa sản phẩm, tính tiền, mở modal, quét QR...)
   được xử lý hoàn toàn ở client bằng fetch API + thao tác DOM trực tiếp.
   ========================================================================= */

(function () {
    "use strict";

    /* ================== HẰNG SỐ & CẤU HÌNH API (từ BanHangService.js) ===== */
    const baseUrl = "http://localhost:8080/ban-hang";
    const apiUrl = "http://localhost:8080";

    const KHACH_HANG_VANG_LAI = {
        id: 999,
        hoTen: "Khách hàng vãng lai",
        sdt: null,
        email: null,
        diaChi: null,
    };
    const PHI_HN = 20000;
    const PHI_TINH_KHAC = 45000;

    /* ============================ STATE (thay cho ref/reactive) =========== */
    const state = {
        hoaDonCho: [],
        activeHoaDon: null,
        chiTietHoaDonHienTai: [],

        dsMauSac: [],
        dsTrongLuong: [],
        danhSach: [],
        idMauSac: null,
        idTrongLuong: null,
        trangThai: null,
        keyword: "",
        giaMin: 0,
        giaMax: 0,
        minPrice: 0,
        maxPrice: 0,

        sanPhamGiaThayDoi: {},
        ghiChu: "",
        phieuGiamGiaHienTai: null,
        soTienKhachDua: 0,

        page: 0,
        size: 5,
        totalPages: 0,
        totalElements: 0,

        dsKhachHang: [],
        searchKhachHang: "",
        pageKh: 0,
        sizeKh: 5,
        totalPagesKh: 0,
        totalElementsKh: 0,

        dsDiaChi: [],
        qrThongTin: { maHoaDon: "", soTien: 0 },
    };

    let isSyncing = false;
    let html5QrCode = null;
    let thongBaoTimeout = null;
    let filterTimeout = null;
    let searchKhTimeout = null;

    /* ============================ TIỆN ÍCH ================================ */
    const formatVND = (n) => Number(n || 0).toLocaleString("vi-VN");

    function getActiveHd() {
        return state.hoaDonCho.find((h) => h.id === state.activeHoaDon) || null;
    }

    function showThongBao(message, type) {
        type = type || "success";
        clearTimeout(thongBaoTimeout);
        const el = document.getElementById("toast");
        if (!el) return;
        el.textContent = message;
        el.style.background = type === "success" ? "#f79b66" : "#ff4d4f";
        el.style.display = "flex";
        thongBaoTimeout = setTimeout(() => {
            el.style.display = "none";
        }, 3000);
    }

    /* ============================ "COMPUTED" =============================== */
    function getKhachHangDuocChon() {
        if (!state.activeHoaDon) return null;
        const hd = getActiveHd();
        return hd ? hd.khachHang : null;
    }
    function getTongTienHienTai() {
        return state.chiTietHoaDonHienTai.reduce(
            (sum, sp) => sum + sp.donGia * sp.soLuong,
            0
        );
    }
    function getTienGiamHienTai() {
        if (!state.activeHoaDon) return 0;
        const hd = getActiveHd();
        return Number(hd && hd.tienGiam) || 0;
    }
    function getLoaiHoaDonHienTai() {
        if (!state.activeHoaDon) return 0;
        const hd = getActiveHd();
        return (hd && hd.loaiHoaDon) ?? 0;
    }
    function getPhiVanChuyen() {
        if (getLoaiHoaDonHienTai() !== 1) return 0;
        const kh = getKhachHangDuocChon();
        if (!kh || kh.id === 999) return 0;
        const tinhThanh = (kh.tinhThanh || "").toLowerCase();
        return tinhThanh.includes("hà nội") || tinhThanh.includes("ha noi")
            ? PHI_HN
            : PHI_TINH_KHAC;
    }
    function getTongThanhToan() {
        return Math.max(
            0,
            getTongTienHienTai() - getTienGiamHienTai() + getPhiVanChuyen()
        );
    }
    function getTienThua() {
        const thua = state.soTienKhachDua - getTongThanhToan();
        return thua > 0 ? thua : 0;
    }

    /* ============================ GỌI API (từ BanHangService.js) =========== */
    async function taoHoaDonCho() {
        const res = await fetch(`${baseUrl}/tao-hoa-don`, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getSanPham(
        keyword, idMauSac, idTrongLuong, giaMin, giaMax, trangThai, page, size
    ) {
        const params = new URLSearchParams();
        if (keyword) params.append("keyword", keyword);
        if (idMauSac) params.append("idMauSac", idMauSac);
        if (idTrongLuong) params.append("idTrongLuong", idTrongLuong);
        if (giaMin) params.append("giaMin", giaMin);
        if (giaMax) params.append("giaMax", giaMax);
        if (trangThai != null) params.append("trangThai", trangThai);
        params.append("page", page);
        params.append("size", size);
        const res = await fetch(`${baseUrl}/san-pham?${params}`);
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getMauSac() {
        const res = await fetch(`${apiUrl}/api/mau-sac/all`);
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getTrongLuong() {
        const res = await fetch(`${apiUrl}/api/trong-luong/all`);
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getKhoangGia() {
        const res = await fetch(`${baseUrl}/khoang-gia`);
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getKhachHangPos(keyword, page, size) {
        const params = new URLSearchParams();
        if (keyword) params.append("keyword", keyword);
        params.append("page", page);
        params.append("size", size);
        const res = await fetch(`${baseUrl}/khach-hang?${params}`);
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function updateKhachHangHoaDon(idHoaDon, idKhachHang) {
        const res = await fetch(
            `${baseUrl}/${idHoaDon}/khach-hang?idKhachHang=${idKhachHang}`,
            { method: "PUT" }
        );
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function themChiTietHoaDon(payload) {
        const res = await fetch(`${baseUrl}/them-san-pham`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function capNhatSoLuong(idChiTiet, soLuongMoi) {
        const res = await fetch(
            `${baseUrl}/chi-tiet/${idChiTiet}/so-luong?soLuong=${soLuongMoi}`,
            { method: "PUT" }
        );
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getHoaDonCho() {
        const res = await fetch(`${baseUrl}/hoa-don-cho`);
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function huyHoaDon(idHoaDon) {
        const res = await fetch(`${baseUrl}/hoa-don/${idHoaDon}`, {
            method: "DELETE",
        });
        if (!res.ok) throw new Error(await res.text());
    }

    async function xoaChiTietHoaDon(idChiTiet) {
        const res = await fetch(`${baseUrl}/chi-tiet/${idChiTiet}`, {
            method: "DELETE",
        });
        if (!res.ok) throw new Error(await res.text());
    }

    async function thanhToanHoaDon(payload) {
        const res = await fetch(`${baseUrl}/thanh-toan`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function capNhatLoaiHoaDon(idHoaDon, loaiHoaDon) {
        const res = await fetch(
            `${baseUrl}/${idHoaDon}/loai-hoa-don?loaiHoaDon=${loaiHoaDon}`,
            { method: "PUT" }
        );
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function capNhatPhiVanChuyen(idHoaDon, phiVanChuyen) {
        const res = await fetch(
            `${baseUrl}/${idHoaDon}/phi-van-chuyen?phiVanChuyen=${phiVanChuyen}`,
            { method: "PUT" }
        );
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getDiaChiKhachHang(idKhachHang) {
        const res = await fetch(
            `${baseUrl}/khach-hang/${idKhachHang}/dia-chi`
        );
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function getPhieuGiamGiaTotNhat(idHoaDon) {
        const res = await fetch(`${baseUrl}/${idHoaDon}/phieu-giam-gia-tot-nhat`);
        if (!res.ok) return null;
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    }

    async function apDungPhieuGiamGia(idHoaDon, idPhieu) {
        const res = await fetch(
            `${baseUrl}/${idHoaDon}/ap-dung-phieu?idPhieu=${idPhieu}`,
            { method: "PUT" }
        );
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function boPhieuGiamGia(idHoaDon) {
        const res = await fetch(`${baseUrl}/${idHoaDon}/bo-phieu`, {
            method: "PUT",
        });
        if (!res.ok) throw new Error(await res.text());
        return await res.json();
    }

    async function kiemTraGiaSanPham(idHoaDon) {
        const res = await fetch(`${baseUrl}/hoa-don/${idHoaDon}/kiem-tra-gia`);
        if (!res.ok) return [];
        return await res.json();
    }

    async function timSanPhamTheoMa(maCtsp) {
        const res = await fetch(
            `${baseUrl}/san-pham/tim-theo-ma?maCtsp=${encodeURIComponent(maCtsp)}`
        );
        if (!res.ok) throw new Error("Không tìm thấy sản phẩm với mã này!");
        return await res.json();
    }

    /* ============================ ĐỒNG BỘ / TẢI DỮ LIỆU ==================== */
    function dongBoChiTietHienTai() {
        if (!state.activeHoaDon) {
            state.chiTietHoaDonHienTai = [];
            return;
        }
        const hd = getActiveHd();
        state.chiTietHoaDonHienTai = hd ? [...(hd.chiTietHoaDon || [])] : [];
    }

    async function loadData() {
        try {
            const response = await getSanPham(
                state.keyword,
                state.idMauSac,
                state.idTrongLuong,
                state.giaMin,
                state.giaMax,
                state.trangThai,
                state.page,
                state.size
            );
            state.danhSach = response.content;
            state.totalPages = response.totalPages;
            state.totalElements = response.totalElements;
            renderProductModalTable();
        } catch (e) {
            console.error("Lỗi tải sản phẩm:", e);
        }
    }

    async function loadKhachHang() {
        try {
            const response = await getKhachHangPos(
                state.searchKhachHang,
                state.pageKh,
                state.sizeKh
            );
            state.dsKhachHang = response.content;
            state.totalPagesKh = response.totalPages;
            state.totalElementsKh = response.totalElements;
            renderCustomerModalTable();
        } catch (e) {
            console.error("Lỗi tải khách hàng:", e);
        }
    }

    async function loadFilterData() {
        try {
            const [mauSacData, trongLuongData, giaData] = await Promise.all([
                getMauSac(),
                getTrongLuong(),
                getKhoangGia(),
            ]);
            state.dsMauSac = mauSacData;
            state.dsTrongLuong = trongLuongData;
            state.minPrice = giaData.giaMin;
            state.maxPrice = giaData.giaMax;
            state.giaMax = giaData.giaMax;
            renderFilterOptions();
        } catch (e) {
            console.error("Lỗi tải bộ lọc:", e);
        }
    }

    async function kiemTraGia() {
        if (!state.activeHoaDon) return;
        try {
            const ketQua = await kiemTraGiaSanPham(state.activeHoaDon);
            const map = {};
            ketQua.forEach((item) => {
                if (item.daThayDoi) {
                    map[item.idChiTiet] = {
                        giaCu: item.giaCu,
                        giaMoi: item.giaMoi,
                        maCtsp: item.maCtsp,
                    };
                }
            });
            state.sanPhamGiaThayDoi = map;
            renderProductTable();
        } catch (e) {
            console.error("Lỗi kiểm tra giá:", e);
        }
    }

    function capNhatHoaDonTuResponse(response) {
        const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
        if (index !== -1) {
            state.hoaDonCho[index].tienGiam = response.tienGiam;
            state.hoaDonCho[index].maPhieuGiamGia = response.maPhieuGiamGia;
            state.hoaDonCho[index].tenPhieuGiamGia = response.tenPhieuGiamGia;
            state.hoaDonCho[index].tongTienThanhToan = response.tongTienThanhToan;
        }
    }

    async function xuLyBoPhieu() {
        const hd = getActiveHd();
        if (!hd || !hd.tienGiam || Number(hd.tienGiam) <= 0) return;
        try {
            const response = await boPhieuGiamGia(state.activeHoaDon);
            capNhatHoaDonTuResponse(response);
        } catch (e) {
            console.error("Lỗi hủy phiếu:", e);
        }
    }

    async function lamMoiPhieuGiamGia() {
        if (!state.activeHoaDon || getTongTienHienTai() <= 0) {
            state.phieuGiamGiaHienTai = null;
            renderPaymentBody();
            return;
        }
        try {
            const phieu = await getPhieuGiamGiaTotNhat(state.activeHoaDon);
            state.phieuGiamGiaHienTai = phieu;
            if (phieu && phieu.coTheApDung) {
                const response = await apDungPhieuGiamGia(state.activeHoaDon, phieu.id);
                capNhatHoaDonTuResponse(response);
            } else {
                await xuLyBoPhieu();
            }
            renderPaymentBody();
        } catch (e) {
            console.error("Lỗi làm mới phiếu giảm giá:", e);
        }
    }

    window.khongDungMa = async function () {
        state.phieuGiamGiaHienTai = null;
        await xuLyBoPhieu();
        renderPaymentBody();
    };

    /* ============================ HÓA ĐƠN CHỜ / TAB ========================= */
    window.taoHoaDon = async function () {
        if (state.hoaDonCho.length >= 5) {
            showThongBao("Đã đạt tối đa 5 hóa đơn chờ!", "error");
            return;
        }
        try {
            const hoaDonMoi = await taoHoaDonCho();
            state.hoaDonCho.unshift({
                ...hoaDonMoi,
                chiTietHoaDon: hoaDonMoi.sanPham || [],
                khachHang: KHACH_HANG_VANG_LAI,
            });
            await setActiveHoaDon(hoaDonMoi.id);
            showThongBao(`Đã tạo hóa đơn ${hoaDonMoi.maHoaDon}`, "success");
            renderTabs();
        } catch (e) {
            showThongBao(e.message || "Tạo hóa đơn thất bại", "error");
        }
    };

    window.dongHoaDon = async function (id) {
        const hd = state.hoaDonCho.find((h) => h.id === id);
        if (!hd) return;
        const message =
            hd.chiTietHoaDon && hd.chiTietHoaDon.length > 0
                ? "Hủy hóa đơn sẽ hoàn lại tồn kho. Xác nhận hủy?"
                : "Xóa hóa đơn trống này?";
        if (!confirm(message)) return;
        try {
            await huyHoaDon(id);
            state.hoaDonCho = state.hoaDonCho.filter((h) => h.id !== id);
            if (state.activeHoaDon === id) {
                await setActiveHoaDon(
                    state.hoaDonCho.length > 0 ? state.hoaDonCho[0].id : null
                );
            }
            renderTabs();
            await loadData();
        } catch (e) {
            showThongBao(e.message || "Không thể hủy hóa đơn!", "error");
        }
    };

    window.chonTab = async function (id) {
        await setActiveHoaDon(id);
        renderTabs();
    };

    // Tương đương watch(activeHoaDon, ...) trong Vue
    async function setActiveHoaDon(newId) {
        if (isSyncing) {
            state.activeHoaDon = newId;
            return;
        }
        state.activeHoaDon = newId;
        state.soTienKhachDua = 0;
        state.ghiChu = "";
        state.sanPhamGiaThayDoi = {};
        const ghiChuEl = document.getElementById("ghiChuInput");
        if (ghiChuEl) ghiChuEl.value = "";
        if (newId) {
            dongBoChiTietHienTai();
            await Promise.all([kiemTraGia(), lamMoiPhieuGiamGia()]);
        } else {
            state.phieuGiamGiaHienTai = null;
            state.chiTietHoaDonHienTai = [];
        }
        renderProductTable();
        renderCustomerCard();
        renderPaymentBody();
    }

    /* ============================ SẢN PHẨM TRONG HÓA ĐƠN ==================== */
    window.themSanPhamVaoHoaDon = async function (sanPham) {
        if (!state.activeHoaDon) return showThongBao("Vui lòng chọn hóa đơn!", "error");
        if (sanPham.soLuongTon <= 0) return showThongBao("Sản phẩm đã hết hàng!", "error");
        try {
            isSyncing = true;
            await themChiTietHoaDon({
                idHoaDon: state.activeHoaDon,
                idSanPhamChiTiet: sanPham.id,
                soLuong: 1,
                donGia: sanPham.gia,
            });
            const dsHoaDonMoi = await getHoaDonCho();
            state.hoaDonCho = dsHoaDonMoi.map(mapHoaDon);
            dongBoChiTietHienTai();
            await Promise.all([loadData(), kiemTraGia(), lamMoiPhieuGiamGia()]);
            renderTabs();
            renderProductTable();
            showThongBao("Đã thêm sản phẩm thành công!", "success");
        } catch (e) {
            console.error("Lỗi khi thêm sản phẩm qua QR:", e);
            showThongBao("Không thể thêm sản phẩm", "error");
        } finally {
            isSyncing = false;
        }
    };

    function mapHoaDon(hd) {
        return {
            ...hd,
            chiTietHoaDon: hd.sanPham || [],
            khachHang: hd.idKhachHang
                ? {
                    id: hd.idKhachHang,
                    hoTen: hd.tenKhachHang,
                    sdt: hd.sdt,
                    diaChi: hd.diaChiKhachHang || "",
                    tinhThanh: hd.tinhThanhKhachHang || "",
                }
                : KHACH_HANG_VANG_LAI,
        };
    }

    window.capNhatSoLuongSanPham = async function (idChiTiet, soLuongMoi) {
        const sl = parseInt(soLuongMoi, 10);
        if (isNaN(sl) || sl < 1) return;
        const sp = state.chiTietHoaDonHienTai.find((s) => s.id === idChiTiet);
        if (!sp) return;
        try {
            isSyncing = true;
            const response = await capNhatSoLuong(sp.id, sl);
            state.chiTietHoaDonHienTai = [...(response.sanPham || [])];
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].chiTietHoaDon = [...(response.sanPham || [])];
            }
            renderProductTable();
            await Promise.all([loadData(), lamMoiPhieuGiamGia()]);
        } catch (e) {
            showThongBao((e && e.message) || "Lỗi số lượng", "error");
        } finally {
            isSyncing = false;
        }
    };

    window.tangSoLuong = function (idChiTiet) {
        const sp = state.chiTietHoaDonHienTai.find((s) => s.id === idChiTiet);
        if (sp) window.capNhatSoLuongSanPham(idChiTiet, sp.soLuong + 1);
    };
    window.giamSoLuong = function (idChiTiet) {
        const sp = state.chiTietHoaDonHienTai.find((s) => s.id === idChiTiet);
        if (sp && sp.soLuong > 1) window.capNhatSoLuongSanPham(idChiTiet, sp.soLuong - 1);
    };

    window.xoaChiTiet = async function (idChiTiet) {
        if (!confirm("Xóa sản phẩm này khỏi hóa đơn?")) return;
        try {
            isSyncing = true;
            await xoaChiTietHoaDon(idChiTiet);
            state.chiTietHoaDonHienTai = state.chiTietHoaDonHienTai.filter(
                (sp) => sp.id !== idChiTiet
            );
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].chiTietHoaDon = state.hoaDonCho[index].chiTietHoaDon.filter(
                    (sp) => sp.id !== idChiTiet
                );
            }
            delete state.sanPhamGiaThayDoi[idChiTiet];
            renderProductTable();
            await Promise.all([loadData(), kiemTraGia(), lamMoiPhieuGiamGia()]);
        } catch (e) {
            showThongBao("Lỗi xóa sản phẩm!", "error");
        } finally {
            isSyncing = false;
        }
    };

    /* ============================ KHÁCH HÀNG ================================ */
    window.chonKhachHang = async function (kh) {
        if (!state.activeHoaDon) return;
        try {
            await updateKhachHangHoaDon(state.activeHoaDon, kh.id);
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].khachHang = {
                    id: kh.id,
                    hoTen: kh.hoTen,
                    sdt: kh.sdt,
                    email: kh.email,
                    diaChi: kh.diaChi,
                    tinhThanh: kh.tinhThanh,
                };
            }
            closeCustomerModal();
            renderCustomerCard();
            renderTabs();
            if (getLoaiHoaDonHienTai() === 1) await capNhatPhiVanChuyenHoaDon();
            renderPaymentBody();
        } catch (e) {
            showThongBao("Không thể gán khách hàng!", "error");
        }
    };

    window.chonKhachVangLai = async function () {
        if (!state.activeHoaDon) return;
        try {
            await updateKhachHangHoaDon(state.activeHoaDon, 999);
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].khachHang = KHACH_HANG_VANG_LAI;
                if (state.hoaDonCho[index].loaiHoaDon === 1) {
                    await capNhatLoaiHoaDon(state.activeHoaDon, 0);
                    state.hoaDonCho[index].loaiHoaDon = 0;
                    showThongBao("Đã chuyển về bán tại quầy cho khách vãng lai!", "info");
                }
            }
            closeCustomerModal();
            renderCustomerCard();
            renderTabs();
            renderPaymentBody();
        } catch (e) {
            showThongBao("Lỗi gán khách vãng lai!", "error");
        }
    };

    window.toggleLoaiHoaDon = async function () {
        if (!state.activeHoaDon) return;
        const loaiMoi = getLoaiHoaDonHienTai() === 0 ? 1 : 0;
        const kh = getKhachHangDuocChon();
        if (loaiMoi === 1 && (!kh || kh.id === 999)) {
            return showThongBao("Vui lòng chọn khách cụ thể để giao hàng!", "error");
        }
        try {
            const response = await capNhatLoaiHoaDon(state.activeHoaDon, loaiMoi);
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].loaiHoaDon = loaiMoi;
                state.hoaDonCho[index].tienVanChuyen = response.tienVanChuyen;
            }
            if (loaiMoi === 1) await capNhatPhiVanChuyenHoaDon();
            renderCustomerCard();
            renderPaymentBody();
        } catch (e) {
            showThongBao("Lỗi cập nhật hình thức đơn!", "error");
        }
    };

    async function capNhatPhiVanChuyenHoaDon() {
        try {
            const response = await capNhatPhiVanChuyen(state.activeHoaDon, getPhiVanChuyen());
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].tienVanChuyen = response.tienVanChuyen;
            }
        } catch (e) {
            console.error("Lỗi cập nhật phí ship:", e);
        }
    }

    window.moModalDiaChi = async function () {
        const kh = getKhachHangDuocChon();
        if (!kh || kh.id === 999) return;
        try {
            state.dsDiaChi = await getDiaChiKhachHang(kh.id);
            renderAddressModalList();
            openModal("addressModal");
        } catch (e) {
            showThongBao("Lỗi tải địa chỉ!", "error");
        }
    };

    window.chonDiaChi = async function (dc) {
        const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
        if (index !== -1) {
            state.hoaDonCho[index].khachHang.diaChi = dc.diaChiDayDu || dc.diaChiChiTiet || "";
            state.hoaDonCho[index].khachHang.tinhThanh = dc.tinhThanh || "";
        }
        closeModal("addressModal");
        if (getLoaiHoaDonHienTai() === 1) await capNhatPhiVanChuyenHoaDon();
        renderCustomerCard();
        renderPaymentBody();
    };

    /* ============================ QUÉT QR SẢN PHẨM =========================== */
    window.moModalQuetQR = async function () {
        if (!state.activeHoaDon) return showThongBao("Vui lòng chọn hóa đơn!", "error");
        document.getElementById("qrScanKetQua").style.display = "none";
        document.getElementById("qrScanLoi").style.display = "none";
        openModal("qrScanModal");
        await new Promise((r) => setTimeout(r, 0)); // đợi DOM hiển thị (thay nextTick)
        if (!html5QrCode && window.Html5Qrcode) {
            html5QrCode = new Html5Qrcode("qr-reader");
        }
        if (!html5QrCode) {
            document.getElementById("qrScanLoi").textContent = "❌ Thư viện quét QR chưa tải xong!";
            document.getElementById("qrScanLoi").style.display = "block";
            return;
        }
        try {
            await html5QrCode.start(
                { facingMode: "environment" },
                { fps: 15, qrbox: { width: 250, height: 250 } },
                async (decodedText) => {
                    await html5QrCode.stop();
                    const ketQuaEl = document.getElementById("qrScanKetQua");
                    ketQuaEl.innerHTML = "✅ Đã quét: <strong>" + decodedText + "</strong>";
                    ketQuaEl.style.display = "block";
                    await xuLyQrQuetDuoc(decodedText);
                },
                () => {}
            );
        } catch (err) {
            const loiEl = document.getElementById("qrScanLoi");
            loiEl.textContent = "Không thể truy cập Camera!";
            loiEl.style.display = "block";
        }
    };

    window.dongModalQuetQR = async function () {
        if (html5QrCode && html5QrCode.getState && html5QrCode.getState() === 2) {
            try {
                await html5QrCode.stop();
            } catch (e) {
                console.error(e);
            }
        }
        closeModal("qrScanModal");
    };

    async function xuLyQrQuetDuoc(maCtsp) {
        const loiEl = document.getElementById("qrScanLoi");
        try {
            const sanPham = await timSanPhamTheoMa(maCtsp);
            if (!sanPham) {
                loiEl.textContent = `Không tìm thấy sản phẩm với mã: ${maCtsp}`;
                loiEl.style.display = "block";
                return;
            }
            if (sanPham.soLuongTon <= 0) {
                loiEl.textContent = `Sản phẩm ${maCtsp} đã hết hàng!`;
                loiEl.style.display = "block";
                return;
            }
            await window.themSanPhamVaoHoaDon(sanPham);
            closeModal("qrScanModal");
            document.getElementById("qrScanKetQua").style.display = "none";
            loiEl.style.display = "none";
        } catch (e) {
            loiEl.textContent = "Lỗi xử lý mã QR hoặc kết nối API.";
            loiEl.style.display = "block";
            console.error(e);
        }
    }

    /* ============================ THANH TOÁN ================================= */
    window.xuLyThanhToan = async function (idHinhThuc) {
        if (!state.activeHoaDon || state.chiTietHoaDonHienTai.length === 0)
            return showThongBao("Đơn hàng trống!", "error");
        if (Object.keys(state.sanPhamGiaThayDoi).length > 0)
            return showThongBao("Vui lòng cập nhật sản phẩm có giá thay đổi!", "error");
        const kh = getKhachHangDuocChon();
        if (getLoaiHoaDonHienTai() === 1 && (!kh || kh.id === 999))
            return showThongBao("Thiếu thông tin người nhận!", "error");
        if (idHinhThuc === 1 && state.soTienKhachDua < getTongThanhToan())
            return showThongBao("Tiền khách đưa không đủ!", "error");

        if (idHinhThuc === 2) {
            const hd = getActiveHd();
            state.qrThongTin = {
                maHoaDon: (hd && hd.maHoaDon) || "",
                soTien: getTongThanhToan(),
            };
            renderQrModalInfo();
            openModal("qrPaymentModal");
            return;
        }

        if (
            !confirm(
                `Xác nhận thanh toán bằng tiền mặt: ${formatVND(getTongThanhToan())} đ?`
            )
        )
            return;
        await thucHienThanhToan(1);
    };

    window.thucHienThanhToan = thucHienThanhToan;
    async function thucHienThanhToan(idHinhThuc) {
        try {
            await thanhToanHoaDon({
                idHoaDon: state.activeHoaDon,
                idHinhThucThanhToan: idHinhThuc,
                soTienKhachDua:
                    idHinhThuc === 1 ? state.soTienKhachDua : getTongThanhToan(),
                ghiChu: state.ghiChu,
            });
            showThongBao(
                getLoaiHoaDonHienTai() === 1 ? "Đã tạo đơn giao hàng!" : "Thanh toán thành công!",
                "success"
            );
            const id = state.activeHoaDon;
            state.hoaDonCho = state.hoaDonCho.filter((hd) => hd.id !== id);
            await setActiveHoaDon(state.hoaDonCho.length > 0 ? state.hoaDonCho[0].id : null);
            state.soTienKhachDua = 0;
            state.ghiChu = "";
            const ghiChuEl = document.getElementById("ghiChuInput");
            if (ghiChuEl) ghiChuEl.value = "";
            closeModal("qrPaymentModal");
            renderTabs();
            await loadData();
        } catch (e) {
            showThongBao("Thanh toán thất bại!", "error");
        }
    }

    async function kiemTraSanPhamNgungHoatDong() {
        if (!state.activeHoaDon || state.chiTietHoaDonHienTai.length === 0) return;
        try {
            const ketQua = await kiemTraGiaSanPham(state.activeHoaDon);
            const dsNgung = ketQua.filter(
                (item) => item.trangThai === 0 || item.trangThai === false
            );
            if (dsNgung.length > 0) {
                for (const sp of dsNgung) {
                    await xoaChiTietHoaDon(sp.idChiTiet);
                }
                showThongBao(
                    "Hệ thống đã loại bỏ sản phẩm ngừng kinh doanh khỏi đơn hàng.",
                    "error"
                );
                dongBoChiTietHienTai();
                renderProductTable();
            }
        } catch (e) {
            console.error(e);
        }
    }

    /* ============================ RENDER: TABS =============================== */
    function renderTabs() {
        const wrap = document.getElementById("tabsWrapper");
        const counter = document.getElementById("soDonHienTai");
        if (counter) counter.textContent = state.hoaDonCho.length;
        if (!wrap) return;
        wrap.innerHTML = state.hoaDonCho
            .map((hd) => {
                const active = hd.id === state.activeHoaDon ? "active" : "";
                const ten =
                    hd.khachHang && hd.khachHang.hoTen ? hd.khachHang.hoTen : "Khách vãng lai";
                return `
          <div class="tab ${active}" onclick="chonTab(${hd.id})">
            <span class="tab-title">
              Hóa Đơn - ${hd.maHoaDon} <br>
              <small style="font-weight:normal;">(${ten})</small>
            </span>
            <span class="tab-badge">${hd.soLuong ?? (hd.chiTietHoaDon || []).length}</span>
            <button class="tab-close" onclick="event.stopPropagation(); dongHoaDon(${hd.id})">×</button>
          </div>`;
            })
            .join("");
    }

    /* ============================ RENDER: BẢNG SẢN PHẨM TRONG ĐƠN ============= */
    function renderProductTable() {
        const body = document.getElementById("productTableBody");
        const tongTienEl = document.getElementById("tongTienHienTaiLabel");
        if (tongTienEl) tongTienEl.textContent = formatVND(getTongTienHienTai()) + " đ";

        if (!body) return;
        if (state.chiTietHoaDonHienTai.length === 0) {
            body.innerHTML = `<tr><td colspan="6" class="text-center text-muted" style="padding:20px;">Chưa có sản phẩm nào trong đơn hàng</td></tr>`;
            return;
        }
        body.innerHTML = state.chiTietHoaDonHienTai
            .map((sp) => {
                const gtd = state.sanPhamGiaThayDoi[sp.id];
                const giaChangedHtml = gtd
                    ? `<p style="color:#cf1322;font-size:12px;margin-top:4px;">
               Giá sản phẩm đã thay đổi
               <span style="text-decoration:line-through;color:#999;">${formatVND(gtd.giaCu)} đ</span>
               → <strong>${formatVND(gtd.giaMoi)} đ</strong>
             </p>`
                    : "";
                const donGiaStyle = gtd ? "color:#cf1322;" : "";
                const disabledPlus = gtd ? "disabled style=\"opacity:0.4;cursor:not-allowed;\"" : "";
                const disabledInput = gtd ? "disabled" : "";
                return `
          <tr>
            <td class="text-center"><div class="img-placeholder"><img src="${sp.anh}" alt="Ảnh SP" style="width:100%;height:auto;"></div></td>
            <td>
              <div class="prod-info">
                <h4>${sp.tenSanPham}</h4>
                <p class="text-muted">Màu: ${sp.mauSac} - TL: ${sp.trongLuong}</p>
                ${giaChangedHtml}
              </div>
            </td>
            <td class="text-right font-bold" style="${donGiaStyle}">${formatVND(sp.donGia)} đ</td>
            <td class="text-center">
              <div class="qty-control">
                <button onclick="giamSoLuong(${sp.id})">-</button>
                <input type="number" value="${sp.soLuong}" min="1" style="width:50px;text-align:center;"
                       onchange="capNhatSoLuongSanPham(${sp.id}, this.value)" ${disabledInput} />
                <button onclick="tangSoLuong(${sp.id})" ${disabledPlus}>+</button>
              </div>
            </td>
            <td class="text-right font-bold">${formatVND(sp.donGia * sp.soLuong)} đ</td>
            <td class="text-center">
              <button class="btn-delete" onclick="xoaChiTiet(${sp.id})">
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M10 11v6M14 11v6"/>
                </svg>
              </button>
            </td>
          </tr>`;
            })
            .join("");
    }

    /* ============================ RENDER: THÔNG TIN KHÁCH HÀNG ================ */
    function renderCustomerCard() {
        const el = document.getElementById("customerCard");
        if (!el) return;
        const kh = getKhachHangDuocChon();
        const loaiHd = getLoaiHoaDonHienTai();

        if (!kh || kh.id === 999) {
            el.innerHTML = `
        <div style="margin-bottom:12px;">
          <span class="text-muted text-sm" style="display:block;">Tên khách hàng</span>
          <h4 class="mt-1">Khách hàng vãng lai</h4>
        </div>`;
            return;
        }

        let html = `
      <div style="margin-bottom:12px;">
        <span class="text-muted text-sm" style="display:block;">Tên khách hàng</span>
        <h4 class="mt-1">${kh.hoTen}</h4>
      </div>`;
        if (kh.sdt) {
            html += `
      <div style="margin-bottom:12px;">
        <span class="text-muted text-sm" style="display:block;">Số điện thoại</span>
        <div class="mt-1 font-bold">${kh.sdt}</div>
      </div>`;
        }
        if (kh.email) {
            html += `
      <div style="margin-bottom:12px;">
        <span class="text-muted text-sm" style="display:block;">Email</span>
        <div class="mt-1">${kh.email}</div>
      </div>`;
        }
        if (kh.diaChi) {
            html += `
      <div style="margin-bottom:12px;">
        <span class="text-muted text-sm" style="display:block;">Địa chỉ</span>
        <div class="mt-1" style="display:flex;justify-content:space-between;align-items:flex-start;gap:8px;">
          <span>${kh.diaChi}</span>
          ${
                loaiHd === 1
                    ? `<button onclick="moModalDiaChi()" style="background:none;border:1px solid #1890ff;color:#1890ff;padding:2px 8px;border-radius:4px;cursor:pointer;font-size:12px;white-space:nowrap;flex-shrink:0;">Thay đổi</button>`
                    : ""
            }
        </div>
      </div>`;
        }
        if (loaiHd === 1) {
            const tinhThanh = (kh.tinhThanh || "").toLowerCase();
            const label = tinhThanh.includes("hà nội") || tinhThanh.includes("ha noi") ? "Hà Nội" : "Tỉnh khác";
            html += `
      <div style="margin-top:12px;padding:8px;background:#f6ffed;border:1px solid #b7eb8f;border-radius:6px;">
        <span class="text-muted text-sm" style="display:block;">Phí vận chuyển</span>
        <div class="mt-1 font-bold" style="color:#52c41a;">
          ${formatVND(getPhiVanChuyen())} đ
          <small class="text-muted" style="font-weight:normal;">(${label})</small>
        </div>
      </div>`;
        }
        el.innerHTML = html;
    }

    /* ============================ RENDER: KHU VỰC THANH TOÁN =================== */
    function renderPaymentBody() {
        // Toggle hình thức
        const loaiHd = getLoaiHoaDonHienTai();
        const toggleEl = document.getElementById("toggleSwitch");
        const labelEl = document.getElementById("loaiHoaDonLabel");
        if (toggleEl) toggleEl.classList.toggle("active", loaiHd === 1);
        if (labelEl) labelEl.textContent = loaiHd === 1 ? "Giao hàng" : "Bán tại quầy";

        // Mã giảm giá
        const btnKhongDung = document.getElementById("btnKhongDungMa");
        if (btnKhongDung) btnKhongDung.style.display = getTienGiamHienTai() > 0 ? "inline-block" : "none";

        const couponArea = document.getElementById("couponArea");
        if (couponArea) {
            const phieu = state.phieuGiamGiaHienTai;
            if (phieu && phieu.coTheApDung) {
                const isPercent =
                    phieu.loaiPhieuGiamGia === "PHAN_TRAM" ||
                    (phieu.loaiPhieuGiamGia && phieu.loaiPhieuGiamGia.includes("%"));
                let dongGiam;
                if (isPercent) {
                    dongGiam = `Giảm ${phieu.giaTriGiam}% (tối đa ${formatVND(phieu.giaTriGiamToiDa)} đ)`;
                } else if (phieu.loaiPhieuGiamGia === "VAN_CHUYEN") {
                    dongGiam = `Miễn phí vận chuyển tối đa ${formatVND(phieu.giaTriGiam)} đ`;
                } else {
                    dongGiam = `Giảm ${formatVND(phieu.giaTriGiam)} đ`;
                }
                let goiYHtml = "";
                if (phieu.phieuGoiY) {
                    const gy = phieu.phieuGoiY;
                    goiYHtml = `
            <div style="background:#fffbe6;border:1px solid #ffe58f;border-radius:6px;padding:10px 12px;margin-top:10px;">
              <div style="font-weight:600;color:#d48806;font-size:13px;margin-bottom:6px;">💡 Có mã tốt hơn — mua thêm để được ưu đãi hơn</div>
              <div style="font-size:13px;color:#333;margin-bottom:4px;"><strong>${gy.tenPhieuGiamGia}</strong> (${gy.maPhieuGiamGia})</div>
              <div style="font-size:13px;color:#555;margin-bottom:4px;">
                Mua thêm <span style="color:#cf1322;font-weight:600;">${formatVND(gy.soTienCanMuaThem)} đ</span>
                để được giảm <span style="color:#389e0d;font-weight:600;">${formatVND(gy.soTienGiamNeuDat)} đ</span>
              </div>
              <div style="font-size:12px;color:#888;">Đơn tối thiểu: ${formatVND(gy.giaTriDonToiThieu)} đ</div>
            </div>`;
                }
                couponArea.innerHTML = `
          <div class="coupon-card">
            <div class="coupon-header">
              <div class="coupon-tags">
                <span class="tag blue">${phieu.loaiPhieuGiamGia}</span>
                <span class="tag gray">Mã tốt nhất</span>
                <span class="tag dark">${phieu.maPhieuGiamGia}</span>
              </div>
              <span class="badge-green">Đang áp dụng</span>
            </div>
            <h4 class="mt-2">${phieu.tenPhieuGiamGia}</h4>
            <p class="mt-1 text-sm">
              ${dongGiam} → <span class="text-danger font-bold">- ${formatVND(phieu.soTienGiamThucTe)} đ</span>
            </p>
            ${
                    phieu.giaTriDonToiThieu > 0
                        ? `<p class="text-muted text-sm mt-1">Đơn tối thiểu: ${formatVND(phieu.giaTriDonToiThieu)} đ</p>`
                        : ""
                }
            <p class="text-muted text-sm mt-2">Hết hạn: ${new Date(phieu.ngayKetThuc).toLocaleDateString("vi-VN")}</p>
          </div>
          ${goiYHtml}`;
            } else if (!phieu || getTongTienHienTai() === 0) {
                couponArea.innerHTML = `<div style="color:#888;font-size:13px;padding:8px 0;">Chưa có mã giảm giá phù hợp</div>`;
            } else {
                couponArea.innerHTML = "";
            }
        }

        // Tổng kết thanh toán
        const summary = document.getElementById("summaryList");
        if (summary) {
            const tienGiam = getTienGiamHienTai();
            const phiVC = getPhiVanChuyen();
            summary.innerHTML = `
        <div class="summary-row">
          <span class="text-muted">Tiền hàng</span>
          <span class="font-bold">${formatVND(getTongTienHienTai())} đ</span>
        </div>
        ${
                tienGiam > 0
                    ? `<div class="summary-row mt-1">
                 <span class="text-muted">Giảm giá</span>
                 <span class="text-danger font-bold">- ${formatVND(tienGiam)} đ</span>
               </div>`
                    : ""
            }
        ${
                loaiHd === 1
                    ? `<div class="summary-row mt-1">
                 <span class="text-muted">Phí vận chuyển</span>
                 <span class="font-bold" style="color:#52c41a;">+ ${formatVND(phiVC)} đ</span>
               </div>`
                    : ""
            }
        <div class="summary-row total-row mt-2">
          <span class="font-bold">Tổng phải trả</span>
          <span class="text-danger font-bold text-lg">${formatVND(getTongThanhToan())} đ</span>
        </div>
        <div class="summary-row align-center mt-3">
          <span class="text-muted">Khách thanh toán</span>
          <span class="input-wrapper-pay">
            <input type="text" id="soTienKhachDuaInput" class="input-right"
                   value="${state.soTienKhachDua ? formatVND(state.soTienKhachDua) : ""}"
                   placeholder="0 đ" style="width:150px;text-align:right;"
                   oninput="onSoTienKhachDuaInput(this)" />
          </span>
        </div>
        <div class="summary-row mt-3">
          <span class="text-muted">Tiền thừa trả khách</span>
          <span class="font-bold" style="color:${getTienThua() > 0 ? "#16a34a" : "#333"};">
            ${formatVND(getTienThua())} đ
          </span>
        </div>`;
        }
    }

    window.onSoTienKhachDuaInput = function (input) {
        const raw = input.value.replace(/\./g, "").replace(/,/g, "");
        const num = parseInt(raw, 10);
        state.soTienKhachDua = isNaN(num) ? 0 : num;
        // Chỉ cập nhật lại dòng "tiền thừa" để không làm mất vị trí con trỏ đang gõ
        const rows = document.querySelectorAll("#summaryList .summary-row");
        const lastRow = rows[rows.length - 1];
        if (lastRow) {
            const valueSpan = lastRow.querySelector("span.font-bold");
            if (valueSpan) {
                valueSpan.style.color = getTienThua() > 0 ? "#16a34a" : "#333";
                valueSpan.textContent = formatVND(getTienThua()) + " đ";
            }
        }
    };

    window.onGhiChuInput = function (value) {
        state.ghiChu = value;
    };

    /* ============================ RENDER: MODAL SẢN PHẨM ======================= */
    function renderFilterOptions() {
        const mauSacSelect = document.getElementById("mauSacSelect");
        if (mauSacSelect) {
            mauSacSelect.innerHTML =
                `<option value="">-- Chọn màu sắc --</option>` +
                state.dsMauSac.map((m) => `<option value="${m.id}">${m.tenMauSac}</option>`).join("");
        }
        const trongLuongSelect = document.getElementById("trongLuongSelect");
        if (trongLuongSelect) {
            trongLuongSelect.innerHTML =
                `<option value="">-- Chọn trọng lượng --</option>` +
                state.dsTrongLuong.map((t) => `<option value="${t.id}">${t.tenTrongLuong}</option>`).join("");
        }
        const giaMaxRange = document.getElementById("giaMaxRange");
        if (giaMaxRange) {
            giaMaxRange.min = 0;
            giaMaxRange.max = state.maxPrice;
            giaMaxRange.value = state.giaMax;
        }
        updateGiaMaxUI();
    }

    function updateGiaMaxUI() {
        const label = document.getElementById("giaMaxLabel");
        const progress = document.getElementById("sliderProgress");
        if (label) label.textContent = "0 đ - " + formatVND(state.giaMax) + " đ";
        if (progress && state.maxPrice > 0) {
            progress.style.width = (state.giaMax / state.maxPrice) * 100 + "%";
        }
    }

    function renderProductModalTable() {
        const body = document.getElementById("productModalTableBody");
        if (body) {
            body.innerHTML = state.danhSach
                .map(
                    (item, index) => `
        <tr>
          <td class="text-center">${index + 1 + state.page * state.size}</td>
          <td>${item.ma}</td>
          <td><div class="modal-img-wrapper"><img src="${item.anh}" alt="product"></div></td>
          <td>${item.ten}</td>
          <td>${item.mauSac}</td>
          <td>${item.trongLuong}</td>
          <td class="text-right">${item.soLuongTon}</td>
          <td class="text-right font-bold">${formatVND(item.gia)} đ</td>
          <td class="text-center"><button class="btn-select-product" onclick='themSanPhamVaoHoaDon(${JSON.stringify(item)})'>Chọn</button></td>
        </tr>`
                )
                .join("");
        }
        const pageInfo = document.getElementById("productPageInfo");
        if (pageInfo) pageInfo.textContent = `Trang ${state.page + 1}/${state.totalPages || 1}`;
        const countInfo = document.getElementById("productCountInfo");
        if (countInfo) countInfo.textContent = `Hiển thị ${state.danhSach.length} / tổng ${state.totalElements} bản ghi`;
        const prevBtn = document.getElementById("productPrevBtn");
        const nextBtn = document.getElementById("productNextBtn");
        if (prevBtn) prevBtn.disabled = state.page === 0;
        if (nextBtn) nextBtn.disabled = state.page + 1 >= state.totalPages;
    }

    window.nextPage = function () {
        if (state.page + 1 < state.totalPages) {
            state.page++;
            loadData();
        }
    };
    window.prevPage = function () {
        if (state.page > 0) {
            state.page--;
            loadData();
        }
    };

    window.resetFilter = function () {
        state.keyword = "";
        state.idMauSac = null;
        state.idTrongLuong = null;
        state.trangThai = null;
        state.giaMin = 0;
        state.giaMax = state.maxPrice;
        state.page = 0;
        document.getElementById("keywordInput").value = "";
        document.getElementById("mauSacSelect").value = "";
        document.getElementById("trongLuongSelect").value = "";
        document.querySelectorAll('input[name="trangThai"]').forEach((r) => (r.checked = r.value === ""));
        document.getElementById("giaMaxRange").value = state.maxPrice;
        updateGiaMaxUI();
        loadData();
    };

    function debounceLoadData() {
        state.page = 0;
        clearTimeout(filterTimeout);
        filterTimeout = setTimeout(loadData, 300);
    }

    window.onKeywordInput = function (value) {
        state.keyword = value;
        debounceLoadData();
    };
    window.onMauSacChange = function (value) {
        state.idMauSac = value || null;
        debounceLoadData();
    };
    window.onTrongLuongChange = function (value) {
        state.idTrongLuong = value || null;
        debounceLoadData();
    };
    window.onTrangThaiChange = function (value) {
        state.trangThai = value === "" ? null : Number(value);
        debounceLoadData();
    };
    window.onGiaMaxInput = function (value) {
        state.giaMax = Number(value);
        updateGiaMaxUI();
        debounceLoadData();
    };

    /* ============================ RENDER: MODAL KHÁCH HÀNG ===================== */
    function renderCustomerModalTable() {
        const body = document.getElementById("customerModalTableBody");
        if (body) {
            body.innerHTML = state.dsKhachHang
                .map(
                    (kh, index) => `
        <tr>
          <td class="text-center">${index + 1 + state.pageKh * state.sizeKh}</td>
          <td>${kh.hoTen}</td>
          <td>${kh.sdt}</td>
          <td>${kh.diaChi}</td>
          <td class="text-center">
            <button style="background-color:#222;color:#fff;border:none;padding:6px 12px;border-radius:4px;cursor:pointer;font-size:13px;"
                    onclick='chonKhachHang(${JSON.stringify(kh)})'>Chọn</button>
          </td>
        </tr>`
                )
                .join("");
        }
        const total = document.getElementById("customerTotalInfo");
        if (total) total.textContent = "Tổng: " + state.totalElementsKh;
        const countInfo = document.getElementById("customerCountInfo");
        if (countInfo) countInfo.textContent = `Hiển thị ${state.dsKhachHang.length} / tổng ${state.totalElementsKh} bản ghi`;
        const pageInfo = document.getElementById("customerPageInfo");
        if (pageInfo) pageInfo.textContent = `Trang ${state.pageKh + 1} / ${state.totalPagesKh === 0 ? 1 : state.totalPagesKh}`;
        const prevBtn = document.getElementById("customerPrevBtn");
        const nextBtn = document.getElementById("customerNextBtn");
        if (prevBtn) prevBtn.disabled = state.pageKh === 0;
        if (nextBtn) nextBtn.disabled = state.pageKh + 1 >= state.totalPagesKh;
    }

    window.nextPageKh = function () {
        if (state.pageKh + 1 < state.totalPagesKh) {
            state.pageKh++;
            loadKhachHang();
        }
    };
    window.prevPageKh = function () {
        if (state.pageKh > 0) {
            state.pageKh--;
            loadKhachHang();
        }
    };
    window.onSearchKhachHangInput = function (value) {
        state.searchKhachHang = value;
        state.pageKh = 0;
        clearTimeout(searchKhTimeout);
        searchKhTimeout = setTimeout(loadKhachHang, 300);
    };

    /* ============================ RENDER: MODAL ĐỊA CHỈ ========================= */
    function renderAddressModalList() {
        const el = document.getElementById("addressModalList");
        if (!el) return;
        if (state.dsDiaChi.length === 0) {
            el.innerHTML = `<div class="text-center text-muted" style="padding:30px;">Khách hàng chưa có địa chỉ nào</div>`;
            return;
        }
        el.innerHTML = state.dsDiaChi
            .map((dc) => {
                const style = dc.macDinh
                    ? "border-color:#52c41a;background:#f6ffed;"
                    : "";
                return `
        <div style="border:1px solid #e8e8e8;border-radius:8px;padding:14px 16px;margin-bottom:12px;${style}">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:12px;">
            <div style="flex:1;">
              <div style="font-weight:600;font-size:14px;margin-bottom:4px;">
                ${dc.nguoiNhan}
                <span style="font-weight:normal;color:#888;margin-left:8px;">${dc.sdt}</span>
                ${
                    dc.macDinh
                        ? `<span style="background:#52c41a;color:#fff;font-size:11px;padding:1px 7px;border-radius:10px;margin-left:8px;font-weight:normal;">Mặc định</span>`
                        : ""
                }
              </div>
              <div style="color:#555;font-size:13px;">${dc.diaChiDayDu}</div>
            </div>
            <button onclick='chonDiaChi(${JSON.stringify(dc)})'
                    style="background:#222;color:#fff;border:none;padding:6px 14px;border-radius:4px;cursor:pointer;font-size:13px;white-space:nowrap;flex-shrink:0;">Chọn</button>
          </div>
        </div>`;
            })
            .join("");
    }

    /* ============================ RENDER: MODAL QR THANH TOÁN =================== */
    function renderQrModalInfo() {
        const maEl = document.getElementById("qrMaHoaDon");
        const soTienEl = document.getElementById("qrSoTien");
        const maEl2 = document.getElementById("qrMaHoaDon2");
        const ghiChuEl = document.getElementById("qrGhiChu");
        if (maEl) maEl.textContent = state.qrThongTin.maHoaDon;
        if (soTienEl) soTienEl.textContent = formatVND(state.qrThongTin.soTien) + " đ";
        if (maEl2) maEl2.textContent = state.qrThongTin.maHoaDon;
        if (ghiChuEl)
            ghiChuEl.textContent = state.ghiChu || "Thanh toán QR - " + state.qrThongTin.maHoaDon;
    }

    /* ============================ MODAL HELPERS ================================= */
    function openModal(id) {
        const el = document.getElementById(id);
        if (el) el.style.display = "flex";
    }
    function closeModal(id) {
        const el = document.getElementById(id);
        if (el) el.style.display = "none";
    }
    window.openModal = openModal;
    window.closeModal = closeModal;

    window.openProductModal = function () {
        openModal("productModal");
    };
    window.openCustomerModal = function () {
        openModal("customerModal");
        if (state.dsKhachHang.length === 0) loadKhachHang();
    };
    function closeCustomerModal() {
        closeModal("customerModal");
    }

    /* ============================ VISIBILITY CHANGE (giao ca / đổi tab) ========= */
    async function handleVisibilityChange() {
        if (document.visibilityState === "visible" && state.activeHoaDon) {
            await Promise.all([
                loadData(),
                kiemTraSanPhamNgungHoatDong(),
                kiemTraGia(),
                lamMoiPhieuGiamGia(),
            ]);
        }
    }

    /* ============================ KHỞI TẠO (thay onMounted) ===================== */
    async function init() {
        await Promise.all([loadData(), loadFilterData()]);
        try {
            const dsHoaDon = await getHoaDonCho();
            state.hoaDonCho = dsHoaDon.map(mapHoaDon);
            if (state.hoaDonCho.length > 0) {
                state.activeHoaDon = state.hoaDonCho[0].id;
                dongBoChiTietHienTai();
                await Promise.all([kiemTraGia(), lamMoiPhieuGiamGia()]);
            }
        } catch (e) {
            console.error("Lỗi khởi tạo danh sách chờ:", e);
        }
        renderTabs();
        renderProductTable();
        renderCustomerCard();
        renderPaymentBody();
        document.addEventListener("visibilitychange", handleVisibilityChange);
    }

    document.addEventListener("DOMContentLoaded", init);
})();