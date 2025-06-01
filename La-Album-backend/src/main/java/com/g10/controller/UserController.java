package com.g10.controller;

import com.g10.model.Album;
import com.g10.model.Result;
import com.g10.model.User;
import com.g10.service.UserService;
import com.g10.service.AlbumService;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final AlbumService albumService;

    // 获取所有用户
    @GetMapping
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }

    // 根据ID查找用户
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(Result::success)
                .orElseGet(() -> Result.error("未找到ID为 " + id + " 的用户"));
    }

    // 根据用户名查找用户
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error("未找到用户名为 " + username + " 的用户");
        }
        return Result.success(user);
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

        Album defaultAlbum = new Album();
        defaultAlbum.setTitle("全部照片");
        defaultAlbum.setDescription("Default album for user");
        defaultAlbum.setUser(newUser);
        defaultAlbum.setType("default");
        albumService.createAlbum(defaultAlbum);

        return Result.success();
    }
    
    // 用户登录
    @PostMapping("/login")
    public Result<String> login(
            @RequestParam String username, 
            @RequestParam String password) {
        
        // 根据用户名查询用户
        User loginUser = userService.getUserByUsername(username);
        
        // 判断用户是否存在
        if (loginUser == null) {
            System.err.println("登录失败，用户名不存在: " + username);
            return Result.error("用户名错误");
        }
        
        // 判断密码是否正确
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            // 登录成功，生成JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            
            // 将令牌存入Redis，使用用户ID作为key
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String userKey = "user:" + loginUser.getId();
            operations.set(userKey, token, 1, TimeUnit.HOURS);

            Album defaultAlbum = albumService.getDefaultAlbumForUser(loginUser.getId());
            if (defaultAlbum == null) {
                Album newDefault = new Album();
                newDefault.setTitle("全部照片");
                newDefault.setDescription("系统默认相册");
                newDefault.setUser(loginUser);
                newDefault.setType("default");
                albumService.createAlbum(newDefault);
            }
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
    public Result deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            return Result.error("删除用户失败");
        }
        return Result.success("用户删除成功");
    }

    // 用户登出
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        try {
            // 解析token获取用户ID
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            // 删除该用户的Redis记录
            String userKey = "user:" + userId;
            stringRedisTemplate.delete(userKey);
            return Result.success();
        } catch (Exception e) {
            return Result.error("登出失败");
        }
    }
}
