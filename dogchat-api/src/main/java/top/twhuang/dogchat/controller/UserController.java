package top.twhuang.dogchat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import top.twhuang.dogchat.dto.ProfileDTO;
import top.twhuang.dogchat.dto.SignInDTO;
import top.twhuang.dogchat.dto.SignUpDTO;
import top.twhuang.dogchat.entity.User;
import top.twhuang.dogchat.mapper.UserMapper;
import top.twhuang.dogchat.util.JwtUtil;
import top.twhuang.dogchat.util.PasswordUtil;
import top.twhuang.dogchat.util.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * @Description: TODO
 * @Date: 2021-04-27 17:03
 * @Author: tw.huang
 * @Version: v1.0.0
 **/
@RestController
@AllArgsConstructor
public class UserController {

    private UserMapper userMapper;

    @PostMapping("/api/signUp")
    public Result signUp(@Valid @RequestBody SignUpDTO signUpDTO) {
        //检验数据库是否存在
        User hasEmail = this.userMapper.selectOne(new QueryWrapper<User>().lambda()
                .eq(User::getEmail, signUpDTO.getEmail()));
        if (hasEmail != null) {
            return Result.failure("邮箱已经注册帐号");
        }
        User hasName = this.userMapper.selectOne(new QueryWrapper<User>().lambda()
                .eq(User::getNickname, signUpDTO.getNickname()));
        if (hasName != null) {
            return Result.failure("昵称已经被人使用");
        }
        String salt = PasswordUtil.getSalt();
        String password = PasswordUtil.encryptPassword(signUpDTO.getPassword(), salt);
        User user = new User();
        user.setSalt(salt);
        user.setPassword(password);
        user.setAvatar("https://peplife-img-1302830364.cos.ap-guangzhou.myqcloud.com/org-avatar/2021-04/4dde2f0444d9496c99ffc87fa4d9a50d.jpeg");
        BeanUtils.copyProperties(signUpDTO, user);
        this.userMapper.insert(user);
        return Result.success("注册成功");
    }

    @GetMapping("/api/signUp")
    public Result signUpCount() {
        Integer count = this.userMapper.selectCount(null);
        int onlineCount = WebSocket.getOnlineCount();
        HashMap<String, Object> map = new HashMap<>();
        map.put("registerCount", count);
        map.put("onlineCount", onlineCount);
        return Result.success(map, "用户统计信息");
    }

    @PostMapping("/api/signIn")
    public Result signIn(@Valid @RequestBody SignInDTO signInDTO) {
        User user = this.userMapper.selectOne(new QueryWrapper<User>().lambda()
                .eq(User::getEmail, signInDTO.getEmail()));
        if (Objects.isNull(user)) {
            return Result.failure("该邮箱还没注册");
        }
        if (PasswordUtil.encryptPassword(signInDTO.getPassword(), user.getSalt()).equals(user.getPassword())) {
            HashMap<String, String> map = new HashMap<>(1);
            String token = JwtUtil.createToken(user);
            map.put("token", token);
            return Result.success(map, "登录成功");
        }
        return Result.failure("帐号/密码错误");
    }

    @GetMapping("/api/user")
    public Result getUser(HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        User user = this.userMapper.selectById(userId);
        return Result.success(user, "用户信息");
    }

    @PutMapping("/api/user")
    public Result putUser(@RequestBody ProfileDTO profileDTO, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        User user = new User();
        BeanUtils.copyProperties(profileDTO, user);
        this.userMapper.update(user, new UpdateWrapper<User>().lambda().eq(User::getId, userId));
        return Result.success(user, "修改用户信息");
    }
}
