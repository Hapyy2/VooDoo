package me.hapyy2.voodoo.service;

import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportTasksToCsv() {
        List<Task> tasks = taskRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PrintWriter writer = new PrintWriter(out, false, StandardCharsets.UTF_8)) {

            writer.println("ID,Title,Description,Status,Category,Due Date");

            for (Task task : tasks) {
                writer.printf("%d,\"%s\",\"%s\",%s,\"%s\",%s%n",
                        task.getId(),
                        escapeSpecialCharacters(task.getTitle()),
                        escapeSpecialCharacters(task.getDescription()), // Dodajmy opis
                        task.getStatus(),
                        task.getCategory() != null ? escapeSpecialCharacters(task.getCategory().getName()) : "",
                        task.getDueDate() != null ? task.getDueDate().toString() : ""
                );
            }

            writer.flush();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}