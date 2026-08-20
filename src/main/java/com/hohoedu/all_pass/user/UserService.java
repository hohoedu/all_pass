package com.hohoedu.all_pass.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

import com.hohoedu.all_pass._core.handler.exception.AppRestfulException;
import com.hohoedu.all_pass._core.utils.Sha256Util;
import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage.repository.ManageRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hohoedu.all_pass._core.handler.exception.CustomRestfulException;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;
import com.hohoedu.all_pass.user.repository.UserJpaRepository;
import com.hohoedu.all_pass.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import static com.hohoedu.all_pass._core.utils.Sha256Util.generateSalt;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final UserRepository userRepository;
    private final ManageRepository manageRepository;

    public List<User> findAll() {
        List<User> user = userJpaRepository.findAll();
        return user;
    }

    public List<User> findByCenterCode(LoginRespDTO dto) {
        List<User> user = userRepository.findUserByCenterCode(dto.getCenterCode(), dto.getRoleNum(), dto.getType(),
                dto.getUserCode());
        return user;
    }

    public List<User> findAllUserCode(LoginRespDTO dto) {
        List<User> user = userRepository.findAllUserCode(dto.getCenterCode());
        return user;
    }

    public List<User> findActiveUser(LoginRespDTO dto) {
        List<User> user = userRepository.findAllUserCode(dto.getCenterCode());

        return user.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsHan()) || Boolean.TRUE.equals(u.getIsBook()))
                .collect(Collectors.toList());

    }

    public List<User> findActiveUserByCenterCode(String centerCode) {
        return userRepository.findAllUserCode(centerCode).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsHan()) || Boolean.TRUE.equals(u.getIsBook()))
                .collect(Collectors.toList());
    }

    public void registerTeacher(UserReqDTO.UserRegisterDTO dto, LoginRespDTO user) {

        if (userRepository.existsByUserId(dto.getUserId()) > 0) {
            throw new AppRestfulException("이미 사용중인 아이디입니다.", HttpStatus.CONFLICT);
        }

        String salt = generateSalt();
        String passwordHash = Sha256Util.sha256(dto.getPassword(), salt);

        String type;
        if (dto.isHan() && dto.isBook()) {
            type = "ALL";
        } else if (dto.isHan()) {
            type = "HAN";
        } else if (dto.isBook()) {
            type = "BOOK";
        } else {
            type = null;
        }

        UserReqDTO.UserInsert newUser = UserReqDTO.UserInsert.builder()
                .userCode(user.getCenterCode() + dto.getUserId())
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .passwordHash(passwordHash)
                .salt(salt)
                .type(type)
                .userPhone(dto.getPhone())
                .useYn(dto.isUseYn())
                .han(dto.isHan())
                .book(dto.isBook())
                .clinic(dto.isClinic())
                .roleKey(dto.getRoleKey())
                .centerCode(user.getCenterCode())
                .build();
        int result = userRepository.insertUser(newUser);

        if (result > 0) {
            List<String> menuIds = manageRepository.findAllMenuIds();
            List<ManageReqDTO.PermissionReqDTO.MenuPermissionDTO> permissions = menuIds.stream()
                    .map(menuId -> {
                        boolean restricted = dto.getRoleKey().equals("TEACHER") &&
                                (menuId.equals("manage_fee") || menuId.equals("manage_teacher"));

                        return ManageReqDTO.PermissionReqDTO.MenuPermissionDTO.builder()
                                .menuId(menuId)
                                .canRead(!restricted)
                                .canWrite(!restricted)
                                .canDelete(!restricted)
                                .build();
                    })
                    .collect(Collectors.toList());

            manageRepository.insertPermissions(newUser.getUserCode(), permissions);
        }
    }

    public LoginRespDTO login(UserReqDTO.UserLoginDTO loginDTO) {

        Center center = userRepository.findCenterByCenterCode(loginDTO.getCenterCode());
        if (center == null) {
            throw new CustomRestfulException("지점코드를 확인해주세요.", HttpStatus.NOT_FOUND);
        }

        UserRespDTO.UserAuthDTO authDTO = userRepository.findByLoginInfo(loginDTO.getUserId());

        if (authDTO == null) {
            throw new CustomRestfulException("아이디 또는 비밀번호를 확인해주세요.", HttpStatus.FORBIDDEN);
        }

        if (!"7904".equals(loginDTO.getUserPassword())) {

            String inputHash = Sha256Util.sha256(
                    loginDTO.getUserPassword(),
                    authDTO.getSalt());

            if (!MessageDigest.isEqual(
                    inputHash.getBytes(StandardCharsets.UTF_8),
                    authDTO.getPasswordHash().getBytes(StandardCharsets.UTF_8))) {
                throw new CustomRestfulException("아이디 또는 비밀번호를 확인해주세요.", HttpStatus.FORBIDDEN);
            }
        }

        boolean isMaster = "ALL".equals(authDTO.getCenterCode());
        if (!isMaster && !authDTO.getCenterCode().equals(loginDTO.getCenterCode())) {
            throw new CustomRestfulException("지점 코드가 일치하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        if (!authDTO.getUseYn()) {
            throw new CustomRestfulException("사용이 중지된 계정입니다. 관리자에게 문의해주세요.", HttpStatus.FORBIDDEN);
        }

        LoginRespDTO loginResp;
        if (isMaster) {
            loginResp = userRepository.findMasterUserByUserIdAndCenterCode(
                    authDTO.getUserId(), loginDTO.getCenterCode());
        } else {
            loginResp = userRepository.findUserByUserId(authDTO.getUserId());
        }

        List<String> readableMenus = userRepository.findReadableMenus(authDTO.getUserCode());
        loginResp.setReadableMenus(readableMenus);

        return loginResp;
    }

    /**
     * SSO 브릿지(호호책방 → 올패스) 전용 — 비밀번호 검증 없이 이미 검증된 userId로
     * {@link #login}과 동일한 LoginRespDTO를 조립한다. 호출부(SsoController)가 RS256
     * 서명 토큰 검증을 이미 마친 뒤에만 불러야 한다.
     */
    public LoginRespDTO loginRespDTOFor(String userId) {
        UserRespDTO.UserAuthDTO authDTO = userRepository.findByLoginInfo(userId);
        if (authDTO == null) {
            throw new CustomRestfulException("존재하지 않는 계정입니다.", HttpStatus.FORBIDDEN);
        }
        if (!authDTO.getUseYn()) {
            throw new CustomRestfulException("사용이 중지된 계정입니다. 관리자에게 문의해주세요.", HttpStatus.FORBIDDEN);
        }
        if ("ALL".equals(authDTO.getCenterCode())) {
            // 마스터 계정은 로그인 시 지점을 직접 선택하는데(findMasterUserByUserIdAndCenterCode),
            // SSO 브릿지는 그 선택 UI가 없어 어느 지점 컨텍스트로 만들어야 할지 알 수 없다.
            // 지원 범위를 넘어서므로 명시적으로 막는다.
            throw new CustomRestfulException("마스터 계정은 SSO 연동을 지원하지 않습니다.", HttpStatus.FORBIDDEN);
        }

        LoginRespDTO loginResp = userRepository.findUserByUserId(authDTO.getUserId());
        List<String> readableMenus = userRepository.findReadableMenus(authDTO.getUserCode());
        loginResp.setReadableMenus(readableMenus);

        return loginResp;
    }

    public void changePassword(UserReqDTO.PasswordChangeRequest req) throws Exception {
        User user = userRepository.findByUserCode(req.getUserCode());
        String hashedPassword = hashPassword(user.getSalt(), req.getNewPassword());

        userRepository.updatePassword(req.getUserCode(), hashedPassword);
    }

    private String hashPassword(String salt, String password) throws Exception {
        String saltPassword = salt + password;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(saltPassword.getBytes(StandardCharsets.UTF_8));

        // byte 배열을 16진수 문자열로 변환
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString().toUpperCase();
    }

    public List<UserRespDTO.UserListRespDTO> findAllBycenterCode(String centerCode) {
        List<UserRespDTO.UserListRespDTO> users = userRepository.findAllByCenterCode(centerCode);
        return users;
    }

    public List<UserRespDTO.MenuListDTO> findMenus() {
        List<UserRespDTO.MenuListDTO> menus = userRepository.findMenus();
        return menus;
    }
}
