package com.revworkforce.revworkforce_web.service;

import com.revworkforce.revworkforce_web.dao.*;
import com.revworkforce.revworkforce_web.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private LeaveBalanceDao leaveBalanceDao;

    @Mock
    private LeaveTypeDao leaveTypeDao;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .employeeId("EMP00001")
                .role(User.Role.EMPLOYEE)
                .department("Engineering")
                .designation("Developer")
                .active(true)
                .joiningDate(LocalDate.now())
                .build();
    }

    // ==================== register() ====================

    @Test
    void register_shouldSaveUserAndInitializeLeaveBalances() {
        User newUser = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("pass123")
                .build();

        User savedUser = User.builder()
                .id(2L)
                .name("Jane Doe")
                .email("jane@example.com")
                .password("pass123")
                .role(User.Role.EMPLOYEE)
                .active(true)
                .joiningDate(LocalDate.now())
                .build();

        List<LeaveType> leaveTypes = List.of(
                LeaveType.builder().id(1L).name("Sick Leave").defaultDays(10).build(),
                LeaveType.builder().id(2L).name("Casual Leave").defaultDays(12).build());

        when(userDao.existsByEmail("jane@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(savedUser);
        when(leaveTypeDao.findAll()).thenReturn(leaveTypes);

        User result = userService.register(newUser);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertTrue(result.isActive());
        verify(userDao).save(any(User.class));
        verify(leaveBalanceDao, times(2)).save(any(LeaveBalance.class));
    }

    @Test
    void register_shouldThrowExceptionWhenEmailExists() {
        when(userDao.existsByEmail("john@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.register(sampleUser));
        assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void register_shouldSetDefaultRoleWhenNull() {
        User newUser = User.builder()
                .name("Test")
                .email("test@example.com")
                .password("pass")
                .build();

        when(userDao.existsByEmail("test@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTypeDao.findAll()).thenReturn(Collections.emptyList());

        userService.register(newUser);

        assertEquals(User.Role.EMPLOYEE, newUser.getRole());
    }

    @Test
    void register_shouldSetJoiningDateWhenNull() {
        User newUser = User.builder()
                .name("Test")
                .email("test@example.com")
                .password("pass")
                .build();

        when(userDao.existsByEmail("test@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTypeDao.findAll()).thenReturn(Collections.emptyList());

        userService.register(newUser);

        assertNotNull(newUser.getJoiningDate());
        assertEquals(LocalDate.now(), newUser.getJoiningDate());
    }

    @Test
    void register_shouldGenerateEmployeeIdWhenEmpty() {
        User newUser = User.builder()
                .name("Test")
                .email("test@example.com")
                .password("pass")
                .build();

        when(userDao.existsByEmail("test@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTypeDao.findAll()).thenReturn(Collections.emptyList());

        userService.register(newUser);

        assertNotNull(newUser.getEmployeeId());
        assertTrue(newUser.getEmployeeId().startsWith("EMP"));
    }

    // ==================== authenticate() ====================

    @Test
    void authenticate_shouldReturnUserWhenCredentialsAreValid() {
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.authenticate("john@example.com", "password123");

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void authenticate_shouldReturnEmptyWhenPasswordIsWrong() {
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.authenticate("john@example.com", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_shouldReturnEmptyWhenEmailNotFound() {
        when(userDao.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("unknown@example.com", "password123");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_shouldThrowWhenAccountDeactivated() {
        sampleUser.setActive(false);
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(RuntimeException.class,
                () -> userService.authenticate("john@example.com", "password123"));
    }

    // ==================== findById / findByEmail / findAll ====================

    @Test
    void findById_shouldReturnUser() {
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void findByEmail_shouldReturnUser() {
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findByEmail("john@example.com");

        assertTrue(result.isPresent());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userDao.findAll()).thenReturn(List.of(sampleUser));

        List<User> result = userService.findAll();

        assertEquals(1, result.size());
    }

    // ==================== updateEmployee() ====================

    @Test
    void updateEmployee_shouldUpdateFields() {
        User updated = User.builder().name("Updated Name").department("HR").build();
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

        User result = userService.updateEmployee(1L, updated);

        assertEquals("Updated Name", result.getName());
        assertEquals("HR", result.getDepartment());
        verify(userDao).update(any(User.class));
    }

    @Test
    void updateEmployee_shouldThrowWhenNotFound() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateEmployee(99L, sampleUser));
    }

    // ==================== deactivate / reactivate ====================

    @Test
    void deactivate_shouldCallDao() {
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.deactivate(1L);

        verify(userDao).deactivate(1L);
    }

    @Test
    void deactivate_shouldThrowWhenNotFound() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.deactivate(99L));
    }

    @Test
    void reactivate_shouldCallDao() {
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.reactivate(1L);

        verify(userDao).reactivate(1L);
    }

    // ==================== searchUsers() ====================

    @Test
    void searchUsers_shouldReturnFilteredResults() {
        when(userDao.searchUsers("John")).thenReturn(List.of(sampleUser));

        List<User> result = userService.searchUsers("John");

        assertEquals(1, result.size());
    }

    @Test
    void searchUsers_shouldReturnAllWhenQueryIsEmpty() {
        when(userDao.findAll()).thenReturn(List.of(sampleUser));

        List<User> result = userService.searchUsers("");

        assertEquals(1, result.size());
        verify(userDao).findAll();
    }

    @Test
    void searchUsers_shouldReturnAllWhenQueryIsNull() {
        when(userDao.findAll()).thenReturn(List.of(sampleUser));

        List<User> result = userService.searchUsers(null);

        assertEquals(1, result.size());
    }

    // ==================== getManagers() ====================

    @Test
    void getManagers_shouldReturnOnlyManagersAndAdmins() {
        User manager = User.builder().id(2L).name("Manager").role(User.Role.MANAGER).build();
        User admin = User.builder().id(3L).name("Admin").role(User.Role.ADMIN).build();
        User employee = User.builder().id(4L).name("Employee").role(User.Role.EMPLOYEE).build();

        when(userDao.findAll()).thenReturn(List.of(manager, admin, employee));

        List<User> result = userService.getManagers();

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(u -> u.getRole() == User.Role.EMPLOYEE));
    }

    // ==================== assignManager() ====================

    @Test
    void assignManager_shouldCallDao() {
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.assignManager(1L, 2L);

        verify(userDao).assignManager(1L, 2L);
    }

    @Test
    void assignManager_shouldThrowWhenEmployeeNotFound() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.assignManager(99L, 2L));
    }
}
