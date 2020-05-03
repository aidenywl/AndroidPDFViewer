package net.codebot.pdfviewer;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

public class Model extends Observable {

    private static Model ourInstance;
    private History history;

    private Tool currentTool = Tool.PEN;
    // all markups on the canvas
    private HashMap<Integer, ArrayList<IPath>> markups;
    private int numPages;
    private int currentPage;

    static Model getInstance() {
        if (ourInstance == null) {
            ourInstance = new Model();
        }
        return ourInstance;
    }

    private Model() {
        markups = new HashMap<>();
        history = History.getInstance();
    }

    public void setPages(int numPages) {
        for (int i = 0; i < numPages; i++) {
            markups.put(i, new ArrayList<IPath>());
        }
        this.numPages = numPages;
        this.currentPage = 0;
    }

    /**
     * Get markups
     */
    public ArrayList<IPath> getCurrentMarkups() {
        return markups.get(this.currentPage);
    }



    public void setHighlighter() {
        this.currentTool = Tool.HIGHLIGHTER;
    }

    public void setPen() {
        this.currentTool = Tool.PEN;
    }

    public void setPan() {
        this.currentTool = Tool.PAN;
    }

    public void setEraser() {
        this.currentTool = Tool.ERASER;
    }

    public Tool getCurrentTool() {
        return this.currentTool;
    }
    public void initObservers() {
        setChanged();
        notifyObservers();
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public int getTotalPageCount() {
        return this.numPages;
    }

    public void addPath(IPath path) {
        // Add to markups
        // Ensure that they are in the right order.
        addPathWithoutHistory(path);
        // add to history.
        Command c = new Command(CommandType.ADD, path);
        history.addCommand(c);
    }

    private void addPathWithoutHistory(IPath path) {
        ArrayList<IPath> markups = this.markups.get(path.getPageNum());
        long pathTime = path.getCreatedTime();
        int counter = 0;

        for (IPath p : markups) {
            counter++;
            long pTime = p.getCreatedTime();
            if (pTime > pathTime) {
                break;
            }
        }
        markups.add(counter, path);
    }

    public void removePath(IPath path) {
        removePathWithoutHistory(path);
        Command c = new Command(CommandType.DELETE, path);
        history.addCommand(c);
    }

    private void removePathWithoutHistory(IPath path) {
        markups.get(path.getPageNum()).remove(path);
    }

    public void pageBack(PDFimage pdfimage) {
        if (currentPage == 0) {
            return; // do nothing
        }
        this.currentPage--;
        pdfimage.resetZoom();
        setChanged();
        notifyObservers();
    }

    public void pageNext(PDFimage pdfimage) {
        if (currentPage + 1 == numPages) {
            // Do nothing
            return;
        }
        this.currentPage++;
        pdfimage.resetZoom();
        setChanged();
        notifyObservers();
    }

    public void undoAction() {
        if (!history.hasUndoHistory()) {
            return;
        }
        Command c = history.popUndo();
        this.currentPage = c.path.getPageNum();
        if (c.commandType == CommandType.ADD) {
            // Remove from the model.
            removePathWithoutHistory(c.path);
        } else if (c.commandType == CommandType.DELETE) {
            addPathWithoutHistory(c.path);
        }
        setChanged();
        notifyObservers();

    }

    public void redoAction() {
        if (!history.hasRedoHistory()) {
            return;
        }
        Command c = history.popRedo();
        this.currentPage = c.path.getPageNum();
        if (c.commandType == CommandType.ADD) {
            addPathWithoutHistory(c.path);
        } else if (c.commandType == CommandType.DELETE) {
            removePathWithoutHistory(c.path);
        }
        setChanged();
        notifyObservers();
    }


    public HashMap<Integer, ArrayList<IPath>> getAllMarkups() {
        return this.markups;
    }

    public void restoreMarkups(HashMap<Integer, ArrayList<IPath>> markups) {
        this.markups = markups;
    }
}
