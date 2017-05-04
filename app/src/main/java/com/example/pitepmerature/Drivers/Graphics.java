package com.example.pitepmerature.Drivers;


import android.util.Log;

import com.example.pitepmerature.font.Font;
import com.google.android.things.contrib.driver.ssd1306.Ssd1306;

import java.nio.charset.Charset;

/**
 * Created by Celestin on 2/05/2017.
 */

public class Graphics {


    public static void drawFastVLine(Ssd1306 ssd1306, int x, int y, int h) {
        line(ssd1306, x, y, x, y + h - 1);

    }

    public static void fillRect(Ssd1306 ssd1306, float x, float y, float w, float h) {

        for (float i = x; i < x + w; i++) {
            drawFastVLine(ssd1306, Math.round(i), Math.round(y), Math.round(h));
        }
    }


    public static void drawTextNew(Ssd1306 ssd1306, int x, int y, Font font, String text, float size) {

        int offset = 6;
        byte [] bytes = text.getBytes(Charset.forName(font.getName()));

        for(int i = 0;i < text.length(); i++ ){
            drawChar(ssd1306, Math.round(x +(offset * size))*i ,y , font, bytes[i], size);
        }


    }

    // draw a character
    public static void drawChar(Ssd1306 ssd1306, int x, int y, Font font, byte c, float size) {

        int[] glyphs = font.getGlyphs();

        if ((x >= 128) || // Clip right
                (y >= 64) || // Clip bottom
                ((x + (5 * size) - 1) < 0) || // Clip left
                ((y + (8 * size) - 1) < 0)) // Clip top
        {
            return;
        }

        for (int i = 0; i < 6; i++) {
            int line;
            if (i == 5) {
                line = 0x0;
            } else {
                line = glyphs[(c * 5) + i];
            }
            for (int j = 0; j < 8; j++) {
                if ((line & 0x01) == 0x01) {
                    if (size == 1) // default size
                    {
                        ssd1306.setPixel(x + i, y + j, true);
                    } else {  // big size
                        fillRect(ssd1306, x + (i * size),y + (j * size), size, size);

                        if(((Math.round(((j+1) * size))) - (Math.round((j * size)))) > 1 ){
                            fillRect(ssd1306, x + (i * size),(y + 1 + (j * size)), size, size);
                        }
                    }

                }

                line >>= 1;
            }
        }
    }




    /**
     * Draw a line from one point to another.
     *
     * @param x0 The X position of the first point.
     * @param y0 The Y position of the first point.
     * @param x1 The X position of the second point.
     * @param y1 The Y position of the second point.
     */
    public static void line(Ssd1306 ssd1306,int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;

        if(dx == 0 && dy == 0) {
            ssd1306.setPixel(x0, y0, true);

            return;
        }

        if(dx == 0) {
            for(int y = Math.min(y0, y1); y <= Math.max(y1, y0); y++) {
                ssd1306.setPixel(x0, y, true);
            }
        } else if(dy == 0) {
            for(int x = Math.min(x0, x1); x <= Math.max(x1, x0); x++) {
                ssd1306.setPixel(x, y0, true);
            }
        } else if(Math.abs(dx) >= Math.abs(dy)) {
            if(dx < 0) {
                int ox = x0;
                int oy = y0;
                x0 = x1;
                y0 = y1;
                x1 = ox;
                y1 = oy;
                dx = x1 - x0;
                dy = y1 - y0;
            }

            double coeff = (double) dy / (double) dx;

            for(int x = 0; x <= dx; x++) {
                ssd1306.setPixel(x + x0, y0 + (int) Math.round(x * coeff), true);
            }
        } else if(Math.abs(dx) < Math.abs(dy)) {
            if(dy < 0) {
                int ox = x0;
                int oy = y0;
                x0 = x1;
                y0 = y1;
                x1 = ox;
                y1 = oy;
                dx = x1 - x0;
                dy = y1 - y0;
            }

            double coeff = (double) dx / (double) dy;

            for(int y = 0; y <= dy; y++) {
                ssd1306.setPixel(x0 + (int) Math.round(y * coeff), y + y0, true);
            }
        }
    }

    /**
     * Draw a rectangle.
     *
     * @param x The X position of the rectangle.
     * @param y The Y position of the rectangle.
     * @param width The width of the rectangle in pixels.
     * @param height The height of the rectangle in pixels.
     * @param fill Whether to draw a filled rectangle.
     */
    public static void rectangle(Ssd1306 ssd1306, int x, int y, int width, int height, boolean fill) {
        if(fill) {
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    ssd1306.setPixel(x + i, y + j, true);
                }
            }
        } else if(width > 0 && height > 0) {
            line(ssd1306, x, y, x, y + height - 1);
            line(ssd1306,x, y + height - 1, x + width - 1, y + height - 1);
            line(ssd1306,x + width - 1, y + height - 1, x + width - 1, y);
            line(ssd1306,x + width - 1, y, x, y);
        }
    }

    /**
     * Draw an arc.
     *
     * @param x The X position of the center of the arc.
     * @param y The Y position of the center of the arc.
     * @param radius The radius of the arc in pixels.
     * @param startAngle The starting angle of the arc in degrees.
     * @param endAngle The ending angle of the arc in degrees.
     */
    public static void arc(Ssd1306 ssd1306, int x, int y, int radius, int startAngle, int endAngle) {
        for(int i = startAngle; i <= endAngle; i++) {
            ssd1306.setPixel(x + (int) Math.round(radius * Math.sin(Math.toRadians(i))), y + (int) Math.round(radius * Math.cos(Math.toRadians(i))), true);
        }
    }

    /**
     * Draw a circle.
     * This is the same as calling arc() with a start and end angle of 0 and 360 respectively.
     *
     * @param x The X position of the center of the circle.
     * @param y The Y position of the center of the circle.
     * @param radius The radius of the circle in pixels.
     */
    public static void circle(Ssd1306 ssd1306, int x, int y, int radius) {
        arc(ssd1306, x, y, radius, 0, 360);
    }


}
