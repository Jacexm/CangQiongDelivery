package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {


    /**
     * 根据openid查询用户信息
     * @param openid
     * @return User对象
     */
    @Select("select * from user where openid = #{openid}")
    User getUserByOpenId(String openid);

    /**
     * 插入用户信息
     * @param user
     */
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid}, #{name}, #{phone},#{sex}, #{idNumber}, #{avatar}, #{createTime})")
    void insert(User user);
}
