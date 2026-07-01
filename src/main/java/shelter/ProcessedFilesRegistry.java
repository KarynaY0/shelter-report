package shelter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessedFilesRegistry {

    private final Path registryPath;
    private final Set<String> processedFiles = new HashSet<>();

    public ProcessedFilesRegistry(Path registryPath) throws IOException {
        this.registryPath = registryPath;
        if (Files.exists(registryPath)) {
            List<String> lines = Files.readAllLines(registryPath, StandardCharsets.UTF_8);
            processedFiles.addAll(lines);
        }
    }

    public boolean isProcessed(String fileName) {
        return processedFiles.contains(fileName);
    }

    public void markProcessed(String fileName) throws IOException {
        processedFiles.add(fileName);
        Files.createDirectories(registryPath.getParent());
        Files.write(registryPath, processedFiles, StandardCharsets.UTF_8);
    }
}
