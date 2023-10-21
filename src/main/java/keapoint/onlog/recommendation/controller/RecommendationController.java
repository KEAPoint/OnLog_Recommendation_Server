package keapoint.onlog.recommendation.controller;

import keapoint.onlog.recommendation.base.BaseErrorCode;
import keapoint.onlog.recommendation.base.BaseException;
import keapoint.onlog.recommendation.base.BaseResponse;
import keapoint.onlog.recommendation.dto.GetRecommendationReqDto;
import keapoint.onlog.recommendation.dto.GetRecommendationResDto;
import keapoint.onlog.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/recommendation")
    public BaseResponse<GetRecommendationResDto> recommendation(@RequestBody GetRecommendationReqDto data) {
        try {
            return new BaseResponse<>(recommendationService.recommend(data));

        } catch (BaseException e) {
            return new BaseResponse<>(e);
        }
    }

}
