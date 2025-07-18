package com.akaxedx.newqqbot.mappers;

import com.akaxedx.newqqbot.entity.Ding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DingMapper extends BaseMapper<Ding> {
    @Update("update ding set `has`=0,time=0 where group_id=#{groupId}")
    void deleteDing(Long groupId);

    @Select("select * from ding where `has`=1 and #{groupId} order by `length` desc ")
    List<Ding> selectDingBang(Long groupId);

}
