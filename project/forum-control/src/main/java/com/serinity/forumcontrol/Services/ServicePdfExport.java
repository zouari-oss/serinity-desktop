package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Models.Reply;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to export threads as PDF documents
 * Uses Apache PDFBox library
 */
public class ServicePdfExport {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 12;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADING_FONT_SIZE = 14;
    private static final float LINE_SPACING = 1.5f;

    /**
     * Export a thread to PDF
     *
     * @param thread The thread to export
     * @param replies List of replies (optional, can be null)
     * @param outputPath Where to save the PDF
     * @return true if successful, false otherwise
     */
    public boolean exportThreadToPdf(Thread thread, List<Reply> replies, String outputPath) {
        try (PDDocument document = new PDDocument()) {

            // Create first page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Start content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            float pageWidth = page.getMediaBox().getWidth() - 2 * MARGIN;

            // Title
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), TITLE_FONT_SIZE);
            yPosition = writeText(contentStream, thread.getTitle(), MARGIN, yPosition, pageWidth, TITLE_FONT_SIZE);
            yPosition -= 20; // Extra space after title

            // Metadata
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            String metadata = String.format("Author: %s | Category: %s | Created: %s",
                    thread.getUserId(),
                    "Category", // You can get actual category name
                    dateFormat.format(thread.getCreatedAt()));
            yPosition = writeText(contentStream, metadata, MARGIN, yPosition, pageWidth, 10);
            yPosition -= 15;

            // Status and Type
            String info = String.format("Status: %s | Type: %s | Likes: %d | Comments: %d",
                    thread.getStatus(),
                    thread.getType(),
                    thread.getLikecount(),
                    thread.getRepliescount());
            yPosition = writeText(contentStream, info, MARGIN, yPosition, pageWidth, 10);
            yPosition -= 20;

            // Separator line
            contentStream.moveTo(MARGIN, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Content heading
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), HEADING_FONT_SIZE);
            yPosition = writeText(contentStream, "Content:", MARGIN, yPosition, pageWidth, HEADING_FONT_SIZE);
            yPosition -= 10;

            // Content text
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
            yPosition = writeWrappedText(contentStream, document, thread.getContent(),
                    MARGIN, yPosition, pageWidth, FONT_SIZE);

            contentStream.close();

            // Add replies if provided
            if (replies != null && !replies.isEmpty()) {
                yPosition = addRepliesSection(document, replies, yPosition);
            }

            // Save document
            document.save(outputPath);
            System.out.println("✅ PDF exported successfully: " + outputPath);
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add replies section to PDF
     */
    private float addRepliesSection(PDDocument document, List<Reply> replies, float yPosition) throws IOException {
        PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);
        PDPageContentStream contentStream = new PDPageContentStream(
                document, currentPage, PDPageContentStream.AppendMode.APPEND, true);

        float pageWidth = currentPage.getMediaBox().getWidth() - 2 * MARGIN;

        // Check if we need a new page
        if (yPosition < 100) {
            contentStream.close();
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            contentStream = new PDPageContentStream(document, newPage);
            yPosition = newPage.getMediaBox().getHeight() - MARGIN;
        }

        yPosition -= 20;

        // Replies heading
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), HEADING_FONT_SIZE);
        yPosition = writeText(contentStream, "Replies (" + replies.size() + "):",
                MARGIN, yPosition, pageWidth, HEADING_FONT_SIZE);
        yPosition -= 15;

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);

        // Add each reply
        for (int i = 0; i < replies.size(); i++) {
            Reply reply = replies.get(i);

            // Check if we need a new page
            if (yPosition < 150) {
                contentStream.close();
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = newPage.getMediaBox().getHeight() - MARGIN;
            }

            // Reply header
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            String replyHeader = String.format("Reply #%d by %s - %s",
                    i + 1, reply.getUserId(), dateFormat.format(reply.getCreatedAt()));

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            yPosition = writeText(contentStream, replyHeader, MARGIN, yPosition, pageWidth, 11);
            yPosition -= 5;

            // Reply content
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
            yPosition = writeWrappedText(contentStream, document, reply.getContent(),
                    MARGIN + 10, yPosition, pageWidth - 10, FONT_SIZE);
            yPosition -= 15; // Space between replies
        }

        contentStream.close();
        return yPosition;
    }

    /**
     * Write text with automatic wrapping
     */
    private float writeWrappedText(PDPageContentStream contentStream, PDDocument document,
                                   String text, float x, float y, float width, float fontSize) throws IOException {

        if (text == null || text.isEmpty()) {
            return y;
        }

        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            List<String> lines = wrapText(paragraph, width, fontSize);

            for (String line : lines) {
                // Check if we need a new page
                if (y < MARGIN + 50) {
                    contentStream.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    contentStream = new PDPageContentStream(document, newPage);
                    y = newPage.getMediaBox().getHeight() - MARGIN;
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(line);
                contentStream.endText();

                y -= fontSize * LINE_SPACING;
            }

            y -= 5; // Extra space between paragraphs
        }

        return y;
    }

    /**
     * Write single line of text
     */
    private float writeText(PDPageContentStream contentStream, String text,
                            float x, float y, float width, float fontSize) throws IOException {

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();

        return y - (fontSize * LINE_SPACING);
    }

    /**
     * Wrap text to fit within width
     */
    private List<String> wrapText(String text, float width, float fontSize) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;

            // Estimate width (rough calculation)
            float estimatedWidth = testLine.length() * fontSize * 0.5f;

            if (estimatedWidth < width) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Generate safe filename from thread title
     */
    public String generateFilename(Thread thread) {
        String title = thread.getTitle();

        // Remove invalid filename characters
        title = title.replaceAll("[^a-zA-Z0-9\\s-]", "");

        // Replace spaces with underscores
        title = title.replaceAll("\\s+", "_");

        // Limit length
        if (title.length() > 50) {
            title = title.substring(0, 50);
        }

        // Add timestamp to make unique
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

        return title + "_" + timestamp + ".pdf";
    }
}