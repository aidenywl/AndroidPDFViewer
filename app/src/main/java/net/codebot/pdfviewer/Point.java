package net.codebot.pdfviewer;

import java.io.Serializable;

public class Point implements Serializable {
    public float x;
    public float y;

    Point(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
