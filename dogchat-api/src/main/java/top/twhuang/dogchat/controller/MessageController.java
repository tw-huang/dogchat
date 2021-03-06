package top.twhuang.dogchat.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import top.twhuang.dogchat.entity.Message;
import top.twhuang.dogchat.entity.User;
import top.twhuang.dogchat.mapper.MessageMapper;
import top.twhuang.dogchat.mapper.UserMapper;
import top.twhuang.dogchat.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@AllArgsConstructor
public class MessageController {

    private MessageMapper messageMapper;

    private UserMapper userMapper;

    @GetMapping("/api/message")
    public Result messagePage(@RequestParam String dateTime,
                              @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                              @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        System.out.println(dateTime);
        Page<Message> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Message> qw = new QueryWrapper<Message>().lambda()
                .eq(Message::getDelFlag, false).lt(Message::getPushTime, new Date(Long.parseLong(dateTime)))
                .orderByDesc(Message::getPushTime);
        Page<Message> result = this.messageMapper.selectPage(page, qw);
        result.getRecords().forEach(x -> {
            User user = this.userMapper.selectById(x.getUserId());
            x.setUser(user);
            if (x.getQuoteMessageId() != null) {
                Message message = this.messageMapper.selectById(x.getQuoteMessageId());
                x.setQuoteMessage(message);
            }
        });
        return Result.success(result);
    }

}
