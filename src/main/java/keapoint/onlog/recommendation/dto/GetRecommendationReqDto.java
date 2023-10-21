package keapoint.onlog.recommendation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetRecommendationReqDto {
    private String content;
    private List<String> hashtag;
}
