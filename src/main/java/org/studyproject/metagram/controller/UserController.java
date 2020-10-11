package org.studyproject.metagram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.studyproject.metagram.domain.Role;
import org.studyproject.metagram.domain.User;
import org.studyproject.metagram.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")

public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @GetMapping public String userList(Model model)
     * returns list of all users
     */
    @GetMapping
    public String userList(Model model) {
        model.addAttribute("users", userService.findAll());
        return "userList";
    }

    /**
     * @PreAuthorize("hasAuthority('ADMIN')")
     * @GetMapping("{user}") public String userEditForm(@PathVariable User user, Model model)
     * editing any user form - only for admin role
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{user}")
    public String userEditForm(@PathVariable User user, Model model) {
        model.addAttribute("user", user);
        List<Role> roles = Arrays.asList(Role.values());
        model.addAttribute("roles", roles);

        return "userEdit";
    }

    /**
     * @PreAuthorize("hasAuthority('ADMIN')")
     * @PostMapping public String userSave(
     * @RequestParam String username,
     * @RequestParam Map<String, String> form,
     * @RequestParam("userId") User user
     * public String userEditForm(@PathVariable User user, Model model)
     * save edited user - only for admin role
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public String userSave(
            @RequestParam String username,
            @RequestParam Map<String, String> form,
            @RequestParam("userId") User user) {

        userService.save(user, username, form);

        return "redirect:/user";
    }

    /**
     * @PreAuthorize("hasAuthority('ADMIN')")
     * @GetMapping("{user}/delete") public String userDelete(@PathVariable User user, Model model)
     * deleting any user and all their posts - only for admin role
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{user}/delete")
    public String userDelete(@PathVariable User user, Model model) {
        userService.deleteUser(user);
        model.addAttribute("users", userService.findAll());
        return "userList";
    }

    /**
     * @GetMapping("profile") public String getProfile(Model model, @AuthenticationPrincipal User user)
     * profile page of authorized account and editing form
     */
    @GetMapping("profile")
    public String getProfile(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * @PostMapping("profile") public String updateProfile(
     * @AuthenticationPrincipal User user,
     * @RequestParam String password,
     * @RequestParam String email) {
     * save updated profile of authorized account
     */
    @PostMapping("profile")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @RequestParam String password,
            @RequestParam String email) {
        userService.updateProfile(user, password, email);
        return "redirect:/user/profile";
    }

    /**
     * @GetMapping("profile/delete") public String deleteProfile(Model model, @AuthenticationPrincipal User user)
     * delete profile of authorized account
     */
    @GetMapping("profile/delete")
    public String deleteProfile(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        userService.deleteUser(user);
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }

    /**
     * @GetMapping("subscribe/{user}") public String subscribe(
     * @AuthenticationPrincipal User currentUser,
     * @PathVariable User user)
     * subscribe to another author
     */
    @GetMapping("subscribe/{user}")
    public String subscribe(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User user) {
        userService.subscribe(currentUser, user);
        return "redirect:/user-messages/" + user.getId();
    }

    /**
     * @GetMapping("unsubscribe/{user}") public String unsubscribe(
     * @AuthenticationPrincipal User currentUser,
     * @PathVariable User user)
     * unsubscribe from another author
     */
    @GetMapping("unsubscribe/{user}")
    public String unsubscribe(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User user) {
        userService.unsubscribe(currentUser, user);
        return "redirect:/user-messages/" + user.getId();
    }

    /**
     * @GetMapping("{type}/{user}/list") public String userList(Model model,
     * @PathVariable User user,
     * @PathVariable String type)
     * list of subscriptions/subscribers
     */
    @GetMapping("{type}/{user}/list")
    public String userList(Model model,
                           @PathVariable User user,
                           @PathVariable String type) {
        model.addAttribute("userChannel", user);
        model.addAttribute("type", type);
        if ("subscriptions".equals(type)) {
            model.addAttribute("users", user.getSubscriptions());
        } else {
            model.addAttribute("users", user.getSubscribers());
        }
        return "subscriptions";
    }
}
