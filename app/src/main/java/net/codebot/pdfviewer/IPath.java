package net.codebot.pdfviewer;

import android.graphics.Paint;
import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class IPath extends Path implements Serializable {
    private ArrayList<Point> points = new ArrayList<>();
    private int page;
    private final long createdTime;
    private final Tool type;

    IPath(int page, Tool type) {
        super();
        this.page = page;
        createdTime = System.currentTimeMillis();
        this.type = type;
    }

    public int getColor() {
        if (this.type == Tool.HIGHLIGHTER) {
            return HighlightPath.color;
        } else if (this.type == Tool.PEN) {
            return PenPath.color;
        }
        return -1;
    }

    public Paint.Style getPaintStyle() {
        if (this.type == Tool.HIGHLIGHTER) {
            return HighlightPath.style;
        } else if (this.type == Tool.PEN) {
            return PenPath.style;
        }
        return null;
    }

    public int getAlpha() {
        if (this.type == Tool.HIGHLIGHTER) {
            return HighlightPath.alpha;
        } else if (this.type == Tool.PEN) {
            return PenPath.alpha;
        }
        return -1;
    };


    public int getStrokeWidth() {
        if (this.type == Tool.HIGHLIGHTER) {
            return HighlightPath.strokeWidth;
        } else if (this.type == Tool.PEN) {
            return PenPath.strokeWidth;
        }
        return -1;
    };

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
        points.add(new Point(x, y));
    }

    public ArrayList<Point> getPoints() {
        return this.points;
    }

    public int getPageNum() {
        return this.page;
    }

    public long getCreatedTime() {
        return this.createdTime;
    }

    public void recreatePath() {
        super.reset();
        boolean first = true;
        for (Point p : points) {
            if (first) {
                super.moveTo(p.x, p.y);
                first = false;
            }
            super.lineTo(p.x, p.y);
        }
    }


}
