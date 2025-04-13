package com.g10.controller;

import com.g10.model.Result;
import com.g10.model.User;
import com.g10.service.UserService;
import com.g10.utils.JwtUtil;
import com.g10.utils.Md5Util;
import com.g10.utils.ThreadLocalUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    
    // @Autowired
    // private StringRedisTemplate stringRedisTemplate;

    // 获取所有用户
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 根据ID查找用户
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    // 根据用户名查找用户
    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return user;
    }

    // 创建新用户（原方法保留，但不用于注册）
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }
    
    // 用户注册
    @PostMapping("/register")
    public Result register(
            @Pattern(regexp = "^\\S{5,16}$", message = "用户名必须是5-16位非空字符") 
            @RequestParam String username, 
            @Pattern(regexp = "^\\S{5,16}$", message = "密码必须是5-16位非空字符") 
            @RequestParam String password) {
        
        // 查询用户是否存在
        User existingUser = userService.getUserByUsername(username);
        if (existingUser != null) {
            return Result.error("用户名已被占用");
        }
        
        // 注册用户
        User newUser = new User();
        newUser.setUsername(username);
        // 加密密码
        newUser.setPassword(Md5Util.getMD5String(password));
        userService.createUser(newUser);
        
        return Result.success();
    }
    
    // 用户登录
    @PostMapping("/login")
    public Result<String> login(
            // @Pattern(regexp = "^\\S{5,16}$", message = "用户名必须是5-16位非空字符") 
            @RequestParam String username, 
            // @Pattern(regexp = "^\\S{5,16}$", message = "密码必须是5-16位非空字符") 
            @RequestParam String password) {
        
        // 根据用户名查询用户
        User loginUser = userService.getUserByUsername(username);
        
        // 判断用户是否存在
        if (loginUser == null) {
            return Result.error("用户名错误");
        }
        
        // 判断密码是否正确
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            // 登录成功，生成JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            
            // 将令牌存入Redis，有效期1小时
            // ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            // operations.set(token, token, 1, TimeUnit.HOURS);
            
            return Result.success(token);
        }
        
        return Result.error("密码错误");
    }
    
    // 获取当前登录用户信息
    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        String username = (String) userInfo.get("username");
        User user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    // 更新用户
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        if (updated == null) {
            throw new RuntimeException("Cannot update, user not found with ID: " + id);
        }
        return updated;
    }
    
    // 更新用户信息（基于当前登录用户）
    @PutMapping("/update")
    public Result update(@RequestBody @Valid User user) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        user.setId(userId);
        User updated = userService.updateUser(userId, user);
        if (updated == null) {
            return Result.error("更新失败");
        }
        return Result.success();
    }
    
    // 更新用户头像
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam String avatarUrl) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setUserPic(avatarUrl);
        userService.updateUser(userId, user);
        return Result.success();
    }

    // 删除用户及其所有关联数据
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            throw new RuntimeException("Failed to delete user with ID: " + id);
        }
        return ResponseEntity.ok("User deleted successfully.");
    }
}
