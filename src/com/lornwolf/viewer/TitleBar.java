package com.lornwolf.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.bulenkov.iconloader.IconLoader;

public class TitleBar extends JPanel implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    private Viewer mainFrame = null;
    private JLabel titleText = null;
    private Point mouseDownCompCoords = null;
    private boolean maxSize = false;
    private Icon maxIcon = null;
    private Icon resIcon = null;
    private Icon minIcon = null;
    private Icon closeIcon = null;
    private Icon backPageIcon = null;
    private Icon prevPageIcon = null;
    private Icon reloadIcon = null;
    private JButton backPage = null;
    private JButton prevPage = null;
    private JButton reload = null;
    private JButton minBtn;
    private JButton maxBtn;
    private JButton closeBtn;
    private Dimension dimension = null;
    private Point point = null;
    private JProgressBar progress;
    private JPanel prgContainer = null;
    private JPanel prgPanel = null;
    private JPanel centerPanel = new JPanel();
    private JPanel mainPanel = new JPanel();

    public TitleBar(Viewer mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));

        titleText = new JLabel();
        titleText.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        titleText.setFont(new Font("Simsun", 0, 16));
        add(titleText, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(1, 3));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        String iconPath = "/com/lornwolf/viewer/icons/";

        backPageIcon = IconLoader.getIcon(iconPath + "back_page.png");
        prevPageIcon = IconLoader.getIcon(iconPath + "prev_page.png");
        reloadIcon = IconLoader.getIcon(iconPath + "reload.png");
        maxIcon = IconLoader.getIcon(iconPath + "maxsize.png");
        minIcon = IconLoader.getIcon(iconPath + "minsize.png");
        resIcon = IconLoader.getIcon(iconPath + "restore.png");
        closeIcon = IconLoader.getIcon(iconPath + "close.png");

        maxBtn = new JButton(maxIcon);
        minBtn = new JButton(minIcon);
        closeBtn = new JButton(closeIcon);

        minBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.setState(Frame.ICONIFIED);
                mainFrame.requestFocusInWindow();
            }
        });
        maxBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!maxSize) {
                    dimension = mainFrame.getSize();
                    point = mainFrame.getLocation();
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(mainFrame.getGraphicsConfiguration());
                    mainFrame.setLocation(insets.left, insets.top);
                    mainFrame.setSize((int) screen.getWidth() - insets.left - insets.right, (int) screen.getHeight() - insets.top - insets.bottom);
                    maxBtn.setIcon(resIcon);
                    removeMouseListener(TitleBar.this);
                    removeMouseMotionListener(TitleBar.this);
                } else {
                    mainFrame.setLocation(point);
                    mainFrame.setSize(dimension);
                    point = null;
                    dimension = null;
                    maxBtn.setIcon(maxIcon);
                    addMouseListener(TitleBar.this);
                    addMouseMotionListener(TitleBar.this);
                    
                }
                maxSize = !maxSize;
                mainFrame.setVisible(true);
                mainFrame.componentResized(null);
                mainFrame.requestFocusInWindow();
                mainFrame.setResizable(!maxSize);
            }
        });
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        rightPanel.add(minBtn);
        rightPanel.add(maxBtn);
        rightPanel.add(closeBtn);
        add(rightPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        backPage = new JButton(backPageIcon);
        prevPage = new JButton(prevPageIcon);
        reload = new JButton(reloadIcon);
        backPage.setMargin(new Insets(2, 2, 2, 2));
        prevPage.setMargin(new Insets(2, 2, 2, 2));
        reload.setMargin(new Insets(2, 2, 2, 2));
        backPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.back();
            }
        });
        prevPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.forward();
            }
        });
        reload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.reload();
            }
        });

        JPanel mainContainer = new JPanel();
        GridBagLayout layout = new GridBagLayout(); 
        mainContainer.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel label = new JLabel(" ");
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(label, gbc);
        mainContainer.add(label);
        gbc.gridx = (int) label.getSize().getWidth() + 10;
        gbc.gridy = 0;
        layout.setConstraints(backPage, gbc);
        mainContainer.add(backPage);
        gbc.gridx = 20 + (int) backPage.getSize().getWidth() + 5;
        gbc.gridy = 0;
        layout.setConstraints(prevPage, gbc);
        mainContainer.add(prevPage);
        gbc.gridx = 20 + ((int) backPage.getSize().getWidth() + 5) * 2;
        gbc.gridy = 0;
        layout.setConstraints(reload, gbc);
        mainContainer.add(reload);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(mainContainer, BorderLayout.LINE_START);
        add(mainPanel, BorderLayout.WEST);

        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public void mouseReleased(MouseEvent e) {
        mouseDownCompCoords = null;
    }

    public void mousePressed(MouseEvent e) {
        mouseDownCompCoords = e.getPoint();
    }

    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        mainFrame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
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

    public void setTitleText(String text) {
        titleText.setText(text);
    }

    public void setEnabled(boolean enabled) {
        if (mainFrame.isFullScreen()) {
            maxBtn.setEnabled(false);
        } else {
            maxBtn.setEnabled(enabled);
        }
        closeBtn.setEnabled(enabled);
    }

    public void close() {
        removeAll();
        for (ActionListener al : minBtn.getActionListeners()) {
            minBtn.removeActionListener(al);
        }
        for (ActionListener al : maxBtn.getActionListeners()) {
            maxBtn.removeActionListener(al);
        }
        for (ActionListener al : closeBtn.getActionListeners()) {
            closeBtn.removeActionListener(al);
        }
        removeMouseListener(this);
        removeMouseMotionListener(this);
    }

    public void initProgressBar() {
        progress = new JProgressBar(JProgressBar.HORIZONTAL);
        // サイズを設定
        progress.setPreferredSize(new Dimension(centerPanel.getWidth() > 300 ? 300 : centerPanel.getWidth(), centerPanel.getHeight() - 10));
        // 文字列表示化
        progress.setStringPainted(true);

        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());

        prgContainer = new JPanel();
        GridBagLayout layout = new GridBagLayout(); 
        prgContainer.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(progress, gbc);
        prgContainer.add(progress);

        prgPanel = new JPanel();
        prgPanel.setLayout(new BorderLayout());
        prgPanel.add(prgContainer, BorderLayout.LINE_START);

        centerPanel.add(prgPanel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    public void setProgressValue(int n) {
        progress.setValue(n);
        if (n == 100) {
            prgContainer.remove(progress);
            prgPanel.remove(prgContainer);
            centerPanel.remove(prgPanel);
            revalidate();
            repaint();
            centerPanel.add(mainPanel, BorderLayout.WEST);
        }
    }

    public boolean isMaxSize() {
        return maxSize;
    }

    public JButton getBackPage() {
        return backPage;
    }

    public JButton getPrevPage() {
        return prevPage;
    }
}
