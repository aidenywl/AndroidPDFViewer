package net.codebot.pdfviewer;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provied them with this code.

public class MainActivity extends AppCompatActivity implements Observer {

    final String LOGNAME = "pdf_viewer";
    final String FILENAME = "shannon1948.pdf";
    final int FILERESID = R.raw.shannon1948;

    // manage the pages of the PDF, see below
    PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer.Page currentPage;

    Model model;
    Menu menu;

    // custom ImageView class that captures strokes and draws them over the image
    PDFimage pageImage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = Model.getInstance();
        model.addObserver(this);

        LinearLayout layout = findViewById(R.id.pdfLayout);
        pageImage = new PDFimage(this);
        layout.addView(pageImage);
        layout.setEnabled(true);
        pageImage.setMinimumWidth(1000);
        pageImage.setMinimumHeight(2000);
        pageImage.setAdjustViewBounds(true);

        // open page 0 of the PDF
        // it will be displayed as an image in the pageImage (above)
        model.addObserver(this);
        try {
            openRenderer(this);
            model.setPages(pdfRenderer.getPageCount());
            // Restore application state if any.
            File file = new File(getFilesDir(), "pdfviewerState.data");
            if (file.exists()) {
                ObjectInputStream inFile = new ObjectInputStream(new FileInputStream(file));
                Object o = inFile.readObject();
                if (o instanceof HashMap) {
                    HashMap<Integer, ArrayList<IPath>> markups = (HashMap<Integer, ArrayList<IPath>>) o;
                    // Restore the path in each markup.
                    recreatePaths(markups);
                    model.restoreMarkups(markups);
                }

                inFile.close();
            }
            model.initObservers();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening file: " + exception);
        } catch (ClassNotFoundException exception) {
            Log.d(LOGNAME, "Error restoring model");
        }
    }

    private void recreatePaths(HashMap<Integer, ArrayList<IPath>> markups) {
        for (ArrayList<IPath> markup : markups.values()) {
            for (IPath path : markup) {
                path.recreatePath();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            InputStream asset = this.getResources().openRawResource(FILERESID);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    // do this before you quit!
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            currentPage.close();
        }
        pdfRenderer.close();
        parcelFileDescriptor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        // Close the current page before opening another one.
        if (null != currentPage) {
            currentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);

        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // Display the page
        pageImage.setImage(bitmap);
    }

    public void updatePageNumber(int index, int numPages) {
        final TextView pageNumTextView = (TextView) findViewById(R.id.page_number);
        String pageNumString = (index + 1) + " / " + numPages;
        pageNumTextView.setText(pageNumString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem m;
        switch (item.getItemId()) {
            case R.id.action_pen:
                deselectAllMenuItems();
                m = menu.findItem(R.id.action_pen);;
                m.setIcon(R.drawable.tool_pen_selected);
                model.setPen();
                break;
            case R.id.action_highlight:
                deselectAllMenuItems();
                m = menu.findItem(R.id.action_highlight);;
                m.setIcon(R.drawable.tool_highlighter_selected);
                model.setHighlighter();
                break;
            case R.id.action_erase:
                deselectAllMenuItems();
                m = menu.findItem(R.id.action_erase);;
                m.setIcon(R.drawable.tool_eraser_selected);
                model.setEraser();
                break;
            case R.id.action_pan:
                deselectAllMenuItems();
                m = menu.findItem(R.id.action_pan);;
                m.setIcon(R.drawable.tool_pan_selected);
                model.setPan();
                break;
            case R.id.undo:
                model.undoAction();
                break;
            case R.id.redo:
                // redo
                model.redoAction();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deselectAllMenuItems() {
        System.out.println("DESELECTEING");
        MenuItem pan = menu.findItem(R.id.action_pan);
        pan.setIcon(R.drawable.tool_pan);

        MenuItem pen = menu.findItem(R.id.action_pen);
        pen.setIcon(R.drawable.tool_pen);

        MenuItem highlighter = menu.findItem(R.id.action_highlight);
        highlighter.setIcon(R.drawable.tool_highlighter);

        MenuItem eraser = menu.findItem(R.id.action_erase);
        eraser.setIcon(R.drawable.tool_eraser);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            File file = new File(getFilesDir(), "pdfviewerState.data");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(model.getAllMarkups());
            oos.close();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error saving application state.");
        }
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onDestroy() {
        super.onDestroy();
        try {
            closeRenderer();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening PDF");
        }
        model.deleteObserver(this);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void update(Observable o, Object arg) {
        // Update
        int pageToDisplay = model.getCurrentPage();
        showPage(pageToDisplay);
        updatePageNumber(model.getCurrentPage(), model.getTotalPageCount());
        return;
    }

    /** Called when the user touches the back button. */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void pageBack(View view) {
        model.pageBack(pageImage);
    }

    /** Called when the user touches the next button. */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void pageNext(View view) {
        model.pageNext(pageImage);
    }
}
