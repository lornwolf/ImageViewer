package com.lornwolf.common;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <p>
 * 文件名称: WindowCloseAdapter.java
 * </p>
 * <p>
 * 文件描述: 窗口关闭器。
 * <p>
 * 用于关闭对话框，并回收其资源。
 * </p>
 */
public class WindowCloseAdapter extends WindowAdapter {
    /**
     * 默认构造方法。
     */
    public WindowCloseAdapter() {
        super();
    }

    /**
     * 带是否释放资源参数的构造方法。
     * 
     * @param dispose boolean
     */
    public WindowCloseAdapter(boolean dispose) {
        super();
        this.dispose = dispose;
    }

    /**
     * @see java.awt.event.WindowListener#windowClosing(WindowEvent)
     * @param e
     */
    public void windowClosing(WindowEvent e) {
        Window source = e.getWindow();
        if (source != null) {
            if (!dispose) {
                source.hide();
                return;
            }

            if (source instanceof UIFreeable) {
                ((UIFreeable) source).freeResource();
            }
            source.dispose();
            UIReleaseUtil.freeSwingObject(source);
        }
    }

    // 关闭的时候是否释放资源
    private boolean dispose = true;
}