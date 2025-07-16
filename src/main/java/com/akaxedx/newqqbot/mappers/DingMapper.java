package com.akaxedx.newqqbot.mappers;

import com.akaxedx.newqqbot.entity.Ding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DingMapper extends BaseMapper<Ding> {
    @Update("update ding set `has`=0,time=0")
    void deleteDing();
}
