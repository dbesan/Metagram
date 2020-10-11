package org.studyproject.metagram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.studyproject.metagram.domain.User;
import org.studyproject.metagram.domain.dto.CaptchaResponceDto;
import org.studyproject.metagram.service.UserService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

import static org.studyproject.metagram.config.Literals.*;

@Controller
public class RegistrationController {
    private static final String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";
    private final UserService userService;
    private final RestTemplate restTemplate;
    @Value("${recaptcha.secret}")
    private String secret;

    @Autowired
    public RegistrationController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    /**
     * @GetMapping("/registration") public String registration()
     * page with registration form
     */
    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    /**
     * @PostMapping("/registration") public String addUser(@RequestParam("password2") String passwordConfirm,
     * @RequestParam("g-recaptcha-response") String captchaResponse,
     * @Valid User user,
     * BindingResult bindingResult,
     * Model model)
     * adding new user
     */
    @PostMapping("/registration")
    public String addUser(@RequestParam("password2") String passwordConfirm,
                          @RequestParam("g-recaptcha-response") String captchaResponse,
                          @Valid User user,
                          BindingResult bindingResult,
                          Model model) {
        String url = String.format(CAPTCHA_URL, secret, captchaResponse);
        CaptchaResponceDto responce = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponceDto.class);
        if (!responce.isSuccess()) {
            model.addAttribute("captchaError", PLEASE_FILL_CAPTCHA);
        }
        boolean isConfirmEmpty = StringUtils.isEmpty(passwordConfirm);

        if (StringUtils.isEmpty(passwordConfirm)) {
            model.addAttribute("password2Error", PASSWORD_CONFIRMATION_CANNOT_BE_EMPTY);
        }
        if (user.getPassword() != null && !user.getPassword().equals(passwordConfirm)) {
            model.addAttribute("passwordError", PASSWORDS_ARE_DIFFERENT);
        }
        if (isConfirmEmpty || bindingResult.hasErrors() || !responce.isSuccess()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            return "registration";
        }
        if (!userService.addUser(user)) {
            model.addAttribute("usernameError", USER_ALREADY_EXIST);
            return "registration";
        }

        return "redirect:/login";
    }

    /**
     * @GetMapping("/activate/{code}") public String activate(Model model, @PathVariable String code)
     * activate account
     */
    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
            model.addAttribute("messageType", "success");
            model.addAttribute("message", USER_SUCCESSFULLY_ACTIVATED);
        } else {
            model.addAttribute("messageType", "danger");
            model.addAttribute("message", ACTIVATION_CODE_IS_NOT_FOUND);
        }

        return "login";
    }
}
