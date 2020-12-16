package com.lornwolf.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.bulenkov.iconloader.IconLoader;
import com.lornwolf.common.PropertyUtil;
import com.lornwolf.common.UIReleaseUtil;

@SuppressWarnings("serial")
public class StatusBar extends JPanel implements MouseListener, MouseMotionListener {

    private JLabel view;
    private Point mouseDownCompCoords = null;
    private JButton resize = null;
    private Viewer mainWindow = null;
    // 全屏幕标记。
    private boolean fullScreenFlag = false;
    // 图片显示模式。
    private int imgMode = 0;

    // 左侧标题栏的宽度。
    final int TITLE_WIDTH = 300;
    JScrollPane scrollPane;
    List<String[]> tableData = new ArrayList<String[]>();
    String[] columnNames = {"標題"};

    public StatusBar(Viewer mainWindow) {
        this.mainWindow = mainWindow;
        setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        setLayout(new GridLayout(1, 2));

        view = new JLabel();
        view.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        view.setFont(new Font("YaHei Mono", 0, 16));
        view.setForeground(Color.GRAY);
        add(view);

        JPanel right = new JPanel();
        right.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        String iconPath = "/com/lornwolf/viewer/icons/";
        JButton selectFile = new JButton(IconLoader.getIcon(iconPath + "select.png"));
        selectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 读取配置文件·。
                String folder = null;
                String settingFile = "/properties/environment.ini";
                folder = PropertyUtil.readProperties(settingFile, "FOLDER");
                // 选择文件。
                JFileChooser filechooser = new JFileChooser();
                if (folder != null) {
                    filechooser.setCurrentDirectory(new File(folder));
                }
                int selected = filechooser.showOpenDialog(mainWindow);
                if (selected == JFileChooser.APPROVE_OPTION){
                    File file = filechooser.getSelectedFile();
                    PropertyUtil.setProperties(settingFile, "FOLDER", file.getAbsolutePath());
                    mainWindow.path = file.getAbsolutePath();
                    // 状态栏显示文件名。
                    StatusBar.this.setText(file.getName());
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
                        tableData.clear();
                        PreparedStatement statement = connection.prepareStatement("select * from 标题");
                        ResultSet resultSet = statement.executeQuery();
                        resultSet.getFetchSize();
                        while (resultSet.next()){
                            String title = resultSet.getString("标题");
                            if (title != null && title.indexOf("说明文档") >= 0) {
                                continue;
                            }
                            String id = String.valueOf(resultSet.getInt("ID"));
                            if (id == null || id.trim().length() == 0) {
                                continue;
                            }
                            String[] head = new String[2];
                            head[0] = title;
                            head[1] = id;
                            tableData.add(head);
                        }
                        statement.close();
                        connection.close();

                        JTable table = new JTable();
                        table.setModel(new DefaultTableModel() {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                // 设置为不可编辑。
                                return false;
                            }
                            public String getColumnName(int col) {
                                return columnNames[col].toString();
                            }
                            public int getRowCount() { return tableData.size(); }
                            public int getColumnCount() { return columnNames.length; }
                            public Object getValueAt(int row, int col) {
                                return tableData.get(row)[col];
                            }
                            public void setValueAt(Object value, int row, int col) {
                                tableData.get(row)[col] = (String) value;
                                fireTableCellUpdated(row, col);
                            }
                        });
                        table.setBackground(new Color(227, 237, 205));
                        table.setFont(new Font("YaHei Mono", 0, 12));
                        // 不显示表头。
                        table.setTableHeader(null);
                        // 设置单行选择模式。
                        table.setColumnSelectionAllowed(false);
                        table.setRowSelectionAllowed(true);
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        // 选中事件处理。
                        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                            public void valueChanged(ListSelectionEvent e) {
                                if (e.getValueIsAdjusting()) {
                                    return;
                                }
                                table.setEnabled(false);
                                int[] selectedRow = table.getSelectedRows();
                                mainWindow.pageId = Integer.valueOf(tableData.get(selectedRow[0])[1]);
                                StatusBar.this.setText(tableData.get(selectedRow[0])[0]);
                                mainWindow.showPage();
                                table.setEnabled(true);
                            }
                        });
                        // 设置表的宽度。
                        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                        DefaultTableColumnModel columnModel= (DefaultTableColumnModel) table.getColumnModel();
                        TableColumn column = columnModel.getColumn(0);
                        column.setPreferredWidth(TITLE_WIDTH);

                        if (scrollPane != null) {
                            UIReleaseUtil.freeSwingObject(scrollPane);
                            mainWindow.getContentPane().remove(scrollPane);
                        }
                        scrollPane = new JScrollPane(table);
                        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
                        // 设置鼠标滚轮灵敏度。
                        scrollPane.getVerticalScrollBar().setUnitIncrement(36);
                        scrollPane.setPreferredSize(new Dimension(TITLE_WIDTH + 2 + ((Integer)(UIManager.get("ScrollBar.width"))).intValue(), table.getHeight()));
                        mainWindow.getContentPane().add(scrollPane, BorderLayout.LINE_START);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        selectFile.setMargin(new Insets(5, 5, 5, 5));
        selectFile.setVerticalAlignment(SwingConstants.CENTER);

        JLabel label01 = new JLabel(" ");
        JLabel label02 = new JLabel(" ");

        JComboBox<String> modeSelector = new JComboBox<String>(new String[] {"自动适应", "原始大小"});;
        modeSelector.setFont(new Font("YaHei Mono", 0, 12));
        modeSelector.setSelectedIndex(0);
        imgMode = 0;
        modeSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imgMode = modeSelector.getSelectedIndex();
            }
        });

        JPanel rightContainer = new JPanel();
        GridBagLayout layout = new GridBagLayout(); 
        rightContainer.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(selectFile, gbc);
        rightContainer.add(selectFile);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        layout.setConstraints(label01, gbc);
        rightContainer.add(label01);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        layout.setConstraints(modeSelector, gbc);
        rightContainer.add(modeSelector);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        layout.setConstraints(label02, gbc);
        rightContainer.add(label02);
        
        JButton money = new JButton("打赏作者");
        money.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new QR(mainWindow, JDialog.ModalityType.DOCUMENT_MODAL).setVisible(true);;
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        layout.setConstraints(money, gbc);
        rightContainer.add(money);

        right.setLayout(new BorderLayout());
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(rightContainer, BorderLayout.WEST);
        right.add(rightPanel, BorderLayout.WEST);

        resize = new JButton(IconLoader.getIcon(iconPath + "resize.png"));
        resize.setMargin(new Insets(2, 2, 2, 2));
        resize.setBackground(Color.GRAY);
        resize.setBorderPainted(false);
        resize.setFocusPainted(false);
        resize.setContentAreaFilled(false);

        JPanel resizeContainer = new JPanel();
        resizeContainer.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(resize, gbc);
        resizeContainer.add(resize);
        right.add(resizeContainer, BorderLayout.EAST);

        resize.addMouseListener(this);
        resize.addMouseMotionListener(this);

        add(right);
    }

    public void mouseReleased(MouseEvent e) {
        if (mouseDownCompCoords == null) {
            return;
        }
        if (mouseDownCompCoords.getX() != e.getPoint().getX() || mouseDownCompCoords.getY() != e.getPoint().getY()) {
            mainWindow.componentResized(null);
        }
        mouseDownCompCoords = null;
        mainWindow.requestFocusInWindow();
    }

    public void mousePressed(MouseEvent e) {
        mouseDownCompCoords = e.getPoint();
    }

    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        int width = currCoords.x - mainWindow.getLocation().x + (int) resize.getSize().getWidth() - (int) mouseDownCompCoords.getX() + 5;
        int height = currCoords.y - mainWindow.getLocation().y + (int) resize.getSize().getHeight() - (int) mouseDownCompCoords.getY() + 5;
        if (width < 200) {
            width = 200;
        }
        if (height < 200) {
            height = 200;
        }
        mainWindow.setSize(width, height);
    }

    public void setText(String text) {
        view.setText(text);
    }

    public String getText() {
        return view.getText();
    }

    public void setResizable(boolean resizable) {
        if (mainWindow.getTitleBar().isMaxSize() || fullScreenFlag) {
            resize.setEnabled(false);
            resize.removeMouseListener(this);
            resize.removeMouseMotionListener(this);
            return;
        }
        resize.setEnabled(resizable);
        if (resizable) {
            if  (resize.getMouseListeners() == null || !Arrays.asList(resize.getMouseListeners()).contains(this)) {
                resize.addMouseListener(this);
            }
            if  (resize.getMouseMotionListeners() == null || !Arrays.asList(resize.getMouseMotionListeners()).contains(this)) {
                resize.addMouseMotionListener(this);
            }
        } else {
            resize.removeMouseListener(this);
            resize.removeMouseMotionListener(this);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void setEnabled(boolean enabled) {
        setResizable(enabled);
   }

    public boolean isFullScreen() {
        return fullScreenFlag;
    }

    public int getImgMode() {
        return imgMode;
    }
}