package net.codebot.pdfviewer;

public class Command {
    public final CommandType commandType;
    public final IPath path;

    public Command(CommandType commandType, IPath path) {
        this.commandType = commandType;
        this.path = path;
    }
}
