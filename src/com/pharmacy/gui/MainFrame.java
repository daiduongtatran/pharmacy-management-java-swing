package com.pharmacy.gui;

import com.pharmacy.service.ThongKeService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    // --- MÀU SẮC & FONT ---
    private final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private final Color COLOR_SECONDARY = new Color(20, 40, 80);
    private final Color COLOR_BG = new Color(240, 248, 255);
    private final Color COLOR_WHITE = Color.WHITE;
    private final Color COLOR_TEXT = new Color(50, 50, 50);
    private final Font FONT_MAIN = new Font("Arial", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);
    private final Font FONT_HEADER = new Font("Arial", Font.BOLD, 18);

    // --- QUẢN LÝ CHUYỂN MÀN HÌNH ---
    private CardLayout cardLayout;
    private JPanel pnlContentArea;

    // Danh sách để quản lý tất cả các nút menu nhằm mục đích highlight
    private List<JButton> menuButtons = new ArrayList<>();

    // --- CÁC LABEL & TABLE (DASHBOARD) ---
    private JLabel lblDoanhThuValue;
    private JLabel lblDonHangValue;
    private JTable tblThuoc;
    private DefaultTableModel tableModel;

    private ThongKeService tkService = new ThongKeService();

    public MainFrame() {
        initUI();
        loadDataFromDB();
    }

    private void initUI() {
        setTitle("Quản Lý Nhà Thuốc");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Sidebar bên trái
        add(createSidebar(), BorderLayout.WEST);

        // 2. Vùng nội dung chính (Sử dụng CardLayout)
        cardLayout = new CardLayout();
        pnlContentArea = new JPanel(cardLayout);

        // Thêm các màn hình con vào CardLayout
        pnlContentArea.add(createDashboardPanel(), "TongQuan");
        pnlContentArea.add(new ProductFrame(), "SanPham");

        add(pnlContentArea, BorderLayout.CENTER);
    }

    // --- SIDEBAR ---
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(COLOR_SECONDARY);

        JLabel lblLogo = new JLabel("PHARMACY PRO", JLabel.CENTER);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 22));
        lblLogo.setForeground(new Color(100, 181, 246));
        lblLogo.setBorder(new EmptyBorder(30, 0, 30, 0));
        sidebar.add(lblLogo, BorderLayout.NORTH);

        JPanel pnlMenu = new JPanel(new GridLayout(8, 1, 0, 5));
        pnlMenu.setBackground(COLOR_SECONDARY);
        pnlMenu.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tạo các nút menu và thêm vào danh sách quản lý
        JButton btnTongQuan = createMenuItem("🏠   Tổng quan", "TongQuan", true);
        JButton btnSanPham = createMenuItem("💊   Sản phẩm & Kho", "SanPham", false);

        pnlMenu.add(btnTongQuan);
        pnlMenu.add(btnSanPham);
        pnlMenu.add(createMenuItem("🛒   Bán hàng (POS)", "POS", false));
        pnlMenu.add(createMenuItem("👥   Khách hàng", "KhachHang", false));
        pnlMenu.add(createMenuItem("🚚   Nhập hàng", "NhapHang", false));
        pnlMenu.add(createMenuItem("📊   Báo cáo", "BaoCao", false));
        pnlMenu.add(createMenuItem("⚙   Cấu hình", "CauHinh", false));

        sidebar.add(pnlMenu, BorderLayout.CENTER);
        return sidebar;
    }

    private JButton createMenuItem(String text, String cardName, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(isActive ? FONT_BOLD : FONT_MAIN);
        btn.setForeground(COLOR_WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 20, 12, 10));
        btn.setBackground(isActive ? COLOR_PRIMARY : COLOR_SECONDARY);

        menuButtons.add(btn); // Thêm vào danh sách để quản lý tập trung

        // Sự kiện Click chuyển trang và Highlight
        btn.addActionListener(e -> {
            cardLayout.show(pnlContentArea, cardName);
            updateMenuHighlight(btn);
        });

        // Hiệu ứng Hover di chuột
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY.brighter());
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Nếu nút đang được active (font đậm) thì giữ màu primary, ngược lại về màu tối
                if (btn.getFont().equals(FONT_BOLD)) {
                    btn.setBackground(COLOR_PRIMARY);
                } else {
                    btn.setBackground(COLOR_SECONDARY);
                }
            }
        });

        return btn;
    }

    // Hàm cập nhật trạng thái in đậm cho nút được chọn
    private void updateMenuHighlight(JButton selectedButton) {
        for (JButton btn : menuButtons) {
            if (btn == selectedButton) {
                btn.setFont(FONT_BOLD);
                btn.setBackground(COLOR_PRIMARY);
            } else {
                btn.setFont(FONT_MAIN);
                btn.setBackground(COLOR_SECONDARY);
            }
        }
    }

    // --- PANEL TỔNG QUAN (DASHBOARD) ---
    private JPanel createDashboardPanel() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(COLOR_BG);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        pnlHeader.setPreferredSize(new Dimension(0, 60));

        JLabel lblTitle = new JLabel("TỔNG QUAN");
        lblTitle.setFont(FONT_HEADER);
        lblTitle.setForeground(COLOR_PRIMARY);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Body
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(COLOR_BG);
        pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cards
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlCards.setBackground(COLOR_BG);
        pnlCards.setMaximumSize(new Dimension(2000, 120));

        lblDoanhThuValue = new JLabel("0 đ");
        lblDonHangValue = new JLabel("0 Đơn");

        pnlCards.add(createCard("DOANH THU", lblDoanhThuValue, COLOR_PRIMARY, "💰"));
        pnlCards.add(createCard("ĐƠN HÀNG", lblDonHangValue, new Color(41, 128, 185), "🧾"));
        pnlCards.add(createCard("SẮP HẾT HÀNG", new JLabel("12 Sản phẩm"), new Color(243, 156, 18), "⚠️"));
        pnlCards.add(createCard("HẾT HẠN", new JLabel("05 Sản phẩm"), new Color(231, 76, 60), "❌"));

        // Table
        JLabel lblTableTitle = new JLabel("Danh sách thuốc cần xử lý gấp");
        lblTableTitle.setFont(FONT_BOLD);
        lblTableTitle.setBorder(new EmptyBorder(20, 0, 10, 0));

        pnlBody.add(pnlCards);
        pnlBody.add(lblTableTitle);
        pnlBody.add(createStyledTable());

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);

        return pnlMain;
    }

    private JPanel createCard(String title, JLabel lblValue, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, color));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Arial", Font.PLAIN, 12));
        lblT.setForeground(Color.GRAY);

        lblValue.setFont(new Font("Arial", Font.BOLD, 20));
        lblValue.setForeground(COLOR_TEXT);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        lblIcon.setBorder(new EmptyBorder(0, 10, 0, 10));

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setBackground(COLOR_WHITE);
        pnlInfo.add(lblT);
        pnlInfo.add(lblValue);
        pnlInfo.setBorder(new EmptyBorder(15, 15, 15, 15));

        card.add(pnlInfo, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);

        return card;
    }

    private JScrollPane createStyledTable() {
        String[] columns = {"Mã Thuốc", "Tên Thuốc", "Hoạt Chất", "Đơn Vị", "Tồn Kho", "Hạn Dùng", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0);
        tblThuoc = new JTable(tableModel);
        tblThuoc.setRowHeight(30);
        tblThuoc.setFont(FONT_MAIN);

        JTableHeader header = tblThuoc.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(COLOR_WHITE);
        header.setPreferredSize(new Dimension(0, 35));

        return new JScrollPane(tblThuoc);
    }

    private void loadDataFromDB() {
        if (tkService == null) tkService = new ThongKeService();
        try {
            DecimalFormat df = new DecimalFormat("#,###");

            // 1. Lấy dữ liệu từ Service
            double doanhThu = tkService.getTongDoanhThu();
            int soDonHang = tkService.getSoDonHang();

            // 2. Cập nhật lên giao diện Dashboard
            lblDoanhThuValue.setText(df.format(doanhThu) + " đ");
            lblDonHangValue.setText(soDonHang + " Đơn");

            // 3. Load bảng thuốc (Nếu bạn muốn bảng này hiện danh sách thuốc, hãy dùng SQL bảng SanPham)
            tableModel.setRowCount(0);
            // Lưu ý: Nếu muốn bảng hiện thuốc, SQL trong getDanhSachThuoc() phải trỏ vào bảng SanPham
            ResultSet rs = tkService.getDanhSachThuoc();
            if (rs != null) {
                while (rs.next()) {
                    // Thêm hàng vào bảng tableModel
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi nạp dữ liệu Dashboard: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}