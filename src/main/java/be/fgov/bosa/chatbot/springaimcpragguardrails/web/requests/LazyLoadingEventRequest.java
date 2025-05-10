package be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class LazyLoadingEventRequest {
    @NotNull
    @Schema(description = "Index of the first record to fetch", example = "0")
    Integer first;
    @NotNull
    @Schema(description = "Number of records per page", example = "10")
    Integer rows;
    @Schema(description = "Field used for sorting", example = "name")
    String sortField;
    @Schema(description = "Sorting order, 1 for ascending, 0 for descending", example = "1")
    Integer sortOrder;
    @Schema(description = "Global filter text to apply", example = "searchQuery")
    String globalFilter;
}