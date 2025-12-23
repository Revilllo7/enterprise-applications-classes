package model;

import java.util.List;

/**
 * Wynik importu CSV: ile wierszy dodano oraz lista błędów
 */
public record ImportSummary(int importedCount, List<String> errors) {

    // dla testów - ile błędów
    public int getFailedCount() {
        return errors == null ? 0 : errors.size();
    }

    @Override
    public String toString() {
        return "ImportSummary{" +
                "importedCount=" + importedCount +
                ", errors=" + errors +
                '}';
    }
}
