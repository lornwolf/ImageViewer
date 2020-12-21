package com.lornwolf.common;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Utils {

    public static BufferedImage createResizedCopy(Image originalImage, 
        int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        // 此行为测试用代码。
        // BufferedImage scaledBI = BigBufferedImage.create(scaledWidth, scaledHeight, imageType);
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        // 消除锯齿。
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage.getScaledInstance(scaledWidth, scaledHeight, BufferedImage.SCALE_SMOOTH), 0, 0,null);
        // 要及时释放资源。
        g.dispose();
        originalImage = null;
        return scaledBI;
    }

    public static byte[] hexToByteArr(String hexStr) {
        String HexStr = "0123456789ABCDEF";
        char[] charArr = hexStr.toCharArray();
        byte btArr[] = new byte[charArr.length / 2];
        int index = 0;
        for (int i = 0; i < charArr.length; i++) {
            int highBit = HexStr.indexOf(charArr[i]);
            int lowBit = HexStr.indexOf(charArr[++i]);
            btArr[index] = (byte) (highBit << 4 | lowBit);
            index++;
        }
        return btArr;
    }
}
