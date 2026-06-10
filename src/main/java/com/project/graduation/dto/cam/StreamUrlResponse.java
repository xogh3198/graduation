package com.project.graduation.dto.cam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamUrlResponse {
    private String streamUrl;
    private String channelName;
    private String region;
    private String viewerTokenPath;
}
