package com.upstox.production.centralconfiguration.service;

import com.upstox.production.centralconfiguration.dto.UserDto;
import com.upstox.production.centralconfiguration.entity.User;
import com.upstox.production.centralconfiguration.enums.UserAccess;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UserRepository;
import com.upstox.production.nifty.utility.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.upstox.production.centralconfiguration.security.RSAGenerator.decrypt;
import static com.upstox.production.centralconfiguration.security.RSAGenerator.encrypt;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.authenticatedUser;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.privateKey;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.publicKey;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public String addUser(UserDto userDto) throws Exception {
        if (authenticatedUser) {
            Optional<User> optionalUser = userRepository.findByUserAccessType(userDto.getUserAccessType().getAccessType());
            if (optionalUser.isPresent()) {
                return "User Already Exists in record!!!";
            }
            Iterable<User> userIterable = userRepository.findAll();
            if (IterableUtils.size(userIterable) > 2) {
                userRepository.deleteAll();
                return "Application removed all the user data please add all users again because you have exceed max user limit which is 2";
            }
            User user = userBuilder(userDto);
            userRepository.save(user);
            return "User added successfully!!!";
        } else {
            throw new UpstoxException("User is not authorized to access");
        }
    }

    public List<UserDto> getAllUsers() throws Exception {
        if (authenticatedUser) {
            Iterable<User> userIterable = userRepository.findAll();
            List<User> users = CollectionUtils.iterableToList(userIterable, User.class);
            List<UserDto> userDtos = new ArrayList<>();
            for (User user : users) {
                userDtos.add(userDtoBuilder(user));
            }
            return userDtos;
        } else {
            throw new UpstoxException("User is not authorized to access");
        }
    }

    public UserDto updateUser(UserDto userDto) throws Exception {
        if (authenticatedUser) {
            Optional<User> optionalUser = userRepository.findByUserAccessType(userDto.getUserAccessType().getAccessType());
            if (optionalUser.isEmpty()) {
                throw new UpstoxException("No Record found for the provided user");
            }
            User user = userBuilder(userDto);
            user = userRepository.save(user);
            return userDtoBuilder(user);
        } else {
            throw new UpstoxException("User is not authorized to access");
        }
    }

    public String deleteUser(String userAccessType) throws UpstoxException {
        if (authenticatedUser) {
            Optional<User> optionalUser = userRepository.findByUserAccessType(userAccessType);
            if (optionalUser.isEmpty()) {
                throw new UpstoxException("There is no user details found for provided userAccessType");
            }
            userRepository.delete(optionalUser.get());
            return "User deleted successfully";
        } else {
            throw new UpstoxException("User is not authorized to access");
        }
    }

    public String deleteAllUsers() throws UpstoxException {
        if (authenticatedUser) {
            userRepository.deleteAll();
            return "All Users Record Deleted SuccessFully!!";
        } else {
            throw new UpstoxException("User is not authorized to access");
        }
    }

    private UserDto userDtoBuilder(User user) throws Exception {

        return UserDto.builder().userAccessType(UserAccess.getAccess(user.getUserAccessType()))
                .clientId(decrypt(user.getClientId(), privateKey))
                .clientSecrete(decrypt(user.getClientSecrete(), privateKey))
                .emailId(decrypt(user.getEmailId(), privateKey)).build();
    }

    private User userBuilder(UserDto userDto) throws Exception {

        return User.builder().userAccessType(userDto.getUserAccessType().getAccessType())
                .clientId(encrypt(userDto.getClientId(), publicKey))
                .clientSecrete(encrypt(userDto.getClientSecrete(), publicKey))
                .emailId(encrypt(userDto.getEmailId(), publicKey)).build();
    }
}
