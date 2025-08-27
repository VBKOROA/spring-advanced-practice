package org.example.expert.domain.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @PostMapping("/todos/{todoId}/managers")
    public ResponseEntity<ManagerSaveResponse> saveManager(@Auth AuthUser authUser,
            @PathVariable long todoId, @Valid @RequestBody ManagerSaveRequest managerSaveRequest) {
        return ResponseEntity.ok(managerService.saveManager(authUser, todoId, managerSaveRequest));
    }

    @GetMapping("/todos/{todoId}/managers")
    public ResponseEntity<List<ManagerResponse>> getMembers(@PathVariable long todoId) {
        return ResponseEntity.ok(managerService.getManagers(todoId));
    }

    @DeleteMapping("/todos/{todoId}/managers/{managerId}")
    public void deleteManager(@Auth AuthUser user, @PathVariable long todoId,
            @PathVariable long managerId) {
        managerService.deleteManager(user.getId(), todoId, managerId);
    }
}
