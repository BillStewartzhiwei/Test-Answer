package com.dace.quiz.controller;

import com.dace.quiz.common.ApiResponse;
import com.dace.quiz.dto.ApiModels.SpaceRequest;
import com.dace.quiz.entity.Space;
import com.dace.quiz.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/spaces")
@RequiredArgsConstructor
public class SpaceController {
    private final SpaceService spaceService;

    @GetMapping
    public ApiResponse<List<Space>> mySpaces(HttpServletRequest request) {
        return ApiResponse.ok(spaceService.mySpaces(userId(request), role(request)));
    }

    @PostMapping
    public ApiResponse<Space> create(HttpServletRequest request, @RequestBody SpaceRequest body) {
        return ApiResponse.ok(spaceService.create(userId(request), role(request), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<Space> update(HttpServletRequest request, @PathVariable Long id, @RequestBody SpaceRequest body) {
        return ApiResponse.ok(spaceService.update(userId(request), id, body));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(HttpServletRequest request, @PathVariable Long id) {
        spaceService.archive(userId(request), id);
        return ApiResponse.ok(null);
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String role(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}
