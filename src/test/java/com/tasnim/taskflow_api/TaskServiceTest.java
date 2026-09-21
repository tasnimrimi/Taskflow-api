package com.tasnim.taskflow_api;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TaskServiceTest {

    @Test
    void shouldReturnTaskWhenItBelongsToAuthenticatedUser() {
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Task existingTask =
                new Task(1L, "Owned task", false);

        existingTask.setOwner(owner);

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.findByIdAndOwner(1L, owner))
                .thenReturn(Optional.of(existingTask));

        Task result = service.getTaskById(
                1L,
                "owner@example.com"
        );

        assertSame(existingTask, result);
    }
    @Test
    void shouldUpdateTaskOwnedByAuthenticatedUser() {
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Task existingTask =
                new Task(1L, "Keep this title", true);

        existingTask.setOwner(owner);

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.findByIdAndOwner(1L, owner))
                .thenReturn(Optional.of(existingTask));

        when(repository.save(existingTask))
                .thenReturn(existingTask);

        Task result = service.updateTask(
                1L,
                "owner@example.com",
                null,
                false
        );

        assertEquals("Keep this title", result.getTitle());
        assertFalse(result.isCompleted());
        assertSame(owner, result.getOwner());
    }
    @Test
    void shouldDeleteTaskOwnedByAuthenticatedUser() {
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Task existingTask =
                new Task(1L, "Owned task", false);

        existingTask.setOwner(owner);

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.findByIdAndOwner(1L, owner))
                .thenReturn(Optional.of(existingTask));

        boolean result = service.deleteTask(
                1L,
                "owner@example.com"
        );

        assertTrue(result);
        verify(repository).delete(existingTask);
    }
    @Test
    void shouldReturnOnlyTasksOwnedByAuthenticatedUser() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Task firstTask =
                new Task(1L, "Owner's first task", false);

        firstTask.setOwner(owner);

        Task secondTask =
                new Task(2L, "Owner's second task", true);

        secondTask.setOwner(owner);

        List<Task> ownedTasks =
                List.of(firstTask, secondTask);

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.findByOwner(owner))
                .thenReturn(ownedTasks);

        // Act
        List<Task> result =
                service.getAllTasks("owner@example.com");

        // Assert
        assertEquals(2, result.size());
        assertSame(firstTask, result.get(0));
        assertSame(secondTask, result.get(1));

        verify(repository).findByOwner(owner);
    }
    @Test
    void shouldFilterOnlyAuthenticatedUsersTasksByCompleted() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Task unfinishedTask =
                new Task(1L, "Owner's unfinished task", false);

        unfinishedTask.setOwner(owner);

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.findByOwnerAndCompleted(
                owner,
                false
        )).thenReturn(List.of(unfinishedTask));

        // Act
        List<Task> result =
                service.getTasksByCompleted(
                        "owner@example.com",
                        false
                );

        // Assert
        assertEquals(1, result.size());
        assertSame(unfinishedTask, result.get(0));

        verify(repository).findByOwnerAndCompleted(
                owner,
                false
        );
    }
    @Test
    void shouldAssignAuthenticatedUserWhenCreatingTask() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.save(
                org.mockito.ArgumentMatchers.any(Task.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // Act
        Task result = service.createTask(
                "owner@example.com",
                "Learn task ownership"
        );

        // Assert
        assertEquals(
                "Learn task ownership",
                result.getTitle()
        );

        assertFalse(result.isCompleted());
        assertSame(owner, result.getOwner());

        verify(repository).save(result);
    }
    @Test
    void shouldNotReturnTaskOwnedByAnotherUser() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser requestingUser =
                new AppUser(
                        "requester@example.com",
                        "password-hash"
                );

        when(userRepository.findByEmailIgnoreCase(
                "requester@example.com"
        )).thenReturn(Optional.of(requestingUser));

        // Task 99 does not belong to requestingUser,
        // so the owner-aware repository search returns empty
        when(repository.findByIdAndOwner(
                99L,
                requestingUser
        )).thenReturn(Optional.empty());

        // Act
        Task result = service.getTaskById(
                99L,
                "requester@example.com"
        );

        // Assert
        assertNull(result);

        verify(repository).findByIdAndOwner(
                99L,
                requestingUser
        );

    }
    @Test
    void shouldNotUpdateTaskOwnedByAnotherUser() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser requestingUser =
                new AppUser(
                        "requester@example.com",
                        "password-hash"
                );

        when(userRepository.findByEmailIgnoreCase(
                "requester@example.com"
        )).thenReturn(Optional.of(requestingUser));

        // Task 99 is not owned by requestingUser
        when(repository.findByIdAndOwner(
                99L,
                requestingUser
        )).thenReturn(Optional.empty());

        // Act
        Task result = service.updateTask(
                99L,
                "requester@example.com",
                "Attempted new title",
                true
        );

        // Assert
        assertNull(result);

        verify(
                repository,
                never()
        ).save(
                org.mockito.ArgumentMatchers.any(Task.class)
        );
    }
    @Test
    void shouldNotDeleteTaskOwnedByAnotherUser() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser requestingUser =
                new AppUser(
                        "requester@example.com",
                        "password-hash"
                );

        when(userRepository.findByEmailIgnoreCase(
                "requester@example.com"
        )).thenReturn(Optional.of(requestingUser));

        // Task 99 is not owned by requestingUser
        when(repository.findByIdAndOwner(
                99L,
                requestingUser
        )).thenReturn(Optional.empty());

        // Act
        boolean result = service.deleteTask(
                99L,
                "requester@example.com"
        );

        // Assert
        assertFalse(result);

        verify(
                repository,
                never()
        ).delete(
                org.mockito.ArgumentMatchers.any(Task.class)
        );
    }
    @Test
    void shouldSearchOnlyAuthenticatedUsersTasks() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Task matchingTask =
                new Task(
                        1L,
                        "Learn Spring Security",
                        false
                );

        matchingTask.setOwner(owner);

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository
                .findByOwnerAndTitleContainingIgnoreCase(
                        owner,
                        "spring"
                )
        ).thenReturn(List.of(matchingTask));

        // Act
        List<Task> result =
                service.searchTasksByTitle(
                        "owner@example.com",
                        "spring"
                );

        // Assert
        assertEquals(1, result.size());
        assertSame(matchingTask, result.get(0));

        verify(repository)
                .findByOwnerAndTitleContainingIgnoreCase(
                        owner,
                        "spring"
                );
    }
    @Test
    void shouldPaginateOnlyAuthenticatedUsersTasks() {
        // Arrange
        TaskRepository repository =
                mock(TaskRepository.class);

        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        TaskService service =
                new TaskService(repository, userRepository);

        AppUser owner =
                new AppUser(
                        "owner@example.com",
                        "password-hash"
                );

        Pageable pageable =
                PageRequest.of(0, 2);

        Task firstTask =
                new Task(1L, "First owned task", false);

        firstTask.setOwner(owner);

        Task secondTask =
                new Task(2L, "Second owned task", true);

        secondTask.setOwner(owner);

        Page<Task> repositoryPage =
                new PageImpl<>(
                        List.of(firstTask, secondTask),
                        pageable,
                        4
                );

        when(userRepository.findByEmailIgnoreCase(
                "owner@example.com"
        )).thenReturn(Optional.of(owner));

        when(repository.findByOwner(
                owner,
                pageable
        )).thenReturn(repositoryPage);

        // Act
        Page<Task> result = service.getAllTasks(
                "owner@example.com",
                pageable
        );

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(4, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        verify(repository).findByOwner(
                owner,
                pageable
        );
    }

}