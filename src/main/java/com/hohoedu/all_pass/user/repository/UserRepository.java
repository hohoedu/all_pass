package com.hohoedu.all_pass.user.repository;

import java.util.List;

import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user.User;

@Mapper
public interface UserRepository {

        int insertUser(UserReqDTO.UserInsert user);

        int existsByUserId(String userId);

        public List<User> findUserByCenterCode(
                        @Param("centerCode") String centerCode,
                        @Param("roleNum") Integer roleNum,
                        @Param("type") String type,
                        @Param("userCode") String userCode);

        List<User> findAllUserCode(
                        @Param("centerCode") String centerCode);

        UserRespDTO.LoginRespDTO findUserByUserId(@Param("userId") String userId);

        List<String> findReadableMenus(String userCode);
        UserRespDTO.UserAuthDTO findByLoginInfo(@Param("userId") String userId);

        public Center findCenterByCenterCode(@Param("centerCode") String centerCode);

        User findByUserCode(String userCode);

        String findUserPhoneByUserCode(String userCode);

        void updatePassword(String userCode, String hashedPassword);

        List<UserRespDTO.UserListRespDTO> findAllByCenterCode(String centerCode);

        List<UserRespDTO.MenuListDTO> findMenus();
}
