package net.codebot.pdfviewer;

import android.graphics.Color;
import android.graphics.Paint;

public class HighlightPath extends IPath {
    public static final Paint.Style style = Paint.Style.STROKE;
    public static final int color = Color.YELLOW;
    public static final int alpha = 100;
    public static final int strokeWidth = 40;

    public HighlightPath(int pageNum) {
        super(pageNum, Tool.HIGHLIGHTER);
    }
}
