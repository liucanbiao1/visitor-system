package com.visitor.dto;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 访客信息传输对象
 */
public class VisitorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2-50之间")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 身份证号：简单格式校验（18位或15位，支持末位X/x）
     * 实际业务建议使用专门工具类进行严格校验
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)?\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}([0-9Xx])?$",
            message = "身份证号格式不正确")
    private String idCard;

    /**
     * 性别: 0-未知, 1-男, 2-女
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值范围0~2")
    @Max(value = 2, message = "性别值范围0~2")
    private Integer gender;

    /**
     * 照片URL（可选）
     */
    @Size(max = 255, message = "照片URL长度不能超过255")
    private String photoUrl;

    // ========== 构造方法 ==========
    public VisitorDTO() {
    }

    public VisitorDTO(String name, String phone, String idCard, Integer gender, String photoUrl) {
        this.name = name;
        this.phone = phone;
        this.idCard = idCard;
        this.gender = gender;
        this.photoUrl = photoUrl;
    }

    // ========== getter / setter ==========
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // 可选：重写 toString 便于日志输出
    @Override
    public String toString() {
        return "VisitorDTO{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", idCard='" + idCard + '\'' +
                ", gender=" + gender +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }
}