/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package guiapt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JViewport;

/**
 *
 * @author andronikos
 */
public class main extends javax.swing.JFrame {

    private javax.swing.Timer listingTimer;
    int action;
    volatile boolean cancelListing = false;
    Process currentProcess = null;
    Thread currentThread = null;
    private boolean dpkgProgressStarted = false;
    private static final Pattern PMSTATUS = Pattern.compile("pmstatus:[^:]+:([0-9.]+):");
    private int lastPercent = -1;
    String log = "";
    private TableModelListener cListener;
    private TableModelListener cListenerIn;
    Set<String> toinstall = new LinkedHashSet<>();
    Set<String> toremove = new LinkedHashSet<>();
    DefaultTableModel allmodel;

    static boolean legacy = false;
    int previ = 1;
    String bashCommand;

    /**
     * Creates new form main
     */
    public void make_motif_light() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

            ColorUIResource lightGray = new ColorUIResource(new Color(232, 232, 232)); // #C0C0C0
            ColorUIResource white = new ColorUIResource(Color.WHITE);

            // Set backgrounds to light gray
            UIManager.put("control", lightGray);
            UIManager.put("info", lightGray);
            UIManager.put("nimbusBase", lightGray);
            UIManager.put("nimbusBlueGrey", lightGray);
            UIManager.put("controlHighlight", new ColorUIResource(224, 224, 224));
            UIManager.put("controlShadow", new ColorUIResource(128, 128, 128));
            UIManager.put("controlDkShadow", new ColorUIResource(64, 64, 64));
            UIManager.put("window", lightGray);
            UIManager.put("background", lightGray);
            UIManager.put("Button.background", lightGray);
            UIManager.put("Panel.background", lightGray);
            UIManager.put("Menu.background", lightGray);
            UIManager.put("MenuBar.background", lightGray);
            UIManager.put("ToolTip.background", lightGray);
            UIManager.put("ScrollPane.background", lightGray);
            UIManager.put("Viewport.background", lightGray);
            UIManager.put("TabbedPane.background", lightGray);
            UIManager.put("OptionPane.background", lightGray);
            UIManager.put("ComboBox.background", lightGray);
            UIManager.put("CheckBox.background", lightGray);
            UIManager.put("RadioButton.background", lightGray);
            UIManager.put("ToggleButton.background", lightGray);
            UIManager.put("Label.background", lightGray);
            UIManager.put("List.background", lightGray);
            UIManager.put("Table.background", lightGray);
            UIManager.put("Tree.background", lightGray);
            UIManager.put("Separator.background", lightGray);

            // Set text component backgrounds to white
            UIManager.put("TextField.background", white);
            UIManager.put("TextArea.background", white);
            UIManager.put("EditorPane.background", white);
            UIManager.put("FormattedTextField.background", white);
            UIManager.put("PasswordField.background", white);
            this.getContentPane().setBackground(new Color(242, 242, 242));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to set Motif Look and Feel.");
        }
    }

    void disable_cde_border() {
        for (Component c : this.getContentPane().getComponents()) {

            if (c instanceof JButton
                    || c instanceof JToggleButton
                    || c instanceof JCheckBox
                    || c instanceof JRadioButton) {

                c.setFocusable(false);
                ((AbstractButton) c).setFocusPainted(false);
            }

        }
    }

    void fixgtkfont() {
        if (UIManager.getLookAndFeel().getName().toLowerCase().contains("gtk")) {
            Font fixedFont = new Font("Inter", Font.PLAIN, 13).deriveFont(13.7f);
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            Enumeration<Object> keys = defaults.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = defaults.get(key);
                if (key.toString().endsWith(".font") && value instanceof Font) {
                    UIManager.put(key, fixedFont);
                }
            }
        }
    }
    DefaultTableModel inmodel = new DefaultTableModel(
            new Object[]{"Icon", "Package", "Description", "Architecture", "", "RAW"}, 0) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ImageIcon.class;
                case 4:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4;
        }
    };
    DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Icon", "Package", "Description", "Architecture", "", "RAW"}, 0) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ImageIcon.class;
                case 4:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4;
        }
    };

    void formatcolumns() {
        onlinepackages.setModel(model);
        localpackages.setModel(inmodel);
        onlinepackages.getColumnModel().getColumn(0).setMaxWidth(40);
        onlinepackages.getColumnModel().getColumn(1).setMinWidth(130);
        onlinepackages.getColumnModel().getColumn(1).setMaxWidth(160);
        onlinepackages.getColumnModel().getColumn(2).setMinWidth(200);
        onlinepackages.getColumnModel().getColumn(3).setMinWidth(110);
        onlinepackages.getColumnModel().getColumn(3).setMaxWidth(90);
        onlinepackages.getColumnModel().getColumn(4).setPreferredWidth(22);
        onlinepackages.getColumnModel().getColumn(4).setMaxWidth(22);
        onlinepackages.getColumnModel().getColumn(5).setMinWidth(0);
        onlinepackages.getColumnModel().getColumn(5).setMaxWidth(0);
        onlinepackages.getColumnModel().getColumn(5).setPreferredWidth(0);
        onlinepackages.setShowVerticalLines(false);
        onlinepackages.setModel(model);
        localpackages.getTableHeader().setReorderingAllowed(false);
        localpackages.getColumnModel().getColumn(0).setMaxWidth(40);
        localpackages.getColumnModel().getColumn(1).setMinWidth(130);
        localpackages.getColumnModel().getColumn(1).setMaxWidth(160);
        localpackages.getColumnModel().getColumn(2).setMinWidth(200);
        localpackages.getColumnModel().getColumn(3).setMinWidth(110);
        localpackages.getColumnModel().getColumn(3).setMaxWidth(90);
        localpackages.getColumnModel().getColumn(4).setPreferredWidth(22);
        localpackages.getColumnModel().getColumn(4).setMaxWidth(22);
        localpackages.getColumnModel().getColumn(5).setMinWidth(0);
        localpackages.getColumnModel().getColumn(5).setMaxWidth(0);
        localpackages.getColumnModel().getColumn(5).setPreferredWidth(0);
        localpackages.setShowVerticalLines(false);
        localpackages.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < onlinepackages.getColumnModel().getColumnCount(); i++) {
            onlinepackages.getColumnModel().getColumn(i).setResizable(false);
        }
        for (int y = 0; y < onlinepackages.getColumnModel().getColumnCount(); y++) {
            localpackages.getColumnModel().getColumn(y).setResizable(false);
        }
    }

    void exit() throws IOException, InterruptedException {
        boolean running = currentProcess != null && currentProcess.isAlive();
        if (running) {
            Toolkit.getDefaultToolkit().beep();
            int choice = JOptionPane.showConfirmDialog(this, "An operation is currently running.\nDo you want to exit?", "Confirm exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.CANCEL_OPTION) {
                return;
            }
            cancelListing = true;
            if (currentProcess != null) {
                currentProcess.destroyForcibly();
            }
            if (currentThread != null) {
                currentThread.interrupt();
            }
            status.setText("dpkg --configure -a needs your permition...");
            Process fix = new ProcessBuilder("bash", "-c", "pkexec dpkg --configure -a").start();

            fix.waitFor();
            System.exit(0);
        } else {
            System.exit(0);
        }

    }

    private void initModelListener() {
        if (cListener != null) {
            model.removeTableModelListener(cListener);
        }
        cListener = e -> {
            if (e.getType() != TableModelEvent.UPDATE) {
                return;
            }
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 4) {
                boolean checked = (boolean) model.getValueAt(row, 4);
                String rawPkg = (String) model.getValueAt(row, 5);
                if (checked == true) {
                    toinstall.add(rawPkg);
                } else {
                    toinstall.remove(rawPkg);
                }
                if (toinstall.size() == 0) {
                    installopt.setEnabled(false);

                } else {
                    installopt.setEnabled(true);

                }
                String inst = "To be installed: " + String.join(", ", toinstall);
                if (inst.length() > 39) {
                    inst = inst.substring(0, 40).replaceAll(",\\s*$", "") + "...";
                }
                status.setText(inst);
            }
        };
        model.addTableModelListener(cListener);
    }

    String getChecked() {
        return String.join(" ", toinstall);
    }

    private void initModelListenerLocal() {
        if (cListenerIn != null) {
            inmodel.removeTableModelListener(cListenerIn);
        }
        cListenerIn = e -> {
            if (e.getType() != TableModelEvent.UPDATE) {
                return;
            }
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 4) {
                boolean checked = (boolean) inmodel.getValueAt(row, 4);
                String rawPkg = (String) inmodel.getValueAt(row, 5);
                if (checked == true) {
                    toremove.add(rawPkg);
                } else {
                    toremove.remove(rawPkg);
                }
                if (toremove.size() == 0) {
                    rmopt.setEnabled(false);

                } else {
                    rmopt.setEnabled(true);

                }
                String remv = "To be removed: " + String.join(", ", toremove);
                if (remv.length() > 39) {
                    remv = remv.substring(0, 40).replaceAll(",\\s*$", "") + "...";
                }
                status.setText(remv);
            }
        };
        inmodel.addTableModelListener(cListenerIn);
    }

    String getCheckedLocal() {
        return String.join(" ", toremove);
    }

    void lock_ops(boolean state) {
        filem.setEnabled(state);
        editm.setEnabled(state);
        helpm.setEnabled(state);
        categories.setEnabled(state);
        categories2.setEnabled(state);
    }

    void totalsize() throws IOException {

        new Thread(() -> {
            try {
                bashCommand
                        = "apt show " + getChecked() + " 2>/dev/null | awk '/Installed-Size/ {sum += $2} END {printf \"%.2f MB\\n\", sum/1024}'";

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String result = reader.readLine();
                SwingUtilities.invokeLater(()
                        -> totalsize.setText("Total size is " + result + " are you sure?")
                );

                process.waitFor();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }).start();

    }

    void dependends(String action) throws IOException {
        new Thread(() -> {
            try {
                String pkgs = action.equals("remove") ? getCheckedLocal() : getChecked();
                bashCommand
                        = "apt show " + pkgs + " 2>/dev/null | grep -E '^Depends:' | sed 's/Depends: //' | tr ',|' '\\n' | sed 's/ .*//' | grep -v '^$' | sort -u | paste -sd,";

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String deps = reader.readLine();
                SwingUtilities.invokeLater(()
                        -> deplist.setListData(deps.split(","))
                );
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }).start();

    }

    String mapCategory(String menu) {
        if (menu == null) {
            return "";
        }

        switch (menu) {

            case "All":
                return "";

            case "Libraries":
                return "libs|libdevel|oldlibs";

            case "Tools":
                return "utils|admin|misc";

            case "Internet":
                return "net|web|mail|ftp|irc";

            case "Games":
                return "games";

            case "Graphics":
                return "graphics|video";

            case "Office":
                return "editors|text";

            case "Binaries":
                return "utils|admin";

            case "Desktops":
                return "gnome|kde|xfce|lxde|mate|x11";

            default:
                return "";
        }
    }

    public ImageIcon getIconFromTheme(String name, String possible) throws IOException {
        try {
            Process p = new ProcessBuilder(
                    "bash", "-c", "gsettings get org.gnome.desktop.interface icon-theme | awk -F\"'\" '{print $2}'"
            ).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String theme = br.readLine();
            p.waitFor();
            if (theme == null || theme.isBlank()) {
                return new ImageIcon(main.class.getResource("pkg.png"));
            }
            String iconName = name.toLowerCase();
            String path;
            if(!possible.equals("N/A")){
                path = "/usr/share/icons/" + theme + "/16x16/apps/" + possible + ".png";
            }else{
                path = "/usr/share/icons/" + theme + "/16x16/apps/" + iconName + ".png";
            }
            
            File f = new File(path);
            if (f.exists()) {
                return new ImageIcon(path);
            }
        } catch (Exception ignored) {
        }
        return new ImageIcon(main.class.getResource("pkg.png"));
    }

    void dpkg_list(String category) throws IOException {
        cancelListing = false;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String timestamp = dtf.format(LocalDateTime.now());
        log = log
                + "[<b>" + timestamp + "</b>] <font color='orange'>(" + category + ") Listing packages with apt-cache dumpavail...</font><br>";
        logtext.setText("<html>" + log + "</html>");
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.show();
        localpackages.setEnabled(false);
        localpackages.setForeground(Color.GRAY);
        currentThread = new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                DefaultTableModel smodel = (DefaultTableModel) localpackages.getModel();
                smodel.setRowCount(0);
                toremove.clear();
                rmopt.setEnabled(false);
            });
            lock_ops(false);
            status.setText("Listing packages...");
            try {
                String cat = mapCategory(category);
                String bashCommand
                        = "installed=$(dpkg -l | awk '/^ii/ {print $2}'); "
                        + "count=0; "
                        + "apt-cache dumpavail | awk -v cat=\"" + cat + "\" '"
                        + "BEGIN { pkg=\"\"; desc=\"\"; arch=\"\"; section=\"\" } "
                        + "/^Package:/ { pkg=$2 } "
                        + "/^Section:/ { section=$2 } "
                        + "/^Architecture:/ { arch=$2 } "
                        + "/^Description:/ { "
                        + "    desc=$0; sub(/^Description: /, \"\", desc); "
                        + "    while (getline nextLine && nextLine ~ /^ /) { "
                        + "        sub(/^ /, \"\", nextLine); desc = desc \" \" nextLine; "
                        + "    } "
                        + "} "
                        + "/^$/ { "
                        + "    if (pkg != \"\" && (cat == \"\" || tolower(section) ~ cat)) { "
                        + "        if (desc == \"\") desc=\"No description available\"; "
                        + "        if (arch == \"\") arch=\"unknown\"; "
                        + "        print pkg \"|\" desc \"|\" arch; "
                        + "    } "
                        + "    pkg=\"\"; desc=\"\"; arch=\"\"; section=\"\"; "
                        + "} "
                        + "' | while IFS=\"|\" read -r pkg desc arch; do "
                        + "    if echo \"$installed\" | grep -qx \"$pkg\"; then "
                        + "        pretty=$(grep -R \"Name=\" /usr/share/applications/ 2>/dev/null | grep -i \"$pkg\" | head -n1 | cut -d'=' -f2); "
                        + "        [ -z \"$pretty\" ] && pretty=\"$pkg\"; "
                        + "        icon=$(grep -R \"Icon=\" /usr/share/applications/ 2>/dev/null | grep -i \"$pkg\" | head -n1 | cut -d'=' -f2); "
                        + "        [ -z \"$icon\" ] && icon=\"N/A\"; "
                        + "        echo \"(${pretty})[${desc}]{${arch}}<${icon}>?${pkg}?\"; "
                        + "        count=$((count+1)); "
                        + "        [ $count -ge 400 ] && break; "
                        + "    fi; "
                        + "done";

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                currentProcess = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                int totalLines = 400;
                int currentLine = 0;
                List<String> results = new ArrayList<>();
                while (!cancelListing && (line = reader.readLine()) != null) {
                    results.add(line);
                    currentLine++;
                    int progress = (int) ((currentLine / (double) totalLines) * 100);
                    SwingUtilities.invokeLater(() -> jProgressBar1.setValue(progress));
                    allmodel = (DefaultTableModel) localpackages.getModel();

                    String name = line.replaceAll(".*\\(([^)]*)\\).*", "$1");
                    if (line.replaceAll(".*\\(([^)]*)\\).*", "$1").isEmpty()) {
                        name = "unknown";
                    }
                    
                    String descr = line.replaceAll(".*\\[([^]]*)\\].*", "$1");
                    String arch = line.replaceAll(".*\\{([^}]*)\\}.*", "$1");
                    String raw = line.replaceAll(".*\\?([^?]*)\\?.*", "$1");
                    ImageIcon icon = getIconFromTheme(raw,line.replaceAll(".*\\<([^}]*)\\>.*", "$1"));
                    Boolean sel = false;

                    Object[] row = {icon, name, descr, arch, sel, raw};
                    if (cancelListing) {
                        if (currentProcess != null) {
                            currentProcess.destroyForcibly();

                        }
                        return;
                    }
                    SwingUtilities.invokeLater(() -> allmodel.addRow(row));

                }
                currentProcess.waitFor();
                SwingUtilities.invokeLater(() -> status.setText("Done"));
                SwingUtilities.invokeLater(() -> jProgressBar1.setValue(100));
                SwingUtilities.invokeLater(() -> localpackages.setEnabled(true));
                SwingUtilities.invokeLater(() -> localpackages.setForeground(Color.BLACK));

                listingTimer = new javax.swing.Timer(1000, e -> {
                    jProgressBar1.setVisible(false);
                    jProgressBar1.revalidate();
                    jProgressBar1.repaint();
                    status.setText("Total pacakges: " + localpackages.getModel().getRowCount());
                });
                listingTimer.setRepeats(false);
                listingTimer.start();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                initModelListenerLocal();
                lock_ops(true);
                SwingUtilities.invokeLater(() -> {
                    log += "<font color='#006400'>Done listing packages...</font><br>";
                    logtext.setText("<html>" + log + "</html>");
                });
            }
        });
        currentThread.start();
        formatcolumns();

    }

    void apt_list(String category) throws IOException {
        cancelListing = false;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String timestamp = dtf.format(LocalDateTime.now());
        log = log
                + "[<b>" + timestamp + "</b>] <font color='orange'>(" + category + ") Listing packages with apt-cache dumpavail...</font><br>";
        logtext.setText("<html>" + log + "</html>");
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.show();
        onlinepackages.setEnabled(false);
        onlinepackages.setForeground(Color.GRAY);
        currentThread = new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                DefaultTableModel smodel = (DefaultTableModel) onlinepackages.getModel();
                smodel.setRowCount(0);
                toinstall.clear();
                installopt.setEnabled(false);
            });
            lock_ops(false);
            status.setText("Listing packages...");
            try {
                String cat = mapCategory(category);
                bashCommand
                        = "installed=$(dpkg -l | awk '/^ii/ {print $2}'); "
                        + "apt-cache dumpavail | awk -v cat=\"" + cat + "\" '"
                        + "/^Package:/ {pkg=$2}"
                        + "/^Description/ && desc==\"\" {sub(/^.*: /, \"\", $0); desc=$0}"
                        + "/^Architecture:/ {arch=$2}"
                        + "/^Section:/ {section=$2}"
                        + "/^$/ {"
                        + "    if (pkg != \"\" && (cat == \"\" || tolower(section) ~ cat)) {"
                        + "        if (desc == \"\") desc=\"No description available\";"
                        + "        if (arch == \"\") arch=\"unknown\";"
                        + "        print pkg \"|\" desc \"|\" arch"
                        + "    }"
                        + "    pkg=\"\"; desc=\"\"; arch=\"\"; section=\"\""
                        + "}' | head -n 500 | while IFS=\"|\" read -r pkg desc arch; do "
                        + "    if ! echo \"$installed\" | grep -qx \"$pkg\"; then " // skip installed here
                        + "        pretty=$(grep -R \"Name=\" /usr/share/applications/ | grep -i \"$pkg\" | head -n1 | cut -d'=' -f2); "
                        + "        [ -z \"$pretty\" ] && pretty=\"$pkg\"; "
                        + "        icon=$(grep -R \"Icon=\" /usr/share/applications/ | grep -i \"$pkg\" | head -n1 | cut -d'=' -f2); "
                        + "        [ -z \"$icon\" ] && icon=\"N/A\"; "
                        + "        echo \"(${pretty})[${desc}]{${arch}}<${icon}>?${pkg}?\"; "
                        + "    fi "
                        + "done";

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                currentProcess = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                int totalLines = 500;
                int currentLine = 0;
                List<String> results = new ArrayList<>();
                while (!cancelListing && (line = reader.readLine()) != null) {
                    results.add(line);
                    currentLine++;
                    int progress = (int) ((currentLine / (double) totalLines) * 100);
                    SwingUtilities.invokeLater(() -> jProgressBar1.setValue(progress));
                    allmodel = (DefaultTableModel) onlinepackages.getModel();

                    String name = line.replaceAll(".*\\(([^)]*)\\).*", "$1");
                    if (line.replaceAll(".*\\(([^)]*)\\).*", "$1").isEmpty()) {
                        name = "unknown";
                    }
                    
                    String descr = line.replaceAll(".*\\[([^]]*)\\].*", "$1");
                    String arch = line.replaceAll(".*\\{([^}]*)\\}.*", "$1");
                    String raw = line.replaceAll(".*\\?([^?]*)\\?.*", "$1");
                    ImageIcon icon = getIconFromTheme(raw,line.replaceAll(".*\\<([^}]*)\\>.*", "$1"));
                    Boolean sel = false;

                    Object[] row = {icon, name, descr, arch, sel, raw};
                    if (cancelListing) {
                        if (currentProcess != null) {
                            currentProcess.destroyForcibly();
                        }
                        return;
                    }
                    SwingUtilities.invokeLater(() -> allmodel.addRow(row));

                }
                currentProcess.waitFor();
                SwingUtilities.invokeLater(() -> status.setText("Done"));
                SwingUtilities.invokeLater(() -> jProgressBar1.setValue(100));
                SwingUtilities.invokeLater(() -> onlinepackages.setEnabled(true));
                SwingUtilities.invokeLater(() -> onlinepackages.setForeground(Color.BLACK));

                javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
                    jProgressBar1.setVisible(false);
                    jProgressBar1.revalidate();
                    jProgressBar1.repaint();
                    status.setText("Total pacakges: " + onlinepackages.getModel().getRowCount());
                });
                t.setRepeats(false);
                t.start();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                initModelListener();
                lock_ops(true);
                SwingUtilities.invokeLater(() -> {
                    log += "<font color='#006400'>Done listing packages...</font><br>";
                    logtext.setText("<html>" + log + "</html>");
                });
            }
        });
        currentThread.start();
        formatcolumns();

    }

    void apt_search(String query, boolean bydescr, boolean byname) throws IOException {
        if (query == null || query.isBlank()) {
            return;
        }
        cancelListing = false;
        JTable curTable = jTabbedPane1.getSelectedIndex() == 1 ? localpackages : onlinepackages;
        if (jTabbedPane1.getSelectedIndex() > 1) {
            return;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String timestamp = dtf.format(LocalDateTime.now());
        log = log
                + "[<b>" + timestamp + "</b>] <font color='orange'>Searching for \"" + query + "\" with apt-cache dumpavail...</font><br>";
        logtext.setText("<html>" + log + "</html>");
        lock_ops(false);
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.show();
        curTable.setEnabled(false);
        curTable.setForeground(Color.GRAY);
        currentThread = new Thread(() -> {
            jProgressBar1.setValue(0);
            status.setText("Searching for " + query + "...");
            List<String> results = new ArrayList<>();
            try {
                bashCommand
                        = "installed=$(dpkg -l | awk '/^ii/ {print $2}'); "
                        + "desktop_cache=$(mktemp); "
                        + "grep -R \"^Name=\\|^Icon=\" /usr/share/applications/ 2>/dev/null | "
                        + "awk -F= '/^Name=/ {name=$2} /^Icon=/ && name!=\"\" {print name\"|\"$2; name=\"\"}' > \"$desktop_cache\"; "
                        + "apt-cache dumpavail | awk -v q=\"" + query + "\" -v byname=" + (byname ? "1" : "0") + " -v bydescr=" + (bydescr ? "1" : "0") + " ' "
                        + "/^Package:/ {pkg=$2} "
                        + "/^Description:/ && desc==\"\" {sub(/^.*: /,\"\",$0); desc=$0} "
                        + "/^Architecture:/ {arch=$2} "
                        + "/^$/ { "
                        + "  if (pkg!=\"\") { "
                        + "    found = (index(pkg,q)>0); "
                        + "    if (bydescr && index(desc,q)>0) found=1; "
                        + "    if (found) print pkg \"|\" desc \"|\" arch "
                        + "  } "
                        + "  pkg=\"\"; desc=\"\"; arch=\"\" "
                        + "}' | head -n 700 | while IFS='|' read -r pkg desc arch; do "
                        + "  if "+(jTabbedPane1.getSelectedIndex()==0?"!":"")+" echo \"$installed\" | grep -qx \"$pkg\"; then "
                        + "    pretty=$(grep -i \"|$pkg$\" \"$desktop_cache\" | head -n1 | cut -d'|' -f1); "
                        + "    [ -z \"$pretty\" ] && pretty=\"$pkg\"; "
                        + "    icon=$(grep -i \"|$pkg$\" \"$desktop_cache\" | head -n1 | cut -d'|' -f2); "
                        + "    [ -z \"$icon\" ] && icon=\"N/A\"; "
                        + "    echo \"(${pretty})[${desc}]{${arch}}<${icon}>?${pkg}?\"; "
                        + "  fi "
                        + "done; "
                        + "rm -f \"$desktop_cache\"";

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                currentProcess = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                int totalLines = 900;
                int currentLine = 0;
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel smodel = (DefaultTableModel) curTable.getModel();
                    toinstall.clear();
                    toremove.clear();
                    installopt.setEnabled(false);
                    rmopt.setEnabled(false);
                    smodel.setRowCount(0);
                });
                while ((line = reader.readLine()) != null) {
                    results.add(line);
                    currentLine++;
                    int progress = (int) ((currentLine / (double) totalLines) * 100);
                    SwingUtilities.invokeLater(() -> jProgressBar1.setValue(progress));
                    DefaultTableModel smodel = (DefaultTableModel) curTable.getModel();

                    String name = line.replaceAll(".*\\(([^)]*)\\).*", "$1");
                    if (line.replaceAll(".*\\(([^)]*)\\).*", "$1").isEmpty()) {
                        name = "unknown";
                    }
                    
                    String descr = line.replaceAll(".*\\[([^]]*)\\].*", "$1");
                    String arch = line.replaceAll(".*\\{([^}]*)\\}.*", "$1");
                    String raw = line.replaceAll(".*\\?([^?]*)\\?.*", "$1");
                    ImageIcon icon = getIconFromTheme(raw,line.replaceAll(".*\\<([^}]*)\\>.*", "$1"));
                    Boolean sel = false;
                    Object[] row = {icon, name, descr, arch, sel, raw};
                    if (cancelListing) {
                        if (currentProcess != null) {
                            currentProcess.destroyForcibly();
                        }
                        return;
                    }
                    SwingUtilities.invokeLater(() -> smodel.addRow(row));

                }
                currentProcess.waitFor();
                SwingUtilities.invokeLater(() -> status.setText("Done"));
                SwingUtilities.invokeLater(() -> jProgressBar1.setValue(100));
                SwingUtilities.invokeLater(() -> curTable.setEnabled(true));
                SwingUtilities.invokeLater(() -> curTable.setForeground(Color.BLACK));
                SwingUtilities.invokeLater(() -> {
                    if (curTable == localpackages) {
                        categories2.clearSelection();
                    } else {
                        categories.clearSelection();
                    }
                });
                javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
                    jProgressBar1.hide();
                    status.setText("Total pacakges: " + curTable.getModel().getRowCount());
                });
                t.setRepeats(false);
                t.start();

                boolean noResults = results.isEmpty();
                SwingUtilities.invokeLater(() -> {
                    if (noResults) {

                        log = log + "<font color='red'>No packages listed...</font><br>";
                        logtext.setText("<html>" + log + "</html>");

                        String newQuery = (String) JOptionPane.showInputDialog(
                                this,
                                "No search results found:",
                                "Search",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                "Search packages..."
                        );

                        if (newQuery == null || newQuery.isBlank() || newQuery.equals("Search packages...")) {
                            lock_ops(true);
                            curTable.setEnabled(true);
                            curTable.setForeground(Color.BLACK);
                            jProgressBar1.setValue(0);
                            jProgressBar1.hide();
                            return;
                        }

                        try {
                            apt_search(newQuery, bydescr, byname);
                        } catch (IOException ex) {
                            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return;
                    }

                    if (jTabbedPane1.getSelectedIndex() == 1) {
                        initModelListenerLocal();
                    } else if (jTabbedPane1.getSelectedIndex() == 0) {
                        initModelListener();
                    }
                    lock_ops(true);

                    log = log + "<font color='#006400'>Done listing packages...</font><br>";

                    logtext.setText("<html>" + log + "</html>");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        currentThread.start();
        formatcolumns();

    }

    void isOSCompatible() {
        //ΕΛΕΝΧΩ ΑΝ OSNAME ΕΙΝΑΙ LINUX
        //ΕΛΕΝΧΩ ΑΝ ΤΟ ETC OS RELEASE ΠΕΡΙΕΧΕΙ ΤΙΣ ΛΕΞΕΙΣ "debian" Η "ubuntu"
        //ΕΛΕΝΧΩ ΑΝ ΤΟ /usr/bin/apt ΥΠΑΡΧΕΙ ΣΑΝ ΑΡΧΕΙΟ
        //ΑΝ ΟΛΑ ΑΥΤΑ ΕΙΝΑΙ ΟΚ ΤΟΤΕ ΞΕΚΙΝΑ ΤΟ ΠΡΟΓΡΑΜΜΑ ΑΛΛΙΩΣ ΒΓΑΛΕ ERROR MESSAGEBOX ΟΤΙ ΔΕΝ ΜΠΟΡΕΙ ΝΑ ΤΡΕΞΕΙ 
        //ΣΕ NON DEBIAN SYSTEMS :)
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "This app is specifically built for linux systems and its not compatible with your oparating system", "Compatibility error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        try {
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("/etc/os-release"))).toLowerCase();
            if (!content.contains("debian") && !content.contains("ubuntu")) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "This app is specifically built for debian or ubuntu based distributions", "Compatibility error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Cannot read /etc/os-release, thus this app is unable to tell if you distribution is compatible and cannot start on your system safely", "Compatibility Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        File apt = new File("/usr/bin/apt");
        if (!apt.exists() || !apt.canExecute()) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "The apt binary cannot be found on /usr/bin/apt, thus the app cannot start safely", "Backend Error", JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
    }

    void scrolllog() {
        JViewport vp = jScrollPane3.getViewport();
        Component view = vp.getView();
        vp.setViewPosition(new Point(0, view.getHeight()));
    }

    private boolean upgrb(String line) {

        Matcher m = PMSTATUS.matcher(line);
        if (m.find()) {

            if (!dpkgProgressStarted) {
                dpkgProgressStarted = true;
                SwingUtilities.invokeLater(() -> {
                    jProgressBar1.setIndeterminate(false);
                    jProgressBar1.setValue(0);
                });

            }
            double pct = Double.parseDouble(m.group(1));
            int percent = (int) Math.round(pct);
            if (percent != lastPercent) {
                lastPercent = percent;
                SwingUtilities.invokeLater(()
                        -> jProgressBar1.setValue(percent)
                );
            }
            return true;
        }
        return false;
    }

    void moveRow(DefaultTableModel from, DefaultTableModel to, int rowIndex) {
        int columnCount = from.getColumnCount();
        Object[] rowData = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            rowData[i] = from.getValueAt(rowIndex, i);
        }
        from.removeRow(rowIndex);
        rowData[4] = false;
        if (rowIndex <= to.getRowCount()) {
            to.insertRow(rowIndex, rowData);
        } else {
            to.addRow(rowData);
        }
    }

    void apt(String arg) {
        String repo = null;
        String laction;
        String saction;
        switch (arg) {
            case "install":
                laction = "Calling apt for package instalation...";
                saction = "Installing requested packages...";
                break;
            case "remove":
                laction = "Calling apt for package removal...";
                saction = "Removing requested packages...";
                break;
            case "update":
                laction = "Calling apt for package update...";
                saction = "Updating necessary packages...";
                break;
            case "upgrade":
                laction = "Calling apt for system upgrade...";
                saction = "Upgrading the system...";
                break;
            case "repoadd":
                repo = (String) JOptionPane.showInputDialog(this, "Paste or insert repository name: ", "Add repository", JOptionPane.PLAIN_MESSAGE, null, null, "ppa:some/ppa");
                if (repo == null || repo.isBlank()) {
                    return;
                }
                laction = "";
                saction = "Adding repository...";
                break;
            case "reporm":
                repo = (String) JOptionPane.showInputDialog(this, "Paste or insert repository name: ", "Remove repository", JOptionPane.PLAIN_MESSAGE, null, null, "ppa:some/ppa");
                if (repo == null || repo.isBlank()) {
                    return;
                }
                laction = "";
                saction = "Removing repository...";
                break;
            default:
                repo = "";
                throw new RuntimeException("Invalid option " + arg + ". Cannot proceed");

        }
        if (listingTimer != null && listingTimer.isRunning()) {
            listingTimer.stop();
        }
        SwingUtilities.invokeLater(() -> {
            jProgressBar1.setValue(0);
            jProgressBar1.setIndeterminate(true);
            jProgressBar1.setVisible(true);
            jTabbedPane1.setEnabledAt(0, false);
            jTabbedPane1.setEnabledAt(1, false);
            jTabbedPane1.setSelectedIndex(2);
        });
        //pkexec env DEBIAN_FRONTEND=noninteractive apt install -y packag1 pacakg2 etc
        //ΑΥΤΟ ΧΡΗΣΙΜΟΠΟΙΩ ΚΑΙ ΕΙΜΑΙ ΟΚ
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String timestamp = dtf.format(LocalDateTime.now());

        SwingUtilities.invokeLater(() -> {
            log = log + "[<b>" + timestamp + "</b>] <font color='orange'>" + laction + "</font><br>";
            logtext.setText("<html>" + log + "</html>");
            logtext.revalidate();
            logtext.repaint();
            scrolllog();
        });

        lock_ops(false);
        jProgressBar1.show();
        onlinepackages.setEnabled(false);
        onlinepackages.setForeground(Color.GRAY);
        final String rp = repo;
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {

                jProgressBar1.setValue(0);
                status.setText(saction);
            });
            try {
                if (arg.equals("repoadd")) {
                    bashCommand
                            = "pkexec env DEBIAN_FRONTEND=noninteractive "
                            + "add-apt-repository -y " + rp;
                } else if (arg.equals("reporm")) {
                    bashCommand
                            = "pkexec env DEBIAN_FRONTEND=noninteractive "
                            + "add-apt-repository -r -y " + rp;
                } else {
                    bashCommand
                            = "pkexec env DEBIAN_FRONTEND=noninteractive "
                            + "apt-get " + arg + " -y -o APT::Status-Fd=1 "
                            + (arg.equals("remove") ? getCheckedLocal() : getChecked());
                }
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                currentProcess = process;
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                int currentLine = 0;
                dpkgProgressStarted = false;
                lastPercent = -1;
                while ((line = reader.readLine()) != null) {
                    if (upgrb(line)) {
                        continue;
                    }

                    String trimmed = line.trim();
                    SwingUtilities.invokeLater(() -> {
                        log += trimmed + "<br>";
                        logtext.setText("<html>" + log + "</html>");
                        logtext.revalidate();
                        logtext.repaint();
                        scrolllog();
                    });

                    currentLine++;

                }
                process.waitFor();
                currentProcess = null;
                currentThread = null;
                SwingUtilities.invokeLater(() -> status.setText("Done"));
                SwingUtilities.invokeLater(() -> jProgressBar1.setValue(100));
                SwingUtilities.invokeLater(() -> onlinepackages.setEnabled(true));
                SwingUtilities.invokeLater(() -> onlinepackages.setForeground(Color.BLACK));
                SwingUtilities.invokeLater(() -> jTabbedPane1.setEnabledAt(0, true));
                SwingUtilities.invokeLater(() -> jTabbedPane1.setEnabledAt(1, true));
                javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
                    jProgressBar1.setIndeterminate(false);
                    jProgressBar1.setValue(0);
                    jProgressBar1.setVisible(false);

                    dpkgProgressStarted = false;
                    lastPercent = -1;

                    status.setText("Operation finished");
                });
                t.setRepeats(false);
                t.start();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (arg.equals("install") || arg.equals("remove")) {
                    DefaultTableModel from;
                    DefaultTableModel to;

                    if (arg.equals("install")) {
                        from = (DefaultTableModel) onlinepackages.getModel();
                        to = (DefaultTableModel) localpackages.getModel();
                    } else {
                        from = (DefaultTableModel) localpackages.getModel();
                        to = (DefaultTableModel) onlinepackages.getModel();
                    }
                    DefaultTableModel activeModel = from;
                    for (int row = activeModel.getRowCount() - 1; row >= 0; row--) {
                        Boolean checked = (Boolean) activeModel.getValueAt(row, 4);
                        if (Boolean.TRUE.equals(checked)) {
                            if (categories.getSelectedIndex() == categories2.getSelectedIndex()) {
                                moveRow(from, to, row);

                            }
                        }
                    }
                    toinstall.clear();
                    toremove.clear();
                    SwingUtilities.invokeLater(() -> {
                        rmopt.setEnabled(false);
                        installopt.setEnabled(false);
                    });
                }
                initModelListener();
                lock_ops(true);
                SwingUtilities.invokeLater(() -> {
                    log += "<font color='#006400'>Oparation finished...</font><br>";
                    logtext.setText("<html>" + log + "</html>");
                    logtext.revalidate();
                    logtext.repaint();
                    SwingUtilities.invokeLater(() -> {
                        scrolllog();
                    });
                });

            }
        }).start();

    }

    public main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (args.length > 0 && "-legacyui".equals(args[0])) {
            make_motif_light();
            disable_cde_border();
        } else {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            fixgtkfont();
        }
        isOSCompatible();
        this.setIconImage(new ImageIcon(main.class.getResource("icon.png")).getImage());

        initComponents();
        SwingUtilities.invokeLater(() -> {
            categories.setSelectedIndex(1);
        });

        jToolBar1.setFloatable(false);
        if (legacy) {
            disable_cde_border();
        }

        DefaultListModel<String> m = new DefaultListModel<>();

        ListModel<String> old = categories.getModel();
        for (int i = 0; i < old.getSize(); i++) {
            m.addElement(old.getElementAt(i));
        }

        categories.setModel(m);

        // onlinepackages.removeColumn(onlinepackages.getColumnModel().getColumn(5));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Confirm = new javax.swing.JDialog();
        confirmtitle = new javax.swing.JLabel();
        checked = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        totalsize = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        deplist = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        about = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jvm = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(7, 0), new java.awt.Dimension(7, 0), new java.awt.Dimension(7, 32767));
        status = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jProgressBar1 = new javax.swing.JProgressBar();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(7, 0), new java.awt.Dimension(7, 0), new java.awt.Dimension(7, 32767));
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        categories = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        onlinepackages = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        localpackages = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        categories2 = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        logtext = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        filem = new javax.swing.JMenu();
        installopt = new javax.swing.JMenuItem();
        rmopt = new javax.swing.JMenuItem();
        refreshopt = new javax.swing.JMenuItem();
        updateopt = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        exitopt = new javax.swing.JMenuItem();
        editm = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        byname = new javax.swing.JCheckBoxMenuItem();
        bydescr = new javax.swing.JCheckBoxMenuItem();
        search = new javax.swing.JMenuItem();
        addrepo = new javax.swing.JMenuItem();
        rmrepo = new javax.swing.JMenuItem();
        helpm = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        Confirm.setTitle("Action comfirmation");
        Confirm.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        Confirm.setModal(true);
        Confirm.setResizable(false);

        confirmtitle.setFont(new java.awt.Font("Inter", 1, 14)); // NOI18N
        confirmtitle.setText("This packages are pending to be ??:");

        checked.setFont(new java.awt.Font("Inter", 0, 13)); // NOI18N
        checked.setText("<html>No data</html>");
        checked.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checked.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        ok.setText("    OK    ");
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });

        cancel.setText(" Cancel ");
        cancel.setMaximumSize(new java.awt.Dimension(100, 23));
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        totalsize.setText("  ");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(totalsize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ok)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ok)
                    .addComponent(cancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalsize))
                .addGap(8, 8, 8))
        );

        deplist.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "None" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(deplist);

        jLabel4.setFont(new java.awt.Font("Inter", 1, 14)); // NOI18N
        jLabel4.setText("Dependencies to be managed:");

        javax.swing.GroupLayout ConfirmLayout = new javax.swing.GroupLayout(Confirm.getContentPane());
        Confirm.getContentPane().setLayout(ConfirmLayout);
        ConfirmLayout.setHorizontalGroup(
            ConfirmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(ConfirmLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
            .addGroup(ConfirmLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(ConfirmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(checked, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confirmtitle, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 35, Short.MAX_VALUE))
        );
        ConfirmLayout.setVerticalGroup(
            ConfirmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ConfirmLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(confirmtitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(checked, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        about.setTitle("About this app");
        about.setModal(true);
        about.setResizable(false);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/iconsmall.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Inter", 1, 14)); // NOI18N
        jLabel2.setText("guiapt v1.0");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("<html>Gui apt is a lightweight and portable <br>gui package manager for the apt  <br>backend made in Java™</html>");
        jLabel3.setToolTipText("");
        jLabel3.setFocusable(false);
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jvm.setText("<html><b>Installed jdk:</b></html> ");

        javax.swing.GroupLayout aboutLayout = new javax.swing.GroupLayout(about.getContentPane());
        about.getContentPane().setLayout(aboutLayout);
        aboutLayout.setHorizontalGroup(
            aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jvm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(74, 74, 74))
        );
        aboutLayout.setVerticalGroup(
            aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jvm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Package Manager");
        setMinimumSize(new java.awt.Dimension(680, 413));
        setSize(new java.awt.Dimension(680, 413));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar1.add(filler3);

        status.setText("Packages N/N");
        jToolBar1.add(status);
        jToolBar1.add(filler1);

        jProgressBar1.setIndeterminate(true);
        jProgressBar1.setStringPainted(true);
        jToolBar1.add(jProgressBar1);
        jToolBar1.add(filler2);

        jTabbedPane1.setFocusable(false);
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        categories.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "<html><b>Categories:</b></html>", "All", "Tools", "Internet", "Games", "Graphics", "Office", "Binaries", "Desktops", "Libraries" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        categories.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categories.setSelectedIndex(1);
        categories.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                categoriesMouseClicked(evt);
            }
        });
        categories.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                categoriesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(categories);

        jScrollPane2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jScrollPane2ComponentResized(evt);
            }
        });

        onlinepackages.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Icon", "Package", "Description", "Architecture", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        onlinepackages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        onlinepackages.setShowGrid(true);
        onlinepackages.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                onlinepackagesComponentResized(evt);
            }
        });
        jScrollPane2.setViewportView(onlinepackages);
        if (onlinepackages.getColumnModel().getColumnCount() > 0) {
            onlinepackages.getColumnModel().getColumn(0).setPreferredWidth(2);
            onlinepackages.getColumnModel().getColumn(2).setPreferredWidth(20);
            onlinepackages.getColumnModel().getColumn(3).setPreferredWidth(10);
            onlinepackages.getColumnModel().getColumn(4).setPreferredWidth(5);
        }

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(2, 2, 2))
        );

        jTabbedPane1.addTab("Available Packages", jPanel1);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jScrollPane5ComponentResized(evt);
            }
        });

        localpackages.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Icon", "Package", "Description", "Architecture", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        localpackages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        localpackages.setShowGrid(true);
        localpackages.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                localpackagesComponentResized(evt);
            }
        });
        jScrollPane5.setViewportView(localpackages);
        if (localpackages.getColumnModel().getColumnCount() > 0) {
            localpackages.getColumnModel().getColumn(0).setPreferredWidth(2);
            localpackages.getColumnModel().getColumn(2).setPreferredWidth(20);
            localpackages.getColumnModel().getColumn(3).setPreferredWidth(10);
            localpackages.getColumnModel().getColumn(4).setPreferredWidth(5);
        }

        categories2.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "<html><b>Categories:</b></html>", "All", "Tools", "Internet", "Games", "Graphics", "Office", "Binaries", "Desktops", "Libraries" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        categories2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categories2.setSelectedIndex(1);
        categories2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                categories2MouseClicked(evt);
            }
        });
        categories2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                categories2ValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(categories2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 558, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                    .addGap(0, 122, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Installed Packages", jPanel3);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        logtext.setBackground(new java.awt.Color(255, 255, 255));
        logtext.setFont(new java.awt.Font("FreeMono", 1, 13)); // NOI18N
        logtext.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        logtext.setOpaque(true);
        jScrollPane3.setViewportView(logtext);

        jTabbedPane1.addTab("System Log", jScrollPane3);

        filem.setText(" File ");
        filem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filemActionPerformed(evt);
            }
        });

        installopt.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        installopt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/check.png"))); // NOI18N
        installopt.setText("Install selected");
        installopt.setEnabled(false);
        installopt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installoptActionPerformed(evt);
            }
        });
        filem.add(installopt);

        rmopt.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        rmopt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/x.png"))); // NOI18N
        rmopt.setText("Remove selected");
        rmopt.setEnabled(false);
        rmopt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmoptActionPerformed(evt);
            }
        });
        filem.add(rmopt);

        refreshopt.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        refreshopt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/refresh.png"))); // NOI18N
        refreshopt.setText("Refresh list");
        refreshopt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshoptActionPerformed(evt);
            }
        });
        filem.add(refreshopt);

        updateopt.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        updateopt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/update.png"))); // NOI18N
        updateopt.setText("Update system");
        updateopt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateoptActionPerformed(evt);
            }
        });
        filem.add(updateopt);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/upgrade.png"))); // NOI18N
        jMenuItem1.setText("Upgrade system");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        filem.add(jMenuItem1);

        exitopt.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        exitopt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/exit.png"))); // NOI18N
        exitopt.setText("Exit guiapt");
        exitopt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitoptActionPerformed(evt);
            }
        });
        filem.add(exitopt);

        jMenuBar1.add(filem);

        editm.setText(" Edit ");

        jMenu2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/filter.png"))); // NOI18N
        jMenu2.setText("Search filter");

        byname.setSelected(true);
        byname.setText("By name");
        jMenu2.add(byname);

        bydescr.setText("By description");
        jMenu2.add(bydescr);

        editm.add(jMenu2);

        search.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        search.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/search.png"))); // NOI18N
        search.setText("Search...");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });
        editm.add(search);

        addrepo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/repo.png"))); // NOI18N
        addrepo.setText("Add Repository");
        addrepo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addrepoActionPerformed(evt);
            }
        });
        editm.add(addrepo);

        rmrepo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/reporm.png"))); // NOI18N
        rmrepo.setText("Remove Repository");
        rmrepo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmrepoActionPerformed(evt);
            }
        });
        editm.add(rmrepo);

        jMenuBar1.add(editm);

        helpm.setText(" Help ");

        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/about.png"))); // NOI18N
        jMenuItem4.setText("About");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        helpm.add(jMenuItem4);

        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guiapt/law.png"))); // NOI18N
        jMenuItem2.setText("Licensing");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        helpm.add(jMenuItem2);

        jMenuBar1.add(helpm);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addGap(0, 0, 0)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

    }//GEN-LAST:event_formComponentResized

    private void exitoptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitoptActionPerformed
        try {
            exit();
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_exitoptActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        String query = (String) JOptionPane.showInputDialog(this, "Search for a package: ", "Search", JOptionPane.PLAIN_MESSAGE, null, null, "Search packages...");
        if (query == null || query.isBlank()) {
            return;
        }
        try {
            refreshopt.setEnabled(false);
            apt_search(query, byname.isSelected(), bydescr.isSelected());
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_searchActionPerformed

    private void jScrollPane2ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jScrollPane2ComponentResized
        onlinepackages.getColumnModel().getColumn(4).setPreferredWidth(jScrollPane2.getWidth() - 452);
        onlinepackages.revalidate();
        onlinepackages.doLayout();
    }//GEN-LAST:event_jScrollPane2ComponentResized

    private void onlinepackagesComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_onlinepackagesComponentResized

    }//GEN-LAST:event_onlinepackagesComponentResized

    private void categoriesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_categoriesValueChanged
        refreshopt.setEnabled(true);
        if (currentProcess != null) {
            currentProcess.destroyForcibly();
        }
        if (currentThread != null) {
            currentThread.interrupt();
        }
        if (categories.getSelectedIndex() == 0) {
            categories.setSelectedIndex(previ);
        } else {
            previ = categories.getSelectedIndex();
        }
        try {
            if (categories.getSelectedIndex() != -1) {
                apt_list(categories.getSelectedValue());
            }

        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_categoriesValueChanged

    private void categoriesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_categoriesMouseClicked

        if (!categories.getSelectedValue().equals("All")) {
            onlinepackages.setModel(allmodel);
            formatcolumns();
            status.setText("Total pacakges: " + onlinepackages.getModel().getRowCount());
        }
    }//GEN-LAST:event_categoriesMouseClicked

    private void installoptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installoptActionPerformed
        deplist.setListData(new String[]{"None"});
        totalsize.setText("");
        Confirm.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        Confirm.pack();
        Confirm.setLocationRelativeTo(this);
        confirmtitle.setText("This packages are pending to be installed:");
        checked.setText(getChecked().replace(" ", ", "));
        if (checked.getText().length() > 70) {
            checked.setText(checked.getText().substring(0, 71).replaceAll(",\\s*$", "") + "...");
        }
        action = 1;
        try {
            totalsize();
            dependends("install");
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Confirm.setVisible(true);
        }

    }//GEN-LAST:event_installoptActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        Confirm.setVisible(false);
        jTabbedPane1.setSelectedIndex(2);
        if (action == 1) {
            if (localpackages.getModel().getRowCount() == 0) {
                new Thread(() -> {
                    try {
                        dpkg_list(categories2.getSelectedValue());
                        while (currentThread != null && currentThread.isAlive()) {
                            Thread.sleep(50);
                        }
                        SwingUtilities.invokeLater(() -> apt("install"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                apt("install");
            }
        } else if (action == 2) {
            apt("remove");
        }
    }//GEN-LAST:event_okActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        Confirm.setVisible(false);
    }//GEN-LAST:event_cancelActionPerformed

    private void rmoptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmoptActionPerformed

        Toolkit.getDefaultToolkit().beep();
        deplist.setListData(new String[]{"None"});
        totalsize.setText("");
        Confirm.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        Confirm.pack();
        Confirm.setLocationRelativeTo(this);
        confirmtitle.setText("This packages are pending to be remove:");
        checked.setText(getCheckedLocal().replace(" ", ", "));
        if (checked.getText().length() > 70) {
            checked.setText(checked.getText().substring(0, 71).replaceAll(",\\s*$", "") + "...");
        }
        action = 2;
        try {
            totalsize();
            dependends("remove");
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Confirm.setVisible(true);
        }
    }//GEN-LAST:event_rmoptActionPerformed

    private void localpackagesComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_localpackagesComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_localpackagesComponentResized

    private void jScrollPane5ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jScrollPane5ComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_jScrollPane5ComponentResized

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
        if (jTabbedPane1.getSelectedIndex() == 0) {
            rmopt.setEnabled(false);
            installopt.setEnabled(true);
            cancelListing = true;
            if (currentProcess != null) {
                currentProcess.destroyForcibly();
            }
            if (currentThread != null) {
                currentThread.interrupt();
            }
            status.setText("Total pacakges: " + onlinepackages.getModel().getRowCount()); 
        } else if (jTabbedPane1.getSelectedIndex() == 1) {
            installopt.setEnabled(false);
            rmopt.setEnabled(true);
            cancelListing = true;
            if (currentProcess != null) {
                currentProcess.destroyForcibly();
            }
            if (currentThread != null) {
                currentThread.interrupt();
            }
        }else if(jTabbedPane1.getSelectedIndex()==2){
            status.setText("");
        }
        if (localpackages.getModel().getRowCount() == 0) {
            try {
                dpkg_list(categories2.getSelectedValue());
            } catch (IOException ex) {
                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
           if(jTabbedPane1.getSelectedIndex()==1){
           status.setText("Total pacakges: " + localpackages.getModel().getRowCount());
           }
        }
       
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void categories2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_categories2MouseClicked
        refreshopt.setEnabled(true);
        try {
            if (currentProcess != null) {
                currentProcess.destroyForcibly();
            }
            if (currentThread != null) {
                currentThread.interrupt();
            }
            dpkg_list(categories2.getSelectedValue());
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_categories2MouseClicked

    private void categories2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_categories2ValueChanged

    }//GEN-LAST:event_categories2ValueChanged

    private void refreshoptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshoptActionPerformed

        try {
            if (currentProcess != null) {
                currentProcess.destroyForcibly();
            }
            if (currentThread != null) {
                currentThread.interrupt();
            }
            if (jTabbedPane1.getSelectedIndex() == 1) {
                dpkg_list(categories2.getSelectedValue());
            } else if (jTabbedPane1.getSelectedIndex() == 0) {
                apt_list(categories.getSelectedValue());
            }

        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_refreshoptActionPerformed

    private void updateoptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateoptActionPerformed
        int choice = JOptionPane.showConfirmDialog(this, "System is pending to be updated.\nAre you sure?", "System update", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            apt("update");
        }
    }//GEN-LAST:event_updateoptActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        int choice = JOptionPane.showConfirmDialog(this, "System is pending to be upgraded.\nAre you sure?", "System upgrade", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            apt("upgrade");
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void filemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_filemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            exit();
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowClosing

    private void addrepoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addrepoActionPerformed
        apt("repoadd");
    }//GEN-LAST:event_addrepoActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        jvm.setText("<html><b>Installed Java Version:</b> " + System.getProperty("java.version") + "</html>");
        about.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        about.pack();
        about.setLocationRelativeTo(this);
        about.setVisible(true);

    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void rmrepoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmrepoActionPerformed
        apt("reporm");
    }//GEN-LAST:event_rmrepoActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        JOptionPane.showMessageDialog(this, "Copyright © AndronikosGl 2026. All rights reserved.\n" +
"This project is source-available.\nModification and redistribution are not permitted. \nThis project includes a modified asset based on Google \nNoto Emoji (SIL Open Font License 1.1).", "Software lisence", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new main(args).setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog Confirm;
    private javax.swing.JDialog about;
    private javax.swing.JMenuItem addrepo;
    private javax.swing.JCheckBoxMenuItem bydescr;
    private javax.swing.JCheckBoxMenuItem byname;
    private javax.swing.JButton cancel;
    private javax.swing.JList<String> categories;
    private javax.swing.JList<String> categories2;
    private javax.swing.JLabel checked;
    private javax.swing.JLabel confirmtitle;
    private javax.swing.JList<String> deplist;
    private javax.swing.JMenu editm;
    private javax.swing.JMenuItem exitopt;
    private javax.swing.JMenu filem;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JMenu helpm;
    private javax.swing.JMenuItem installopt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel jvm;
    private javax.swing.JTable localpackages;
    private javax.swing.JLabel logtext;
    private javax.swing.JButton ok;
    private javax.swing.JTable onlinepackages;
    private javax.swing.JMenuItem refreshopt;
    private javax.swing.JMenuItem rmopt;
    private javax.swing.JMenuItem rmrepo;
    private javax.swing.JMenuItem search;
    private javax.swing.JLabel status;
    private javax.swing.JLabel totalsize;
    private javax.swing.JMenuItem updateopt;
    // End of variables declaration//GEN-END:variables
}
