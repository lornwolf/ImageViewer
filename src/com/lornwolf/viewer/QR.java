package com.lornwolf.viewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.lornwolf.common.Utils;

public class QR extends JDialog {

    private JPanel mainPanel;
    private JPanel downPanel;
    private JLabel console;
    private JButton closeBtn;

    public QR(Frame owner, ModalityType modal) {
        super(owner, modal);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        URL iconUrl = this.getClass().getResource("/com/lornwolf/viewer/icons/lornwolf.jpg");
        BufferedImage icon = null;
        try {
            icon = ImageIO.read(iconUrl);
        } catch (IOException e1) {
        }
        int width = 360;
        int height = (int) ((double) icon.getHeight() * ((double) width / (double) icon.getWidth()));
        BufferedImage image = Utils.createResizedCopy(icon, width, height, true);
        ImageIcon ico = new ImageIcon(image);
        console = new JLabel(ico);
        mainPanel.add(console, BorderLayout.CENTER);
        downPanel = new JPanel();
        closeBtn = new JButton("Close");
        downPanel.add(closeBtn);
        mainPanel.add(downPanel, BorderLayout.SOUTH);
        Container cp = getContentPane();
        cp.add(mainPanel);
        setTitle("请作者喝杯咖啡");
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                setVisible(false);
            }
        });
    }
}
