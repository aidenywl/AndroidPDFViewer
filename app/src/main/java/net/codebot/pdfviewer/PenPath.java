package net.codebot.pdfviewer;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;

public class PenPath extends IPath {
    public static final Paint.Style style = Paint.Style.STROKE;
    public static final int color = Color.BLUE;
    public static final int alpha = 255;
    public static final int strokeWidth = 5;

    public PenPath(int path) {
        super(path, Tool.PEN);
    }
}
