package com.example.pitepmerature;

import com.example.pitepmerature.font.Font;
import com.google.android.things.contrib.driver.ssd1306.Ssd1306;

import java.nio.charset.Charset;

/**
 * Created by Celestin on 2/05/2017.
 */

public class Graphics {



    public static void text(Ssd1306 ssd1306,int x, int y, Font font, String text) {
        int rows = font.getRows();
        int cols = font.getColumns();
        int[] glyphs = font.getGlyphs();
        byte[] bytes = text.getBytes(Charset.forName(font.getName()));

        for(int i = 0; i < text.length(); i++) {
            int p = (bytes[i] & 0xFF) * cols;

            for(int col = 0; col < cols; col++) {
                int mask = glyphs[p++];

                for(int row = 0; row < rows; row++) {
                    ssd1306.setPixel(x, y + row, (mask & 1) == 1);
                    mask >>= 1;
                }

                x++;
            }

            x++;
        }
    }
}
