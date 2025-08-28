package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Nested
    class 담당자_지정_테스트 {
        @Test
        void todo의_user가_null인_경우_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", null);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test // 테스트코드 샘플
        void todo가_정상적으로_등록된다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser); // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            long managerUserId = 2L;
            User managerUser = new User("b@b.com", "password", UserRole.USER); // 매니저로 등록할 유저
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request
                                                                                           // dto 생성

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
            given(managerRepository.save(any(Manager.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ManagerSaveResponse response =
                    managerService.saveManager(authUser, todoId, managerSaveRequest);

            // then
            assertNotNull(response);
            assertEquals(managerUser.getId(), response.getUser().getId());
            assertEquals(managerUser.getEmail(), response.getUser().getEmail());
        }

        @Test
        void 요청자가_Todo제작자가아니면_실패한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User todoCreater = new User("null", "null", UserRole.ADMIN); // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", todoCreater);

            long managerUserId = 2L;
            User managerUser = new User("b@b.com", "password", UserRole.USER); // 매니저로 등록할 유저
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request
                                                                                           // dto 생성
            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, managerSaveRequest));
        }

        @Test // 테스트코드 샘플
        void 지정할담당자가_Todo제작자면_실패한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser); // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(1L); // request
                                                                                // dto 생성

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> managerService.saveManager(authUser, todoId, managerSaveRequest));
        }
    }

    @Test
    public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(),
                managerResponses.get(0).getUser().getEmail());
    }

    @Nested
    class 담당자_삭제_테스트 {
        @Test
        void 담당자를_삭제할수_있다() {
            // given
            User todoCreator = new User("null", "null", UserRole.USER);
            ReflectionTestUtils.setField(todoCreator, "id", 1L);

            Todo todo = new Todo("null", "null", "null", todoCreator);
            ReflectionTestUtils.setField(todo, "id", 1L);

            User managerUser = new User("llun", "llun", UserRole.USER);
            ReflectionTestUtils.setField(managerUser, "id", 2L);

            Manager manager = new Manager(managerUser, todo);
            ReflectionTestUtils.setField(manager, "id", 1L);

            given(userRepository.findById(todoCreator.getId()))
                    .willReturn(Optional.of(todoCreator));
            given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));
            given(managerRepository.findById(manager.getId())).willReturn(Optional.of(manager));

            willDoNothing().given(managerRepository).delete(manager);

            // when
            managerService.deleteManager(todoCreator.getId(), todo.getId(), manager.getId());

            // then
            then(managerRepository).should().delete(manager);
        }

        @Test
        void User가_일정제작자가아니면_실패한다() {
            // given
            User todoCreator = new User("null", "null", UserRole.USER);
            ReflectionTestUtils.setField(todoCreator, "id", 1L);

            Todo todo = new Todo("null", "null", "null", todoCreator);
            ReflectionTestUtils.setField(todo, "id", 1L);

            User managerUser = new User("llun", "llun", UserRole.USER);
            ReflectionTestUtils.setField(managerUser, "id", 2L);

            Manager manager = new Manager(managerUser, todo);
            ReflectionTestUtils.setField(manager, "id", 1L);

            given(userRepository.findById(managerUser.getId()))
                    .willReturn(Optional.of(managerUser));
            given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));

            // when & then
            assertThrows(InvalidRequestException.class, () -> managerService
                    .deleteManager(managerUser.getId(), todo.getId(), manager.getId()));
        }

        @Test
        void 담당자와Todo가_일치하지않으면_실패한다() {
            // given
            User todoCreator = new User("null", "null", UserRole.USER);
            ReflectionTestUtils.setField(todoCreator, "id", 1L);

            Todo todo = new Todo("null", "null", "null", todoCreator);
            ReflectionTestUtils.setField(todo, "id", 1L);

            Todo managedTodo = new Todo("null", "null", "null", todoCreator);
            ReflectionTestUtils.setField(managedTodo, "id", 2L);

            User managerUser = new User("llun", "llun", UserRole.USER);
            ReflectionTestUtils.setField(managerUser, "id", 2L);

            Manager manager = new Manager(managerUser, managedTodo);
            ReflectionTestUtils.setField(manager, "id", 1L);

            given(userRepository.findById(todoCreator.getId()))
                    .willReturn(Optional.of(todoCreator));
            given(todoRepository.findById(todo.getId())).willReturn(Optional.of(todo));
            given(managerRepository.findById(manager.getId())).willReturn(Optional.of(manager));

            // when & then
            assertThrows(InvalidRequestException.class, () -> managerService
                    .deleteManager(todoCreator.getId(), todo.getId(), manager.getId()));
        }
    }
}
