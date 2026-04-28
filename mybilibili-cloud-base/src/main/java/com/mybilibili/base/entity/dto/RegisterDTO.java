package com.mybilibili.base.entity.dto;

import com.mybilibili.base.constants.Constants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotEmpty
    @Pattern(regexp = Constants.EMAIL_REGEX)
    private String email;

    @NotEmpty
    private String nickName;

    @NotEmpty
    @Size(max = 20)
    @Pattern(regexp = Constants.PASSWORD_REGEX)
    private String registerPassword;

    @NotEmpty
    private String checkCodeKey;

    @NotEmpty
    private String checkCode;
}
