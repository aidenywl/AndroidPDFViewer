package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class PDFimage extends ImageView {

    final String LOGNAME = "pdf_image";

    // drawing path
    Model model;
    IPath path = null;

    // image to display
    Bitmap bitmap;

    // For zoom gestures.
    float startDistance;
    Matrix matrix = new Matrix();
    Matrix startMatrix = new Matrix();
    Matrix stagingMatrix = new Matrix();
    Point zoomPoint;
    float stagingZoom = 1;
    boolean isZooming = false;
    boolean isPanning = false;
    Point startPanPoint;

    // constructor
    public PDFimage(Context context) {
        super(context);
        model = Model.getInstance();
        matrix.setScale(1, 1);
        startMatrix.set(matrix);
    }

    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Tool currentTool = model.getCurrentTool();
        if (event.getPointerCount() == 1) {
            Point docPoint = getDocumentCoord(event.getX(), event.getY());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(LOGNAME, "Action down");
                    if (currentTool == Tool.PEN) {
                        path = new PenPath(model.getCurrentPage());
                        path.moveTo(docPoint.x, docPoint.y);
                    } else if (currentTool == Tool.HIGHLIGHTER) {
                        path = new HighlightPath(model.getCurrentPage());
                        path.moveTo(docPoint.x, docPoint.y);
                    } else if (currentTool == Tool.ERASER) {
                        this.erase(docPoint.x, docPoint.y);
                    } else if (currentTool == Tool.PAN) {
                        // Save the starting point
                        isPanning = true;
                        startPanPoint = new Point(event.getX(), event.getY());
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(LOGNAME, "Action move");
                    if (currentTool == Tool.PEN || currentTool == Tool.HIGHLIGHTER) {
                        path.lineTo(docPoint.x, docPoint.y);
                    } else if (currentTool == Tool.ERASER) {
                        this.erase(docPoint.x, docPoint.y);
                    } else if (currentTool == Tool.PAN) {
                        // Calculate and save the distance from start point.
                        float dx = event.getX() - startPanPoint.x;
                        float dy = event.getY() - startPanPoint.y;
                        handleStagingPan(dx, dy);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(LOGNAME, "Action up");
                    if (currentTool == Tool.PEN || currentTool == Tool.HIGHLIGHTER) {
                        model.addPath(path);
                        path = null;
                    } else if (currentTool == Tool.PAN) {
                        // Commit the change.
                        isPanning = false;
                        this.matrix.set(stagingMatrix);
                    }
                    break;
            }
        } else if (event.getPointerCount() == 2) {
            // Handle Zooming
            float x1 = event.getX(0);
            float y1 = event.getY(0);
            float x2 = event.getX(1);
            float y2 = event.getY(1);
            float dist = PDFimage.dist(x1, y1, x2, y2);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    startDistance = dist;
                    Point mid = new Point((x1 + x2) / 2, (y1 + y2) / 2);
                    this.zoomPoint = mid;
                    break;
                case MotionEvent.ACTION_MOVE:
                    isZooming = true;
                    stagingZoom = dist / startDistance;
                    handleStagingZoom(this.zoomPoint, this.stagingZoom);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    isZooming = false;
                    startDistance = 0;
                    this.matrix.set(stagingMatrix);
                    break;
            }
        }
        return true;
    }

    private Point getDocumentCoord(float x, float y) {
        float[] point = new float[] {x, y};
        Matrix inverse = new Matrix();
        matrix.invert(inverse);
        inverse.mapPoints(point);
        return new Point(point[0], point[1]);
    }

    private void handleStagingZoom(Point zoomPoint, float stagingZoom) {
        float[] canvasValues = new float[9];
        stagingMatrix.set(matrix);
        matrix.getValues(canvasValues);
        stagingMatrix.postTranslate(-zoomPoint.x, -zoomPoint.y);
        stagingMatrix.postScale(stagingZoom, stagingZoom);
        stagingMatrix.postTranslate(zoomPoint.x, zoomPoint.y);
    }

    private void handleStagingPan(float dx, float dy) {
        float[] canvasValues = new float[9];
        stagingMatrix.set(matrix);
        matrix.getValues(canvasValues);
        stagingMatrix.postTranslate(dx, dy);
    }

    private void erase(float x, float y) {
        ArrayList<IPath> pathsToErase = new ArrayList<>();
        for (IPath path : model.getCurrentMarkups()) {
            // Check if it intersects.
            ArrayList<Point> points = path.getPoints();
            int strokeWidth = path.getStrokeWidth();
            float dist;

            for (Point point : points) {
                dist = dist(x, y, point.x, point.y);
                if (dist <= strokeWidth + 5) {
                    // The path has to be erased.
                    pathsToErase.add(path);
                    break;
                }
            }
        }
        // Erase the path.
        for (IPath pathToErase : pathsToErase) {
            model.removePath(pathToErase);
        }
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    // set image as background
    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void resetZoom() {
        this.matrix.set(startMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw background
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
        }
        if (isZooming || isPanning) {
            canvas.setMatrix(stagingMatrix);
        } else {
            canvas.setMatrix(matrix);
        }

        Paint p;
        for (IPath path : model.getCurrentMarkups()) {
            p = new Paint();
            p.setColor(path.getColor());
            p.setStyle(path.getPaintStyle());
            p.setAlpha(path.getAlpha());
            p.setStrokeWidth(path.getStrokeWidth());
            canvas.drawPath(path, p);
        }
        if (path != null) {
            p = new Paint();
            p.setColor(path.getColor());
            p.setStyle(path.getPaintStyle());
            p.setAlpha(path.getAlpha());
            p.setStrokeWidth(path.getStrokeWidth());
            canvas.drawPath(path, p);
        }

        super.onDraw(canvas);
    }
}
