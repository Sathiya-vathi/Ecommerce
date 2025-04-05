package com.website.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.website.entities.Order;
import com.website.entities.OrderItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.stream.Stream;

@Service
public class InvoiceService {

    public ByteArrayInputStream generateInvoice(Order order) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("Invoice - Order ID: " + order.getId(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // Line Break
            document.add(new Paragraph("Order Date: " + order.getOrderDate()));
            document.add(new Paragraph("Customer ID: " + order.getUser().getId()));
            document.add(new Paragraph(" "));

            // Table with Items
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 1, 2, 2});

            // Table Header
            Stream.of("Product", "Qty", "Price", "Total").forEach(header -> {
                PdfPCell cell = new PdfPCell();
                cell.setPhrase(new Phrase(header));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            });

            // Table Rows
            for (OrderItem item : order.getOrderItems()) {
                table.addCell(item.getProduct().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("₹" + item.getProduct().getPrice());
                BigDecimal Discount_total = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                table.addCell("₹" + Discount_total);
            }

            // Total Amount
            PdfPCell totalCell = new PdfPCell(new Phrase("Total"));
            totalCell.setColspan(3);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
            table.addCell("₹" + order.getTotalAmount());

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}

