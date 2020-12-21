package com.lornwolf.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.InflaterInputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.lornwolf.common.UIReleaseUtil;
import com.lornwolf.common.Utils;

public class Viewer extends SuperFrame implements ComponentListener, ActionListener {
    
    private static final long serialVersionUID = 1L;

    // 滚动条的宽度。
    private final int SCROLL_WIDTH = ((Integer)(UIManager.get("ScrollBar.width"))).intValue();

    // 标题栏。
    public TitleBar titleBar;

    // 状态栏。
    public StatusBar statusBar;

    // 主面板。
    public JPanel mainPanel;

    // 右侧显示内容的面板。
    public ImagePanel imagePanel;

    // 右侧显示内容的滚动面板。
    public JScrollPane scrollPane;

    // 当前选中页的ID。
    public int pageId = 0;

    // 数据库文件的路径。
    public String path = null;

    // 存储所有图片/文字信息的列表。
    public List<Section> images = new ArrayList<Section>();

    public static void main( String[] args) throws IOException, ClassNotFoundException {
        try {
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
            UIManager.put("RootPane.setupButtonVisible", false);
        } catch(Exception e) {}

        try {
            File fontFile = new File(new File(".").getAbsoluteFile().getParent() + "/fonts/YaHei Mono.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            initGlobalFontSetting(new Font("YaHei Mono", 0, 12));
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Viewer();
    }

    public Viewer() {

        Toolkit.getDefaultToolkit().setDynamicLayout(false);
        titleBar = new TitleBar(this);
        statusBar = new StatusBar(this);

        // ウィンドウクローズイベント処理。
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
            }
        });

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            return;
        }

        // アイコン設定。
        URL iconUrl = this.getClass().getResource("/com/lornwolf/viewer/icons/viewer.png");
        ImageIcon icon = new ImageIcon(iconUrl);
        setIconImage(icon.getImage());

        // 去掉标题栏。
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        // 设置画面大小。
        int width = 1600;
        int height = 900;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.getWidth() < width || screenSize.getHeight() < height) {
            width = (int) screenSize.getWidth();
            height = (int) screenSize.getHeight();
        }
        setSize(width, height);

        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.GRAY));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titleBar, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Object[] options = {"好的", "算了"};
                int confirm = JOptionPane.showOptionDialog(null,
                        "確定关閉本窗口？",
                        "確認",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        new ImageIcon(this.getClass().getResource("/com/lornwolf/viewer/icons/confirm.png")),
                        options,
                        1);
                if (confirm == 0) {
                    ((JFrame) windowEvent.getSource()).dispose();
                }
            }
        });

        getContentPane().requestFocusInWindow();
    }

    public void show(List<Section> images) throws IOException {
        // 先释放一下资源。
        if (mainPanel != null) {
            UIReleaseUtil.freeSwingObject(mainPanel);
            getContentPane().remove(mainPanel);
        }

        // 初始化显示区域。
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.GRAY));
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // 计算显示区域的宽度。
        int width = this.getContentPane().getWidth() - 320 - ((Integer)(UIManager.get("ScrollBar.width"))).intValue() * 2;
        imagePanel = new ImagePanel(images, this, width);
        scrollPane = new JScrollPane(imagePanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(36);

        // 设置可拖拽。
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            private final Point pp = new Point();

            public void mouseDragged(final MouseEvent e) {
                JViewport vport = (JViewport)e.getSource();
                Point cp = e.getPoint();
                Point vp = vport.getViewPosition();
                vp.translate(pp.x - cp.x, pp.y - cp.y);
                imagePanel.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
                pp.setLocation(cp);
            }

            public void mousePressed(MouseEvent e) {
                imagePanel.setCursor(hndCursor);
                pp.setLocation(e.getPoint());
            }

            public void mouseReleased(MouseEvent e) {
                imagePanel.setCursor(defCursor);
                imagePanel.repaint();
            }
        };
        scrollPane.getViewport().addMouseListener(mouseAdapter);
        scrollPane.getViewport().addMouseMotionListener(mouseAdapter);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * 检索数据库，读取图片/文字信息。
     */
    public void showPage() {
        // 先释放一下资源。
        images.clear();
        if (imagePanel != null) {
            imagePanel.close();
        }

        // 定义需忽略的内容。
        String[] ignore = {".JPG", ".GIF"};

        // 开始读取并解析内容。
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            PreparedStatement statement = connection.prepareStatement("select * from 资料库 where fid = " + String.valueOf(pageId));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                ByteArrayInputStream bais = (ByteArrayInputStream) (resultSet.getBinaryStream("内容")); 
                InflaterInputStream iis = new InflaterInputStream(bais); 

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] bs = new byte[1024];
                int len = 0;
                while ((len = iis.read(bs)) > 0) {
                    os.write(bs, 0, len);
                }
                os.flush();
                iis.close();

                String line;
                boolean isImage = false, jump = false, sectionStart = false;
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray())));
                Section section = null;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (isImage) {
                        if (line.startsWith("width=")) {
                            section.setWidth(Integer.parseInt(line.substring(6)));
                            continue;
                        }
                        if (line.startsWith("height=")) {
                            section.setHeight(Integer.parseInt(line.substring(7)));
                            continue;
                        }
                        section.setContent(line);
                        images.add(section);
                        section = null;
                        isImage = false;
                        continue;
                    } else {
                        // 空白行处理。
                        if (line.length() == 0) {
                            sectionStart = true;
                            if (sb.length() > 0) {
                                // 如果是空白行，并且缓存里有数据，则认为是段落的结束。
                                section = new Section();
                                section.setType("string");
                                section.setContent(sb.toString());
                                images.add(section);
                                sb.setLength(0);
                                section = null;
                            }
                        }
                        // 图片处理。
                        if ("TJPEGImage".equals(line) || "TGIFImage".equals(line) || "TMetafile".equals(line)) {
                            isImage = true;
                            section = new Section();
                            section.setType("image");
                            if ("TJPEGImage".equals(line)) {
                                section.setImageType("jpeg");
                            } else if ("TGIFImage".equals(line)) {
                                section.setImageType("gif");
                            } else if ("TMetafile".equals(line)) {
                                section.setImageType("wmf");
                            }
                            continue;
                        }
                        if (jump) {
                            jump = false;
                            continue;
                        }
                        if ("RVStyle1".equals(line)) {
                            jump = true;
                            continue;
                        }
                        if ("1".equals(line)) {
                            section = new Section();
                            section.setType("exception");
                            section.setContent("无法解析的内容");
                            images.add(section);
                            section = null;
                            jump = true;
                            continue;
                        }
                        // 可以被解析的文字内容。
                        if (line.indexOf(" ") < 0 && line.length() % 4 == 0 && !Arrays.asList(ignore).contains(line)) {
                            sb.append(line);
                            // 处理没有以空白行作为段落的文字。
                            if (!sectionStart) {
                                section = new Section();
                                section.setType("string");
                                section.setContent(sb.toString());
                                images.add(section);
                                sb.setLength(0);
                                section = null;
                            }
                        }
                    }
                }
                // 如果最后一行不是空行，则需要做一些收尾工作。
                if (sb.length() > 0) {
                    section = new Section();
                    section.setType("string");
                    section.setContent(sb.toString());
                    images.add(section);
                    sb.setLength(0);
                    section = null;
                }

                /* 试验用代码 开始
                System.out.println(String.valueOf(pageId));
                try (FileOutputStream fileOuputStream = new FileOutputStream("D:/test.txt")) {
                    fileOuputStream.write(os.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                试验用代码 结束 */

                os.close();
                break;
            }
            statement.close();
            connection.close();

            show(images);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void back() {
        pageId = pageId - 1;
        showPage();
    }

    public void forward() {
        pageId = pageId + 1;
        showPage();
    }

    public void reload() {
    }

    public TitleBar getTitleBar() {
        return titleBar;
    }

    public boolean isFullScreen() {
        return statusBar.isFullScreen();
    }

    public void setResizable(boolean resizable) {
        if (statusBar != null) {
            statusBar.setResizable(resizable);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (mainPanel != null && imagePanel != null) {
            // 设置图像面板的大小。
            imagePanel.setSize((int) mainPanel.getSize().getWidth() - SCROLL_WIDTH, (int) mainPanel.getSize().getHeight() - SCROLL_WIDTH);
            // 调整图像面板中文字行的宽度。 
            imagePanel.onResize();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public static void initGlobalFontSetting(Font fnt){
        FontUIResource fontRes = new FontUIResource(fnt);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

    /**
     * 保存当前页面的所有图片。
     * 
     * @param path 保存路径。
     */
    public void savePictures(String path) {
        if (!path.endsWith(java.io.File.separator)) {
            path = path + java.io.File.separator;
        }
        String folderName = statusBar.getText().replace("\\", "").replace("/", "").replace(":", "").replace("?", "").replace("*", "").replace("<", "").replace(">", "").replace("|", "");
        File folder = new File(path + folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            if (!folder.mkdir()) {
                JOptionPane.showConfirmDialog(this, "failed to create the folder.", "Message", JOptionPane.CLOSED_OPTION, 1);
                return;
            }
        }
        int imgCnt = (int) images.stream().filter(o -> "image".equals(o.getType())).count();
        if (imgCnt == 0) {
            JOptionPane.showConfirmDialog(this, "no image to export.", "Message", JOptionPane.CLOSED_OPTION, 1);
            return;
        }
        int count = 1;
        for (Section section : images) {
            if ("image".equals(section.getType())) {
                try (FileOutputStream fileOuputStream = new FileOutputStream(path + folderName + java.io.File.separator + String.format("%02d", count) + "." + section.getImageType())) {
                    fileOuputStream.write(Utils.hexToByteArr(section.getContent()));
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(this, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION, 1);
                    // 更新进度。
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setProgressValue(100);
                        }
                    });
                    return;
                }
                count++;
            }
        }
        JOptionPane.showConfirmDialog(this, "OK.", "Message", JOptionPane.CLOSED_OPTION, 1);
    }

    public void initProgressBar() {
        this.titleBar.initProgressBar();
    }

    public void setProgressValue(int n) {
        this.titleBar.setProgressValue(n);
    }

    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }
}