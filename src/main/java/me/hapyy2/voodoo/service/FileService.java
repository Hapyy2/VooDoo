package me.hapyy2.voodoo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.exception.BaseException;
import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.User;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final TaskRepository taskRepository;
    private final UserHelper userHelper;
    private final Path fileStorageLocation = Paths.get("/app/uploads").toAbsolutePath().normalize();

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportTasksToCsv() {
        User currentUser = userHelper.getCurrentUser();
        List<Task> tasks = taskRepository.findAllByUser(currentUser, Pageable.unpaged()).getContent();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, false, StandardCharsets.UTF_8)) {
            writer.println("ID,Title,Status,Category,Original File");
            for (Task task : tasks) {
                writer.printf("%d,\"%s\",%s,\"%s\",\"%s\"%n",
                        task.getId(),
                        task.getTitle(),
                        task.getStatus(),
                        task.getCategory() != null ? task.getCategory().getName() : "",
                        task.getOriginalFilename() != null ? task.getOriginalFilename() : ""
                );
            }
            writer.flush();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportTasksToPdf() {
        User currentUser = userHelper.getCurrentUser();
        List<Task> tasks = taskRepository.findAllByUser(currentUser, Pageable.unpaged()).getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph title = new Paragraph("Task Report - VooDoo Manager", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 2, 2});

            addTableHeader(table, "ID");
            addTableHeader(table, "Title");
            addTableHeader(table, "Status");
            addTableHeader(table, "Category");
            addTableHeader(table, "Due Date");

            for (Task task : tasks) {
                table.addCell(String.valueOf(task.getId()));
                table.addCell(task.getTitle());
                table.addCell(String.valueOf(task.getStatus()));
                table.addCell(task.getCategory() != null ? task.getCategory().getName() : "-");
                table.addCell(task.getDueDate() != null ? task.getDueDate().toString().replace("T", " ") : "-");
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new BaseException("Error generating PDF", HttpStatus.INTERNAL_SERVER_ERROR) {};
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle));
        header.setPadding(5);
        table.addCell(header);
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) return null;
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException ex) {
            throw new BaseException("Could not store file", HttpStatus.INTERNAL_SERVER_ERROR) {};
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) return resource;
            else throw new BaseException("File not found", HttpStatus.NOT_FOUND) {};
        } catch (MalformedURLException ex) {
            throw new BaseException("File not found", HttpStatus.NOT_FOUND) {};
        }
    }
}