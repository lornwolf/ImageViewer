package com.lornwolf.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.lornwolf.common.UIReleaseUtil;
import com.lornwolf.common.Utils;

public class ImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Viewer mainWindow;
    private int width;
    private BufferedImage image = null;
    private JLabel imageLabel = null;
    private List<JEditorPane> textLines = new ArrayList<JEditorPane>();

    public ImagePanel(List<Section> images, Viewer mainWindow, int width) throws IOException {
        this.mainWindow = mainWindow;
        this.width = width;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(234, 234, 239));
        textLines.clear();

        // 初始化进度条。
        mainWindow.initProgressBar();

        if (images.size() == 0) {
            JLabel label = new JLabel();
            label.setText("此部分内容缺失。");
            label.setOpaque(true);
            label.setBackground(Color.RED);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("YaHei Mono", 0, 16));
            add(label);
            mainWindow.setProgressValue(100);
        }

        new Thread() {
            public void run() {
                for (int i = 0; i < images.size(); i++) {
                    int percent = i + 1;
                    Section line = images.get(i);
                    if (line.getType().equals("string")) {
                        JEditorPane label = new JEditorPane();
                        label.setText("    " + decode(line.getContent()).replace(" ", ""));
                        label.setBackground(new Color(234, 234, 239));
                        label.setEditable(false);
                        label.setFont(new Font("YaHei Mono", 0, 16));
                        add(label);
                        ImagePanel.this.addTextLines(label);
                    } else if (line.getType().equals("image")) {
                        if (line.getImageType().equals("jpeg")) {
                            imageLabel = ImagePanel.this.genJpegPanel(line, ImagePanel.this.mainWindow);
                        } else if (line.getImageType().equals("gif")) {
                            imageLabel = ImagePanel.this.genGifPanel(line, ImagePanel.this.mainWindow);
                        } else {
                            imageLabel = new JLabel();
                            imageLabel.setText("不支持的図片格式。");
                            imageLabel.setOpaque(true);
                            imageLabel.setBackground(Color.RED);
                            imageLabel.setForeground(Color.WHITE);
                            imageLabel.setFont(new Font("YaHei Mono", 0, 16));
                        }
                        add(imageLabel);
                    } else if (line.getType().equals("exception")) {
                        JLabel label = new JLabel();
                        label.setText(line.getContent());
                        label.setOpaque(true);
                        label.setBackground(Color.RED);
                        label.setForeground(Color.WHITE);
                        label.setFont(new Font("YaHei Mono", 0, 16));
                        add(label);
                    }
                    // 更新进度。
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            mainWindow.setProgressValue(percent * 100 / images.size());
                        }
                    });
                }
            }
        }.start();
    }

    private JLabel genJpegPanel(Section section, Viewer mainWindow) {
        try {
            InputStream src = new ByteArrayInputStream(Utils.hexToByteArr(section.getContent()));
            image = ImageIO.read((ByteArrayInputStream) src);

            /* 测试用代码。
            try (FileOutputStream fileOuputStream = new FileOutputStream(System.getProperty("java.io.tmpdir") + "image_viewer_tmp_img")) {
                fileOuputStream.write(hexToByteArr(section.getContent()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            image = BigBufferedImage.create(new File(TMP_FILE), BufferedImage.TYPE_INT_RGB);
            */

            int width = section.getWidth() > 0 ? section.getWidth() : image.getWidth();
            int height = section.getHeight() > 0 ? section.getHeight() : image.getHeight();
            // 判断是不是自适应模式。
            if (mainWindow.getStatusBar().getImgMode() == 0) {
                if (width > this.width) {
                    height = (int) ((double) height * ((double) this.width / (double) width));
                    width = this.width;
                    image = Utils.createResizedCopy(image, width, height, true);
                }
            } else {
                // 图片太大容易内存溢出，最大宽度设置为16000，-Xmx512M的情况下勉强做到不出异常。
                if (width > 16000) {
                    height = (int) ((double) height * ((double) 16000 / (double) width));
                    width = 16000;
                    image = Utils.createResizedCopy(image, width, height, true);
                }
            }

            ImageIcon imageIcon = new ImageIcon(image);
            // 及时释放资源。
            image.getGraphics().dispose();
            // 生成图片标签对象。
            JLabel imageLabel = new JLabel(imageIcon);
            imageLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            src.close();

            return imageLabel;
        } catch (IOException e) {
        }
        return null;
    }

    private JLabel genGifPanel(Section section, Viewer mainWindow) {
        byte[] src = Utils.hexToByteArr(section.getContent());
        ImageIcon image = new ImageIcon(src);
        JLabel imageLabel = new JLabel(image);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        return imageLabel;
    }

    public void close() {
        textLines.clear();
        UIReleaseUtil.freeSwingObject(this);
        removeAll();
    }

    private String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    private String decode(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i+=4) {
            sb.append("%u");
            sb.append(s.charAt(i + 2));
            sb.append(s.charAt(i + 3));
            sb.append(s.charAt(i + 0));
            sb.append(s.charAt(i + 1));
        }
        return unescape(sb.toString());
    }

    /**
     * 窗口调整大小后，设置文本显示框的最大宽度，使其适应主窗口。
     */
    public void onResize() {
        Dimension dimension = new Dimension((int) this.getSize().getWidth() - 5, (int) this.getSize().getHeight() - 5);
        for (JEditorPane line : textLines) {
            line.setMaximumSize(dimension);
        }
    }

	public List<JEditorPane> getTextLines() {
		return textLines;
	}

	public void addTextLines(JEditorPane line) {
		if (this.textLines == null) {
			this.textLines = new ArrayList<JEditorPane>();
		}
		textLines.add(line);
	}
}
