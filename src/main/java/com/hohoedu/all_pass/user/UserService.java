package com.hohoedu.all_pass.user;

import java.util.List;

import com.hohoedu.all_pass.user._dto.UserRespDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass._core.handler.exception.CustomRestfulException;
import com.hohoedu.all_pass.admin.center.Center;
import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;
import com.hohoedu.all_pass.user.repository.UserJpaRepository;
import com.hohoedu.all_pass.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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
            throw new CustomRestfulException("존재하지 않는 지점 코드입니다.", HttpStatus.NOT_FOUND);
        }

        UserRespDTO.UserAuthDTO authDTO = userRepository.findByLoginInfo(loginDTO.getUserId());

        // 아이디 체크
        if (authDTO == null) {
            throw new CustomRestfulException("존재하지 않는 아이디입니다.", HttpStatus.FORBIDDEN);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String encode = passwordEncoder.encode(loginDTO.getUserPassword());
        // 아이디 비밀번호 체크
        if (!passwordEncoder.matches(loginDTO.getUserPassword(), authDTO.getPasswordHash())) {
            throw new CustomRestfulException("비밀번호가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        // 아이디 지점코드 체크
        if (!authDTO.getCenterCode().equals(loginDTO.getCenterCode())) {
            throw new CustomRestfulException("지점 코드가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        return userRepository.findUserByUserId(authDTO.getUserId());
    }

}
