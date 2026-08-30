package com.visitor.mapper;

import com.visitor.entity.AiReviewLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiReviewLogMapper {

    int insert(AiReviewLog log);

    List<AiReviewLog> findPage();
}
