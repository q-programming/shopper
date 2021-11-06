package com.qprogramming.shopper.app.account;

import com.qprogramming.shopper.app.login.RegisterForm;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordForm {
    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String password;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String confirmpassword;

    @NotBlank(message = RegisterForm.NOT_BLANK_MESSAGE)
    private String token;

}
