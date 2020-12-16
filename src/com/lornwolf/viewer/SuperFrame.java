package com.lornwolf.viewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

/**
 * Frameの親クラス。
 */
public abstract class SuperFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * すべてのFrameを保存するリスト。
     */
    private static List<SuperFrame> windows = new ArrayList<SuperFrame>();

    /**
     * 子クラスから呼び出されるsetVisibleを再定義する。
     */
    @Override
    public void setVisible(boolean b) {
        if (b) {
            if (!windows.contains(this)) {
                windows.add(this);
            }
        }
        showWindow(b);
    }

    /**
     * 本クラスに利用され、子クラス.showSelf()の形で、Frameの表示・非表示を制御する。
     */
    public void showSelf(boolean b) {
        showWindow(b);
    }

    /**
     * FrameをHideする。
     * 
     * 子クラスが継承できなくて、確実にJFrameのメソッドを呼び出せる。
     */
    private void showWindow(boolean b) {
        super.setVisible(b);
    }

    /**
     * 子クラスから呼び出されるdisposeを再定義する。
     */
    @Override
    public void dispose() {
        if (windows.contains(this)) {
            windows.remove(this);
        }
        disposeWindow();
    }

    /**
     * Frameをdisposeする。
     *  
     * 子クラスが継承できなくて、確実にJFrameのメソッドを呼び出せる。
     */
    private void disposeWindow() {
        super.dispose();
    }

    /**
     * すべてのFrameをHideする。
     */
    public static void hideAll() {
        for (SuperFrame frame : windows) {
            frame.showSelf(false);
        }
    }

    /**
     * すべてのFrameを表示する。
     */
    public static void showAll() {
        for (SuperFrame frame : windows) {
            frame.showSelf(true);
        }
    }

    /**
     * フォント変更。
     */
    public void updateFont() {}

    public static boolean isDisposed(SuperFrame obj) {
        return !windows.contains(obj);
    }
}
