package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.config.AuthContext;
import com.dace.dto.CreateSpaceRequest;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spaces")
public class SpaceController {
    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateSpaceRequest request) {
        return ApiResponse.ok(Map.of("id", 1, "owner_id", AuthContext.get().getId(), "name", request.getName()));
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(List.of());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("id", id, "name", "Demo Space", "member_count", 0, "quiz_count", 0));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody CreateSpaceRequest request) {
        return ApiResponse.ok(Map.of("id", id, "name", request.getName()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> archive(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("id", id, "status", 0));
    }
}
