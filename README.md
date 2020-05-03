# PDFViewer: App for viewing PDFs

Android mobile PDF reader where a user can read and annotate a document on an Android Tablet.

# Implemented Features:
- Undo/Redo: Undo the last change that was made to the document. The user should be able to undo at least the last 5 actions that were performed. Redo can be used to revert the undo. A command pattern is used for the change record with a reverse undo strategy.
- Drawing: The user can write on the screen with a thin line in blue ink, allowing them to write notes or annotate the document. The user cannot change the color or thickness of the line, so select line characteristics that is appropriate for writing on the PDF.
- Highlighting: The user can draw over the existing document with a thick, transparent yellow brush that allows the user to highlight the text in the PDF. Make sure that the highlighter is transparent enough that the text beneath it remains visible!
- Erase: The user should be able to erase an existing drawing or highlighting (note that 'undo' does not replace this feature).
- Zoom & Pan: The user can use two fingers to zoom-in and zoom-out (by bringing their fingers closer together or spreading them apart over the area of interest). When zoomed-in, users can pan around to reposition the document. These gestures should behave the same as standard pan-and-zoom.


# Development Information

Tablet designed for: Pixel C.

Java Vendor: AdoptOpenJDK

Java version: 11.0.6

Android Version used: Android 10.0


# Image Attributions

The image for the highlighter and eraser is from www.flaticon.com

# Testing

An APK file is provided named PDFViewer.apk in the root directory which can be installed on any Android tablet emulator or android tablet to test.
