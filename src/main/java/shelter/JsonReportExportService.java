package shelter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class JsonReportExportService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public Path export(Path outputDir, ShelterReportData report) throws IOException {
        Files.createDirectories(outputDir);
        String fileName = "upload-summary-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".json";
        Path outputPath = outputDir.resolve(fileName);

        JsonSerializer<Optional<?>> optionalSerializer = (src, typeOfSrc, context) ->
                src.isPresent() ? context.serialize(src.get()) : JsonNull.INSTANCE;

        JsonSerializer<LocalDate> localDateSerializer = (src, typeOfSrc, context) ->
                new JsonPrimitive(src.toString());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Optional.class, optionalSerializer)
                .registerTypeAdapter(LocalDate.class, localDateSerializer)
                .create();
        String json = gson.toJson(report);
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);

        return outputPath;
    }
}