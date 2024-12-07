package com.upstox.production.centralconfiguration.controller;

import com.upstox.production.centralconfiguration.dto.UserDto;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public String addUser(@RequestBody UserDto userDto) throws Exception {
        return userService.addUser(userDto);
    }

    @GetMapping("/getAll")
    public List<UserDto> getAllUsers() throws Exception {
        return userService.getAllUsers();
    }

    @PutMapping("/edit")
    public UserDto updateUser(@RequestBody UserDto userDto) throws Exception {
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/delete/{userAccessType}")
    public String deleteUser(@PathVariable("userAccessType") String userAccessType) throws UpstoxException {
        return userService.deleteUser(userAccessType);
    }

    @DeleteMapping("deleteAll")
    public String deleteAllUsers() throws UpstoxException {
        return userService.deleteAllUsers();
    }
}
