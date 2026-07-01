package shelter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record ShelterReportData(
        int totalImported,
        int totalSkipped,
        Set<String> uniqueSpecies,
        Map<String, Long> vaccinatedCounts,
        Map<String, Long> unvaccinatedCounts,
        Map<String, Optional<Animal>> oldestPerSpecies,
        List<String> animalsNeedingVetInput,
        List<Integer> invalidRowNumbers
) {
}
