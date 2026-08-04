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
    const PHI_NOI_TINH = 22000;   // cùng tỉnh/thành với shop (Hà Nội)
    const PHI_NOI_MIEN = 30000;   // khác tỉnh nhưng cùng miền Bắc
    const PHI_LIEN_MIEN = 32000;  // khác miền (Trung/Nam)
    const NGUONG_MIEN_PHI_SHIP = 5000000;

// Danh sách tỉnh/thành theo miền (tên rút gọn, dùng để so khớp bằng includes — không phân biệt tiền tố "Tỉnh"/"Thành phố")
    const MIEN_BAC = [
        "hà nội", "hà giang", "cao bằng", "bắc kạn", "tuyên quang", "lào cai",
        "điện biên", "lai châu", "sơn la", "yên bái", "hòa bình", "thái nguyên",
        "lạng sơn", "quảng ninh", "bắc giang", "phú thọ", "vĩnh phúc", "bắc ninh",
        "hải dương", "hải phòng", "hưng yên", "thái bình", "hà nam", "nam định", "ninh bình",
    ];
    const MIEN_TRUNG = [
        "thanh hóa", "nghệ an", "hà tĩnh", "quảng bình", "quảng trị", "thừa thiên huế", "huế",
        "đà nẵng", "quảng nam", "quảng ngãi", "bình định", "phú yên", "khánh hòa",
        "ninh thuận", "bình thuận", "kon tum", "gia lai", "đắk lắk", "đắk nông", "lâm đồng",
    ];
    const MIEN_NAM = [
        "hồ chí minh", "bà rịa", "vũng tàu", "bình dương", "bình phước", "đồng nai",
        "tây ninh", "long an", "tiền giang", "bến tre", "vĩnh long", "trà vinh",
        "đồng tháp", "an giang", "kiên giang", "cần thơ", "hậu giang", "sóc trăng",
        "bạc liêu", "cà mau",
    ];
    const capNhatDiaChiGiaoHangThuCongApi = (idHoaDon, payload) =>
        apiFetch(`${baseUrl}/${idHoaDon}/dia-chi-giao-hang-thu-cong`, { method: "PUT", ...jsonBody(payload) });
    function xacDinhMien(tinhThanh) {
        const s = (tinhThanh || "").toLowerCase();
        if (MIEN_BAC.some((t) => s.includes(t))) return "BAC";
        if (MIEN_TRUNG.some((t) => s.includes(t))) return "TRUNG";
        if (MIEN_NAM.some((t) => s.includes(t))) return "NAM";
        return null;
    }
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
        trangThai: 1,
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
    function layTinhTuChuoiDiaChi(diaChiDayDu) {
        if (!diaChiDayDu) return "";
        const parts = diaChiDayDu.split(",");
        return parts[parts.length - 1].trim();
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
        if (!kh) return 0;
        if (!kh.tinhThanh) return 0;   // chưa có địa chỉ giao hàng -> chưa tính được phí ship

        // ✅ Tiền hàng sau khi trừ giảm giá (tổng phải trả trước khi cộng phí ship)
        // — dùng số này thay vì "tổng phải trả" để tránh vòng lặp phụ thuộc,
        // vì tổng phải trả lại bao gồm chính phí vận chuyển
        const tienHangSauGiam = Math.max(0, getTongTienHienTai() - getTienGiamHienTai());
        if (tienHangSauGiam >= NGUONG_MIEN_PHI_SHIP) return 0;

        const tinhThanh = kh.tinhThanh.toLowerCase();
        if (tinhThanh.includes("hà nội")) return PHI_NOI_TINH;
        if (xacDinhMien(tinhThanh) === "BAC") return PHI_NOI_MIEN;
        return PHI_LIEN_MIEN;
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

    /* ============================ POLLING: TỰ ĐỘNG KIỂM TRA SẢN PHẨM NGỪNG HOẠT ĐỘNG ==== */
    function batDauPollingSanPhamNgungHoatDong() {
        setInterval(async () => {
            if (state.activeHoaDon) {
                await kiemTraSanPhamNgungHoatDong();
            }
        }, 2000); // kiểm tra mỗi 5 giây, không cần đổi tab
    }
    /* ============================ GỌI API (từ BanHangService.js) =========== */
    /* ============================ GỌI API (đã gộp qua apiFetch) ============ */
    async function apiFetch(url, options = {}) {
        const res = await fetch(url, options);
        if (!res.ok) throw new Error(await res.text());
        if (res.status === 204) return null;
        const text = await res.text();
        return text ? JSON.parse(text) : null;
    }
    const jsonBody = (payload) => ({
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
    });

    const taoHoaDonCho = () => apiFetch(`${baseUrl}/tao-hoa-don`, { method: "POST" });

    function getSanPham(keyword, idMauSac, idTrongLuong, giaMin, giaMax, trangThai, page, size) {
        const params = new URLSearchParams();
        if (keyword) params.append("keyword", keyword);
        if (idMauSac) params.append("idMauSac", idMauSac);
        if (idTrongLuong) params.append("idTrongLuong", idTrongLuong);
        if (giaMin) params.append("giaMin", giaMin);
        if (giaMax) params.append("giaMax", giaMax);
        if (trangThai != null) params.append("trangThai", trangThai);
        params.append("page", page);
        params.append("size", size);
        return apiFetch(`${baseUrl}/san-pham?${params}`);
    }

    const getMauSac = () => apiFetch(`${apiUrl}/api/mau-sac/all`);
    const getTrongLuong = () => apiFetch(`${apiUrl}/api/trong-luong/all`);
    const getKhoangGia = () => apiFetch(`${baseUrl}/khoang-gia`);

    function getKhachHangPos(keyword, page, size) {
        const params = new URLSearchParams();
        if (keyword) params.append("keyword", keyword);
        params.append("page", page);
        params.append("size", size);
        return apiFetch(`${baseUrl}/khach-hang?${params}`);
    }

    const themKhachHangNhanh = (payload) =>
        apiFetch(`${baseUrl}/khach-hang/them-nhanh`, { method: "POST", ...jsonBody(payload) });
    const updateKhachHangHoaDon = (idHoaDon, idKhachHang) =>
        apiFetch(`${baseUrl}/${idHoaDon}/khach-hang?idKhachHang=${idKhachHang}`, { method: "PUT" });

    const themChiTietHoaDon = (payload) =>
        apiFetch(`${baseUrl}/them-san-pham`, { method: "POST", ...jsonBody(payload) });

    const capNhatSoLuong = (idChiTiet, soLuongMoi) =>
        apiFetch(`${baseUrl}/chi-tiet/${idChiTiet}/so-luong?soLuong=${soLuongMoi}`, { method: "PUT" });

    const getHoaDonCho = () => apiFetch(`${baseUrl}/hoa-don-cho`);
    const huyHoaDon = (idHoaDon) => apiFetch(`${baseUrl}/hoa-don/${idHoaDon}`, { method: "DELETE" });
    const xoaChiTietHoaDon = (idChiTiet) => apiFetch(`${baseUrl}/chi-tiet/${idChiTiet}`, { method: "DELETE" });
    const thanhToanHoaDon = (payload) => apiFetch(`${baseUrl}/thanh-toan`, { method: "POST", ...jsonBody(payload) });

    const capNhatLoaiHoaDon = (idHoaDon, loaiHoaDon) =>
        apiFetch(`${baseUrl}/${idHoaDon}/loai-hoa-don?loaiHoaDon=${loaiHoaDon}`, { method: "PUT" });

    const capNhatPhiVanChuyen = (idHoaDon, phiVanChuyen) =>
        apiFetch(`${baseUrl}/${idHoaDon}/phi-van-chuyen?phiVanChuyen=${phiVanChuyen}`, { method: "PUT" });

    const getDiaChiKhachHang = (idKhachHang) => apiFetch(`${baseUrl}/khach-hang/${idKhachHang}/dia-chi`);

    const capNhatDiaChiGiaoHangHoaDon = (idHoaDon, idDiaChi) =>
        apiFetch(`${baseUrl}/${idHoaDon}/dia-chi-giao-hang?idDiaChi=${idDiaChi}`, { method: "PUT" });

    const themDiaChiKhachHang = (idKhachHang, payload) =>
        apiFetch(`${baseUrl}/khach-hang/${idKhachHang}/dia-chi`, { method: "POST", ...jsonBody(payload) });

    const suaDiaChiKhachHang = (idDiaChi, payload) =>
        apiFetch(`${baseUrl}/dia-chi/${idDiaChi}`, { method: "PUT", ...jsonBody(payload) });

    /* ===== API tỉnh/huyện/xã (dữ liệu 3 cấp trước sáp nhập 07/2025) ===== */
    const OPEN_API_BASE = "https://provinces.open-api.vn/api/v1";
    const layDanhSachTinhThanh = () => apiFetch(`${OPEN_API_BASE}/p/`);

    async function layDanhSachQuanHuyen(maTinh) {
        const data = await apiFetch(`${OPEN_API_BASE}/p/${maTinh}?depth=2`);
        return data?.districts || [];
    }
    async function layDanhSachPhuongXa(maHuyen) {
        const data = await apiFetch(`${OPEN_API_BASE}/d/${maHuyen}?depth=2`);
        return data?.wards || [];
    }

// Các hàm dưới đây tự nuốt lỗi (trả về giá trị mặc định) thay vì throw,
// nên giữ try/catch riêng thay vì dùng thẳng apiFetch
    async function getPhieuGiamGiaTotNhat(idHoaDon) {
        try {
            return await apiFetch(`${baseUrl}/${idHoaDon}/phieu-giam-gia-tot-nhat`);
        } catch {
            return null;
        }
    }

    const apDungPhieuGiamGia = (idHoaDon, idPhieu) =>
        apiFetch(`${baseUrl}/${idHoaDon}/ap-dung-phieu?idPhieu=${idPhieu}`, { method: "PUT" });

    const boPhieuGiamGia = (idHoaDon) =>
        apiFetch(`${baseUrl}/${idHoaDon}/bo-phieu`, { method: "PUT" });

    async function kiemTraGiaSanPham(idHoaDon) {
        try {
            return await apiFetch(`${baseUrl}/hoa-don/${idHoaDon}/kiem-tra-gia`);
        } catch {
            return [];
        }
    }

    async function timSanPhamTheoMa(maCtsp) {
        try {
            return await apiFetch(`${baseUrl}/san-pham/tim-theo-ma?maCtsp=${encodeURIComponent(maCtsp)}`);
        } catch {
            throw new Error("Không tìm thấy sản phẩm với mã này!");
        }
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
            // ✅ Sau khi tiền giảm giá đã cập nhật, đồng bộ lại phí vận chuyển —
            // vì ngưỡng miễn phí ship (5tr) phụ thuộc vào tiền hàng SAU giảm giá,
            // nếu không làm bước này, DB vẫn giữ phí ship cũ dù giao diện hiển thị đúng 0
            if (getLoaiHoaDonHienTai() === 1) {
                await capNhatPhiVanChuyenHoaDon();
            }
            renderPaymentBody();
        } catch (e) {
            console.error("Lỗi làm mới phiếu giảm giá:", e);
        }
    }

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
        const thongTinGiaoChung = {
            diaChi: hd.diaChiGiaoHang || "",
            tinhThanh: hd.diaChiGiaoHang ? layTinhTuChuoiDiaChi(hd.diaChiGiaoHang) : "",
            nguoiNhanGiao: hd.tenNguoiNhanGiao || "",
            sdtGiao: hd.sdtNguoiNhanGiao || "",
        };
        return {
            ...hd,
            chiTietHoaDon: hd.sanPham || [],
            khachHang: hd.idKhachHang
                ? {
                    id: hd.idKhachHang,
                    hoTen: hd.tenKhachHang,
                    sdt: hd.sdt,
                    email: hd.email,
                    ...thongTinGiaoChung,
                    // Khách đã đăng ký: nếu hóa đơn chưa có địa chỉ giao hàng riêng, fallback về địa chỉ mặc định của khách
                    diaChi: hd.diaChiGiaoHang || hd.diaChiKhachHang || "",
                    tinhThanh: hd.diaChiGiaoHang ? layTinhTuChuoiDiaChi(hd.diaChiGiaoHang) : (hd.tinhThanhKhachHang || ""),
                }
                : {
                    ...KHACH_HANG_VANG_LAI,
                    ...thongTinGiaoChung,
                },
        };
    }
    function renderAddressModalListVangLai() {
        const el = document.getElementById("addressModalList");
        if (!el) return;
        const kh = getKhachHangDuocChon();
        el.innerHTML = renderDiaChiFormVangLai(kh);
        khoiTaoDropdownDiaChiVangLai(kh);
    }
    function khoiTaoDropdownDiaChiVangLai(kh) {
        // Parse lại "chi tiết, xã, huyện, tỉnh" đã lưu trong hóa đơn để chọn đúng khi mở lại
        let phuongXaGop = "";
        if (kh && kh.diaChi) {
            const parts = kh.diaChi.split(",").map((s) => s.trim());
            if (parts.length >= 4) {
                phuongXaGop = `${parts[1]}, ${parts[2]}`;
            }
        }
        khoiTaoDropdownDiaChi({ tinhThanh: kh?.tinhThanh || "", phuongXa: phuongXaGop });
    }

    window.luuDiaChiVangLai = async function () {
        xoaTatCaLoiDiaChi();

        const tinhSelect = document.getElementById("dcTinhThanhSelect");
        const huyenSelect = document.getElementById("dcQuanHuyenSelect");
        const xaSelect = document.getElementById("dcPhuongXaSelect");

        const tenTinh = tinhSelect.options[tinhSelect.selectedIndex]?.dataset.name || "";
        const tenHuyen = huyenSelect.options[huyenSelect.selectedIndex]?.dataset.name || "";
        const tenXa = xaSelect.options[xaSelect.selectedIndex]?.dataset.name || "";

        const nguoiNhan = document.getElementById("dcNguoiNhan").value.trim();
        const sdt = document.getElementById("dcSdt").value.trim();
        const diaChiChiTiet = document.getElementById("dcDiaChiChiTiet").value.trim();

        let coLoi = false;
        if (!nguoiNhan) { hienThiLoiField("dcNguoiNhanGroup", "dcNguoiNhanError", "Vui lòng nhập tên người nhận!"); coLoi = true; }
        if (!sdt) { hienThiLoiField("dcSdtGroup", "dcSdtError", "Vui lòng nhập số điện thoại!"); coLoi = true; }
        else if (!/^(0[3|5|7|8|9])([0-9]{8})$/.test(sdt)) { hienThiLoiField("dcSdtGroup", "dcSdtError", "Số điện thoại không đúng định dạng nhà mạng VN!"); coLoi = true; }
        if (!tenTinh) { hienThiLoiField("dcTinhThanhGroup", "dcTinhThanhError", "Vui lòng chọn Tỉnh/Thành phố!"); coLoi = true; }
        if (!tenHuyen) { hienThiLoiField("dcQuanHuyenGroup", "dcQuanHuyenError", "Vui lòng chọn Quận/Huyện!"); coLoi = true; }
        if (!tenXa) { hienThiLoiField("dcPhuongXaGroup", "dcPhuongXaError", "Vui lòng chọn Phường/Xã!"); coLoi = true; }
        if (!diaChiChiTiet) { hienThiLoiField("dcDiaChiChiTietGroup", "dcDiaChiChiTietError", "Vui lòng nhập địa chỉ chi tiết!"); coLoi = true; }
        if (coLoi) return;

        const payload = {
            nguoiNhan, sdt,
            tinhThanh: tenTinh,
            phuongXa: `${tenXa}, ${tenHuyen}`,
            diaChiChiTiet,
        };

        try {
            const response = await capNhatDiaChiGiaoHangThuCongApi(state.activeHoaDon, payload);
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].khachHang.diaChi = response.diaChiGiaoHang || "";
                state.hoaDonCho[index].khachHang.tinhThanh = tenTinh;
                state.hoaDonCho[index].khachHang.nguoiNhanGiao = nguoiNhan;
                state.hoaDonCho[index].khachHang.sdtGiao = sdt;
            }
            closeModal("addressModal");
            await capNhatPhiVanChuyenHoaDon();
            renderCustomerCard();
            renderPaymentBody();
            showThongBao("Đã lưu địa chỉ giao hàng!", "success");
        } catch (e) {
            showThongBao(e.message || "Lỗi lưu địa chỉ!", "error");
        }
    };
    function hienThiLoiField(idGroup, idError, message) {
        const group = document.getElementById(idGroup);
        const errorEl = document.getElementById(idError);
        if (group) group.classList.add("has-error");
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.style.display = "block";
        }
    }
    window.xoaLoiField = function (idGroup, idError) {
        const group = document.getElementById(idGroup);
        const errorEl = document.getElementById(idError);
        if (group) group.classList.remove("has-error");
        if (errorEl) {
            errorEl.textContent = "";
            errorEl.style.display = "none";
        }
    };
    window.moModalThemKhachNhanh = function () {
        // Đặt giới hạn ngày sinh mới nhất được chọn = hôm nay - 15 năm (tương ứng "từ 15 tuổi trở lên")
        const ngaySinhInput = document.getElementById("qaNgaySinh");
        const homNay = new Date();
        const ngayToiDa = new Date(homNay.getFullYear() - 15, homNay.getMonth(), homNay.getDate());
        ngaySinhInput.max = ngayToiDa.toISOString().split("T")[0];

        // Reset form + lỗi mỗi lần mở
        document.getElementById("qaHoTen").value = "";
        document.getElementById("qaSdt").value = "";
        document.getElementById("qaEmail").value = "";
        ngaySinhInput.value = "";
        document.querySelector('input[name="qaGioiTinh"][value="1"]').checked = true;
        ["qaHoTen", "qaSdt", "qaEmail", "qaNgaySinh"].forEach((prefix) => {
            xoaLoiField(`${prefix}Group`, `${prefix}Error`);
        });

        openModal("quickAddCustomerModal");
    };

    window.luuKhachHangNhanh = async function () {
        ["qaHoTen", "qaSdt", "qaEmail", "qaNgaySinh"].forEach((prefix) => {
            xoaLoiField(`${prefix}Group`, `${prefix}Error`);
        });

        const hoTen = document.getElementById("qaHoTen").value.trim();
        const sdt = document.getElementById("qaSdt").value.trim();
        const email = document.getElementById("qaEmail").value.trim();
        const ngaySinh = document.getElementById("qaNgaySinh").value;
        const gioiTinh = Number(document.querySelector('input[name="qaGioiTinh"]:checked').value);

        let coLoi = false;

        if (!hoTen) {
            hienThiLoiField("qaHoTenGroup", "qaHoTenError", "Vui lòng nhập tên khách hàng!");
            coLoi = true;
        }
        if (!sdt) {
            hienThiLoiField("qaSdtGroup", "qaSdtError", "Vui lòng nhập số điện thoại!");
            coLoi = true;
        } else if (!/^0\d{9}$/.test(sdt)) {
            hienThiLoiField("qaSdtGroup", "qaSdtError", "Số điện thoại phải có 10 số và bắt đầu bằng số 0!");
            coLoi = true;
        }
        if (!email) {
            hienThiLoiField("qaEmailGroup", "qaEmailError", "Vui lòng nhập email!");
            coLoi = true;
        } else if (!/^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/.test(email)) {
            hienThiLoiField("qaEmailGroup", "qaEmailError", "Email không đúng định dạng!");
            coLoi = true;
        }
        if (!ngaySinh) {
            hienThiLoiField("qaNgaySinhGroup", "qaNgaySinhError", "Vui lòng chọn ngày sinh!");
            coLoi = true;
        } else {
            // ✅ Tính tuổi chính xác theo ngày/tháng/năm, không chỉ theo hiệu số năm
            const ns = new Date(ngaySinh);
            const homNay = new Date();
            let tuoi = homNay.getFullYear() - ns.getFullYear();
            const chuaToiSinhNhat =
                homNay.getMonth() < ns.getMonth() ||
                (homNay.getMonth() === ns.getMonth() && homNay.getDate() < ns.getDate());
            if (chuaToiSinhNhat) tuoi--;
            if (tuoi < 15) {
                hienThiLoiField("qaNgaySinhGroup", "qaNgaySinhError", "Khách hàng phải từ 15 tuổi trở lên!");
                coLoi = true;
            }
        }

        if (coLoi) return;

        try {
            const khachHangMoi = await themKhachHangNhanh({ hoTen, sdt, email, gioiTinh, ngaySinh });
            showThongBao("Đã thêm khách hàng mới!", "success");
            closeModal("quickAddCustomerModal");

            // ✅ Chọn luôn khách hàng vừa tạo cho hóa đơn hiện tại, đóng luôn modal chọn khách hàng
            await window.chonKhachHang(khachHangMoi);

            // Làm mới danh sách khách hàng trong modal chọn khách (nếu người dùng mở lại)
            await loadKhachHang();
        } catch (e) {
            showThongBao(e.message || "Lỗi thêm khách hàng!", "error");
        }
    };
    function xoaTatCaLoiDiaChi() {
        ["dcNguoiNhan", "dcSdt", "dcTinhThanh", "dcQuanHuyen", "dcPhuongXa", "dcDiaChiChiTiet"].forEach((prefix) => {
            window.xoaLoiField(`${prefix}Group`, `${prefix}Error`);
        });
    }
    function renderDiaChiFormVangLai(kh) {
        kh = kh || {};
        return `
      <div class="address-form" style="border-top:none;padding-top:0;margin-top:0;">
        <h4 style="margin:0 0 12px 0;">Địa chỉ giao hàng (khách vãng lai)</h4>
        <div class="filter-row">
          <div class="filter-group flex-1" id="dcNguoiNhanGroup">
            <label>Người nhận <span>*</span></label>
            <input id="dcNguoiNhan" type="text" class="modal-input" value="${kh.nguoiNhanGiao || ""}"
                   oninput="xoaLoiField('dcNguoiNhanGroup','dcNguoiNhanError')"/>
            <span class="error-text" id="dcNguoiNhanError" style="display:none;"></span>
          </div>
          <div class="filter-group flex-1" id="dcSdtGroup">
            <label>Số điện thoại <span>*</span></label>
            <input id="dcSdt" type="text" class="modal-input" value="${kh.sdtGiao || ""}"
                   oninput="xoaLoiField('dcSdtGroup','dcSdtError')"/>
            <span class="error-text" id="dcSdtError" style="display:none;"></span>
          </div>
        </div>
        <div class="filter-row" style="margin-top:12px;">
          <div class="filter-group flex-1" id="dcTinhThanhGroup">
            <label>Tỉnh/Thành phố <span>*</span></label>
            <select id="dcTinhThanhSelect" class="modal-select">
              <option value="">-- Đang tải... --</option>
            </select>
            <span class="error-text" id="dcTinhThanhError" style="display:none;"></span>
          </div>
          <div class="filter-group flex-1" id="dcQuanHuyenGroup">
            <label>Quận/Huyện <span>*</span></label>
            <select id="dcQuanHuyenSelect" class="modal-select" disabled>
              <option value="">-- Chọn tỉnh/thành trước --</option>
            </select>
            <span class="error-text" id="dcQuanHuyenError" style="display:none;"></span>
          </div>
          <div class="filter-group flex-1" id="dcPhuongXaGroup">
            <label>Phường/Xã/Thị trấn <span>*</span></label>
            <select id="dcPhuongXaSelect" class="modal-select" disabled>
              <option value="">-- Chọn quận/huyện trước --</option>
            </select>
            <span class="error-text" id="dcPhuongXaError" style="display:none;"></span>
          </div>
        </div>
        <div class="filter-group" id="dcDiaChiChiTietGroup" style="margin-top:12px;">
          <label>Địa chỉ chi tiết (số nhà, tên đường...) <span>*</span></label>
          <input id="dcDiaChiChiTiet" type="text" class="modal-input" value="${kh.diaChiChiTietCu || ""}"
                 oninput="xoaLoiField('dcDiaChiChiTietGroup','dcDiaChiChiTietError')"/>
          <span class="error-text" id="dcDiaChiChiTietError" style="display:none;"></span>
        </div>
        <div style="display:flex;gap:12px;margin-top:16px;">
          <button class="btn-primary" onclick="luuDiaChiVangLai()">Lưu</button>
          <button class="btn-outline-modal" onclick="closeModal('addressModal')">Hủy</button>
        </div>
      </div>`;
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
                    nguoiNhanGiao: "",   // ✅ reset tạm, sẽ được điền lại đúng bên dưới nếu đang giao hàng
                    sdtGiao: "",
                };
            }
            closeCustomerModal();
            renderCustomerCard();
            renderTabs();
            if (getLoaiHoaDonHienTai() === 1) {
                await capNhatPhiVanChuyenHoaDon();
                await tuDongChonDiaChiMacDinh();   // ✅ lấy đúng người nhận/SĐT từ bảng dia_chi_khach_hang của khách MỚI
            }
            await lamMoiPhieuGiamGia();
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
                state.hoaDonCho[index].khachHang = {
                    ...KHACH_HANG_VANG_LAI,
                    diaChi: "",
                    tinhThanh: "",
                    nguoiNhanGiao: "",
                    sdtGiao: "",
                };
            }
            closeCustomerModal();
            renderCustomerCard();
            renderTabs();
            if (getLoaiHoaDonHienTai() === 1) {
                await capNhatPhiVanChuyenHoaDon();
                showThongBao("Vui lòng nhập địa chỉ giao hàng cho khách vãng lai!", "info");
            }
            await lamMoiPhieuGiamGia();
            renderPaymentBody();
        } catch (e) {
            showThongBao("Lỗi gán khách vãng lai!", "error");
        }
    };
    window.toggleLoaiHoaDon = async function () {
        if (!state.activeHoaDon) return;
        const loaiMoi = getLoaiHoaDonHienTai() === 0 ? 1 : 0;
        const kh = getKhachHangDuocChon();
        if (loaiMoi === 1 && !kh) {
            return showThongBao("Vui lòng chọn khách hàng trước!", "error");
        }
        try {
            const response = await capNhatLoaiHoaDon(state.activeHoaDon, loaiMoi);
            const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
            if (index !== -1) {
                state.hoaDonCho[index].loaiHoaDon = loaiMoi;
                state.hoaDonCho[index].tienVanChuyen = response.tienVanChuyen;
            }
            if (loaiMoi === 1) {
                if (kh.id !== 999) {
                    await capNhatPhiVanChuyenHoaDon();
                    await tuDongChonDiaChiMacDinh();
                } else if (!kh.diaChi) {
                    await capNhatPhiVanChuyenHoaDon();
                    renderCustomerCard();
                    renderPaymentBody();
                    await window.moModalDiaChi();   // vãng lai chưa có địa chỉ -> mở form nhập tay ngay
                    return;
                } else {
                    await capNhatPhiVanChuyenHoaDon();
                }
            }
            renderCustomerCard();
            renderPaymentBody();
        } catch (e) {
            showThongBao("Lỗi cập nhật hình thức đơn!", "error");
        }
    };
    async function tuDongChonDiaChiMacDinh() {
        const kh = getKhachHangDuocChon();
        if (!kh) return;
        try {
            const ds = await getDiaChiKhachHang(kh.id);
            state.dsDiaChi = ds;
            const macDinh = ds.find((d) => d.macDinh) || ds[0];
            if (macDinh) await window.chonDiaChi(macDinh);
        } catch (e) {
            console.error("Lỗi tự động chọn địa chỉ:", e);
        }
    }
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
        if (!kh) return;
        if (kh.id === 999) {
            openModal("addressModal");
            renderAddressModalListVangLai();
            return;
        }
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
            state.hoaDonCho[index].khachHang.nguoiNhanGiao = dc.nguoiNhan || "";
            state.hoaDonCho[index].khachHang.sdtGiao = dc.sdt || "";
        }
        closeModal("addressModal");
        try {
            await capNhatDiaChiGiaoHangHoaDon(state.activeHoaDon, dc.id);
        } catch (e) {
            console.error("Lỗi lưu địa chỉ giao hàng:", e);
            showThongBao("Lỗi lưu địa chỉ giao hàng: " + (e.message || ""), "error");   // ✅ báo lỗi rõ ràng
        }
        if (getLoaiHoaDonHienTai() === 1) await capNhatPhiVanChuyenHoaDon();
        renderCustomerCard();
        renderPaymentBody();
    };
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
            document.getElementById("qrScanLoi").textContent = "Thư viện quét QR chưa tải xong!";
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
            loiEl.textContent = " Lỗi xử lý mã QR hoặc kết nối API.";
            loiEl.style.display = "block";
            console.error(e);
        }
    }

    window.xuLyThanhToan = async function (idHinhThuc) {
        if (!state.activeHoaDon || state.chiTietHoaDonHienTai.length === 0)
            return showThongBao("Đơn hàng trống!", "error");

        // ❌ XÓA đoạn này — đây chính là nguyên nhân không thể thanh toán được sau khi giá đổi:
        // if (Object.keys(state.sanPhamGiaThayDoi).length > 0)
        //     return showThongBao("Vui lòng cập nhật sản phẩm có giá thay đổi!", "error");


        const kh = getKhachHangDuocChon();
        if (getLoaiHoaDonHienTai() === 1 && (!kh || !kh.diaChi))
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
            console.error("Chi tiết lỗi thanh toán:", e);
            showThongBao(e.message || "Thanh toán thất bại!", "error");   // ✅ hiện lỗi thật
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

                    // ✅ Cập nhật ngay trong state, không cần F5
                    state.chiTietHoaDonHienTai = state.chiTietHoaDonHienTai.filter(
                        (ct) => ct.id !== sp.idChiTiet
                    );
                    const index = state.hoaDonCho.findIndex((hd) => hd.id === state.activeHoaDon);
                    if (index !== -1) {
                        state.hoaDonCho[index].chiTietHoaDon = state.hoaDonCho[index].chiTietHoaDon.filter(
                            (ct) => ct.id !== sp.idChiTiet
                        );
                    }
                }
                showThongBao(
                    "Hệ thống đã loại bỏ sản phẩm ngừng kinh doanh khỏi đơn hàng.",
                    "error"
                );
                renderProductTable();
                renderTabs();
                await lamMoiPhieuGiamGia();
            }
        } catch (e) {
            console.error(e);
        }
    }
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

        // ✅ Nhóm các dòng theo cùng 1 sản phẩm (tên + màu + trọng lượng) để
        // phát hiện trường hợp: cùng 1 sản phẩm nhưng có 2 dòng khác giá
        // (dòng cũ thêm trước khi đổi giá + dòng mới thêm sau khi đổi giá,
        // vì backend không gộp 2 dòng khác đơn giá lại với nhau)
        const nhomTheoSanPham = {};
        state.chiTietHoaDonHienTai.forEach((sp) => {
            const key = `${sp.tenSanPham}|${sp.mauSac}|${sp.trongLuong}`;
            if (!nhomTheoSanPham[key]) nhomTheoSanPham[key] = [];
            nhomTheoSanPham[key].push(sp);
        });

        body.innerHTML = state.chiTietHoaDonHienTai
            .map((sp) => {
                const key = `${sp.tenSanPham}|${sp.mauSac}|${sp.trongLuong}`;
                const nhom = nhomTheoSanPham[key];
                const coNhieuDong = nhom.length > 1;

                let laDongCu = false;      // dòng cũ -> khoá tăng SL, không hiện thông báo
                let giaChangedHtml = "";   // chỉ dòng mới mới hiện thông báo này

                if (coNhieuDong) {
                    // Dòng có id nhỏ nhất trong nhóm = dòng được thêm trước = dòng cũ
                    const idDongCuNhat = Math.min(...nhom.map((x) => x.id));
                    laDongCu = sp.id === idDongCuNhat;

                    if (!laDongCu) {
                        // Dòng này được thêm sau khi giá đã đổi -> hiện thông báo
                        const dongCu = nhom.find((x) => x.id === idDongCuNhat);
                        giaChangedHtml = `<p style="color:#cf1322;font-size:12px;margin-top:4px;">
               Giá sản phẩm đã thay đổi
               <span style="text-decoration:line-through;color:#999;">${formatVND(dongCu.donGia)} đ</span>
               → <strong>${formatVND(sp.donGia)} đ</strong>
             </p>`;
                    }
                    // Dòng cũ: giaChangedHtml giữ nguyên rỗng, không hiện gì cả
                } else {
                    // Chỉ có 1 dòng cho sản phẩm này -> dựa vào cờ giá trôi lấy từ server (polling)
                    const gtd = state.sanPhamGiaThayDoi[sp.id];
                    if (gtd) {
                        laDongCu = true; // giá đã trôi so với lúc thêm -> khoá tăng SL, không hiện thông báo
                    }
                }


                const donGiaStyle = laDongCu ? "color:#cf1322;" : "";
                const disabledPlus = laDongCu ? "disabled style=\"opacity:0.4;cursor:not-allowed;\"" : "";
                const disabledInput = laDongCu ? "disabled" : "";

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
          <td class="text-right font-bold" style="${donGiaStyle}">
  ${formatVND(sp.donGia)} đ
</td>
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

    function renderCustomerCard() {
        const el = document.getElementById("customerCard");
        if (!el) return;
        const kh = getKhachHangDuocChon();
        const loaiHd = getLoaiHoaDonHienTai();

        if (!kh) {
            el.innerHTML = `
      <div>
        <span class="text-muted text-sm" style="display:block;">Tên khách hàng</span>
        <h4 class="mt-1">Khách hàng vãng lai</h4>
      </div>`;
            return;
        }

        let html = `
      <div style="margin-bottom:12px;">
        <span class="text-muted text-sm" style="display:block;">Tên khách hàng</span>
        <h4 class="mt-1">${kh.id === 999 ? "Khách hàng vãng lai" : kh.hoTen}</h4>
      </div>`;
        if (kh.sdt) {
            html += `
      <div style="margin-bottom:12px;">
        <span class="text-muted text-sm" style="display:block;">Số điện thoại</span>
        <div class="mt-1 font-bold">${kh.sdt}</div>
      </div>`;
        }
        if (loaiHd === 1) {
            const nguoiNhan = kh.nguoiNhanGiao || (kh.id === 999 ? "" : kh.hoTen) || "";
            const sdtNhan = kh.sdtGiao || (kh.id === 999 ? "" : kh.sdt) || "";
            const diaChiHienThi = kh.diaChi || "Chưa có địa chỉ giao hàng";

            html += `
      <hr class="divider" style="margin:14px 0;">
      <div class="shipping-header">
        <span class="shipping-title">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="1" y="3" width="15" height="13"></rect>
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon>
            <circle cx="5.5" cy="18.5" r="2.5"></circle>
            <circle cx="18.5" cy="18.5" r="2.5"></circle>
          </svg>
          Thông tin giao hàng
        </span>
        <a class="shipping-edit-link" onclick="moModalDiaChi()">Chỉnh sửa</a>
      </div>
      <div class="shipping-grid">
        <div>
          <span class="text-muted text-sm" style="display:block;">Người nhận</span>
          <div class="mt-1 font-bold">${nguoiNhan || "Chưa có"}</div>
        </div>
        <div>
          <span class="text-muted text-sm" style="display:block;">Số điện thoại</span>
          <div class="mt-1 font-bold">${sdtNhan || "Chưa có"}</div>
        </div>
      </div>
      <div style="margin-top:12px;">
        <span class="text-muted text-sm" style="display:block;">Địa chỉ giao hàng</span>
        <div class="shipping-address-box mt-1">${diaChiHienThi}</div>
      </div>
      <div style="margin-top:12px;padding:8px;background:#f6ffed;border:1px solid #b7eb8f;border-radius:6px;">
        <span class="text-muted text-sm" style="display:block;">Phí vận chuyển</span>
        <div class="mt-1 font-bold" style="color:#52c41a;">${formatVND(getPhiVanChuyen())} đ</div>
      </div>`;
        }

        el.innerHTML = html;
    }
    function renderPaymentBody() {
        // Toggle hình thức
        const loaiHd = getLoaiHoaDonHienTai();
        const toggleEl = document.getElementById("toggleSwitch");
        const labelEl = document.getElementById("loaiHoaDonLabel");
        if (toggleEl) toggleEl.classList.toggle("active", loaiHd === 1);
        if (labelEl) labelEl.textContent = loaiHd === 1 ? "Giao hàng" : "Bán tại quầy";
        const couponArea = document.getElementById("couponArea");
        if (couponArea) {
            const phieu = state.phieuGiamGiaHienTai;
            const buildGoiYHtml = (p) => {
                if (!p || !p.phieuGoiY) return "";
                const gy = p.phieuGoiY;
                return `
            <div style="background:#fffbe6;border:1px solid #ffe58f;border-radius:6px;padding:10px 12px;margin-top:10px;">
              <div style="font-weight:600;color:#d48806;font-size:13px;margin-bottom:6px;">💡 Có mã tốt hơn — mua thêm để được ưu đãi hơn</div>
              <div style="font-size:13px;color:#333;margin-bottom:4px;"><strong>${gy.tenPhieuGiamGia}</strong> (${gy.maPhieuGiamGia})</div>
              <div style="font-size:13px;color:#555;margin-bottom:4px;">
                Mua thêm <span style="color:#cf1322;font-weight:600;">${formatVND(gy.soTienCanMuaThem)} đ</span>
                để được giảm <span style="color:#389e0d;font-weight:600;">${formatVND(gy.soTienGiamNeuDat)} đ</span>
              </div>
              <div style="font-size:12px;color:#888;">Đơn tối thiểu: ${formatVND(gy.giaTriDonToiThieu)} đ</div>
            </div>`;
            };

            if (phieu && phieu.coTheApDung) {
                const isPercent =
                    phieu.loaiPhieuGiamGia === "PHAN_TRAM" ||
                    (phieu.loaiPhieuGiamGia && phieu.loaiPhieuGiamGia.includes("%"));
                let dongGiam;
                if (isPercent) {
                    dongGiam = `Giảm ${phieu.giaTriGiam}% (tối đa ${formatVND(phieu.giaTriGiamToiDa)} đ)`;
                } else {
                    dongGiam = `Giảm ${formatVND(phieu.giaTriGiam)} đ`;
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
          ${buildGoiYHtml(phieu)}`;
            } else if (phieu && phieu.phieuGoiY) {
                // ✅ Chưa có phiếu nào áp dụng được, nhưng có phiếu gợi ý mua thêm để đạt điều kiện
                couponArea.innerHTML = `
          <div style="color:#888;font-size:13px;padding:8px 0;">Chưa có mã giảm giá phù hợp</div>
          ${buildGoiYHtml(phieu)}`;
            } else if (!phieu || getTongTienHienTai() === 0) {
                couponArea.innerHTML = `<div style="color:#888;font-size:13px;padding:8px 0;">Chưa có mã giảm giá phù hợp</div>`;
            } else {
                couponArea.innerHTML = "";
            }
        }
        const summary = document.getElementById("summaryList");
        if (summary) {
            const tienGiam = getTienGiamHienTai();
            const phiVC = getPhiVanChuyen();
            const dongPhiVanChuyen = loaiHd === 1
                ? `<div class="summary-row">
      <span class="font-bold shipping-fee-label">
        Phí vận chuyển <img class="imageVanChuyen" src="/images/image.png" alt="">
      </span>
      <span class="font-bold" style="color:${phiVC > 0 ? "#52c41a" : "#333"};">
        ${phiVC > 0 ? "+ " : ""}${formatVND(phiVC)} đ
      </span>
    </div>`
                : "";
            summary.innerHTML = `
    <div class="summary-row">
      <span class="font-bold">Tiền hàng</span>
      <span class="font-bold">${formatVND(getTongTienHienTai())} đ</span>
    </div>
    ${dongPhiVanChuyen}
    <div class="summary-row">
      <span class="font-bold">Giảm giá</span>
      <span class="font-bold" style="color:${tienGiam > 0 ? "#cf1322" : "#333"};">
        ${tienGiam > 0 ? "- " : ""}${formatVND(tienGiam)} đ
      </span>
    </div>
    <div class="summary-row total-row">
      <span class="font-bold">Tổng phải trả</span>
      <span class="text-danger font-bold text-lg">${formatVND(getTongThanhToan())} đ</span>
    </div>
    <div class="summary-row align-center">
      <span class="font-bold">Khách thanh toán</span>
      <span class="input-wrapper-pay">
        <input type="text" id="soTienKhachDuaInput" class="input-right"
               value="${state.soTienKhachDua ? formatVND(state.soTienKhachDua) : ""}"
               placeholder="0 đ" style="width:150px;text-align:right;"
               oninput="onSoTienKhachDuaInput(this)" />
      </span>
    </div>
    <div class="summary-row">
      <span class="font-bold">Tiền thừa trả khách</span>
      <span class="font-bold" id="tienThuaValue" style="color:${getTienThua() > 0 ? "#16a34a" : "#333"};">
        ${formatVND(getTienThua())} đ
      </span>
    </div>`;
        }
    }

    window.onSoTienKhachDuaInput = function (input) {
        const raw = input.value.replace(/\D/g, "");
        const num = parseInt(raw, 10);
        state.soTienKhachDua = isNaN(num) ? 0 : num;

        input.value = state.soTienKhachDua ? formatVND(state.soTienKhachDua) : "";
        input.setSelectionRange(input.value.length, input.value.length);

        const tienThuaEl = document.getElementById("tienThuaValue");
        if (tienThuaEl) {
            tienThuaEl.style.color = getTienThua() > 0 ? "#16a34a" : "#333";
            tienThuaEl.textContent = formatVND(getTienThua()) + " đ";
        }
    };
    window.onGhiChuInput = function (value) {
        state.ghiChu = value;
    };
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
                .map((item, index) => {
                    const coGiam = item.giaGoc && Number(item.giaGoc) > Number(item.gia);
                    return `
        <tr>
          <td class="text-center">${index + 1 + state.page * state.size}</td>
          <td>${item.ma}</td>
          <td><div class="modal-img-wrapper"><img src="${item.anh}" alt="product"></div></td>
          <td>${item.ten}</td>
          <td>${item.mauSac}</td>
          <td>${item.trongLuong}</td>
          <td class="text-right">${item.soLuongTon}</td>
         <td class="text-right font-bold" style="${coGiam ? "color:#cf1322;" : ""}">
    ${coGiam ? `<div style="text-decoration:line-through;color:#999;font-size:11px;">${formatVND(item.giaGoc)} đ</div>` : ""}
    ${formatVND(item.gia)} đ
</td>
          <td class="text-center"><button class="btn-select-product" onclick='themSanPhamVaoHoaDon(${JSON.stringify(item)})'>Chọn</button></td>
        </tr>`;
                })
                .join("");
        }

        const countInfo = document.getElementById("productCountInfo");
        if (countInfo) countInfo.textContent = `Tổng:${state.danhSach.length} sản phẩm `;
        const pageInfo = document.getElementById("productPageInfo");
        if (pageInfo) pageInfo.textContent = `Trang ${state.page + 1}/${state.totalPages || 1}`;
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
        state.trangThai = 1;   // ✅ reset về "Còn hàng" thay vì "Tất cả"
        state.giaMin = 0;
        state.giaMax = state.maxPrice;
        state.page = 0;
        document.getElementById("keywordInput").value = "";
        document.getElementById("mauSacSelect").value = "";
        document.getElementById("trongLuongSelect").value = "";
        document.querySelectorAll('input[name="trangThai"]').forEach((r) => (r.checked = r.value === "1"));
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
          <td>${kh.email || ""}</td>
          <td>${kh.diaChi}</td>
          <td class="text-center">
            <button style="background-color:#222;color:#fff;border:none;padding:6px 12px;border-radius:4px;cursor:pointer;font-size:13px;"
                    onclick='chonKhachHang(${JSON.stringify(kh)})'>Chọn</button>
          </td>
        </tr>`
                )
                .join("");
        }
        const countInfo = document.getElementById("customerCountInfo");
        if (countInfo) countInfo.textContent = `Tổng:${state.dsKhachHang.length} khách hàng`;
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
    function renderAddressModalList() {
        const el = document.getElementById("addressModalList");
        if (!el) return;
        const listHtml = state.dsDiaChi.length === 0
            ? `<div class="text-center text-muted" style="padding:20px;">Khách hàng chưa có địa chỉ nào</div>`
            : state.dsDiaChi.map((dc) => {
                const style = dc.macDinh ? "border-color:#52c41a;background:#f6ffed;" : "";
                return `
        <div style="border:1px solid #e8e8e8;border-radius:8px;padding:14px 16px;margin-bottom:12px;${style}">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:12px;">
            <div style="flex:1;">
              <div style="font-weight:600;font-size:14px;margin-bottom:4px;">
                ${dc.nguoiNhan}
                <span style="font-weight:normal;color:#888;margin-left:8px;">${dc.sdt}</span>
                ${dc.macDinh ? `<span style="background:#52c41a;color:#fff;font-size:11px;padding:1px 7px;border-radius:10px;margin-left:8px;font-weight:normal;">Mặc định</span>` : ""}
              </div>
              <div style="color:#555;font-size:13px;">${dc.diaChiDayDu}</div>
            </div>
            <div style="display:flex;flex-direction:column;gap:6px;flex-shrink:0;">
              <button onclick='chonDiaChi(${JSON.stringify(dc)})'
                      style="background:#222;color:#fff;border:none;padding:6px 14px;border-radius:4px;cursor:pointer;font-size:13px;">Chọn</button>
              <button onclick='moFormSuaDiaChi(${JSON.stringify(dc)})'
                      style="background:#fff;color:#1890ff;border:1px solid #1890ff;padding:6px 14px;border-radius:4px;cursor:pointer;font-size:13px;">Sửa</button>
            </div>
          </div>
        </div>`;
            }).join("");

        el.innerHTML = `
      <div id="addressListWrap">${listHtml}</div>
      <button class="btn-outline-modal" style="width:100%;margin-top:8px;" onclick="moFormThemDiaChi()">+ Thêm địa chỉ mới</button>
      <div id="addressFormWrap" style="display:none;"></div>`;
    }
    function renderDiaChiForm(mode, dc) {
        dc = dc || {};
        return `
      <div class="address-form">
        <h4 style="margin:0 0 12px 0;">${mode === "edit" ? "Sửa địa chỉ" : "Thêm địa chỉ mới"}</h4>
        <div class="filter-row">
          <div class="filter-group flex-1">
            <label>Người nhận <span>*</span></label>
            <input id="dcNguoiNhan" type="text" class="modal-input" value="${dc.nguoiNhan || ""}"/>
          </div>
          <div class="filter-group flex-1">
            <label>Số điện thoại <span>*</span></label>
            <input id="dcSdt" type="text" class="modal-input" value="${dc.sdt || ""}"/>
          </div>
        </div>
        <div class="filter-row" style="margin-top:12px;">
          <div class="filter-group flex-1">
            <label>Tỉnh/Thành phố <span>*</span></label>
            <select id="dcTinhThanhSelect" class="modal-select">
              <option value="">-- Đang tải... --</option>
            </select>
          </div>
          <div class="filter-group flex-1">
            <label>Quận/Huyện <span>*</span></label>
            <select id="dcQuanHuyenSelect" class="modal-select" disabled>
              <option value="">-- Chọn tỉnh/thành trước --</option>
            </select>
          </div>
          <div class="filter-group flex-1">
            <label>Phường/Xã/Thị trấn <span>*</span></label>
            <select id="dcPhuongXaSelect" class="modal-select" disabled>
              <option value="">-- Chọn quận/huyện trước --</option>
            </select>
          </div>
        </div>
        <div class="filter-group" style="margin-top:12px;">
          <label>Địa chỉ chi tiết (số nhà, tên đường...) <span>*</span></label>
          <input id="dcDiaChiChiTiet" type="text" class="modal-input" value="${dc.diaChiChiTiet || ""}"/>
        </div>
        <label class="radio-label" style="margin-top:12px;">
          <input type="checkbox" id="dcMacDinh" ${dc.macDinh ? "checked" : ""}/> Đặt làm địa chỉ mặc định
        </label>
        <div style="display:flex;gap:12px;margin-top:16px;">
          <button class="btn-primary" onclick="luuDiaChi('${mode}', ${dc.id ?? "null"})">Lưu</button>
          <button class="btn-outline-modal" onclick="dongFormDiaChi()">Hủy</button>
        </div>
      </div>`;
    }
    async function khoiTaoDropdownDiaChi(dc) {
        dc = dc || {};
        let tenXaCu = "", tenHuyenCu = "";
        if (dc.phuongXa) {
            const parts = dc.phuongXa.split(",").map((s) => s.trim());
            tenXaCu = parts[0] || "";
            tenHuyenCu = parts[1] || "";
        }
        const tinhSelect = document.getElementById("dcTinhThanhSelect");
        const huyenSelect = document.getElementById("dcQuanHuyenSelect");
        const xaSelect = document.getElementById("dcPhuongXaSelect");
        try {
            const dsTinh = await layDanhSachTinhThanh();
            tinhSelect.innerHTML = `<option value="">-- Chọn tỉnh/thành --</option>` +
                dsTinh.map((t) => `<option value="${t.code}" data-name="${t.name}" ${t.name === dc.tinhThanh ? "selected" : ""}>${t.name}</option>`).join("");
            tinhSelect.onchange = async () => {
                const maTinh = tinhSelect.value;
                huyenSelect.innerHTML = `<option value="">-- Đang tải... --</option>`;
                xaSelect.innerHTML = `<option value="">-- Chọn quận/huyện trước --</option>`;
                huyenSelect.disabled = true;
                xaSelect.disabled = true;
                if (!maTinh) return;
                try {
                    const dsHuyen = await layDanhSachQuanHuyen(maTinh);
                    huyenSelect.innerHTML = `<option value="">-- Chọn quận/huyện --</option>` +
                        dsHuyen.map((h) => `<option value="${h.code}" data-name="${h.name}">${h.name}</option>`).join("");
                    huyenSelect.disabled = false;
                } catch (e) {
                    showThongBao("Lỗi tải quận/huyện!", "error");
                }
            };
            huyenSelect.onchange = async () => {
                const maHuyen = huyenSelect.value;
                xaSelect.innerHTML = `<option value="">-- Đang tải... --</option>`;
                xaSelect.disabled = true;
                if (!maHuyen) return;
                try {
                    const dsXa = await layDanhSachPhuongXa(maHuyen);
                    xaSelect.innerHTML = `<option value="">-- Chọn phường/xã --</option>` +
                        dsXa.map((x) => `<option value="${x.code}" data-name="${x.name}">${x.name}</option>`).join("");
                    xaSelect.disabled = false;
                } catch (e) {
                    showThongBao("Lỗi tải phường/xã!", "error");
                }
            };
            if (dc.tinhThanh && tinhSelect.value) {
                const dsHuyen = await layDanhSachQuanHuyen(tinhSelect.value);
                huyenSelect.innerHTML = `<option value="">-- Chọn quận/huyện --</option>` +
                    dsHuyen.map((h) => `<option value="${h.code}" data-name="${h.name}" ${h.name === tenHuyenCu ? "selected" : ""}>${h.name}</option>`).join("");
                huyenSelect.disabled = false;
                if (tenHuyenCu && huyenSelect.value) {
                    const dsXa = await layDanhSachPhuongXa(huyenSelect.value);
                    xaSelect.innerHTML = `<option value="">-- Chọn phường/xã --</option>` +
                        dsXa.map((x) => `<option value="${x.code}" data-name="${x.name}" ${x.name === tenXaCu ? "selected" : ""}>${x.name}</option>`).join("");
                    xaSelect.disabled = false;
                }
            }
        } catch (e) {
            showThongBao("Lỗi tải danh sách tỉnh/thành!", "error");
        }
    }
    window.moFormThemDiaChi = function () {
        const wrap = document.getElementById("addressFormWrap");
        wrap.style.display = "block";
        wrap.innerHTML = renderDiaChiForm("add");
        khoiTaoDropdownDiaChi();   // ✅ thêm dòng này — gọi API tải tỉnh/huyện/xã
    };
    window.moFormSuaDiaChi = function (dc) {
        const wrap = document.getElementById("addressFormWrap");
        wrap.style.display = "block";
        wrap.innerHTML = renderDiaChiForm("edit", dc);
        khoiTaoDropdownDiaChi(dc);   // ✅ thêm dòng này
    };
    window.dongFormDiaChi = function () {
        const wrap = document.getElementById("addressFormWrap");
        wrap.style.display = "none";
        wrap.innerHTML = "";
    };
    window.luuDiaChi = async function (mode, id) {
        const tinhSelect = document.getElementById("dcTinhThanhSelect");
        const huyenSelect = document.getElementById("dcQuanHuyenSelect");
        const xaSelect = document.getElementById("dcPhuongXaSelect");
        const tenTinh = tinhSelect.options[tinhSelect.selectedIndex]?.dataset.name || "";
        const tenHuyen = huyenSelect.options[huyenSelect.selectedIndex]?.dataset.name || "";
        const tenXa = xaSelect.options[xaSelect.selectedIndex]?.dataset.name || "";
        if (!tenTinh || !tenHuyen || !tenXa) {
            return showThongBao("Vui lòng chọn đủ Tỉnh/Thành, Quận/Huyện, Phường/Xã!", "error");
        }
        const payload = {
            nguoiNhan: document.getElementById("dcNguoiNhan").value.trim(),
            sdt: document.getElementById("dcSdt").value.trim(),
            tinhThanh: tenTinh,
            phuongXa: `${tenXa}, ${tenHuyen}`,   // ✅ ghép chung giống trang khách hàng, không lưu cột riêng
            diaChiChiTiet: document.getElementById("dcDiaChiChiTiet").value.trim(),
            macDinh: document.getElementById("dcMacDinh").checked,
        };
        if (!payload.nguoiNhan || !payload.sdt || !payload.diaChiChiTiet) {
            return showThongBao("Vui lòng nhập đủ người nhận, SĐT và địa chỉ chi tiết!", "error");
        }
        const kh = getKhachHangDuocChon();
        try {
            if (mode === "edit") {
                state.dsDiaChi = await suaDiaChiKhachHang(id, payload);
            } else {
                state.dsDiaChi = await themDiaChiKhachHang(kh.id, payload);
            }
            renderAddressModalList();
            showThongBao("Đã lưu địa chỉ!", "success");
        } catch (e) {
            showThongBao(e.message || "Lỗi lưu địa chỉ!", "error");
        }
    };
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
        loadKhachHang();   // ✅ luôn tải lại để đồng bộ địa chỉ/thông tin mới nhất
    };
    function closeCustomerModal() {
        closeModal("customerModal");
    }
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
        batDauPollingSanPhamNgungHoatDong();   // ✅ thêm dòng này
    }
    document.addEventListener("DOMContentLoaded", init);
})();