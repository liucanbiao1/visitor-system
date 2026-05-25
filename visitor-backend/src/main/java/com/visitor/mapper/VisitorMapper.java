package com.visitor.mapper;

import com.visitor.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VisitorMapper {
    List<Visitor> findAll();
    Visitor findById(Long id);
    int insert(Visitor visitor);
    int update(Visitor visitor);
    int deleteById(Long id);

    Visitor findByPhone(@Param("phone") String phone);

    Visitor findByIdCardAndStatus(@Param("idCard") String idCard, @Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    List<Visitor> findPage(@Param("keyword") String keyword);
}
