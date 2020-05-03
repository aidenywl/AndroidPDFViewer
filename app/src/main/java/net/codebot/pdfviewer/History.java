package net.codebot.pdfviewer;

import java.util.ArrayList;

public class History {
    ArrayList<Command> undoHistory;
    ArrayList<Command> redoHistory;
    private static History ourInstance;

    static History getInstance() {
        if (ourInstance == null) {
            ourInstance = new History();
        }
        return ourInstance;
    }

    private History() {
        undoHistory = new ArrayList<>();
        redoHistory = new ArrayList<>();
    }

    public void addCommand(Command c) {
        // Clear undo list.
        redoHistory.clear();
        // Add command to undo history.
        undoHistory.add(c);
    }

    public boolean hasUndoHistory() {
        return this.undoHistory.size() > 0;
    }

    public boolean hasRedoHistory() {
        return this.redoHistory.size() > 0;
    }

    public Command popUndo() {
        // Get next undo
        Command c = undoHistory.remove(undoHistory.size() - 1);
        // Add to redo
        redoHistory.add(c);
        return c;
    }

    public Command popRedo() {
        Command c = redoHistory.remove(redoHistory.size() - 1);
        undoHistory.add(c);
        return c;
    }
}
