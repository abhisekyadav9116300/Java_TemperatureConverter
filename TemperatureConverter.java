import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class TemperatureConverter extends JFrame {

    // ─── Color Palette ───────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(18, 18, 22);
    private static final Color CARD_BG       = new Color(28, 28, 36);
    private static final Color CARD_HOVER    = new Color(38, 38, 50);
    private static final Color ACCENT        = new Color(99, 102, 241);   // Indigo
    private static final Color ACCENT_LIGHT  = new Color(129, 132, 255);
    private static final Color ACCENT_BG     = new Color(99, 102, 241, 30);
    private static final Color TEXT_PRIMARY  = new Color(240, 240, 248);
    private static final Color TEXT_MUTED    = new Color(140, 140, 160);
    private static final Color BORDER_COLOR  = new Color(55, 55, 70);
    private static final Color INPUT_BG      = new Color(22, 22, 30);
    private static final Color SUCCESS_COLOR = new Color(52, 211, 153);

    // ─── Unit Constants ───────────────────────────────────────────────────────
    private static final String[] UNIT_KEYS  = {"C", "F", "K", "R"};
    private static final String[] UNIT_SYM   = {"°C", "°F", "K", "°R"};
    private static final String[] UNIT_NAMES = {"Celsius", "Fahrenheit", "Kelvin", "Rankine"};

    // ─── Formulas map [from][to] ──────────────────────────────────────────────
    private static final String[][] FORMULAS = {
        // C→C,               C→F,                    C→K,                 C→R
        {"T(°C) = T(°C)",    "T(°F) = T(°C)×9/5+32", "T(K) = T(°C)+273.15", "T(°R) = (T(°C)+273.15)×9/5"},
        // F→C,                    F→F,              F→K,                          F→R
        {"T(°C) = (T(°F)−32)×5/9", "T(°F) = T(°F)", "T(K) = (T(°F)−32)×5/9+273.15", "T(°R) = T(°F)+459.67"},
        // K→C,                 K→F,                      K→K,        K→R
        {"T(°C) = T(K)−273.15", "T(°F) = (T(K)−273.15)×9/5+32", "T(K) = T(K)", "T(°R) = T(K)×9/5"},
        // R→C,                          R→F,                  R→K,              R→R
        {"T(°C) = (T(°R)−491.67)×5/9", "T(°F) = T(°R)−459.67", "T(K) = T(°R)×5/9", "T(°R) = T(°R)"}
    };

    // ─── UI State ─────────────────────────────────────────────────────────────
    private JTextField inputField;
    private JComboBox<String> fromUnitCombo;
    private ResultCard[] resultCards;
    private JLabel formulaLabel;
    private int activeCardIndex = 0;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public TemperatureConverter() {
        setTitle("Temperature Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBackground(BG_DARK);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildInputPanel(), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBackground(BG_DARK);
        bottom.add(buildResultsGrid(),  BorderLayout.NORTH);
        bottom.add(buildFormulaBox(),   BorderLayout.CENTER);
        bottom.add(buildRefTable(),     BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);
        pack();
        setMinimumSize(new Dimension(540, 600));
        setLocationRelativeTo(null);
        convert();
    }

    // ─── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Temperature Converter");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Celsius · Fahrenheit · Kelvin · Rankine");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 3));
        texts.setBackground(BG_DARK);
        texts.add(title);
        texts.add(sub);
        p.add(texts, BorderLayout.WEST);
        return p;
    }

    // ─── Input Row ────────────────────────────────────────────────────────────
    private JPanel buildInputPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        GridBagConstraints gbc = new GridBagConstraints();

        // Value label
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        JLabel valLabel = styledLabel("Value", 11, TEXT_MUTED);
        p.add(valLabel, gbc);

        // From unit label
        gbc.gridx = 2;
        JLabel fromLabel = styledLabel("From unit", 11, TEXT_MUTED);
        p.add(fromLabel, gbc);

        // Input field
        inputField = new JTextField("100", 10);
        styleTextField(inputField);
        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { convert(); }
        });
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.insets = new Insets(0, 0, 0, 10);
        p.add(inputField, gbc);

        // Arrow
        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        arrow.setForeground(TEXT_MUTED);
        gbc.gridx = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 10);
        p.add(arrow, gbc);

        // Combo box
        String[] options = {"Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)", "Rankine (°R)"};
        fromUnitCombo = new JComboBox<>(options);
        styleComboBox(fromUnitCombo);
        fromUnitCombo.addActionListener(e -> convert());
        gbc.gridx = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(fromUnitCombo, gbc);

        return p;
    }

    // ─── 4 Result Cards ───────────────────────────────────────────────────────
    private JPanel buildResultsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 10, 0));
        grid.setBackground(BG_DARK);
        grid.setBorder(new EmptyBorder(0, 0, 14, 0));

        resultCards = new ResultCard[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            resultCards[i] = new ResultCard(UNIT_SYM[i], UNIT_NAMES[i], i == 0);
            resultCards[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    setActiveCard(idx);
                }
                @Override public void mouseEntered(MouseEvent e) {
                    if (idx != activeCardIndex) {
                        resultCards[idx].setHovered(true);
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    resultCards[idx].setHovered(false);
                }
            });
            grid.add(resultCards[i]);
        }
        return grid;
    }

    // ─── Formula Box ─────────────────────────────────────────────────────────
    private JPanel buildFormulaBox() {
        JPanel box = new RoundedPanel(10, CARD_BG, BORDER_COLOR);
        box.setLayout(new BorderLayout(0, 6));
        box.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = styledLabel("Formula used", 11, TEXT_MUTED);
        formulaLabel = new JLabel("—");
        formulaLabel.setFont(new Font("Consolas", Font.PLAIN, 13));
        formulaLabel.setForeground(ACCENT_LIGHT);

        box.add(title, BorderLayout.NORTH);
        box.add(formulaLabel, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(0, 0, 14, 0));
        wrapper.add(box);
        return wrapper;
    }

    // ─── Reference Table ─────────────────────────────────────────────────────
    private JPanel buildRefTable() {
        JPanel outer = new RoundedPanel(10, CARD_BG, BORDER_COLOR);
        outer.setLayout(new BorderLayout(0, 10));
        outer.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = styledLabel("Reference points", 11, TEXT_MUTED);
        outer.add(title, BorderLayout.NORTH);

        String[][] refs = {
            {"Water boils",   "100",   "212",    "373.15"},
            {"Body temp",     "37",    "98.6",   "310.15"},
            {"Room temp",     "22",    "71.6",   "295.15"},
            {"Water freezes", "0",     "32",     "273.15"},
            {"Dry ice",       "−78.5", "−109.3", "194.65"},
            {"Absolute zero", "−273.15","−459.67","0"},
        };

        String[] headers = {"Landmark", "°C", "°F", "K"};
        JPanel tablePanel = new JPanel(new GridLayout(refs.length + 1, 4, 0, 0));
        tablePanel.setBackground(CARD_BG);

        // Header row
        for (String h : headers) {
            JLabel lbl = styledLabel(h, 11, TEXT_MUTED);
            lbl.setBorder(new EmptyBorder(0, 0, 6, 8));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            tablePanel.add(lbl);
        }

        // Data rows
        for (String[] row : refs) {
            for (int c = 0; c < 4; c++) {
                JLabel cell = styledLabel(row[c], 12, c == 0 ? TEXT_PRIMARY : SUCCESS_COLOR);
                cell.setBorder(new EmptyBorder(4, 0, 4, 8));
                if (c != 0) {
                    cell.setFont(new Font("Consolas", Font.PLAIN, 12));
                }
                tablePanel.add(cell);
            }
        }

        outer.add(tablePanel, BorderLayout.CENTER);
        return outer;
    }

    // ─── Core Conversion Logic ────────────────────────────────────────────────
    private double toCelsius(double val, int fromIdx) {
        switch (fromIdx) {
            case 0: return val;                          // C → C
            case 1: return (val - 32.0) * 5.0 / 9.0;   // F → C
            case 2: return val - 273.15;                 // K → C
            case 3: return (val - 491.67) * 5.0 / 9.0; // R → C
            default: return val;
        }
    }

    private double fromCelsius(double celsius, int toIdx) {
        switch (toIdx) {
            case 0: return celsius;                          // C → C
            case 1: return celsius * 9.0 / 5.0 + 32.0;     // C → F
            case 2: return celsius + 273.15;                 // C → K
            case 3: return (celsius + 273.15) * 9.0 / 5.0; // C → R
            default: return celsius;
        }
    }

    private void convert() {
        int fromIdx = fromUnitCombo.getSelectedIndex();
        String text = inputField.getText().trim();

        if (text.isEmpty() || text.equals("-") || text.equals(".")) {
            for (ResultCard card : resultCards) { card.setValue("—"); }
            formulaLabel.setText("Enter a value to see the formula");
            return;
        }

        try {
            double input = Double.parseDouble(text);
            double celsius = toCelsius(input, fromIdx);

            for (int i = 0; i < 4; i++) {
                double result = fromCelsius(celsius, i);
                resultCards[i].setValue(formatResult(result));
            }

            formulaLabel.setText(FORMULAS[fromIdx][activeCardIndex]);

        } catch (NumberFormatException ex) {
            for (ResultCard card : resultCards) { card.setValue("Invalid"); }
            formulaLabel.setText("Enter a valid number");
        }
    }

    private String formatResult(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return "—";
        }
        if (Math.abs(val) >= 1_000_000 || (Math.abs(val) < 0.0001 && val != 0)) {
            return String.format("%.4e", val);
        }
        String s = String.format("%.4f", val);
        // trim trailing zeros but keep at least 2 decimal places
        s = s.replaceAll("(\\.[0-9]{2})0+$", "$1");
        return s;
    }

    private void setActiveCard(int idx) {
        resultCards[activeCardIndex].setActive(false);
        activeCardIndex = idx;
        resultCards[activeCardIndex].setActive(true);
        formulaLabel.setText(FORMULAS[fromUnitCombo.getSelectedIndex()][activeCardIndex]);
    }

    // ─── Styling Helpers ──────────────────────────────────────────────────────
    private JLabel styledLabel(String text, int size, Color color) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, size));
        lbl.setForeground(color);
        return lbl;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_LIGHT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(new RoundedBorder(8, BORDER_COLOR));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT : INPUT_BG);
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
    }

    // ─── Inner Class: Result Card ─────────────────────────────────────────────
    class ResultCard extends JPanel {
        private JLabel unitLabel;
        private JLabel valueLabel;
        private JLabel nameLabel;
        private boolean active;
        private boolean hovered;

        ResultCard(String unit, String name, boolean active) {
            this.active = active;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(14, 14, 14, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            unitLabel = new JLabel(unit);
            unitLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            unitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            valueLabel = new JLabel("—");
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(unitLabel);
            add(Box.createVerticalStrut(4));
            add(valueLabel);
            add(Box.createVerticalStrut(2));
            add(nameLabel);

            refreshColors();
        }

        void setValue(String v) {
            valueLabel.setText(v);
        }

        void setActive(boolean a) {
            this.active = a;
            refreshColors();
            repaint();
        }

        void setHovered(boolean h) {
            this.hovered = h;
            refreshColors();
            repaint();
        }

        private void refreshColors() {
            if (active) {
                setBackground(ACCENT_BG);
                unitLabel.setForeground(ACCENT_LIGHT);
                valueLabel.setForeground(ACCENT_LIGHT);
                nameLabel.setForeground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 180));
            } else if (hovered) {
                setBackground(CARD_HOVER);
                unitLabel.setForeground(TEXT_MUTED);
                valueLabel.setForeground(TEXT_PRIMARY);
                nameLabel.setForeground(TEXT_MUTED);
            } else {
                setBackground(CARD_BG);
                unitLabel.setForeground(TEXT_MUTED);
                valueLabel.setForeground(TEXT_PRIMARY);
                nameLabel.setForeground(TEXT_MUTED);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(active ? ACCENT : BORDER_COLOR);
            g2.setStroke(new BasicStroke(active ? 1.5f : 0.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
        }

        @Override
        public boolean isOpaque() { return false; }
    }

    // ─── Inner Class: Rounded Panel ───────────────────────────────────────────
    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        private final Color border;

        RoundedPanel(int radius, Color bg, Color border) {
            this.radius = radius;
            this.bg = bg;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(0.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
            g2.dispose();
        }
    }

    // ─── Inner Class: Rounded Border ─────────────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(4, 4, 4, 4); }
    }

    // ─── Main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            TemperatureConverter app = new TemperatureConverter();
            app.setVisible(true);
        });
    }
}
