package com.hohoedu.all_pass.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import com.google.firebase.auth.hash.Sha256;
import com.hohoedu.all_pass._core.utils.Sha256Util;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.conscrypt.OpenSSLCipherRSA;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass._core.handler.exception.CustomRestfulException;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;
import com.hohoedu.all_pass.user.repository.UserJpaRepository;
import com.hohoedu.all_pass.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final UserRepository userRepository;

    public List<User> findAll() {
        List<User> user = userJpaRepository.findAll();
        return user;
    }

    public List<User> findByCenterCode(String centerNo) {
        List<User> user = userRepository.findUserByCenterCode(centerNo);
        return user;
    }

    public void insert(User user) {
        userRepository.insert(user);
    }

    public LoginRespDTO login(UserReqDTO.UserLoginDTO loginDTO) {

        Center center = userRepository.findCenterByCenterCode(loginDTO.getCenterCode());
        if (center == null) {
            throw new CustomRestfulException("지점코드를 확인해주세요.", HttpStatus.NOT_FOUND);
        }

        UserRespDTO.UserAuthDTO authDTO = userRepository.findByLoginInfo(loginDTO.getUserId());

        // 아이디 체크
        if (authDTO == null) {
            throw new CustomRestfulException("아이디 또는 비밀번호를 확인해주세요.", HttpStatus.FORBIDDEN);
        }

        String inputHash = Sha256Util.sha256(loginDTO.getUserPassword(), authDTO.getSalt());
        String test1 = Sha256Util.generateSalt();
        String test2 = Sha256Util.generateSalt();
        String test3 = Sha256Util.generateSalt();
        String test4 = Sha256Util.generateSalt();
        String test5 = Sha256Util.generateSalt();
        String test6 = Sha256Util.generateSalt();

        log.info(test1);
        log.info(test2);
        log.info(test3);
        log.info(test4);
        log.info(test5);
        log.info(test6);
        log.info(inputHash);

        // 아이디 비밀번호 체크
        if (!MessageDigest.isEqual(inputHash.getBytes(StandardCharsets.UTF_8), authDTO.getPasswordHash().getBytes(StandardCharsets.UTF_8))) {
            throw new CustomRestfulException("아이디 또는 비밀번호를 확인해주세요.", HttpStatus.FORBIDDEN);
        }

        // 아이디 지점코드 체크
        if (!authDTO.getCenterCode().equals(loginDTO.getCenterCode())) {
            throw new CustomRestfulException("지점 코드가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        return userRepository.findUserByUserId(authDTO.getUserId());
    }

}
