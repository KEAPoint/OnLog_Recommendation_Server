package keapoint.onlog.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetRecommendationResDto {
    private String summary;
    private List<String> imageUrl;
}
