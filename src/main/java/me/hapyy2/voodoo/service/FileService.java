package me.hapyy2.voodoo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import me.hapyy2.voodoo.exception.BaseException;
import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.User;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final TaskRepository taskRepository;
    private final UserHelper userHelper;
    private final Path fileStorageLocation;

    public FileService(TaskRepository taskRepository, UserHelper userHelper) {
        this.taskRepository = taskRepository;
        this.userHelper = userHelper;
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
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

    public ByteArrayInputStream exportTasksToCsv() {
        User user = userHelper.getCurrentUser();
        List<Task> tasks = taskRepository.findAllByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             java.io.PrintWriter writer = new java.io.PrintWriter(out)) {

            writer.println("ID,Title,Status,Due Date,Category");
            for (Task task : tasks) {
                writer.printf("%d,\"%s\",%s,%s,%s\n",
                        task.getId(),
                        task.getTitle(),
                        task.getStatus(),
                        task.getDueDate(),
                        task.getCategory() != null ? task.getCategory().getName() : "None"
                );
            }
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new BaseException("Failed to export CSV", HttpStatus.INTERNAL_SERVER_ERROR) {};
        }
    }

    public ByteArrayInputStream exportTasksToPdf() {
        User user = userHelper.getCurrentUser();
        List<Task> tasks = taskRepository.findAllByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph para = new Paragraph("Task Report", font);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            addTableHeader(table, "ID");
            addTableHeader(table, "Title");
            addTableHeader(table, "Status");
            addTableHeader(table, "Date");
            addTableHeader(table, "Category");

            for (Task task : tasks) {
                table.addCell(String.valueOf(task.getId()));
                table.addCell(task.getTitle());
                table.addCell(String.valueOf(task.getStatus()));
                table.addCell(String.valueOf(task.getDueDate()));
                table.addCell(task.getCategory() != null ? task.getCategory().getName() : "");
            }

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            throw new BaseException("Error generating PDF", HttpStatus.INTERNAL_SERVER_ERROR) {};
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setPhrase(new Phrase(headerTitle));
        header.setPadding(5);
        table.addCell(header);
    }
}